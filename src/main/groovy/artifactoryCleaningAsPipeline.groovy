#!groovy
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic
import java.util.stream.Collectors

def reposDefinition = env.reposDefinitionAsJson
def repositories = new JsonSlurper().parseText(reposDefinition) 
def repoUser
def repoPwd

withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
			usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
		repoUser = "${USERNAME}"
		repoPwd = "${PASSWORD}"
}

repositories.repositories.each {repo -> 

	stage(repo.name) {
		
		node {

			def excludedReleases = getExcludedReleases()

			println "excludedReleases: ${excludedReleases}"
						
			def query = "items.find({\"repo\":\"${repo.name}\", \"created\":{\"\$lt\":\"${repo.maxFileDate}\"}, \"type\":\"file\", \"\$and\": [{\"name\":{\"\$match\":\"${repo.fileNamePattern}\"}}${getExcludeNameFilter()}]})"
			
			println "query: ${query}"
			
			def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/${env.ARTIFACTORY_SERVER_ID}"
			def searchRequestUrl = "${artifactoryUrl}/api/search/aql"
			
			def curlCmd
			def res
			
			curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X POST -H \"Content-Type: text/plain\" -d '${query}' ${searchRequestUrl}"
			res = sh script:curlCmd, returnStdout:true
//			println "res from search: ${res}"
			
			def results = new JsonSlurper().parseText(res)
			
			println "Total Artifacts found: ${results.range.total}"
			
			results.results.each { result ->
				println "${result.path}/${result.name} : will be deleted"
				def resultPath
				
				if (result.path.toString().equals(".")) {
					resultPath = result.repo + "/" + result.name
				}
				else {
					resultPath = result.repo + "/" + result.path + "/" + result.name
				}
				
//				println "Following Artifact will be deleted (resultPath): ${resultPath}"
//				curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X DELETE ${artifactoryUrl}/${resultPath}"
//				res = sh script:curlCmd, returnStdout:true
//				println "res from delete: ${res}"
	
			}			
		}
	}
}

private def getExcludedReleases() {
	def prodReleases = ["9.1.0.ADMIN-UIMIG-46","9.1.0.ADMIN-UIMIG-198","9.1.0.ADMIN-UIMIG-214","9.1.0.ADMIN-UIMIG-234","9.1.0.ADMIN-UIMIG-237","9.1.0.ADMIN-UIMIG-240","9.1.0.ADMIN-UIMIG-249","9.1.0.ADMIN-UIMIG-252","9.1.0.ADMIN-UIMIG-255","9.1.0.ADMIN-UIMIG-258","9.1.0.ADMIN-UIMIG-261"]
	def releasesToBeExcluded
	releasesToBeExcluded = prodReleases.stream().map{r -> getSingleAQLExcludeReleaseStatement(r)}.collect(Collectors.toList()).join("")
	return releasesToBeExcluded
}

private def getSingleAQLExcludeReleaseStatement(def release) {
	def firstPart = ",{\"name\":{\"\$nmatch\":\"*"
	def extractedRelease = release.substring(release.lastIndexOf("-"),release.length()) + "."
	def lastPart = "*\"}}"
	return "${firstPart}${extractedRelease}${lastPart}"
}