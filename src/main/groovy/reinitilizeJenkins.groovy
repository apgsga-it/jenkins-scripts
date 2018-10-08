import java.nio.file.Files

import groovy.json.JsonSlurper

properties([
	parameters([
		booleanParam(
			defaultValue: true,
			description: 'Parameter',
			name: 'dryRun'
			),
		stringParam(
			defaultValue: "9.1.0.ADMIN-UIMIG-",
			description: 'Parameter',
			name: 'releaseArtifactToDelete'
			
			)
	])
])

def dry = params.dryRun
def releaseArtifactToDelete = params.releaseArtifactToDelete

stage("Delete all Patch Job with corresponding Json file") {
	node {
		["ProductivePatches", "Patches"].each { viewName ->
		
			Jenkins.instance.getView(viewName).items.each { item ->
				if (!dry) {
					def jobName = item.name
					item.delete()
					println "Deleted ${item.name}"
					if (!jobName.contains("Download")) {
						def patchNumber = jobName.substring(5,jobName.length())
						def cmd = "/opt/apg-patch-cli/bin/apscli.sh -r ${patchNumber}"
						if(new File("/var/opt/apg-patch-service-server/db/Patch${patchNumber}.json").exists()) {
							println "Following ccommand will be executed: ${cmd}"
							sh(cmd)
						}
					}
				} else {
					println "Didn't do anything for ${item.name}, running dry ..."
				}
			}
		}
	}
}
	
stage("Delete Revisions.json and patchToBeReinstalled.json files") {
	node {
		if(!dry) {
			def msg
			def revisionFileName = "/var/opt/apg-patch-cli/Revisions.json"
			def revisionFile = new File(revisionFileName)
			def patchToBeInstalledFileName = "/var/opt/apg-patch-cli/patchToBeReinstalled.json"
			def patchToBeInstalledFile = new File(patchToBeInstalledFileName)
			
			if(revisionFile.exists()) {
				msg = (revisionFile.delete() ? "${revisionFileName} has been deleted!" : "Error while deleting ${revisionFileName}")
				println msg
			}
			
			if(patchToBeInstalledFile.exists()) {
				msg = (patchToBeInstalledFile.delete() ? "${patchToBeInstalledFileName} has been deleted!" : "Error while deleting ${patchToBeInstalledFileName}")
				println msg
			}
		} else {
			println "Revisions.json and patchToBeReinstalled.json files haven't been deleted, running dry ..."
		}
	}
}

stage("Cleaning up Jenkins Maven Local Repository") {
	node {
		if(!dry) {
			def affichageFolder = "/var/jenkins/maven/repository/com/affichage"
			def apgsgaFolder = "/var/jenkins/maven/repository/com/apgsga"
			
			sh("rm -rf ${affichageFolder}")
			sh("rm -rf ${apgsgaFolder}")
			
			println "${affichageFolder} and ${apgsgaFolder} have been deleted!"
		}
		else {
			println "Jenkins Maven Local Repository has not been cleaned up, running dry ..."
		}
	}
}

stage("Cleaning up Artifactory releases") {
	node {
		if(!dry) {
			println "All Artifacty for ${releaseArtifactToDelete} Release will be deleted..."
			
			def fileName = "artifactsToDelete.json"
			def f = new File(fileName)
			if(f.exists()) {
				f.delete()
			}
			def cmd = "curl -o ${fileName} -udev:dev1234 \"https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/artifact?name=${releaseArtifactToDelete}*&repos=releases\""
			sh(cmd)
			f = new File("/var/jenkins/workspace/Reinitialize Jenkins/artifactsToDelete.json")
			JsonSlurper slurper = new JsonSlurper()
			def artifacts = slurper.parse(f)
			
			artifacts.results.each{
				//sh("curl -udev:dev1234 -XDELETE ${it.uri}")
				
				def urlforDelete = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/releases/"
				def firstIndex = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/storage/releases/"
				def lastPartUrl = it.uri.substring(firstIndex.length(), it.uri.length())
				
				println "Would be deleted: ${urlforDelete}/${lastPartUrl}" 
			}
		}
		else {
			println "No Release have been deleted from Artifactory, running dry ..."
		}
	}
}