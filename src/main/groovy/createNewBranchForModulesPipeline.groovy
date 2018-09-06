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
		[ "Module ${it}" : branchModule(it, request.rootBranch, request.targetBranch)]
	}
	parallel parallelBranching
}


def branchModule(module,rootBranch,targetBranch) {
	return {
		node {
			sh "#!/bin/bash \n" + 
			"export CVSROOT=/var/local/cvs/root\n"
			"cvs rtag -b -r ${rootBranch} ${targetBranch}"
		}
	}
}