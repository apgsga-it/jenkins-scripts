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
			defaultValue: "",
			description: 'Parameter',
			name: 'dryRun'
			),
	])
])

def dry = params.dryRun

stage("delete all Jobs from Patches View") {
	
	Jenkins.instance.getView("Patches").items.each { item ->
		if (!dry) {
			//item.delete()
			println "Deleted ${item.name}"
		} else {
			println "Didn't delete ${item.name}, running dry"
		}
	}
}
	
