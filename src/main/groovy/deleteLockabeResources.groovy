properties([
	parameters([
		booleanParam(
			defaultValue: true,
			description: 'Parameter',
			name: 'dryRun'
			)
	])
])

def boolean dry = params.dryRun

println "Running with following parameter: dry=${dry}"

stage("Delete Lockable Resources") {
	node {
		def manager = org.jenkins.plugins.lockableresources.LockableResourcesManager.get()
		def resources = manager.getResources().findAll{
		  !it.locked
		}
		resources.each{
			if(!dryRun) {
				manager.getResources().remove(it)
				println "${it} has been removed."
			}
			else {
				println "Running dry, otherwise ${it} would have been removed."
			}
		}
		if(!dryRun) {
			manager.save()
			println "Save done."
		}
	}
}