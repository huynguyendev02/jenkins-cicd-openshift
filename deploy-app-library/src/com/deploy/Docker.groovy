package com.deploy
class Docker {
    def nexusIP
    def nexusDockerPort
    def imageName

    def scripts = []

    Docker(String nexusIP, String nexusDockerPort, String imageName) {
        this.nexusIP = nexusIP
        this.nexusDockerPort = nexusDockerPort
        this.imageName = imageName
    }

    def login() {
        this.scripts.add("echo \${PSW} | docker login -u \${USER} --password-stdin ${nexusIP}:${nexusDockerPort}")
        return this

    }

    def build(String pathDockerfile, def imageTag) {
        String script = "docker build -f ${pathDockerfile} "
        for (tag in imageTag) {
            script+="-t ${nexusIP}:${nexusDockerPort}/${imageName}:${tag} "
        }
        this.scripts.add("${script} .")
        return this
    }
    def tag(String imageTag, def imageReTag) {
        this.scripts.add("docker tag ${nexusIP}:${nexusDockerPort}/${imageName}:${imageTag}  ${nexusIP}:${nexusDockerPort}/${imageName}:${imageReTag} ")
        return this
    }

    def push(String imageTag) {
       
        this.scripts.add("docker push ${nexusIP}:${nexusDockerPort}/${imageName}:${imageTag}")
  
        return this
    }

    def pull(String imageTag) {
       
        this.scripts.add("docker pull ${nexusIP}:${nexusDockerPort}/${imageName}:${imageTag}")
  
        return this
    }

    def remove(String containerName) {
        this.scripts.add("set +e && docker rm -f ${containerName} && set -e")
        return this
    }

    def run(String imageTag, String containerName, def ports, def volumes) {
        String script = "docker run -d "
        for (port in ports) 
            script+="-p ${port} "

        for (volume in volumes) 
            script+="-v ${volume} "
        script+="--name ${containerName} ${nexusIP}:${nexusDockerPort}/${imageName}:${imageTag}"
        this.scripts.add(script)
        return this
    }
    String toString() {
        return this.scripts.join(' && ')
    }
    
}