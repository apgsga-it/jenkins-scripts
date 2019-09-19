#!/usr/bin/env groovy

import net.sf.json.groovy.JsonSlurper


def env = System.getenv()
def username = env["artifactoryUser"]
def userpwd = env["artifactoryPassword"]
/*
println "trying to run a sh command ..."


def query = "items.find({\"repo\":\"releases-test\", \"created\":{\"\$lt\":\"2099-01-01\"}, \"type\":\"file\", \"name\":{\"\$match\":\"*.zip\"}})"

def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
def searchRequestUrl = "${artifactoryUrl}/api/search/aql"

def curlCmd
def res


//println "query : ${query}"
//println "searchRequestUrl : ${searchRequestUrl}"

//curlCmd = "curl -L -u ${username}:${userpwd} -X POST -H 'Content-Type: text/plain' -d '${query}' ${searchRequestUrl}"
curlCmd = "curl -L -u ${username}:${userpwd} -X POST -H 'Content-Type: text/plain' -d \"items.find({\\\"type\\\":\\\"file\\\",\\\"\\\$and\\\": [{\\\"name\\\":{\\\"\\\$match\\\":\\\"*9.1.0.ADMIN-UIMIG-198*\\\"}},{\\\"name\\\":{\\\"\\\$nmatch\\\":\\\"*zip*\\\"}}]})\" http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql"
//curlCmd = "curl -L -u xxx:xxx -X POST -H \"Content-Type: text/plain\" -d \"items.find({\\\"type\\\":\\\"file\\\",\\\"\$and\\\": [{\\\"name\\\":{\\\"\$match\\\":\\\"*9.1.0.ADMIN-UIMIG-198*\\\"}},{\\\"name\\\":{\\\"\$nmatch\\\":\\\"*zip*\\\"}}]})\" http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql"

//println "curlCmd : ${curlCmd}"

def curlCmdAsArray = ["curl","-L","-u","${username}:${userpwd}","-X","POST","-H","Content-Type: text/plain","-d","\"items.find({\\\"type\\\":\\\"file\\\",\\\"\\\$and\\\": [{\\\"name\\\":{\\\"\\\$match\\\":\\\"*9.1.0.ADMIN-UIMIG-198*\\\"}},{\\\"name\\\":{\\\"\\\$nmatch\\\":\\\"*zip*\\\"}}]})\"","http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql"]
println "curlCmdAsArray : ${curlCmdAsArray}"
def proc = curlCmdAsArray.execute()
def sout = new StringBuilder()
def serr = new StringBuilder()
proc.consumeProcessOutput(sout,serr)
proc.waitForOrKill(10000)
println "out: ${sout}"
println "=========================================================================="
println "err: ${serr}"


println "Done"
*/

System.setProperty("http.proxyHost", "127.0.0.1");
//System.setProperty("https.proxyHost", "127.0.0.1");
System.setProperty("http.proxyPort", "8889");
//System.setProperty("https.proxyPort", "8889");



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

/*
Authenticator.setDefault(new Authenticator() {
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication ("dev-test", "dev1234-test".toCharArray());
	}
})
*/

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
//https://www.mkyong.com/java/java-httpurlconnection-follow-redirect-example/

	if (redirect) {
		
				// get redirect url from "location" header field
				String newUrl = http.getHeaderField("Location");
		

				
				// get the cookie if need, for login
//				String cookies = http.getHeaderField("Set-Cookie");
		
				// open the new connnection again
				http = new URL(newUrl).openConnection() as HttpURLConnection

								
				String userpass2 = "${username}:${userpwd}";
				String basicAuth2 = "Basic " + new String(Base64.getEncoder().encode(userpass2.getBytes()));
				http.setRequestProperty ("Authorization", basicAuth2);
				

//				http.setRequestMethod('POST')
				http.setDoOutput(true)
				http.setRequestProperty("Content-Type", "text/plain")
//				http.setFollowRedirects(true)
//				http.setInstanceFollowRedirects(true)
				
				
				http.outputStream.write(body.getBytes("UTF-8"))
								
				http.connect()
//				http.setRequestProperty("Cookie", cookies);
//				http.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
//				http.addRequestProperty("User-Agent", "Mozilla");
//				http.addRequestProperty("Referer", "google.com");
//										
//				System.out.println("Redirect to URL : " + newUrl);
		
			}

def response = [:]

	if (http.responseCode == 200) {
		println "OK"
		println http.inputStream.getText('UTF-8')
	} else {
//		response = new JsonSlurper().parseText(http.errorStream.getText('UTF-8'))
		println "KO ${http.responseCode}"
		println http.getResponseMessage()
		println http.getRespons
	}








