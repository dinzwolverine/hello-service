pipeline {
    agent any

    environment {
        // Updated to us-east-1 as per our successful deployment
        AWS_REGION = 'us-east-1' 
        IMAGE_NAME = 'hello-service'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        // Replace with your actual ECR Registry URI (e.g., 971201280747.dkr.ecr.us-east-1.amazonaws.com)
        ECR_REGISTRY = "971201280747.dkr.ecr.${AWS_REGION}.amazonaws.com"
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
        // ── STAGE 1: Checkout ─────────────────────────────
        stage('Checkout') {
            steps {
                // Ensure your credentialsId 'github-token' is correctly set in Jenkins
                checkout scm
            }
        }

        // ── STAGE 2: Build ───────────────────────────────
        stage('Build') {
            steps {
                sh 'mvn clean compile -B'
            }
        }

        // ── STAGE 3: Test ────────────────────────────────
        stage('Test') {
            steps {
                sh 'mvn test -B'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // ── STAGE 4: Package ─────────────────────────────
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests -B'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // ── STAGE 5: Docker Build ────────────────────────
        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        // ── STAGE 6: Push to ECR ─────────────────────────
        stage('Push to ECR') {
            steps {
                // Using 'aws-credentials' (Access Key/Secret Key) stored in Jenkins
                withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${ECR_REGISTRY}/${IMAGE_NAME}:latest

                        docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        // ── STAGE 7: Deploy to ECS ───────────────────────
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
            echo "✅ SUCCESS: Build #${env.BUILD_NUMBER} is live in us-east-1"
        }
        failure {
            echo "❌ FAILED: Build #${env.BUILD_NUMBER}"
        }
        always {
            // Clean up local images to save VM disk space
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh "docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh "docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:latest || true"
            sh 'docker image prune -f'
        }
    }
}
