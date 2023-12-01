# jenkins-cicd-openshift
This project demonstrates a comprehensive CI/CD pipeline using Jenkins, Maven, Nexus, Sonarqube, Docker, and Helm on OpenShift. The pipeline is designed to build, test, scan with Sonarqube, build Docker images with Kaniko, push images to Nexus, and deploy with Helm. It also includes health check probes and automatic rollback functionality with Helm. The pipeline is configured for three environments: Production, UAT, and Development.

## Overview
The pipeline deploys a Maven web application, with integration for Nexus, Sonarqube, and Docker. The pipeline is written in a scripted format and runs on a Jenkins agent in OpenShift.

![image](https://github.com/huynguyendev02/jenkins-cicd-openshift/assets/109943707/49f86ff3-2fd5-4984-814d-89495e467622)


## Technologies Used

- **Jenkins**: Used for continuous integration and delivery.
- **Maven**: Used for building the web application.
- **Nexus**: Used as a repository manager to store and distribute Docker images.
- **Sonarqube**: Used for Static Application Security Testing (SAST) source code scanning.
- **Kaniko**: Utilized for building Docker images and uploading them to Nexus repositories.
- **Helm**: Employs Helm charts to deploy the web application.
- **OpenShift**: The platform where the Jenkins agent runs and where the application is deployed.

## Pipeline Stages

1. **Build**: The application is built using Maven.
2. **Test**: Unit tests are run.
3. **Scan**: The code is scanned using Sonarqube for static application security testing.
4. **Build Image**: A Docker image of the application is built using Kaniko.
5. **Push to Nexus**: The Docker image is pushed to a Nexus repository.
6. **Deploy with Helm**: The application is deployed to OpenShift using Helm.
7. **Health Check Probe**: The health of the application is checked.
8. **Auto Rollback with Helm**: If the health check fails, the application is automatically rolled back to the previous stable version using Helm.

### Convention
Image:
- `stable`: Stable version of well-health app
- `latest`: Build with newest code, may not work correctly
- `$hash ($githash + 'DDMMYYYY')`: Build with version 'X' pushed by `$githash`, may not work correctly

Environment
- Production: Full pipeline
- Uat: Full pipeline
- Dev: First 4 stages
