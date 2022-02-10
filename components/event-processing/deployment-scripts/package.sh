#!/bin/sh
cd ./../..
mvn clean
mvn package -Dcomponent=event-processing -DskipTests