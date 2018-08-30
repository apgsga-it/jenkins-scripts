import hudson.*
import hudson.model.*
import jenkins.model.*

def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver

def crumb = resolver.resolve("CONFIRMATION")
if (!crumb.equals("SURE")) {
	println "Not executing script, done with you"
	return
}

def dry = resolver.resolve("DRY")
println "Running with DRY:  ${dry}"
// First Delete Job in Patch Views
println "Deleting all Job for Patch Views"
["ProductivePatches", "Patches"].each { viewName ->
	println "Deleteing Jobs from ${viewName}"
	Jenkins.instance.getView(viewName).items.each { item ->
		println "About to delete $item.name"
		if (dry.equals('false')) {
			item.delete()
			println "Deleted $item.name"
		} else {
			println "Did'nt delete anything, running dry"
		}
	}
}
println "Done."
def withBuilds = resolver.resolve("BUILDS")
println "With Builds: ${withBuilds}"
if (!withBuilds.equals('true')) {
	println "Skipping cleaning Builds"
	return
}
println "Deleteing all Builds from remaining Jobs"
// Then Deleted all Builds for existing Jobs
def jobs = Jenkins.instance.getAllItems(hudson.model.AbstractProject.class).each {  job ->
	println "About to delete Builds for ${job.name}"
	if (!job.name.equals('Init Jenkins')) {
		job.getBuilds().each { b ->
			println "About to deleted Build ${b}"
			if (dry.equals('false')) {
				b.delete()
				println "Deleted Build"
			} else  {
				println "Did'nt delete anything, running dry"
			}
		}}
}
println "Deleteing all Builds from remaining Maven Jobs"
// Delete the missed Maven Jobs
jobs = Jenkins.instance.getAllItems(hudson.maven.MavenModuleSet.class).each {  job ->
	println "About to delete Builds for Maven Job ${job.name}"
	job.getBuilds().each { b ->
		println "About to deleted Build ${b}"
		if (dry.equals('false')) {
			b.delete()
			println "Deleted Build"
		} else  {
			println "Did'nt delete anything, running dry"
		}
	}
}