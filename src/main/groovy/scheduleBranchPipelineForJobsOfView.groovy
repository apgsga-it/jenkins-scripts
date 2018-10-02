import groovy.json.*
import groovy.xml.*
import hudson.*
import hudson.model.*
import jenkins.model.*

def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver
def viewName = resolver.resolve("VIEWNAME")
def rootBranch = resolver.resolve("ROOTBRANCH")
def targetBranch = resolver.resolve("TARGETBRANCH")
def cvsRoot = resolver.resolve("CVSROOT")


def moduleList = []
hudson.model.Hudson.instance.getView(viewName).items.each()  { job ->
	def configXMLFile = job.getConfigFile().getFile().getAbsolutePath();
	def configXml = new XmlSlurper().parse(configXMLFile)
	// TODO (che, 4.9.2018) : could be more then one
	def remoteName =  configXml.depthFirst().find{ node -> node.name() == 'remoteName'}
	def moduleName = remoteName.toString()
	if (moduleName.endsWith("dao") | moduleName.endsWith(".ui") | moduleName.endsWith("impl") 
		| moduleName.endsWith("proc") 
		| moduleName.endsWith("domainwerte") | moduleName.endsWith("runtime") | moduleName.endsWith("rep") | moduleName.endsWith("utils")) {
		moduleList << [moduleName:"${moduleName}"]
		println moduleName
	}

}
println moduleList
def jsonOutput = new JsonBuilder(["rootBranch":rootBranch, "targetBranch":targetBranch ,"cvsRoot":cvsRoot,modules:moduleList])
println jsonOutput
def parameter = new StringParameterValue('PARAMETER', jsonOutput.toPrettyString());
def paramsAction = new ParametersAction(parameter)
def cause = new hudson.model.Cause.UpstreamCause(build)
def causeAction = new hudson.model.CauseAction(cause)
def job = hudson.model.Hudson.instance.getJob('createNewBranchForModulesPipeline')
hudson.model.Hudson.instance.queue.schedule(job, 0, causeAction, paramsAction)

