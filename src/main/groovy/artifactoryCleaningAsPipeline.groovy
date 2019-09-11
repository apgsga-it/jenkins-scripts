#!groovy
import org.jfrog.artifactory.client.ArtifactoryClientBuilder

["repo1","repo2"].each {repo -> 

	def artifactory = ArtifactoryClientBuilder.create().setUrl(mavenRepoBaseUrl).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();
		
	stage(repo) {
		println "this was a test"
	}
	
}

