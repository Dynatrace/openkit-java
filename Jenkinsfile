def currentVersion = readVersion('version.properties')
def buildVersion = currentVersion + "-b" + env.BUILD_NUMBER
def jvmsToTest = ["JAVA_HOME","JAVA_HOME_6","JAVA_HOME_7","JAVA_HOME_8","JAVA_HOME_11"]

properties([
		buildDiscarder(logRotator(numToKeepStr: '10'))
])

timeout(time: 15, unit: 'MINUTES') {
	timestamps {
		node('default') {
			stage('Checkout') {
				checkout scm

				currentBuild.displayName += " - ${buildVersion}"
			}

			stage('Build') {
				parallel (['Java': createBuildTask(jvmsToTest.join(","),currentVersion)])
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

def createBuildTask(jvmsToTest,currentVersion) {
	return {
		node('default') {
			withEnv(["JVMS_TO_TEST=${jvmsToTest}"]) {
				checkout scm

				gradlew "-Pversion=${currentVersion} clean assemble compileTestJava --parallel"

				try {
					gradlew "-Pversion=${currentVersion} check --continue --parallel"
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
