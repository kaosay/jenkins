#!/bin/bash

echo "===============> powerspeedcloud-web deploying...."
# Navigate to the project directory
#cd /var/jenkins_home/workspace/jingsuai
cd $WORKSPACE

pwd
ls
# Pull the latest changes from the repository
#git pull origin main
# Install dependencies (if using npm, composer, etc.)

#sed -i 's#"vue": "^3.5.13",#"vue": "^3.5.13","@rollup/rollup-linux-x64-gnu": "*",#' ./package.json
npm install
# or
# composer install

# Build the project (if necessary)
npm run build
# or
# ./gradlew build

#scp -r ./dist ubuntu@172.16.20.211:/var/www/html
scp -r ./dist/* ubuntu@172.16.20.211:/var/www/html/dist


ls
echo "===============> powerspeedcloud-web deployed!!!!!!!!!!"
shasum index.html
