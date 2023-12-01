def call(Map config = [:]) {
    container('helm') {
        sh """
            helm upgrade ${config.repoPush} ${config.helmChart} --namespace ${config.namespace} --set image.repository=${config.pushServer}/huyng14/${config.repoPush} --set image.tag=${config.appTag}  --set service.port=${config.appPort} --atomic  --install --debug
        """
    }
}