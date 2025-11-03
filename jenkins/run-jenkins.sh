sudo nerdctl  run -d --name=jenkins --restart=on-failure -p 80:8080 -p 50000:50000  -v /opt/jenkins_home:/var/jenkins_home jenkins/jenkins:v3
