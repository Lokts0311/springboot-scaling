#!/bin/bash

apply() {
    echo "Creating resources..."
    kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
    kubectl apply -f myapp-deployment.yaml
    kubectl apply -f myapp-service.yaml
    echo "Resources created."
    read
}

destory() {
    echo "Deleting resources..."
    kubectl delete -f myapp-service.yaml
    kubectl delete -f myapp-deployment.yaml
    kubectl delete -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
    echo "Resources Deleted."
    read
}

case "$1" in
    "apply") 
    apply;;
    "destory") 
    destory;;
esac