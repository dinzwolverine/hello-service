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
                echo "🔍 Checking VM Tooling..."
                // These will fail if not installed, telling us exactly what's missing
                sh 'java -version'
                sh 'mvn -version'
                sh 'aws --version'
                sh 'docker --version'
            }
        }

        stage('AWS Auth Check') {
            steps {
                echo "🔐 Testing AWS IAM Connectivity..."
                // We extract the keys directly from your 'aws-credentials' secret
                withCredentials([aws(credentialsId: 'aws-credentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh """
                        export AWS_DEFAULT_REGION=${AWS_REGION}
                        aws sts get-caller-identity
                    """
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
            echo "🏆 SYSTEM READY, DINESH! AWS and Docker are fully functional."
        }
        failure {
            echo "❌ STILL FAILING. Check the 'Environment Audit' stage logs."
        }
    }
}
