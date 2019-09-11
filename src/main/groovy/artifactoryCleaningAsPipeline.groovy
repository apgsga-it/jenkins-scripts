#!groovy

["repo1","repo2"].each {repo -> 

	//def artifactory = ArtifactoryClientBuilder.create().setUrl(mavenRepoBaseUrl).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();
		
	stage(repo) {
		println "this was a test"
		
		
		
		println "${env.ARTIFACTORY_SERVER_ID}"
		
//		def server = Artifactory.server env.ARTIFACTORY_SERVER_ID
		def u
		def p
		
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
					usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
		
				u = "${USERNAME}"
				p = "${PASSWORD}"
			}
		
		
		println "${u} / ${p}"
		
		def res = sh script:"curl www.google.com", returnStatus:true
		
		println "res: ${res}"
		
	}
	
}

