#!/bin/bash

apply() {
    echo "Creating resources..."
    kubectl apply -f myapp-deployment.yaml
    kubectl apply -f myapp-service.yaml
    kubectl apply -f metrics-server.yaml
    kubectl apply -f myapp-hpa.yaml
    echo "Resources created."
    read
}

destory() {
    echo "Deleting resources..."
    kubectl delete -f myapp-hpa.yaml
    kubectl delete -f myapp-service.yaml
    kubectl delete -f myapp-deployment.yaml
    kubectl delete -f metrics-server.yaml
    echo "Resources Deleted."
    read
}

case "$1" in
    "apply") 
    apply;;
    "destory") 
    destory;;
esac