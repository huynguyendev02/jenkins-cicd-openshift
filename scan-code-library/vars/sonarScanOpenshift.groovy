def call(Map config = [:]) {
    if (config.settings == null)
        config.settings="$HOME/.settings/settings.xml"
    container('maven') {
        sh """yes | cp ${config.settings} ."""
        sh 'mvn --settings settings.xml sonar:sonar -Dsonar.login="$SONAR_TOKEN"'
    }

}
