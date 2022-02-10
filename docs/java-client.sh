#!/bin/bash
java -jar ./swagger-codegen/3.0.9/libexec/swagger-codegen-cli.jar generate -i ./api-reference.yaml -l java -o java-client
