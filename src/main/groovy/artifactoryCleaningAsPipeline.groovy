#!groovy

["repo1","repo2"].each {repo -> 

	//def artifactory = ArtifactoryClientBuilder.create().setUrl(mavenRepoBaseUrl).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();
		
	stage(repo) {
		println "this was a test"
		
		
		
		println "${env.ARTIFACTORY_SERVER_ID}"
		
		def server = Artifactory.server env.ARTIFACTORY_SERVER_ID
		
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
					usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
		
				server.username = "${USERNAME}"
				server.password = "${PASSWORD}"
			}
		
		
		println "${server.username}"	
		
	}
	
}

