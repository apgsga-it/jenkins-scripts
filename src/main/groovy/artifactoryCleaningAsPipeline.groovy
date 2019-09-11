
["repo1","repo2"].each {repo -> 

	stage(repo) {
		println "this was a test"
	}
	
}

