#!/usr/bin/env groovy

import java.util.function.Predicate
import groovy.json.JsonSlurper
import groovy.transform.Field


def final env = System.getenv()
def final repositoriesAsJson = new JsonSlurper().parseText(env["repoToBeCleanedUp"])
@Field dryRun = System.getenv()["dryRun"].equals("true") ? true : false
@Field releasesFormatedForAqlSearch = formatReleasesForAqlSearch()

repositoriesAsJson.repositories.each { repo ->
	println "Cleaning repo ${repo.name} started..."
	deleteArtifacts(repo)
	println "Repo ${repo.name} successfully cleaned"	
}

private def formatReleasesForAqlSearch() {
	def nonProdReleases = targetInstancesReleases()
	def aql = ""
	nonProdReleases.each { release ->
		def firstPart = '{"name":{"$match":"*'
		def extractedRelease = release.substring(release.lastIndexOf("-"),release.length()) + "."
		def lastPart = '*"}},'
		aql += "${firstPart}${extractedRelease}${lastPart}"
	}
	return aql.substring(0, aql.lastIndexOf(","))
}

private def deleteArtifacts(def repo) {
	def artifactsToBeDeleted = artifactsToBeDeletedFor(repo)
	def resultPath
	artifactsToBeDeleted.results.each { result ->
		if (result.path.toString().equals(".")) {
			resultPath = result.repo + "/" + result.name
		}
		else {
			resultPath = result.repo + "/" + result.path + "/" + result.name
		}
		doDeleteArtifact(resultPath)
	}
}

private def doDeleteArtifact(def artifactPath) {
	
	println "dryRun: ${dryRun}"
	
	if(!dryRun) {
		def env = System.getenv()
		def username = env["artifactoryUser"]
		def userpwd = env["artifactoryPassword"]
		def http = new URL("http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/${artifactPath}").openConnection() as HttpURLConnection
		http.setRequestMethod('DELETE')
		http.setDoOutput(true)
		http.setFollowRedirects(true)
		http.setInstanceFollowRedirects(true)
		
		String userpass = "${username}:${userpwd}";
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
		http.setRequestProperty ("Authorization", basicAuth);
		
		http.connect()
		
		
		boolean redirect = false;
		
		// normally, 3xx is redirect
		int status = http.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER
						|| status == 308)
			redirect = true;
		}
		
		if (redirect) {
			
			// get redirect url from "location" header field
			String newUrl = http.getHeaderField("Location");
			// open the new connnection again
			http = new URL(newUrl).openConnection() as HttpURLConnection
			http.setRequestMethod('DELETE')
			String userpass2 = "${username}:${userpwd}";
			String basicAuth2 = "Basic " + new String(Base64.getEncoder().encode(userpass2.getBytes()));
			http.setRequestProperty ("Authorization", basicAuth2);
			http.setDoOutput(true)
			http.connect()
		}
		
		
		assert http.responseCode == 204 : "Deletion from ${artifactPath} failed. Code: ${http.responseCode} , Message: ${http.getResponseMessage()}"
		println "${artifactPath} deleted"
	}
	else {
		println "${artifactPath} would have been deleted"
	}
	
}

private def targetInstancesReleases() {
	def releases = []
	def targets = loadTargetInstances()
	
	targets.each { t -> 
		def cmd = "/opt/apg-patch-cli/bin/apsrevcli.sh -gr ${t.name}"
		def targetReleases = executeSystemCmd(cmd,5000)
		println "Following releases found for target ${t.name}: ${targetReleases}"
		releases.addAll(targetReleases.toString().trim().split(","))
	}	
	
	Predicate<String> removeCondition = {s -> s.length()==0 || s == null}
	releases.removeIf(removeCondition)
	println "Releases which can potientially be deleted: ${releases}"
	releases
}

private def loadTargetInstances() {
	def targetSystemMappingFilePath = "/etc/opt/apg-patch-common/TargetSystemMappings.json"
	def targetSystemMapping = new File(targetSystemMappingFilePath)
	assert targetSystemMapping.exists() : "${targetSystemMappingFilePath} does not exist"
	return new JsonSlurper().parse(targetSystemMapping).targetInstances
}


private def executeSystemCmd(def cmd, def waitTimeInMs) {
	def proc = cmd.execute()
	def sout = new StringBuilder()
	def serr = new StringBuilder()
	proc.consumeProcessOutput(sout,serr)
	proc.waitForOrKill(waitTimeInMs)
	assert (serr==null || serr.size()==0) : "Error occured while running following command: ${cmd}  /  ${serr}"  
	return sout
}

private artifactsToBeDeletedFor(def repo) {

	def env = System.getenv()
	def username = env["artifactoryUser"]
	def userpwd = env["artifactoryPassword"]
	
	def body = 'items.find({"repo":"' + "${repo.name}" + '", "created":{"$lt":"2099-01-01"}, "type":"file", "$or":[' + "${releasesFormatedForAqlSearch}" + ']})'
	
	def http = new URL("http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql").openConnection() as HttpURLConnection
	http.setRequestMethod('POST')
	http.setDoOutput(true)
	http.setRequestProperty("Content-Type", "text/plain")
	http.setFollowRedirects(true)
	http.setInstanceFollowRedirects(true)
	
	
	String userpass = "${username}:${userpwd}";
	String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
	http.setRequestProperty ("Authorization", basicAuth);
	
	http.outputStream.write(body.getBytes("UTF-8"))
	http.connect()
	
	
	boolean redirect = false;
	
	// normally, 3xx is redirect
	int status = http.getResponseCode();
	if (status != HttpURLConnection.HTTP_OK) {
		if (status == HttpURLConnection.HTTP_MOVED_TEMP
			|| status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_SEE_OTHER
					|| status == 308)
		redirect = true;
	}
	
	if (redirect) {
		
		// get redirect url from "location" header field
		String newUrl = http.getHeaderField("Location");
		// open the new connnection again
		http = new URL(newUrl).openConnection() as HttpURLConnection
		String userpass2 = "${username}:${userpwd}";
		String basicAuth2 = "Basic " + new String(Base64.getEncoder().encode(userpass2.getBytes()));
		http.setRequestProperty ("Authorization", basicAuth2);
		http.setDoOutput(true)
		http.setRequestProperty("Content-Type", "text/plain")
		http.outputStream.write(body.getBytes("UTF-8"))
		http.connect()
	}
	
	assert http.responseCode == 200 : "Error while fetching list of Artifacts on Artifactory. Code: ${http.responseCode} , Message: ${http.getResponseMessage()}"
	
	return new JsonSlurper().parse(http.inputStream)
}








