FROM docker.1ms.run/jenkins/jenkins
# if we want to install via apt
USER root
#RUN apt-get update && apt-get install -y ruby make more-thing-here

COPY ./sources.list /etc/apt/sources.list
RUN echo "" > /etc/apt/sources.list.d/debian.sources

RUN apt-get update 
RUN apt-get install -y tzdata rsync && \
        ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
        echo "Asia/Shanghai" > /etc/timezone

# drop back to the regular jenkins user - good practice
USER jenkins
