#!/bin/bash

# take image tag as parameter. default: latest
IMAGE_TAG=$1
IMAGE_TAG=${IMAGE_TAG:=latest}

# build emqx-chat package
cd ./../..
mvn clean
mvn package -Dcomponent=chat-emqx -DskipTests

# copy file into current folder
cd ./chat-emqx/docker-deployment
cp -R ./../target/chat-emqx-1.0-SNAPSHOT.jar application.jar

# # build docker image and push to aws ecr
docker build -t fizz-emqx-chat:$IMAGE_TAG .
docker tag fizz-emqx-chat:$IMAGE_TAG 261980672878.dkr.ecr.us-east-1.amazonaws.com/fizz-emqx-chat:$IMAGE_TAG
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 261980672878.dkr.ecr.us-east-1.amazonaws.com
docker push 261980672878.dkr.ecr.us-east-1.amazonaws.com/fizz-emqx-chat:$IMAGE_TAG

# # remove package 
rm -f application.jar
