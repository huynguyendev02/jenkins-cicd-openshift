def call() {
    timeout(time: 10, unit: 'MINUTES') {
        echo "Initializing quality gates..."
        sh 'sleep 10'
        def result = waitForQualityGate()
        if (result.status != 'OK') {
             error "Pipeline aborted due to quality gate failure: ${result.status}"
        } else {
             echo "Quality gate passed with result: ${result.status}"
        }
    }
}
