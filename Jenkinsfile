pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1' 
    }

    options {
        timeout(time: 5, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Environment Audit') {
            steps {
                echo "🔍 Verifying VM Tooling..."
                sh 'aws --version'
                sh 'docker --version'
                sh 'java -version'
            }
        }

        stage('AWS Auth Check') {
            steps {
                echo "🔐 Testing AWS IAM Connectivity..."
                // This uses your 'AWS Credentials' type in Jenkins
                withAWS(credentials: 'aws-credentials', region: env.AWS_REGION) {
                    sh 'aws sts get-caller-identity'
                }
            }
        }

        stage('Docker Check') {
            steps {
                echo "🐳 Testing Docker Socket Access..."
                sh 'docker info | grep "Containers"'
            }
        }
    }

    post {
        success {
            echo "🏆 SYSTEM READY: AWS and Docker are fully functional!"
        }
        failure {
            echo "❌ STILL FAILING: Check Jenkins Console Output for the exact error."
        }
    }
}
