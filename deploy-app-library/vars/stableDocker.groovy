import com.deploy.Docker

def call(Map config = [:]) {
    echo "Retag Docker image to Stable and push to Nexus"
    def docker = new Docker(
        config.nexusIP, 
        config.nexusDockerPort,
        config.imageName)
    
    withCredentials([usernamePassword(credentialsId: config.nexusCredentialsId, passwordVariable: 'PSW', usernameVariable: 'USER')]) {
        def script = docker
                        .login()
                        .tag(config.imageTag, "stable")
                        .push("stable")
                        .toString()
        sh "${script}"
    }
    
}
