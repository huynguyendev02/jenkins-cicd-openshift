def call(Map config = [:]) {
    echo "Wait 30 seconds to perform health check the application"
    sleep 30

    container('bash') {
        def chartName = config.helmChart.tokenize('/').last()
        def httpStatus = sh(
            returnStatus: true, 
            script: "wget --connect-timeout=300 --spider --server-response ${config.repoPush}-${chartName}.${config.namespace}.svc.cluster.local:${config.port}"
        ) == 0

        echo "${httpStatus}"
        
        return httpStatus
    }
   
}
