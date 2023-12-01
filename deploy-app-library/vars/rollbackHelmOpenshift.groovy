def call(Map config = [:]) {
    container('helm') {
        sh """
            helm rollback ${config.repoPush} --namespace ${config.namespace} --debug
        """
    }
}