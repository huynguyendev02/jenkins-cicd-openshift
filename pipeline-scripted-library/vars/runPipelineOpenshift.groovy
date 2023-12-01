def call() {
    def buildCodeLibrary = library('build-code-library@main')
    def scanCodeLibrary = library('scan-code-library@main')
    def deployAppLibrary = library('deploy-app-library@main')


    properties([gitLabConnection('huyng14-gitlab')])

    podTemplate(
        cloud: 'kubernetes-huyng14',
        yaml: readTrusted('./jenkins/KubernetesPod.yaml')
    )
    {
    node(POD_LABEL) {
        try {
            gitlabBuilds(builds: ["Checkout SCM",
                                  "Build with Maven", 
                                  "Test with Maven", 
                                  "Scan code with SonarQube",
                                  "Build & Push Image with Kaniko",
                                  "Deploy with Helm Chart",
                                  "Check health"
                                ]) {
                stage("Checkout SCM") {
                    gitlabCommitStatus("Checkout SCM") {
                        checkout scm
                        env.GIT_COMMIT = checkout(scm).GIT_COMMIT
                        load "/home/jenkins/jenkins-env/Environment.groovy"
                    }
                }
                stage("Build with Maven") {
                    gitlabCommitStatus("Build with Maven") {
                        mavenCommandOpenshift(
                            options: "-DskipTests -Dcheckstyle.skip -Dproject.version=${env.APP_TAG}",
                            command: 'clean install'
                        )
                    }
                }
                stage("Test with Maven") {
                    gitlabCommitStatus("Test with Maven") {
                        mavenCommandOpenshift(
                            options: "-Dcheckstyle.skip",
                            command: 'test'
                        )
                    }
                }
                stage("Scan code with SonarQube") {
                    gitlabCommitStatus("Scan code with SonarQube") {
                        sonarScanOpenshift()
                        
                    }
                }

                if ((env.BRANCH_NAME == 'main' ||
                env.BRANCH_NAME ==~ /(uat)(\/)*((\/)*\w+)*/ ||
                env.BRANCH_NAME ==~ /(dev)(\/)*((\/)*\w+)*/ ) && env.CHANGE_ID == null
                ) {
                    stage("Build & Push Image with Kaniko") {
                        gitlabCommitStatus("Build & Push Image with Kaniko") {
                            imageBuildPushOpenshift(
                                dockerfile: '/docker/Dockerfile',
                                pushServer: env.PUSH_SERVER,
                                pushRepo: env.REPO_PUSH,
                                appTag: env.APP_TAG
                            )
                        }
                    }
                    stage("Deploy with Helm Chart") {
                        gitlabCommitStatus("Deploy with Helm Chart") {
                            deployHelmOpenshift(
                                repoPush: env.REPO_PUSH,
                                helmChart: env.HELM_CHART,
                                namespace: env.NAMESPACE,
                                pushServer: env.PUSH_SERVER,
                                pushRepo: env.REPO_PUSH,
                                appTag: env.APP_TAG,
                                appPort: env.APP_PORT
                            )
                        }
                    }

                    try {
                        stage('Health Check Application') {
                            gitlabCommitStatus("Health Check Application") {
                                def result = healthCheckOpenshift(
                                    namespace: env.NAMESPACE,
                                    repoPush: env.REPO_PUSH,
                                    port: env.APP_PORT,
                                    helmChart: env.HELM_CHART
                                )
                                if (result == true) {
                                    echo "Health check successfully!!!"
                                    echo "Tag image to stable"
                                    imageBuildPushOpenshift(
                                        dockerfile: '/docker/Dockerfile',
                                        pushServer: env.PUSH_SERVER,
                                        pushRepo: env.REPO_PUSH,
                                        appTag: 'stable'
                                    )

                                } else {
                                    echo "Health check failed!!!"
                                    throw new Exception("Health check failed")
                                }
                            }
                        }
                    } catch (Exception e) {
                        gitlabCommitStatus("Rolling back with Helm") {
                            echo "Trying to rollback to the previous image with Helm"
                            stage('Rolling back with Helm') {
                                rollbackHelmOpenshift(
                                    namespace: env.NAMESPACE,
                                    repoPush: env.REPO_PUSH
                                )
                            }
                            echo "Rollback has done."
                            currentBuild.result = 'FAILURE'
                        }
                    }
                }
            }
        } catch (Exception e) {
            currentBuild.result = 'FAILURE'  
        } finally {
        }
    }
    }
}