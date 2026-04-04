pipeline {
    agent any

    environment {
        // AWS Configuration
        AWS_REGION = 'us-east-1' 
        
        // Docker/ECR Configuration
        // Replace with your actual Account ID if it changes
        ECR_REGISTRY = "971201280747.dkr.ecr.us-east-1.amazonaws.com"
        IMAGE_NAME   = "hello-service"
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        // ── STAGE 1: Environment Audit ─────────────────────
        stage('Audit Tools') {
            steps {
                echo "🔍 Verifying Build Environment for Dinesh..."
                sh 'java -version'
                sh 'mvn -version'
                sh 'aws --version'
                sh 'docker --version'
            }
        }

        // ── STAGE 2: Build & Test ──────────────────────────
        stage('Build & Test') {
            steps {
                echo "📦 Compiling and Testing with Maven..."
                sh 'mvn clean package -B'
            }
            post {
                always {
                    // This creates the nice test graphs in Jenkins
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── STAGE 3: Docker Build & ECR Push ───────────────
        stage('Docker & ECR Push') {
            steps {
                echo "🏗️ Building Docker Image & Pushing to ECR..."
                // Using the verified 'aws' credentials wrapper
                withCredentials([aws(credentialsId: 'aws-credentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh """
                        export AWS_DEFAULT_REGION=${AWS_REGION}
                        
                        # Login to ECR
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        # Build the image
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        
                        # Tag as both Build Number and Latest
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_NAME}:latest

                        # Push to AWS
                        docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        // ── STAGE 4: Deploy to ECS ─────────────────────────
        stage('Deploy to ECS') {
            when {
                branch 'main'
            }
            steps {
                echo "🚀 Deploying to ECS Fargate Cluster..."
                withCredentials([aws(credentialsId: 'aws-credentials', accessKeyVariable: 'AWS_ACCESS_KEY_ID', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh """
                        export AWS_DEFAULT_REGION=${AWS_REGION}
                        
                        # Trigger a Rolling Update in ECS
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
            echo "------------------------------------------------"
            echo "🏆 SUCCESS: Build #${env.BUILD_NUMBER} is Live!"
            echo "------------------------------------------------"
        }
        failure {
            echo "❌ FAILED: Build #${env.BUILD_NUMBER}. Please check the logs above."
        }
        always {
            echo "🧹 Cleaning up local Docker images..."
            // Removes the local images to save disk space on your VM
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh "docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh "docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:latest || true"
            sh 'docker image prune -f'
        }
    }
}
