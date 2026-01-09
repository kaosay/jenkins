pipeline {
    // Run on an agent where we want to use Go
    //agent any
    agent { label 'ubuntu' }
 
    // Ensure the desired Go version is installed for all stages,
    // using the name defined in the Global Tool Configuration
    //tools { go 'go1.24' }

    environment {
        PATH = "$PATH:/usr/local/go/bin:/root/go/bin"
        REMOTE_DIR = 'ubuntu@3:/server'
        REMOTE_HOST = 'ubuntu@3'
	    GOPROXY = "https://goproxy.cn,direct"
	    //APP = "task"
	    //APP_DIR = "com"
    }
    
    stages {
        stage('Checkout') {
            steps {
                sh 'echo "${APP}"'
                // 检出 proto 仓库
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/test']],
                    extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'proto']],
                    userRemoteConfigs: [[credentialsId: 'gitlab-kaosay', url: 'http://10.0.0./proto.git']]
                ])
                // 检出 go-com 仓库
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/test']],
                    extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'go-com']],
                    userRemoteConfigs: [[credentialsId: 'gitlab-kaosay', url: 'http://10.0.0./go-com.git']]
                ])
                // checkout ser-com
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${BRANCH}"]],
                    extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false], [$class: 'RelativeTargetDirectory', relativeTargetDir: "${APP}"]],
                    userRemoteConfigs: [[credentialsId: 'gitlab-kaosay', url: "http://10.0.0./${APP}.git"]]
                ])
            }
        }

        stage('Dependency proto') {
            steps {
                dir("proto") {
                    //sh 'go mod tidy'
                    sh 'make gen'
                    sh 'rm app/com/file.go || true'
                }
            }
        }
  
        stage('Dependency go-com') {
            steps {
                dir("go-com") {
                    //sh 'go mod tidy'
                    sh '''
                        sed 's/clear/#clear/' Makefile -i
                        make auto
                    '''
                }
            }
        }      
        
        stage('Build') {
            steps {
                dir("$APP") {
                    sh 'go mod tidy'
                    sh 'GOOS=linux GOARCH=amd64 go build -ldflags "-s -w" -o ./$APP main.go'

                }
            }
        }
      
        stage('Deploy to Remote Server') {
            steps {
		          dir("$APP") {
                    // Use SSH credentials to copy files to the remote server
                    sshagent(['kaosay']) {
                        sh 'shasum ./$APP'
                        sh 'rsync -avzp ./$APP $REMOTE_DIR'
                        sh 'ssh $REMOTE_HOST sudo pkill ${APP} || true'
                        sh """
                            if [ "$APP" = "trans" ];then
                                ssh $REMOTE_HOST 'cd /server && sudo bash -c "nohup ./$APP -c conf/conf.yaml >> ./nolog/${APP}.log 2>&1 &"'
                            elif [ "$APP" = "game" ];then
                                ssh $REMOTE_HOST 'cd /server && sudo bash -c "nohup ./$APP >> ./nolog/${APP}.log 2>&1 &"'
                            else
                                ssh $REMOTE_HOST 'cd /server && sudo bash -c "nohup ./$APP >> ./nolog/${APP}.log 2>&1 &"'
                            fi
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "-- ${APP} -- Deployment successful!"
        }
        failure {
            echo "-- ${APP} -- Deployment failed!"
        }
    }
}	
