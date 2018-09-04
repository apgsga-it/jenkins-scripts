import hudson.*
import hudson.model.*
import jenkins.model.*
import groovy.xml.*
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource


def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver
def viewName = resolver.resolve("VIEWNAME")

hudson.model.Hudson.instance.getView(viewName).items.each()  { job ->
	println "Processing Job " + job.name 
	def configXMLFile = job.getConfigFile().getFile().getAbsolutePath();
	def configXml = new XmlSlurper().parse(configXMLFile)
	println "Modulename : "
	def remoteName =  configXml.depthFirst().find{ node -> node.name() == 'remoteName'}
	println remoteName
  
}