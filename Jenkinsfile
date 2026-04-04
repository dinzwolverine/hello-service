pipeline {
    agent any

    environment {
        // Updated to us-east-1 to match your active cluster
        AWS_REGION = 'ap-south-1' 
        IMAGE_NAME = 'hello-service'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        // ── STAGE 1: Validation (Secrets Check) ──────────────
        stage('Validate Secrets') {
            steps {
                echo "🔍 Testing Jenkins Credentials..."
                withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                    // This verifies your AWS Access/Secret keys work
                    sh 'aws sts get-caller-identity'
                }
                withCredentials([string(credentialsId: 'ecr-registry', variable: 'ECR_REPO')]) {
                    // This verifies the 'ecr-registry' secret exists
                    echo "✅ ECR Secret found: ${env.ECR_REPO.substring(0, 5)}***"
                }
            }
        }

        // ── STAGE 2: Checkout ─────────────────────────────
        stage('Checkout') {
            steps {
                // Using scm variable ensures webhook triggers work correctly
                checkout scm
            }
        }

        // ── STAGE 3: Build & Package ───────────────────────
        // Combined for faster validation during testing
        stage('Build & Package') {
            steps {
                sh 'mvn clean package -DskipTests -B'
            }
        }

        // ── STAGE 4: Docker Build & Push ───────────────────
        stage('Docker & ECR Push') {
            steps {
                withCredentials([string(credentialsId: 'ecr-registry', variable: 'ECR_REPO')]) {
                    withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | \
                            docker login --username AWS --password-stdin ${env.ECR_REPO}

                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${env.ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${env.ECR_REPO}/${IMAGE_NAME}:latest

                            docker push ${env.ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}
                            docker push ${env.ECR_REPO}/${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }

        // ── STAGE 5: Deploy to ECS ───────────────────────
        stage('Deploy to ECS') {
            when {
                branch 'main'
            }
            steps {
                withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                    sh """
                        aws ecs update-service \
                          --cluster hello-cluster \
                          --service hello-service \
                          --force-new-deployment \
                          --region ${AWS_REGION}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ SUCCESS: Build #${env.BUILD_NUMBER}. Webhook and Credentials are verified!"
        }
        failure {
            echo "❌ FAILED: Build #${env.BUILD_NUMBER}. Check 'Validate Secrets' stage logs."
        }
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh 'docker image prune -f'
        }
    }
}
