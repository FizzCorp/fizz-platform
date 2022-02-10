#!/bin/bash
mvn clean install -B -DskipTests -Dcomponent=gateway
mvn -DfailIfNoTests=false -B -pl chat test
mvn -DfailIfNoTests=false -B -pl chat-access test
mvn -DfailIfNoTests=false -B -pl chat-common test
mvn -DfailIfNoTests=false -B -pl api-gateway test
mvn clean install -B -DskipTests -Dcomponent=event-processing
mvn -DfailIfNoTests=false -B -pl event-processing test
