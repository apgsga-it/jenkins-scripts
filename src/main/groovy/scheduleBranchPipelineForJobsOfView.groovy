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
def moduleList = []
hudson.model.Hudson.instance.getView(viewName).items.each()  { job ->
	println "Processing Job " + job.name 
	def configXMLFile = job.getConfigFile().getFile().getAbsolutePath();
	def configXml = new XmlSlurper().parse(configXMLFile)
	println "Modulename : "
	// TODO (che, 4.9.2018) : could be more then one
	def remoteName =  configXml.depthFirst().find{ node -> node.name() == 'remoteName'}
	moduleList << [name:remoteName]
	println remoteName
 
}
println moduleList
def jsonOutput = new JsonBuilder(moduleList)
println jsonOutput

