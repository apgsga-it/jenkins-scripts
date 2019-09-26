#!/usr/bin/env groovy

import java.util.function.Predicate
import groovy.json.JsonSlurper
import groovy.transform.Field

def final repositoriesAsJson = new JsonSlurper().parseText(System.getenv()["repoToBeCleanedUp"])
@Field dryRun
@Field releasesFormatedForAqlSearch
@Field revNumberToCompleteRevision = [:]
@Field Set revisionNumbersToBeRemoved = []

printHeader()
initGlobalVariable()
repositoriesAsJson.repositories.each { repo ->
	println "Cleaning repo ${repo.name} started..."
	deleteArtifacts(repo)
	println "Repo ${repo.name} successfully cleaned"	
}
doDeleteRevisions()

private def doDeleteRevisions() {
	println "Calling apsrevcli to remove following revision numbers: ${revisionNumbersToBeRemoved}"
	def revisionWithSemiColumn = revisionNumbersToBeRemoved.split(";")
	def apsRevCliCmd = "/opt/apg-patch-cli/bin/apsrevcli.sh -dr \"${revisionWithSemiColumn}\""
	if(!dryRun) {
		executeSystemCmd(apsRevCliCmd, 10000)
	}
	else {
		println "Running dry, following would have been called: ${apsRevCliCmd}"
	}
}

private def printHeader() {
	println ""
	println ""
	println "============================================="
	println "========== < P A R A M E T E R S > =========="
	println "============================================="
	println "dryRun: ${System.getenv()["dryRun"]}"
	println "Repository Configuration: ${System.getenv()["repoToBeCleanedUp"]}"
	println "=============================================="
	println "========== </ P A R A M E T E R S > =========="
	println "=============================================="
	println ""
	println ""
}

private def initGlobalVariable() {
	def nonProdReleases = targetInstancesReleases()
	def aql = ""
	nonProdReleases.each { release ->
		def firstPart = '{"name":{"$match":"*'
		def extractedRelease = release.substring(release.lastIndexOf("-"),release.length()) + "."
		def lastPart = '*"}},'
		aql += "${firstPart}${extractedRelease}${lastPart}"
		storeRevisionMappingForSearch(release)
	}
	
	releasesFormatedForAqlSearch = aql.substring(0, aql.lastIndexOf(","))
	dryRun = System.getenv()["dryRun"].equals("true") ? true : false
}

private def storeRevisionMappingForSearch(def release) {
	def searchedRevision = release.substring(release.lastIndexOf("-"),release.length()) + "."
	def revisionNumberOnly = release.substring(release.lastIndexOf("-")+1,release.length())
	revNumberToCompleteRevision.put(searchedRevision,revisionNumberOnly)
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
		storeRevisionToBeDeleted(resultPath)
	}
	println "Done deleting ${artifactsToBeDeleted.range.total} Artifacts and corresponding Revisions for repo ${repo.name} (dryRun was ${dryRun})"
}

private def storeRevisionToBeDeleted(def artifactoryPath) {
	revisionNumbersToBeRemoved.add(revNumberToCompleteRevision.get(searchedRevision))
//	revNumberToCompleteRevision.keySet().each { searchedRevision ->
//		if(artifactoryPath.contains(searchedRevision)) {
//			def cmd = "/opt/apg-patch-cli/bin/apsrevcli.sh -dr ${revNumberToCompleteRevision.get(searchedRevision)}"
//			if(!dryRun) {
//				println "Following revision will be removed from Revisions.json: ${revNumberToCompleteRevision.get(searchedRevision)}"
//				executeSystemCmd(cmd, 10000)
//			}
//			else {
//				println "Running dry ... Following would otherwise have been called: ${cmd}"
//			}
//		}		
//	}
}

private def doDeleteArtifact(def artifactPath) {
	if(!dryRun) {
		executeArtifactoryHttpRequest(artifactPath, "DELETE", [:])
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
		def targetReleases = executeSystemCmd(cmd,10000)
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
	def keepMinDate = new Date().minus(Integer.valueOf(repo.keepMaxDays))
	def keepMinDateFormatted = keepMinDate.format("yyyy-MM-dd")
	def body = 'items.find({"repo":"' + "${repo.name}" + '", "created":{"$lt":"' + "${keepMinDateFormatted}" + '"}, "type":"file", "$or":[' + "${releasesFormatedForAqlSearch}" + ']})'
	return executeArtifactoryHttpRequest("api/search/aql", "POST", ["Content-Type":"text/plain"], body)
}

private def executeArtifactoryHttpRequest(def contextPath, def method, def reqProperties) {
	executeArtifactoryHttpRequest(contextPath, method, reqProperties, null)
}

private httpArtifactoryBasicAuth() {
	def username = System.getenv()["artifactoryUser"]
	def userpwd = System.getenv()["artifactoryPassword"]
	String userpass = "${username}:${userpwd}";
	return "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
}

private def isHttpResponseRedirect(def status) {
	return status != HttpURLConnection.HTTP_OK &&
			(
				status == HttpURLConnection.HTTP_MOVED_TEMP ||
				status == HttpURLConnection.HTTP_MOVED_PERM ||
				status == HttpURLConnection.HTTP_SEE_OTHER  ||
				status == 308 // 308 doesn't seem to be part of HttpUrlConnection :(
			)
}

private def doExecuteHttpAndReturn(def url, def method, def reqProperties, def body) {
	def http = new URL(url).openConnection() as HttpURLConnection
	http.setRequestMethod(method)
	http.setDoOutput(true)
	http.setFollowRedirects(true)
	http.setInstanceFollowRedirects(true)
	reqProperties.keySet().each { propertyKey ->
		http.setRequestProperty(propertyKey, reqProperties.get(propertyKey))
	}
	http.setRequestProperty ("Authorization", httpArtifactoryBasicAuth());
	if(body != null) {
		http.outputStream.write(body.getBytes("UTF-8"))
	}
	http.connect()
	return http
}

private def executeArtifactoryHttpRequest(def contextPath, def method, def Map reqProperties, def body) {
	def completeUrl = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/${contextPath}"
	def http = doExecuteHttpAndReturn(completeUrl, method, reqProperties, body)
	while(isHttpResponseRedirect(http.getResponseCode())) {
		// get redirect url from "location" header field
		String newUrl = http.getHeaderField("Location");
		http = doExecuteHttpAndReturn(newUrl, method, reqProperties, body)
	}
	assert http.responseCode >= 200 && http.responseCode < 300 : "Error while calling http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/${contextPath} . Code: ${http.responseCode} , Message: ${http.getResponseMessage()}"
	// HTTP 204 -> No Content
	def output = http.responseCode != 204 ? new JsonSlurper().parse(http.inputStream) : ""
	return output
}
