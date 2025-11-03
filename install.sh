#!/bin/bash

chown 1000:1000 jenkins_home/

docker run -d --name jenkins  -p 8080:8080 -p 50000:50000 --restart=on-failure -v /opt/jenkins/jenkins_home:/var/jenkins_home jenkins/jenkins:lts


