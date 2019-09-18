#!groovy
import java.util.stream.Collectors

import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

def reposDefinition = env.reposDefinitionAsJson
def repositories = new JsonSlurper().parseText(reposDefinition) 
def repoUser
def repoPwd

def targetSystemMappingFilePath = env.targetSystemMappingFilePath ?: "/etc/opt/apg-patch-common/TargetSystemMappings.json"
def revisionsFilePath = env.revisionsFilePath ?: "/var/opt/apg-patch-cli/Revisions.json"

def targetsName = []
def targetToReleases = [:]

withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
			usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
		repoUser = "${USERNAME}"
		repoPwd = "${PASSWORD}"
}


stage("one") {
	def a = "salut"
	println a
}

stage("two") {
	println a
}


//stage("Pre-requisite") {
//	node() {
//			checkPre()
//		assert new File(targetSystemMappingFilePath).exists() : "${targetSystemMappingFilePath} does not exist"
//		assert new File(revisionsFilePath).exists() : "${revisionsFilePath} does not exist"
//		def apsrevcliCmd = "/opt/apg-patch-cli/bin/apsrevcli.sh"
//		def result = sh script:apsrevcliCmd, returnStatus:true
//		assert result == 0 : "Problem while trying to call apsrevcli with following command: ${apsrevcliCmd}"
//	}
//}

//stage("Loading targetInstances") {
//	loadTargetInstances()
//}

//stage("Loading Revisions") {
////	def 
//}

//stage("Loading Production Releases") {
//	
//}


// iterate over all Repositories, fetch released Artifact, check not in prod, delete


def checkPre() {
	assert new File(targetSystemMappingFilePath).exists() : "${targetSystemMappingFilePath} does not exist"
	assert new File(revisionsFilePath).exists() : "${revisionsFilePath} does not exist"
	def apsrevcliCmd = "/opt/apg-patch-cli/bin/apsrevcli.sh"
	def result = sh script:apsrevcliCmd, returnStatus:true
	assert result == 0 : "Problem while trying to call apsrevcli with following command: ${apsrevcliCmd}"
}


def loadTargetInstances() {
	def targetsInstancesAsJson = new JsonSlurper().parse(new File(targetSystemMappingFilePath))
	// JHE: could be as simple as that, but : https://issues.jenkins-ci.org/browse/JENKINS-56330
	//targetInstances = targetsInstancesAsJson.targetInstances.stream().map{t -> t.name}.collect()(Collectors.toList())
	println "targetsInstancesAsJson : ${targetsInstancesAsJson}"
	targetsInstancesAsJson.targetInstances.each { target ->
		targetsName.add(target.name)
	}
	println "Following targetInstances have been loaded: ${targetsName}"
}

/*
repositories.repositories.each {repo -> 
	
	stage(repo.name) {
		
		node {

			def query = "items.find({\"repo\":\"${repo.name}\", \"created\":{\"\$lt\":\"${repo.maxFileDate}\"}, \"type\":\"file\", \"\$and\": [{\"name\":{\"\$match\":\"${repo.fileNamePattern}\"}}${getExcludedReleases()}]}).include(\"property.*\")"
			
			def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/${env.ARTIFACTORY_SERVER_ID}"
			def searchRequestUrl = "${artifactoryUrl}/api/search/aql"
			
			def curlCmd
			def res
			
			curlCmd = "curl -L -u ${repoUser}:${repoPwd} -X POST -H \"Content-Type: text/plain\" -d '${query}' ${searchRequestUrl}"
			res = sh script:curlCmd, returnStdout:true
			
			def results = new JsonSlurper().parseText(res)
			def resultsToBeParsed
			
			if(!repo.minNbFiles.equals("0")) {
				resultsToBeParsed = resultsWithoutArtifactToBeKept(results)
			}else {
				resultsToBeParsed = results.results
			}
			
			println "Total Artifacts within results: ${results.range.total}"
			println "results: ${results}"
			println "Total Artifacts within resultsToBeParsed: ${resultsToBeParsed.range.total}"
			println "resultsToBeParsed: ${resultsToBeParsed}"
			
			resultsToBeParsed.results.each { result ->
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
*/




// JHE(13.09.2019): At best, getExcludedReleases() method could be written using a Closure, but seems we're encoutnering the following bug: https://issues.jenkins-ci.org/browse/JENKINS-56330
//					So for now, the method is as the below one ... :(
/*
private def getExcludedReleases() {
	def prodReleases = ["9.1.0.ADMIN-UIMIG-46","9.1.0.ADMIN-UIMIG-198","9.1.0.ADMIN-UIMIG-214","9.1.0.ADMIN-UIMIG-234","9.1.0.ADMIN-UIMIG-237","9.1.0.ADMIN-UIMIG-240","9.1.0.ADMIN-UIMIG-249","9.1.0.ADMIN-UIMIG-252","9.1.0.ADMIN-UIMIG-255","9.1.0.ADMIN-UIMIG-258","9.1.0.ADMIN-UIMIG-261"]
	return prodReleases.stream().map{r -> getSingleAQLExcludeReleaseStatement(r)}.collect(Collectors.toList()).join("")
}
*/
/*
private def getExcludedReleases() {
	def prodReleases = sh script:'/opt/apg-patch-cli/bin/apsrevcli.sh -gr dev-chpi211', returnStdout:true
	def releasesToBeExcluded = []
	prodReleases.split(",").each{r -> 
		releasesToBeExcluded.add(getSingleAQLExcludeReleaseStatement(r))
	}
	return releasesToBeExcluded.join("")
}
*/
/*
private def getSingleAQLExcludeReleaseStatement(def release) {
	def firstPart = ",{\"name\":{\"\$nmatch\":\"*"
	def extractedRelease = release.substring(release.lastIndexOf("-"),release.length()) + "."
	def lastPart = "*\"}}"
	return "${firstPart}${extractedRelease}${lastPart}"
}
*/
/*
private def resultsWithoutArtifactToBeKept(def results) {
	return results;
}
*/