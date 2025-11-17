pipeline {
    agent any
    stages {
        stage('Trigger Services') {
            steps {
                echo "${ser_task}"
                script {
                    def services = [
                        'app1' : "${app_1}",
                        'app2': "${app_2}"
                    ]
                    services.each { svc ->
                        if (svc.value == "true") {
                            stage(svc.key) {
                                //echo "Building ${svc.name}"
                                build job: 'test--app-single',
                                      parameters: [string(name: 'APP', value: svc.key)],
                                      //wait: true,
                                      wait: false,
                                      propagate: true
                            }
                        } else {
                            echo "Skipped: ${svc.key}"
                        }
                    }
                }
            }
        }
    }
}
