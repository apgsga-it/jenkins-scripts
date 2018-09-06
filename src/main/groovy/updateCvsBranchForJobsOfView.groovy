import hudson.*
import hudson.model.*
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import jenkins.model.*
import groovy.xml.*
import groovy.json.*

def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver
def viewName = resolver.resolve("VIEWNAME")
def targetBranch = resolver.resolve("TARGETBRANCH")
def dry = resolver.resolve("DRY")

hudson.model.Hudson.instance.getView(viewName).items.each()  { job ->
	def configXMLFile = job.getConfigFile().getFile().getAbsolutePath();
	def configXml = new XmlSlurper().parse(configXMLFile)
	def branchName =  configXml.depthFirst().find{ node -> node.name() == 'locationName'}
	branchName.replaceNode {
		locationName(targetBranch)
	}
	println "After Update: "
	println XmlUtil.serialize(configXml).toString()
	Source xmlInput=new StreamSource(new StringReader(XmlUtil.serialize(configXml)));
	if (dry.equals('false') {
		job.updateByXml(xmlInput)
	}

}



