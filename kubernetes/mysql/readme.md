1. Apply configuration
```cmd
kubectl apply -f mysql-pvc.yaml
kubectl apply -f mysql-secret.yaml
kubectl apply -f mysql-deployment.yaml
```

2.  Forward local port to service
```cmd
kubectl port-forward svc/mysql 3306:3306
```

3. Get a shell to the running pod
```cmd
kubectl exec -it [pod name] -- bash
```

4. Delete resource
```
kubectl delete -f mysql-deployment.yaml
kubectl delete -f mysql-pvc.yaml
kubectl delete -f mysql-secret.yaml
```