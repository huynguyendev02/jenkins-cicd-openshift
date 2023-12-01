def call(Map config = [:]) {
    container('kaniko') {
        sh """
            /kaniko/executor \
                -f `pwd`${config.dockerfile} \
                -c `pwd` --insecure --skip-tls-verify --cache=true \
                --destination=${config.pushServer}/huyng14/${config.pushRepo}:${config.appTag}
        """
    }
}