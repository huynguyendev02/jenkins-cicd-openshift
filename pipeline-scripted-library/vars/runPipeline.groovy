
def call() {
    def buildCodeLibrary = library('build-code-library@main')
    def scanCodeLibrary = library('scan-code-library@main')
    def deployAppLibrary = library('deploy-app-library@main')

    
    node {
        try {
            stage('Checkout SCM') {
                checkout scm
                env.GIT_COMMIT = checkout(scm).GIT_COMMIT

                echo env.BRANCH_NAME
                echo env.CHANGE_ID
            }
            stage('Load environment variables') { 
                FAILED_STAGE = env.STAGE_NAME
                load "./jenkinsEnv/Environment.groovy"
            }

            stage('Build code with Maven') { 
                FAILED_STAGE = env.STAGE_NAME

                def pomFile = 'pom.xml'
                def pom = readMavenPom file: pomFile
                pom.version = env.APP_TAG
                writeMavenPom file: pomFile, model: pom

                
                mavenCommand(
                    this: this,
                    mavenHome: env.MAVEN_HOME,
                    options: '-DskipTests -Dcheckstyle.skip',
                    command: 'clean install'
                )
            }
            stage('Run Unit test') { 
                FAILED_STAGE = env.STAGE_NAME
                mavenCommand(
                    this: this,
                    mavenHome: env.MAVEN_HOME,
                    options: '-Dcheckstyle.skip',
                    command: 'test'
                )
            }

            stage('Analysis code with SonarQube') {
                FAILED_STAGE = env.STAGE_NAME
                sonarScanner(
                    sonarServer: env.SONAR_SERVER,
                    sonarProjectName: env.SONAR_PROJECT_NAME,
                    scannerHome: env.SCANNER_HOME,
                    appName: env.APP_NAME,
                    projectVersion: env.APP_TAG
                )
            }    
            
            stage('SonarQube Quality Gate') {
                FAILED_STAGE = env.STAGE_NAME
                sonarQualityGate()
            }

            if ((env.BRANCH_NAME == 'main' ||
                env.BRANCH_NAME ==~ /(uat)(\/)*((\/)*\w+)*/ ||
                env.BRANCH_NAME ==~ /(dev)(\/)*((\/)*\w+)*/ ) && env.CHANGE_ID == null
            ) {
                stage('Push the Jar file to Nexus Repo') {
                    FAILED_STAGE = env.STAGE_NAME
                    nexusUploader(
                        nexusIP: env.NEXUS_IP,
                        nexusPort: env.NEXUS_DEFAULT_PORT,
                        appTag: env.APP_TAG,
                        groupId: env.NEXUS_GROUP_ID,
                        nexusRepoPush: env.NEXUS_REPO_PUSH,
                        nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                        artifactId: env.NEXUS_ARTIFACT_ID
                    )
                }

                stage('Build Docker image from Jar file') {
                    FAILED_STAGE = env.STAGE_NAME
                    dockerBuild(
                        nexusIP: env.NEXUS_IP,
                        nexusDockerPort: env.NEXUS_DOCKER_PORT,
                        pathDockerfile: env.DOCKER_FILE,
                        imageName: env.APP_NAME,
                        imageTag: ["latest", env.APP_TAG]
                    )
                }

                stage('Push Docker image to Nexus Repo') {
                    FAILED_STAGE = env.STAGE_NAME
                    dockerPush(
                        nexusIP: env.NEXUS_IP,
                        nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                        nexusDockerPort: env.NEXUS_DOCKER_PORT,
                        imageName: env.APP_NAME,
                        imageTag: env.APP_TAG
                    )
                }
                stage('Pull Artifact and Deploy with Ansible (Jar version)') {
                    FAILED_STAGE = env.STAGE_NAME
                    pullDeployJar(
                        nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                        playbook: './ansible/playbook.yml',
                        inventory: './ansible/inventory.ini',
                        deployCredentialsId:  env.DEPLOY_CREDENTIALS_ID,
                        workingDirArtifact: env.WORKING_DIR_ARTIFACT,
                        artifactUrl: env.ARTIFACT_URL,
                        deployIP: env.DEPLOY_IP,
                        deployUser: env.DEPLOY_USER
                    )
                }
                stage('Setup and config Docker (Nginx) with Ansible)') {
                    FAILED_STAGE = env.STAGE_NAME
                    setupConfigDocker(
                        playbook: './ansible/playbook-docker.yml',
                        inventory: './ansible/inventory.ini',
                        deployCredentialsId:  env.DEPLOY_CREDENTIALS_ID,
                        deployIP: env.DEPLOY_IP,
                        deployUser: env.DEPLOY_USER,

                        nexusIP: env.NEXUS_IP,
                        nexusDockerPort: env.NEXUS_DOCKER_PORT,
                    )
                }

                stage('Pull Image and Deploy (Docker Version)') {
                    FAILED_STAGE = env.STAGE_NAME
                    deployDocker(
                        nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                        nexusDockerPort: env.NEXUS_DOCKER_PORT,
                        nexusIP: env.NEXUS_IP,

                        deployIP: env.DEPLOY_IP,
                        deployCredentialsId:  env.DEPLOY_CREDENTIALS_ID,

                        imageName: env.APP_NAME,
                        imageTag: env.APP_TAG,
                        containerName: env.APP_NAME,
                        ports: ["8081:8080"],
                        volumes: [env.WORKING_DIR_ARTIFACT+"log/app.log:/app/app.log"],
                        workingDirArtifact: env.WORKING_DIR_ARTIFACT
                    )
                }
                try {
                    stage('Health Check Application (JAR Version)') {
                        def result = healthCheck(checkIP: env.DEPLOY_IP, checkPort: 80)
                        if (result == true) {
                            echo "Health check successfully for JAR Version on Port: 80"
                        } else {
                            echo "Health check failed for JAR Version"
                            throw new Exception("Health check failed")
                        }
                    }
                } catch (Exception e) {
                    echo "Stage failed for JAR"
                    currentBuild.result = 'FAILURE'  
                }   

                try {
                    stage('Health Check Application (Docker Version)') {
                        def result = healthCheck(checkIP: env.DEPLOY_IP, checkPort: 8081)
                        if (result == true) {
                            echo "Health check successfully for Docker Version on Port: 8081"
                            stableDocker(
                                nexusIP: env.NEXUS_IP,
                                nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                                nexusDockerPort: env.NEXUS_DOCKER_PORT,
                                imageName: env.APP_NAME,
                                imageTag: env.APP_TAG
                            )
                        } else {
                            echo "Health check failed for Docker Version"
                            throw new Exception("Health check failed")
                        }
                    }
                } catch (Exception e) {
                    echo "Trying to rollback to the previous image in Nexus..."
                    stage('Rolling back to the previous stable image (Docker)') {
                        rollbackDocker(
                            nexusCredentialsId: env.NEXUS_CREDENTIALS_ID,
                            nexusDockerPort: env.NEXUS_DOCKER_PORT,
                            nexusIP: env.NEXUS_IP,

                            deployIP: env.DEPLOY_IP,
                            deployCredentialsId:  env.DEPLOY_CREDENTIALS_ID,

                            imageName: env.APP_NAME,
                            imageTag: env.APP_TAG,
                            containerName: env.APP_NAME,
                            ports: ["8081:8080"],
                            volumes: [env.WORKING_DIR_ARTIFACT+"log/app.log:/app/app.log"],
                            workingDirArtifact: env.WORKING_DIR_ARTIFACT
                        )
                    }
                    echo "Rollback has done."
                    currentBuild.result = 'FAILURE'
                }
            }

        } catch (Exception e) {
            currentBuild.result = 'FAILURE'  
        } finally {
            notifyBuild(currentBuild.result)

        }
    }           
}


def notifyBuild(String buildStatus = 'STARTED') {
    // build status of null means successful
    buildStatus = buildStatus ?: 'SUCCESS'

    // Default values
    def colorName = 'RED'
    def colorCode = '#FF0000'
    def subject = "${buildStatus}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
    def summary = "${subject} (${env.BUILD_URL})"
    def details = """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
    <p>Check console output at &QUOT;<a href='${env.BUILD_URL}'>${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>&QUOT;</p>"""

    // Override default values based on build status
    if (buildStatus == 'STARTED') {
        color = 'YELLOW'
        colorCode = '#FFFF00'
    } else if (buildStatus == 'SUCCESS') {
        color = 'GREEN'
        colorCode = '#00FF00'
    } else {
        color = 'RED'
        colorCode = '#FF0000'
    }

    // Send notifications
    //slackSend (color: colorCode, message: summary)

    //hipchatSend (color: color, notify: true, message: summary)

    emailext (
        subject: subject,
        body: details,
        to: 'huyngyyendev@gmail.com',
        mimeType: 'text/html'
    )
}


