pipelineJob('te-cluster-mgr') {
    description('---- test evironment ---- \ncluster-mgr of jinsuai.com!')
    
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
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Use Git credentials to pull code
                git(
                    url: "http://10.6.136.55/compute-platform/cluster_mgr.git",
                    branch: "main",
                    credentialsId: 'gitlab-kaosay'
                )
            }
        }

        stage('Build Project') {
            steps {
                // Add build steps if needed (e.g., npm install, mvn build, etc.)
                sh 'go mod tidy'
                sh 'go build -o cluster-mgr'
            }
        }

        stage('Deploy to Remote Server') {
            steps {
                // Use SSH credentials to copy files to the remote server
                sshagent(['jump-root']) {
                    //sh 'chmod 755 -R ./dist'
                    sh 'shasum ./cluster-mgr'
                    sh 'rsync -avzp ./cluster-mgr root@10.6.136.236:/opt/app/goapps'
                        
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
