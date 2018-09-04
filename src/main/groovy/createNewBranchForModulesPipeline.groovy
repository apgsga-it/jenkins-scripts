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
	
	request.modules.each { module -> println module
		
	}
}