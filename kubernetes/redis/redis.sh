#!/bin/bash

apply() {
    echo "Creating resources..."
    kubectl apply -f redis-pvc.yaml
    kubectl apply -f redis-secret.yaml
    kubectl apply -f redis-deployment.yaml
    echo "Resources created."
    read
}

destory() {
    echo "Deleting resources..."
    kubectl delete -f redis-deployment.yaml
    kubectl delete -f redis-secret.yaml
    kubectl delete -f redis-pvc.yaml
    echo "Resources Deleted."
    read
}

case "$1" in
    "apply") 
    apply;;
    "destory") 
    destory;;
esac