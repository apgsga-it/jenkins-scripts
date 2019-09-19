#!/usr/bin/env groovy

import groovy.json.JsonSlurper


def final repositories = ["dbpatch-test","releases-test","yumpatchrepo-test","yumrepodev-test","yumrepoprod-test","dummyRepoWhichDoesNotExist"]
def dryRun = true
 
def nonProdReleases = targetInstancesReleases() 

println "nonProdReleases : ${nonProdReleases}"
println "done"


private def targetInstancesReleases() {
	def releases = []
	def targets = loadTargetInstances()
	
	targets.each { t -> 
		def cmd = "/opt/apg-patch-cli/bin/apsrevcli.sh -gr ${t.name}"
		def targetReleases = executeSystemCmd(cmd,5000)
		println "Following releases found for target ${t.name}: ${targetReleases}"
		releases.addAll(targetReleases.split(","))
	}	
	
	println "Releases which can potientially be deleted: ${releases}"
	releases
}

private def loadTargetInstances() {
	def targetSystemMappingFilePath = "/etc/opt/apg-patch-common/TargetSystemMappings.json"
	def targetSystemMapping = new File(targetSystemMappingFilePath)
	assert targetSystemMapping.exists() : "${targetSystemMappingFilePath} does not exist"
	return new JsonSlurper().parse(targetSystemMapping).targetInstances
}


//artifactToBeDeleted()


//executeSystemCmd()


private def executeSystemCmd(def cmd, def waitTimeInMs) {
	def proc = cmd.execute()
	def sout = new StringBuilder()
	def serr = new StringBuilder()
	proc.consumeProcessOutput(sout,serr)
	proc.waitForOrKill(waitTimeInMs)
	assert serr?.trim() : "Error occured while running following command: ${cmd}  /  ${serr}"  
	return sout
}

private artifactToBeDeleted() {

	def env = System.getenv()
	def username = env["artifactoryUser"]
	def userpwd = env["artifactoryPassword"]

	
	def body = 'items.find({"type":"file","$and": [{"name":{"$match":"*9.1.0.ADMIN-UIMIG-198*"}},{"name":{"$nmatch":"*zip*"}}]})'
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
		println "OK"
	//	def response = http.inputStream.getText('UTF-8')
	//	println response
		def resultsAsJson = new JsonSlurper().parse(http.inputStream)
		resultsAsJson.results.each { r ->
			println "Artifact name: ${r.name}"
		}
	} else {
		println "KO ${http.responseCode}"
		println http.getResponseMessage()
	}
}








