import hudson.*
import hudson.model.*
import jenkins.model.*
import groovy.xml.*
import groovy.json.*

def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver
def viewName = resolver.resolve("VIEWNAME")
def targetBranch = resolver.resolve("TARGETBRANCH")



hudson.model.Hudson.instance.getView(viewName).items.each()  { job ->
	def configXMLFile = job.getConfigFile().getFile().getAbsolutePath();
	def configXml = new XmlSlurper().parse(configXMLFile)
	println XmlUtil.serialize(configXml).toString()
	println " "
	// TODO (che, 4.9.2018) : could be more then one
	def branchName =  configXml.depthFirst().find{ node -> node.name() == 'locationName'}
	println branchName

}


