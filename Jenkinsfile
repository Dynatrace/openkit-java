def branchPattern = /v\d+\.\d+\.\d+.*/
def jvmToTest = ["JAVA_HOME","JAVA_HOME_6","JAVA_HOME_7","JAVA_HOME_8"]

properties([
		buildDiscarder(logRotator(numToKeepStr: '10'))
])

timeout(time: 15, unit: 'MINUTES') {
	timestamps {
		node('default') {
			stage('Checkout') {
				checkout scm

				def currentVersion = sh script: printVersion(), returnStdout: true
				currentBuild.displayName += " - ${currentVersion}"
			}

			stage('Build') {
				parallel (['Java': createBuildTask(jvmToTest.join(","))])
			}

			echo "Branch: ${env.BRANCH_NAME}"
			stage('Publish') {
				withCredentials([file(credentialsId: 'init.gradle', variable: 'INIT_GRADLE')]) {
					copy env.INIT_GRADLE, 'init.gradle'
				}

				// Builds the artifacts with the default target compatibility (Java 6)
				// and publishes them to the artifactory
				// Adds the build number to the project version
				gradlew "assemble publish --init-script init.gradle"
			}
	  }
	}
}

def createBuildTask(jvmToTest) {
	return {
		node('default') {
			withEnv(["JVM_TO_TEST=${jvmToTest}"]) {
				checkout scm

				gradlew "clean assemble compileTestJava --parallel"

				try {
					gradlew "check --continue --parallel"
				} finally {
					junit testResults: '**/build/test-results/test/TEST-*.xml', keepLongStdio: false
					archiveArtifacts artifacts: '**/build/reports/**'
				}
			}
		}
	}
}

def copy(String source, String destination) {
	if (isUnix()) {
		sh "cp -f ${source} ${destination}"
	} else {
		bat "copy ${source} ${destination}"
	}
}

def printVersion() {
	if (isUnix()) {
		return "./gradlew -q printVersion"
	} else {
		return "gradlew.bat -q printVersion"
	}
}
