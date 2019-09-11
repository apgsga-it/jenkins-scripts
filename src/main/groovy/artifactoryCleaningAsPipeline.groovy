
["repo1","repo2"].each {repo -> 

	def artifactory = ArtifactoryClientBuilder.create().setUrl(env.mavenRepoBaseUrl).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();
		
	stage(repo) {
		println "this was a test"
	}
	
}

