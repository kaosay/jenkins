#!/bin/bash
echo $WORKSPACE
COMMAND=/opt/yarn/bin
HOME=/var/jenkins_home
cd $HOME/workspace/imagegallery-web && $COMMAND/yarn  && $COMMAND/yarn build

#scp ubuntu@172.16.20.223:/home/yhk/files
