println "Starting to clean Artifactory Repositories ...."


/*
def thr = Thread.currentThread()
// get current build
def build = thr?.executable
def resolver = build.buildVariableResolver


def testv = resolver.resolve("testv")


println "testv: ${testv}"
*/

//def artifactory = ArtifactoryClientBuilder.create().setUrl(env.ARTIFACTORY_SERVER_ID).setUsername(mavenRepoUser).setPassword(mavenRepoPwd).build();


withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'artifactoryDev',
	usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {

u = "${USERNAME}"
p = "${PASSWORD}"
}


println "${u} / ${p}"


println "Cleaning Artifactory Repositories - DONE"
