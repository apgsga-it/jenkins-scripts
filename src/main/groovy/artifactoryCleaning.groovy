import org.jfrog.artifactory.client.ArtifactoryClientBuilder

println "Starting to clean Artifactory Repositories ...."

artifactory = ArtifactoryClientBuilder.create().setUrl(env.mavenRepoBaseUrl).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();

println "Cleaning Artifactory Repositories - DONE"
