

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

//curlCmd = "curl -L -u ${username}:${userpwd} -X POST -H 'Content-Type: text/plain' -d '${query}' ${searchRequestUrl}"
curlCmd = "curl -L -u ${username}:${userpwd} -X POST -H \"Content-Type: text/plain\" -d \"items.find({\\\"type\\\":\\\"file\\\",\\\"\\\$and\\\": [{\\\"name\\\":{\\\"\\\$match\\\":\\\"*9.1.0.ADMIN-UIMIG-198*\\\"}},{\\\"name\\\":{\\\"\\\$nmatch\\\":\\\"*zip*\\\"}}]})\" http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql"
//curlCmd = "curl -L -u xxx:xxx -X POST -H \"Content-Type: text/plain\" -d \"items.find({\\\"type\\\":\\\"file\\\",\\\"\$and\\\": [{\\\"name\\\":{\\\"\$match\\\":\\\"*9.1.0.ADMIN-UIMIG-198*\\\"}},{\\\"name\\\":{\\\"\$nmatch\\\":\\\"*zip*\\\"}}]})\" http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/api/search/aql"

println "curlCmd : ${curlCmd}"

def proc = curlCmd.execute()
def sout = new StringBuilder()
def serr = new StringBuilder()
proc.consumeProcessOutput(sout,serr)
proc.waitForOrKill(10000)
println "out: ${sout}"
println "=========================================================================="
println "err: ${serr}"


println "Done"