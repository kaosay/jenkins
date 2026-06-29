@Library('test-global-library') _

pipeline {
    agent any
    //tools { nodejs "NodeJS 25" }
    tools { go 'Go 1.26' }
    options {
        buildDiscarder( logRotator( artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '9'))
    }
    parameters {
        choice(name: 'BRANCH', choices: ['master','dev','main'], description: '选择要构建的分支')
    }

    environment {
        GIT_URL = "https://github.com/codeup/test/test.git"
        GIT_KEY = "test"
        REMOTE_HOST = 'root@192.168'
        REMOTE_PORT = '33'
        REMOTE_KEY = 'keys'

        APP = 'test-app'
        APP_PORT = '1'
        BUILD_CMD = 'go mod tidy && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -ldflags "-s -w" -o $GO_BUILD_FILE $GO_BUILD_SOURCE'
        GO_BUILD_FILE = 'test-app'
        GO_BUILD_SOURCE = './cmd/server.go'
        DEPLOY_NAME = 'test-app'
        CONTAINER_NAME = 'test-app'
        NAMESPACE = 'test-ns'
        IMAGE_NAME = 'docker.io/common/test-app'
        // 获取时间格式
        NEW_IMAGE_TAG = sh(
                script: '#!/bin/bash\ndate +%Y%m%d%H%M%S',
                returnStdout: true
        ).trim()

        TELEGRAM_TOKEN = '123:abc'
        CHAT_ID = '-123'  //test group
        //CHAT_ID = '-123'  //test env deploy report
        JOB_NAME = 'test-${APP}'

        // 生成时间戳标签 (YYYYMMDDHHMMSS)
        TIMESTAMP = sh(script: "date '+%Y-%m-%d %H:%M:%S'", returnStdout: true).trim()
        FULL_IMAGE = "$IMAGE_NAME:$BRANCH-${NEW_IMAGE_TAG}"

        HOMEPAGE_NAME ='test-app'
        HOMEPAGE_STR = '---- test ----' +
                '\ntest environment of tg-app' +
                '\n\nhttps://test.shop' +
                '\n'
    }

    stages {
        stage('set homepage'){
            steps{
                script{
                    jh.call("$HOMEPAGE_NAME", "$HOMEPAGE_STR")
                }
            }
        }
        stage('Checkout') {
            steps {
                // Use Git credentials to pull code
                git(
                        url: "${GIT_URL}",
                        branch: "${BRANCH}",
                        credentialsId: "${GIT_KEY}"
                )
                // 获取 Git 提交信息
                script {
                    env.GIT_COMMIT = sh(script: "git log -1 --format='%H'", returnStdout: true).trim()
                    env.GIT_COMMIT_SHORT = sh(script: "git log -1 --format='%h'", returnStdout: true).trim()
                    env.GIT_COMMIT_MSG = sh(script: "git log -1 --format='%s'", returnStdout: true).trim()
                    env.GIT_COMMIT_AUTHOR = sh(script: "git log -1 --format='%an'", returnStdout: true).trim()
                    env.GIT_COMMIT_DATE = sh(script: "git log -1 --format='%cd' --date=format:'%Y-%m-%d %H:%M:%S'", returnStdout: true).trim()
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh "$BUILD_CMD"
                    generateDockerfile()
                    sh 'cat Dockerfile'
                    docker.build("$FULL_IMAGE").push()
                }
            }
        }

        stage('Deploy to Remote Server') {
            steps {
                // Use SSH credentials to copy files to the remote server
                sshagent(["$REMOTE_KEY"]) {
                    //sh 'ssh -p$REMOTE_PORT -o StrictHostKeyChecking=no $REMOTE_HOST ls /opt'
                    sh 'ssh -p$REMOTE_PORT $REMOTE_HOST ls /opt'

                    //server
                    sh """
                        ssh -p$REMOTE_PORT $REMOTE_HOST "kubectl -n $NAMESPACE set image deploy/$DEPLOY_NAME  $CONTAINER_NAME=$FULL_IMAGE"
                        ssh -p$REMOTE_PORT $REMOTE_HOST "kubectl -n $NAMESPACE rollout status deploy/$DEPLOY_NAME --timeout=35s"
                        ssh -p$REMOTE_PORT $REMOTE_HOST "kubectl -n $NAMESPACE get pods -l app=$CONTAINER_NAME"
                    """
                }
            }
        }
    }

    post {
        success {
            echo "-- ${APP} -- Deployment successful!"
            script {
                jh.sendTelegram("Successful!", "✅")
            }
        }
        failure {
            echo "-- ${APP} -- Deployment failed!"
            script {
                jh.sendTelegram("Failed!", "❌")
            }
        }
    }
}

// Define function for generate dockerfile
def generateDockerfile() {
    def df = """
FROM alpine:3.22.1

WORKDIR /app
RUN mkdir logs conf \
    && apk add --no-cache curl ca-certificates
COPY ./test-app /app/test-app
COPY ./conf/log.xml /app/conf/log.xml
"""
    writeFile(file: 'Dockerfile', text: df.stripIndent())

}

// shared library for send message to telegram
def sendTelegram(status, emoji) {
    def message = """
📦 项目: <b>${JOB_NAME}</b> ${emoji}
🌿 分支: ${BRANCH}
🔖 镜像: ${FULL_IMAGE}
🕐 构建时间: ${TIMESTAMP}

📝 提交信息:
   Commit: ${env.GIT_COMMIT_SHORT}
   作者: ${env.GIT_COMMIT_AUTHOR}
   提交时间: ${env.GIT_COMMIT_DATE}
   消息: ${env.GIT_COMMIT_MSG}
    """

    sh """
        curl -s -X POST https://api.telegram.org/bot${TELEGRAM_TOKEN}/sendMessage \
        -d chat_id=${CHAT_ID} \
        -d parse_mode=HTML \
        -d text="${message}"
    """
    // Show branch in build number
    currentBuild.displayName = "#${BUILD_NUMBER} ${BRANCH}"
}
