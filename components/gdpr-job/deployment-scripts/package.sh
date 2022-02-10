#!/bin/sh
cd ./../..
mvn clean
mvn package -Dcomponent=moderation-job -DskipTests