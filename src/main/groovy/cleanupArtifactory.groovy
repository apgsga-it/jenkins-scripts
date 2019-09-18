

def env = System.getenv()
def username = env["artifactoryUser"]
def userpwd = env["artifactoryPassword"]

println "trying to run a sh command ..."


def query = "items.find({\"repo\":\"releases-test\", \"created\":{\"\$lt\":\"2099-01-01\"}, \"type\":\"file\", \"name\":{\"\$match\":\"*.zip\"}})"

def artifactoryUrl = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga"
def searchRequestUrl = "${artifactoryUrl}/api/search/aql"

def curlCmd
def res


println "query : ${query}"
println "searchRequestUrl : ${searchRequestUrl}"

curlCmd = "curl -L -u ${username}:${userpwd} -X POST -H \"Content-Type: text/plain\" -d '${query}' ${searchRequestUrl}"

println "curlCmd : ${curlCmd}"

def proc = curlCmd.execute()
def sout = new StringBuilder()
def serr = new StringBuilder()
proc.consumeProcessOutput(sout,serr)
proc.waitForOrKill(5000)
println "out: ${sout}"
println "=========================================================================="
println "err: ${serr}"


println "Done"