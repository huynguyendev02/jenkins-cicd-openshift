import com.build.Docker

def call(Map config = [:]) {
    echo "Pushing Docker image..."
    def docker = new Docker(
        config.nexusIP, 
        config.nexusDockerPort,
        config.imageName)
    
    withCredentials([usernamePassword(credentialsId: config.nexusCredentialsId, passwordVariable: 'PSW', usernameVariable: 'USER')]) {
        def script = docker
                        .login()
                        .push("latest")
                        .push(config.imageTag)
                        .toString()
        sh "${script}"
    }
    
}
