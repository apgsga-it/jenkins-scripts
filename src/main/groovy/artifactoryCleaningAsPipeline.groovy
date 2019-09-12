#!groovy
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic




def reposDefinition = env.reposDefinitionAsJson


println "reposDefinition"
println "==============="
println reposDefinition



def repositories = new JsonSlurper().parseText(reposDefinition) 


repositories.each {repo -> 

	stage(repo.name) {
		
		node {

			def maxDate = "2019-08-01"
			def fileName = "it21gui-dist-zip-9.1.0.ADMIN-UIMIG-9*.zip"
					
			def query = "items.find({\"repo\":\"${repo.name}\", \"created\":{\"\$lt\":\"${maxDate}\"}, \"type\":\"file\", \"name\":{\"\$match\":\"${fileName}\"}})"
			def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/${env.ARTIFACTORY_SERVER_ID}"
			def searchRequestUrl = "${artifactoryUrl}/api/search/aql"
			
			def repoUser
			def repoPwd
			
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
						usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
			
					repoUser = "${USERNAME}"
					repoPwd = "${PASSWORD}"
			}
			
			
			println "${repoUser} / ${repoPwd}"
			
			def curlCmd
			def res
			
			
			curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X POST -H \"Content-Type: text/plain\" -d '${query}' ${searchRequestUrl}"
			res = sh script:curlCmd, returnStdout:true
			println "res from search: ${res}"
			
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
				
				println "Following Artifact will be deleted (resultPath): ${resultPath}"
//				curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X DELETE ${artifactoryUrl}/${resultPath}"
//				res = sh script:curlCmd, returnStdout:true
//				println "res from delete: ${res}"
	
			}			
		}
	}
}

