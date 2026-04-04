pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-1' 
        IMAGE_NAME = 'hello-service'
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    // REMOVED the 'tools' block to avoid naming conflicts. 
    // It will now use the Maven and JDK installed on your Linux VM.

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {
        stage('Validate Environment') {
            steps {
                echo "🔍 Checking VM Tools..."
                sh 'mvn -version'
                sh 'java -version'
                
                withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                    sh 'aws sts get-caller-identity'
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Package') {
            steps {
                // Use absolute path if 'mvn' isn't in global PATH, 
                // but usually just 'mvn' works on Ubuntu.
                sh 'mvn clean package -DskipTests -B'
            }
        }

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

        stage('Deploy to ECS') {
            when { branch 'main' }
            steps {
                withAWS(region: AWS_REGION, credentials: 'aws-credentials') {
                    sh "aws ecs update-service --cluster hello-cluster --service hello-service --force-new-deployment --region ${AWS_REGION}"
                }
            }
        }
    }

    post {
        always {
            sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true"
            sh 'docker image prune -f'
        }
    }
}
