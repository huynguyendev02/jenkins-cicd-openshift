import com.deploy.Docker

def call(Map config = [:]) {
    def docker = new Docker(
        config.nexusIP, 
        config.nexusDockerPort,
        config.imageName
    )
    withCredentials([usernamePassword(credentialsId: config.nexusCredentialsId, passwordVariable: 'PSW', usernameVariable: 'USER')]) {
        def script = docker
                        .login()
                        .pull("latest")
                        .pull(config.imageTag)
                        .remove(config.containerName)
                        .run("latest", config.containerName, config.ports, config.volumes)
                        .toString()
        sshagent([config.deployCredentialsId]) {
            sh """
                ssh -o StrictHostKeyChecking=no ubuntu@${config.deployIP} <<EOF
                    mkdir -p ${config.workingDirArtifact}log; touch ${config.workingDirArtifact}log/app.log
                    ${script}
                <<EOF
            """
        }
    }
    
}
