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
		[ "Branching Module ${it}" : branchModule(it, request.rootBranch, request.targetBranch)]
	}
	parallel parallelBranching
}


def branchModule(module,rootBranch,targetBranch) {
	return {
		node {
			def cvsCmd = "cvs rtag -b -r ${rootBranch} ${targetBranch}"
			sh "#!/bin/bash \n" + 
			"${cvsCmd}"
		}
	}
}