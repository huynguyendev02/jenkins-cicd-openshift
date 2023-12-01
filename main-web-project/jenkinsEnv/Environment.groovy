//Application Setting
env.APP_NAME = 'web-app-spring'
env.APP_TAG = configEnvBasedOnBranch().get(1)

//Nexus
env.PUSH_SERVER = '10.98.86.99:8082'
env.PULL_SERVER = '10.98.86.99:8085'

env.RELEASE_REPO = 'web-app-spring-release'
env.UAT_REPO = 'web-app-spring-uat'
env.DEV_REPO = 'web-app-spring-dev'

env.REPO_PUSH = configEnvBasedOnBranch().get(0)


env.HELM_CHART = './helm/web-app-spring-helm'
env.NAMESPACE = configEnvBasedOnBranch().get(2)
env.APP_PORT = 8080

//Docker
env.DOCKER_FILE = '/docker/Dockerfile'

def configEnvBasedOnBranch() {
    switch(env.BRANCH_NAME) {
        case 'main':
            return [env.RELEASE_REPO, "${BUILD_TIMESTAMP}-${env.GIT_COMMIT}-RELEASE","huyng14-prod"]
        case ~/(uat)(\/)*((\/)*\w+)*/:
            return [env.UAT_REPO, "${BUILD_TIMESTAMP}-UAT-${env.GIT_COMMIT}","huyng14-uat"]
        case ~/(dev)(\/)*((\/)*\w+)*/:
            return [env.DEV_REPO, "${BUILD_TIMESTAMP}-DEV-${env.GIT_COMMIT}","huyng14-dev"]
        default:
            return ['dev', 'dev', 'dev','dev']
        break
    }
}