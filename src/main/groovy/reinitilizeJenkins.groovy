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

def boolean dry = params.dryRun
def releaseArtifactToDelete = params.releaseArtifactToDelete

println "Running with following parameter: dry=${dry} , releaseArtifactToDelete=${releaseArtifactToDelete}"

stage("Delete all Patch Job with corresponding Json file") {
	node {
		["ProductivePatches", "Patches"].each { viewName ->
		
			Jenkins.instance.getView(viewName).items.each { item ->
				def jobName = item.name
				if(!dry) {
					item.delete()
					println "Deleted ${item.name}"
				}
				else{
					println "dryRun only ... ${item.name} would have been deleted."
				}
				
				if (!jobName.contains("Download")) {
					def patchNumber = jobName.substring(5,jobName.length())
					def cmd = "/opt/apg-patch-cli/bin/apscli.sh -r ${patchNumber}"
					if(new File("/var/opt/apg-patch-service-server/db/Patch${patchNumber}.json").exists()) {
						println "Following ccommand will be executed: ${cmd}"
						if(!dry) {
							sh(cmd)
						}
						else {
							println "dryRun only ... ${cmd} has not been executed."
						}
					}
				}
			}
		}
	}
}
	
stage("Delete Revisions.json and patchToBeReinstalled.json files") {
	node {
		def msg
		def revisionFileName = "/var/opt/apg-patch-cli/Revisions.json"
		def revisionFile = new File(revisionFileName)
		def patchToBeInstalledFileName = "/var/opt/apg-patch-cli/patchToBeReinstalled.json"
		def patchToBeInstalledFile = new File(patchToBeInstalledFileName)
		
		if(revisionFile.exists()) {
			if(!dry) {
				msg = (revisionFile.delete() ? "${revisionFileName} has been deleted!" : "Error while deleting ${revisionFileName}")
				println msg
			}
			else {
				println "dryRun only ... ${revisionFileName} would have been deleted."
			}
		}
		
		if(patchToBeInstalledFile.exists()) {
			if(!dry) {
				msg = (patchToBeInstalledFile.delete() ? "${patchToBeInstalledFileName} has been deleted!" : "Error while deleting ${patchToBeInstalledFileName}")
				println msg
			}
			else {
				println "dryRun only ... ${patchToBeInstalledFileName} would have been deleted."
			}
		}
	}
}

stage("Cleaning up Jenkins Maven Local Repository") {
	node {
		def affichageFolder = "/var/jenkins/maven/repository/com/affichage"
		def apgsgaFolder = "/var/jenkins/maven/repository/com/apgsga"

		if(!dry) {
			sh("rm -rf ${affichageFolder}")
			sh("rm -rf ${apgsgaFolder}")
			println "${affichageFolder} and ${apgsgaFolder} have been deleted!"
		}
		else {
			println "dryRun only ... ${affichageFolder} and ${apgsgaFolder} would have been deleted."
		}
	}
}

stage("Cleaning up Artifactory releases") {
	node {
		println "All Artifacty for ${releaseArtifactToDelete} Release will be deleted..."
		
		def fileName = "artifactsToDelete.json"
		def f = new File(fileName)
		if(f.exists()) {
			if(!dry) {
				f.delete()
			}
			else{
				println "dryRun only ... ${fileName} would have been deleted."
			}
		}
		def cmd = "curl -o ${fileName} -udev:dev1234 \"https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/artifact?name=${releaseArtifactToDelete}*&repos=releases\""
		sh(cmd)
		f = new File("/var/jenkins/workspace/Reinitialize Jenkins/artifactsToDelete.json")
		JsonSlurper slurper = new JsonSlurper()
		def artifacts = slurper.parse(f)
		
		artifacts.results.each{
			
			def urlforDelete = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/releases/"
			def firstIndex = "https://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/storage/releases/"
			def lastPartUrl = it.uri.substring(firstIndex.length(), it.uri.length())
			
			if(!dry) {
				println "Will be deleted: ${urlforDelete}${lastPartUrl}" 
				sh("curl -udev:dev1234 -XDELETE ${urlforDelete}${lastPartUrl}")
			}
			else {
				println "dryRun only ... ${urlforDelete}${lastPartUrl} would have been deleted."
			}
		}
	}
}