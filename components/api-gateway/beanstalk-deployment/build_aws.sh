#!/bin/bash

cd ./../..
mvn clean
mvn package -Dcomponent=gateway -DskipTests
cd ./api-gateway/beanstalk-deployment
find ./ -name ".DS_Store" -depth -exec rm {} \;
cp -R ./../target/api-gateway-1.0-SNAPSHOT.jar application.jar
NAME_PREFIX=api-gateway-v
rm -f $NAME_PREFIX*.zip
zip -r -D "$NAME_PREFIX`cat ../version`.zip" application.jar Procfile run.sh .ebextensions .platform newrelic
rm -f application.jar
