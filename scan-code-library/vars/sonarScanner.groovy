def call(Map config = [:]) {

    echo "Analysis your code with SonarQube..."
    withSonarQubeEnv("${config.sonarServer}") {
        sh """
            ${config.scannerHome}/bin/sonar-scanner \
                -Dsonar.projectKey=${config.appName} \
                -Dsonar.projectName=${config.sonarProjectName} \
                -Dsonar.projectVersion=${config.projectVersion} \
                -Dsonar.sources=src/ \
                -Dsonar.java.binaries=target/classes \
                -Dsonar.junit.reportsPath=target/surefire-reports/ \
                -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml 
        """

    }
}
