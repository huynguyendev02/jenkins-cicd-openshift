
def call(Map config = [:]) {
    echo "Pushing Jar file to Nexus Repo..."

    readXmlFile = readMavenPom file: ''
    nexusArtifactUploader(
        nexusVersion: 'nexus3',
        protocol: 'http',
        nexusUrl: "${config.nexusIP}:${config.nexusPort}",
        groupId: "${config.groupId}",
        version: "${config.appTag}",
        repository: "${config.nexusRepoPush}",
        credentialsId: "${config.nexusCredentialsId}",
        artifacts: [
            [artifactId: "${config.artifactId}" ,
            classifier: '',
            file: "target/${readXmlFile.artifactId}-${readXmlFile.version}.jar",
            type: 'jar']
        ]
    )
}
