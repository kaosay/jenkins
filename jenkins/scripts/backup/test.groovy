pipelineJob('My-Imported-Project') {
    definition {
        cps {
            script('''
                pipeline {
                    agent any
                    stages {
                        stage('Build') {
                            steps {
                                echo 'Building...'
                            }
                        }
                    }
                }
            ''')
            sandbox()
        }
    }
}
