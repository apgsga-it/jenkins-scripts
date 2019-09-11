import org.jfrog.*

println "Starting to clean Artifactory Repositories ...."

artifactory = ArtifactoryClientBuilder.create().setUrl("url").setUsername("oneuser").setPassword("onepwd").build();

println "Cleaning Artifactory Repositories - DONE"
