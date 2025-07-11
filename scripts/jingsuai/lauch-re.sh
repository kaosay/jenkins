#!/bin/bash
ARRAY=("$@")
APP=${ARRAY[0]}
CONF=${ARRAY[1]}
HOSTS=("${ARRAY[@]:2}")


case $APP in
        "web-bg" )
                APP_DIR=/opt/app/web_bg
                ;;
        "cluster-mgr")
                APP_DIR=/opt/app/cluster_mgr
                ;;
        "jsy-admin")
                APP_DIR=/opt/app/jsy_admin
                ;;
        "bg-timer")
                APP_DIR=/opt/app/bg-timer
                ;;
esac
echo "--------------------app dir is $APP_DIR"


if [[ -d web_bg ]]; then
        if [[ ! -f ./go.work ]]; then
                go work init
                go work use ./web_bg
                go work use ./jsy_admin
        fi
        cd ./web_bg
fi

#---- build package ----
export GOPROXY=https://goproxy.cn,direct
go mod tidy
go build -o $APP

#---- get version of code to version.txt ----
echo -e "\n-------------------------------------------" >> $CONF/version.txt
echo "URL: "$GIT_URL >> $CONF/version.txt
echo "Branch: "$GIT_BRANCH >> $CONF/version.txt
git log -1 $GIT_COMMIT >> $CONF/version.txt
chmod 644 $CONF/version.txt

#---- rsync package ----
release_app() {
        rsync -avzp ./$APP ${NODE_SERVER}:/opt/app/goapps/
        rsync -avzp $CONF/* ${NODE_SERVER}:${APP_DIR}/
        ssh ${NODE_SERVER} ${APP_DIR}/update.sh
}

for HOST in ${HOSTS[*]}; do
    NODE_SERVER=$HOST
    #release_app
done

             
