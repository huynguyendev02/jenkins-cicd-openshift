
def call(Map config = [:]) {
    echo "Setup and config Docker with Ansible..."
   
    ansiblePlaybook(
        credentialsId:  config.deployCredentialsId,
        playbook: config.playbook,
        inventory: config.inventory,
        colorized: true,
        disableHostKeyChecking: true,
        extraVars: [
            USER_IN_NGINX: config.deployUser,
            DEPLOY_IP: config.deployIP,
            DEPLOY_USER: config.deployUser,
            NEXUS_IP: config.nexusIP,
            NEXUS_DOCKER_PORT: config.nexusDockerPort
        ]
    )
}

