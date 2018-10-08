import java.nio.file.Files

properties([
	parameters([
		booleanParam(
			defaultValue: true,
			description: 'Parameter',
			name: 'dryRun'
			),
	])
])

def dry = params.dryRun

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
			
			msg = (revisionFile.delete() ? "${revisionFileName} has been deleted!" : "Error while deleting ${revisionFileName}")
			println msg
			
			msg = (patchToBeInstalledFile.delete() ? "${patchToBeInstalledFileName} has been deleted!" : "Error while deleting ${patchToBeInstalledFileName}")
			println msg
		} else {
			println "Revisions.json and patchToBeReinstalled.json files haven't been deleted, running dry ..."
		}
	}
}