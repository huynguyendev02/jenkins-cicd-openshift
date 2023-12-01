import com.build.Docker

def call(Map config = [:]) {
    echo "Building Docker image..."
    def docker = new Docker(
        config.nexusIP, 
        config.nexusDockerPort,
        config.imageName)
        
    def script = docker.build(
        config.pathDockerfile, 
        config.imageTag
    ).toString()

    sh "${script}"
}
