#!/usr/bin/env groovy

import java.util.function.Predicate
import groovy.json.JsonSlurper


def final env = System.getenv()
def final repositoriesAsJson = new JsonSlurper().parseText(env["repoToBeCleanedUp"])
def dryRun = true
def final nonProdReleases = targetInstancesReleases()

repositoriesAsJson.repositories.each { repo ->
	println "Cleaning repo ${repo.name} started..."
	deleteArtifacts(repo)
	println "Repo ${repo.name} successfully cleaned"	
}

private def deleteArtifacts(def repo) {
	def artifactsToBeDeleted = artifactsToBeDeletedFor(repo)
	println "Artifacts to be deleted for repo ${repo.name}"
	println "=============================================="
	artifactsToBeDeleted.results.each { result ->
		println "${result.patch}/${result.name} (created: ${result.created})"
	}
	println "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-"
	println "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-"
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

	
	def body = 'items.find({"repo":"' + "${repo.name}" + '", "created":{"$lt":"2099-01-01"}, "type":"file", "name":{"$match":"*"}'
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
	
	
	if (http.responseCode == 200) {
		return new JsonSlurper().parse(http.inputStream)
	} else {
		println "KO ${http.responseCode}"
		println http.getResponseMessage()
		return "ERRRRRRRROOOOOOOOOORRRRRRRRRRRR"
	}
}








