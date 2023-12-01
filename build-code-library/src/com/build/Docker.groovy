package com.build
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

    def push(String imageTag) {
       
        this.scripts.add("docker push ${nexusIP}:${nexusDockerPort}/${imageName}:${imageTag}")
  
        return this
    }
    String toString() {
        return this.scripts.join(' && ')
    }
    
}