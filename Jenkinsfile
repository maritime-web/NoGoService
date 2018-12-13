pipeline {
    agent any

    triggers {
        pollSCM('H/15 * * * *')
    }

    stages {
        stage('build') {
            steps {
                sh './gradlew clean build'
            }
            post {
                success {
                    archiveArtifacts(artifacts: '**/build/libs/*.war', allowEmptyArchive: true)
                }
            }
        }
    }


    post {
        failure {
            // notify users when the Pipeline fails
            mail to: 'steen@lundogbendsen.dk',
                    subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                    body: "Something is wrong with ${env.BUILD_URL}"
        }
    }
}

