<<<<<<< HEAD
pipeline {
    agent any

    stages {
        stage('Webhook Trigger Test') {
            steps {
                echo "Webhook triggered successfully!"
                sh 'date'
                sh 'whoami'
            }
        }
    }
}
=======
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import jenkins.model.*

def creds = CredentialsProvider.lookupCredentials(
    Credentials.class,
    Jenkins.instance,
    null,
    null
)

def pipelineCreds = ['github-token', 'ecr-registry', 'aws-credentials']

creds.each { c ->
    if (pipelineCreds.contains(c.id)) {
        println "ID: ${c.id}"
        println "Type: ${c.getClass().getName()}"
        println "Description: ${c.description}"
        println "-----------------------------"
    }
}
>>>>>>> 6e9082d (Testing Webhooks)
