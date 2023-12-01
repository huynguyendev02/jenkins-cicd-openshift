
def call(Map config = [:]) {
    echo "Pulling and Deploying Jar file with Ansible..."
   
    
    withCredentials([usernamePassword(credentialsId: config.nexusCredentialsId, passwordVariable: 'NEXUS_CREDENTIALS_PSW', usernameVariable: 'NEXUS_CREDENTIALS_USER')]) {
        ansiblePlaybook(
            playbook: config.playbook,
            inventory: config.inventory,
            credentialsId: config.deployCredentialsId,
            disableHostKeyChecking: true,
            colorized: true,
            extraVars: [
                WORKING_DIR_ARTIFACT: config.workingDirArtifact,
                ARTIFACT_URL: "http://${NEXUS_CREDENTIALS_USER}:${NEXUS_CREDENTIALS_PSW}@"+config.artifactUrl,
                DEPLOY_IP: config.deployIP,
                DEPLOY_USER: config.deployUser 
            ]
        )
    }
    
}
