#!/bin/bash

cd C:\exercise
mvn clean install -DskipTests
docker build -t exerciseapp .
kubectl get pods --no-headers -o custom-columns=":metadata.name" | grep "app" | xargs kubectl delete pod