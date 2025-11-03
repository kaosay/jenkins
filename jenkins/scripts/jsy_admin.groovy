pipelineJob('te-jsy-admin') {
    description('---- test evironment ----\njsy-admin of jinsuai.com!')
    
    // 设置丢弃旧构建策略
    properties {
        logRotator {
            daysToKeep(7)
            numToKeep(9)
        }
    }

    // 定义流水线
    definition {
        cps {
            script("""
pipeline {
    // Run on an agent where we want to use Go
    agent any
 
    // Ensure the desired Go version is installed for all stages,
    // using the name defined in the Global Tool Configuration
    tools { go 'go1.24' }

    environment {
        REMOTE_DIR = '/path/to/deploy'
	GOPROXY = "https://goproxy.cn,direct"
	APP = jsy-admin
	APP_DIR = jsy_admin
    }

    stages {
        stage('Checkout') {
            steps {
                // 检出 web_bg 仓库
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/test']],
                    extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'web_bg']],
                    userRemoteConfigs: [[credentialsId: 'gitlab-kaosay', url: 'http://10.6.136.55/compute-platform/web_bg.git']]
                ])
                // 检出 jsy_admin 仓库
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/test']],
                    extensions: [[$class: 'SubmoduleOption', disableSubmodules: false, recursiveSubmodules: true, trackingSubmodules: false], [$class: 'RelativeTargetDirectory', relativeTargetDir: 'jsy_admin']],
                    userRemoteConfigs: [[credentialsId: 'gitlab-kaosay', url: 'http://10.6.136.55/compute-platform/jsy_admin.git']]
                ])
            }
        }

        stage('Initialize Go Workspace') {
            steps {
                sh '''
                    if [ ! -f go.work ]; then
                        go work init
                        go work use ./web_bg
                        go work use ./jsy_admin
                    fi
                    go work sync
                '''
            }
        }

        stage('Build') {
            steps {
                dir('$APP_DIR') {
                    sh '''
                        go mod tidy
                        go mod download
                        go build -o $APP
                    '''
                }
            }
        }
      
        stage('Deploy to Remote Server') {
            steps {
		dir('$APP_DIR') 
                    // Use SSH credentials to copy files to the remote server
                    sshagent(['jump-root']) {
                        //sh 'chmod 755 -R ./dist'
                        sh 'shasum ./$APP'
                        sh 'rsync -avzp ./$APP root@10.6.136.236:/opt/app/goapps'

		{}
                        
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}	
            """)
            //sandbox()
        }
    }
}
