

def env = System.getenv()
println env["artifactoryUser"]
println env["artifactoryPassword"]

println "trying to run a sh command ..."

def proc = "curl www.google.ch".execute()
def sout = new StringBuilder()
def serr = new StringBuilder()
proc.consumeProcessOutput(sout,serr)
proc.waitForOrKill(5000)
println "out: ${sout} ..... err: ${serr}"


println "Done"