pipelineJob('jinsuai-web') {
    description('test evironment \n web of jinsuai.com!')
    
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
    agent any
    tools {nodejs "NodeJS24"}

    environment {
        REMOTE_DIR = '/path/to/deploy'
    }

    triggers {
      gitlab( branchFilterType: 'All',
      excludeBranchesSpec: '',
      includeBranchesSpec: '',
      noteRegex: 'Jenkins please retry a build',
      pendingBuildName: '',
      secretToken: '',
      skipWorkInProgressMergeRequest: true,
      sourceBranchRegex: '',
      targetBranchRegex: '',
      triggerOnApprovedMergeRequest: true,
      triggerOpenMergeRequestOnPush: 'never'
      )
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Use Git credentials to pull code
                git(
                    url: "http://10.6.136.55/compute-platform/arithmetic-lease-web.git",
                    branch: "test",
                    credentialsId: 'gitlab-kaosay'
                )
            }
        }

        stage('Build Project') {
            steps {
                // Add build steps if needed (e.g., npm install, mvn build, etc.)
                sh 'npm install'
                sh 'npm run build'
            }
        }

        stage('Deploy to Remote Server') {
            steps {
                // Use SSH credentials to copy files to the remote server
                sshagent(['jump-root']) {
                    sh 'chmod 755 -R ./dist'
                    sh 'shasum ./dist/index.html'
                    sh 'rsync -avzp ./dist root@10.6.136.236:/opt/web/www.jinsuai.com'
                        
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
