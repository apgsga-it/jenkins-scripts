#!groovy
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

["repo1","repo2"].each {repo -> 

	stage(repo) {
		
		node {
		
			println "this was a test"

			
			def query = 'items.find({"type":"file","name":{"$match":"it21gui-dist-zip-9.1.0.ADMIN-UIMIG-102.zip"}})'
			def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
			def requestUrl = "${artifactoryUrl}/api/search/aql"
			
			
			println "${env.ARTIFACTORY_SERVER_ID}"
			
			def repoUser
			def repoPwd
			
				withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
						usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
			
					repoUser = "${USERNAME}"
					repoPwd = "${PASSWORD}"
				}
			
			
			println "${repoUser} / ${repoPwd}"
			
			def curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X POST -H \"Content-Type: text/plain\" -d '${query}' ${requestUrl}"
			
			def res = sh script:curlCmd, returnStatus:true
			
			println "res: ${res}"
			
			def results = new JsonSlurper().parseText(res)
			
			results.results.each { result ->
				println "${result.path}"
				println "${result.name}"
			}			
						
			
			
		}
		
	}
	
}

