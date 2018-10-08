import java.nio.file.Files

/*
*
* This pipeline will:
* 
* 	- Delete all jobs from the "Patches" view
* 	- Archive all jobs from the "ProductivePatches" view
*   - Delete all /var/opt/apg-patch-service-server/db/PatchXXXX.json file by calling apscli.sh
*   - Delete /var/jenkins/maven/repository/com/affichage and /var/jenkins/maven/repository/com/apgsga folders
*   - Delete all "9.1.0.ADMIN-UIMIG-X" releases on Artifactory
*
*/

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

stage("Delete all Patch Job") {
	node {
		Jenkins.instance.getView("Patches").items.each { item ->
			if (!dry) {
				def jobName = item.name
				//item.delete()
				println "Deleted ${item.name}"
				if (!jobName.contains("Download")) {
					def patchNumber = jobName.substring(5,jobName.length())
					def cmd = "/opt/apg-patch-cli/bin/apscli.sh -r ${patchNumber}"
					if(Files.exists("/var/opt/apg-patch-service-server/db/Patch${patchNumber}.json")) {
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
	
