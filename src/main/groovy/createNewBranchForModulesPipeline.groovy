#!groovy
import groovy.json.JsonSlurperClassic
properties([
	parameters([
		stringParam(
		defaultValue: "",
		description: 'Parameter',
		name: 'PARAMETER'
		)
	])
])
def request = new JsonSlurperClassic().parseText(params.PARAMETER)

stage ("Cvs Branching Modules") {
	def parallelBranching = request.modules.collectEntries {
		[ "Module ${it.moduleName}" : branchModule(it, request.rootBranch, request.targetBranch, request.cvsRoot)]
	}
	parallel parallelBranching
}


def branchModule(module,rootBranch,targetBranch,cvsRoot) {
	return {
		node {
			def returnCode = sh "#!/bin/bash \n" + 
			"set -x\n" +
			"export CVSROOT=${cvsRoot}\n" +
			"cvs rtag -b -r ${rootBranch} ${targetBranch} ${module.moduleName}"
			println returnCode
			returnCode
		}
	}
}