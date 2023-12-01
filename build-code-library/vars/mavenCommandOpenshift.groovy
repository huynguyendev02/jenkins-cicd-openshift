def call(Map config = [:]) {
    if (config.settings == null)
        config.settings="$HOME/.settings/settings.xml"
    container('maven') {
        sh """mvn --settings ${config.settings} ${config.options} ${config.command}"""
    }
}
