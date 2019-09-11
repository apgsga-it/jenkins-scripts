@Grab('org.jfrog.artifactory.client:artifactory-java-client-services:2.6.0')

import org.jfrog.artifactory.client.*

println "Starting to clean Artifactory Repositories ...."


/*
def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver


def testv = resolver.resolve("testv")


println "testv: ${testv}"
*/

def artifactory = ArtifactoryClientBuilder.create().setUrl(env.ARTIFACTORY_SERVER_ID).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();

println "Cleaning Artifactory Repositories - DONE"
