# jenkins
How to use jenkins

## 1 Send message to lark

```
    post {
        success {
            echo "-- ${APP} -- Deployment successful!"
            sh """
             curl -X POST -H "Content-Type: application/json" \
              https://open.larksuite.com/open-apis/bot/v2/hook/2d8 \
              -d '{
                "msg_type": "post",
                "content": {
                  "post": {
                    "zh_cn": {
                      "title": "-- ${APP} --successful",
                      "content": [
                        [{"tag": "text", "text": "Engineer：${gitlabUserName}"}],
                        [{"tag": "text", "text": "Branch：${gitlabBranch}"}],
                        [{"tag": "text", "text": "Commit：${gitlabMergeRequestLastCommit}"}],
                        [{"tag": "text", "text": "结果：", "color": "green" }, {"tag": "text", "text": "successful"}]
                      ]
                    }
                  }
                }
              }'         
              """
        }
