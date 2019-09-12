#!groovy
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

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

			def query = "items.find({\"repo\":\"${repo.name}\", \"created\":{\"\$lt\":\"${repo.maxFileDate}\"}, \"type\":\"file\", \"name\":{\"\$match\":\"${repo.fileNamePattern}\"}})"
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

