#!/bin/bash

apply() {
    echo "Creating resources..."
    kubectl apply -f mysql-pvc.yaml
    kubectl apply -f mysql-secret.yaml
    kubectl apply -f mysql-deployment.yaml
    echo "Resources created."
    read
}

destory() {
    echo "Deleting resources..."
    kubectl delete -f mysql-deployment.yaml
    kubectl delete -f mysql-secret.yaml
    kubectl delete -f mysql-pvc.yaml
    echo "Resources Deleted."
    read
}

case "$1" in
    "apply") 
    apply;;
    "destory") 
    destory;;
esac