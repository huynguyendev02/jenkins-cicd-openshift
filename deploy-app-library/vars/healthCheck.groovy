def call(Map config = [:]) {
    FAILED_STAGE = env.STAGE_NAME
    def urlToCheck = "http://${config.checkIP}:${config.checkPort}/actuator/health"
    sleep 30
    def timeoutSeconds = 240
    try {
        def response = httpRequest(url: urlToCheck, timeout: timeoutSeconds)
        if (response.status == 200) {
            echo "Health check successful for ${urlToCheck}"
            return true
        }
    } catch (Exception e) {
         echo "Health check failed for ${urlToCheck}. HTTP status code: ${response.status}"
    }
    
    return false

}

