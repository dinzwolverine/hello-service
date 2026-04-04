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
        // ── STAGE 1: WEBHOOK & GITHUB VALIDATION ─────────────
        stage('Validate Webhook') {
            steps {
                echo "✅ Webhook Triggered Successfully!"
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Running on VM: ${env.NODE_NAME}"
            }
        }

        // ── STAGE 2: AWS IAM CREDENTIALS VALIDATION ──────────
        stage('Validate AWS Secrets') {
            steps {
                echo "🔐 Testing Jenkins AWS Credentials..."
                // Using 'aws-credentials' ID from Jenkins Credentials Store
                withCredentials([usernamePassword(credentialsId: 'aws-credentials', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
                    sh """
                        export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                        export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
                        export AWS_DEFAULT_REGION=${AWS_REGION}
                        
                        echo "🛰️ Contacting AWS Security Token Service..."
                        aws sts get-caller-identity
                    """
                }
            }
        }

        // ── STAGE 3: LOCAL DOCKER VALIDATION ─────────────────
        stage('Validate Docker Installation') {
            steps {
                echo "🐳 Testing Local Docker Permissions..."
                // This ensures 'jenkins' user can run docker without 'sudo'
                sh 'docker version'
                sh 'docker info | grep "Containers"'
            }
        }
    }

    post {
        success {
            echo "------------------------------------------------"
            echo "🏆 AUTHENTICATION & ENVIRONMENT VERIFIED!"
            echo "------------------------------------------------"
        }
        failure {
            echo "❌ VALIDATION FAILED. Check 'Manage Jenkins > Credentials' or Docker permissions."
        }
    }
}
