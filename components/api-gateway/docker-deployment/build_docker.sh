#!/bin/bash

cd ./../..
mvn clean
mvn package -Dcomponent=gateway -DskipTests
cd ./api-gateway/docker-deployment
find ./ -name ".DS_Store" -depth -exec rm {} \;
cp -R ./../target/api-gateway-1.0-SNAPSHOT.jar application.jar
docker build -t fizz.io/api-gateway .
rm -f application.jar
