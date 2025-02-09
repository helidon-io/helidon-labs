# Multi-Cluster Deployment

## A. Infrastructure

### Provision OKE Clusters

This section uses Terraform and the [Terraform module for OKE](https://github.com/oracle-terraform-modules/terraform-oci-oke) to create 2 OKE clusters and related infrastructure. The Terraform module will also provision 1 additional host (operator) where all relevant utilities (kubectl, helm, kubectx, istioctl, cilium) will be installed. Once provisioned, most of the shell commands will be performed on the operator host.

1. Use the terraform module in infra/multi-cluster:

```shell
cd infra/multi-cluster
cp terraform.tfvars.example terraform.tfvars
```

2. Edit terraform.tfvars and enter appropriate values for:
- `api_fingerprint`
- `api_private_key_path`
- `home_region`
- `tenancy_id`
- `user_id`
- `compartment_id`
- `ssh_private_key_path`
- `ssh_public_key_path`
- `kubernetes_version`
- `timezone`
- `istio_version`
- `istio_mesh_id`
3. Edit the clusters input variable and set the appropriate regions:

```
clusters = {
  c1 = { region = "paris", vcn = "10.1.0.0/16", pods = "10.201.0.0/16", services = "10.101.0.0/16", enabled = true }
  c2 = { region = "marseille", vcn = "10.2.0.0/16", pods = "10.202.0.0/16", services = "10.102.0.0/16", enabled = true }
  # c3 = { region = "frankfurt", vcn = "10.3.0.0/16", pods = "10.203.0.0/16", services = "10.103.0.0/16", enabled = true }  
}
```
4. Run terraform init and apply:

```
terraform init
terraform apply --auto-approve
```
5. RPCs will have been already created. Login to OCI Console and peer the VCNs using the RPCs.

### Configure Multi cluster
1. ssh to the operator host

2. Deploy Cilium in all clusters:
```shell
helm --kube-context c1 install cilium cilium/cilium --namespace=kube-system -f $HOME/cilium/cilium-c1.yaml
helm --kube-context c2 install cilium cilium/cilium --namespace=kube-system -f $HOME/cilium/cilium-c2.yaml
```

3. Delete pods not managed by Cilium:

```shell
for c in c1 c2; do
  kubectx $c
  bash $HOME/cilium_delete_pods.sh
done

for c in c1 c2; do
  kubectx $c
  cilium status
done
```

4. Connect the clusters:

```shell
cilium clustermesh connect --context c1 --destination-context c2
```
> Note: If you have more than 2 clusters, connect all the clusters to each other in a mesh:
> ```
>   cilium clustermesh connect --context c1 --destination-context c3
>   cilium clustermesh connect --context c2 --destination-context c3
>   ```
5. Check multi-cluster status:

```shell
for c in c1 c2; do
  kubectx $c
  cilium clustermesh status
done
```
6. Expose CoreDNS for each cluster as a Network Load Balancer:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f coredns/kubedns-$c.yaml
done
```

6. Obtain the IP addresses of NLBs:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl -n kube-system get svc kube-dns-lb
done
```
7. Edit the CoreDNS configuration files in $HOME/coredns/ for c1, and respectively and replace each NLB's private IP address.

8. Create the CoreDNS ConfigMap and restart CoreDNS for each cluster:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f coredns/coredns-$c.yaml
  kubectl delete pod --namespace kube-system -l k8s-app=kube-dns  
done
```
9. Install Istio in each cluster:

```shell
bash $HOME/istio/install_istio.sh
```


## B. Database

### Configure DR for Autonomous Database
1. In OCI Console, navigate to the Autonomous Database instance page
2. Under Resources, select Disaster Recovery
3. Click on 'Add Peer Database'
4. Select an OCI Region
5. Select Autonomous Data Guard as the disaster recovery type
6. Check 'Enable cross-region backup replication to disaster recovery peer'
7. Click on 'Add Peer Database' to finish creating the Autonomous DR instance

## C. Kubernetes Operators

### Deploy Coherence Operator

1. Add Helm chart repository for Coherence Operator:
```shell
helm repo add coherence https://oracle.github.io/coherence-operator/charts
helm repo update
```

2. Install Coherence Operator:
```shell
for c in c1 c2; do
  kubectx $c
  helm install --namespace coherence-operator coherence coherence/coherence-operator --create-namespace  
done
```
### Prepare Kubernetes Namespaces, ConfigMaps and Secrets

1. Create and label a namespace for the todo application in Kubernetes:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl create ns todo
  kubectl label namespace todo istio-injection=enabled
done
``` 

2. Create a secret to authenticate with OCIR:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl create secret -n todo docker-registry ocir-secret --docker-server=<your-registry-server> \
  --docker-username=<your-name> --docker-password=<your-pword> --docker-email=<your-email>
done
```

3. Download the Database Wallet and upload it to the database folder on the operator host:

```shell
scp Wallet_TasksDB.zip operator:~/database/
```
4. Extract the Wallet and switch the tnsnames files:

```shell
cd database
unzip Wallet_TasksDB.zip
mv tnsnames.ora tnsnames.ora.orig
mv tnsnames.ora.ha tnsnames.ora
```
 
5. Create a secret to store the wallet:

```shell
export OJDBC=$HOME/database/ojdbc.properties
export TNSNAMES=$HOME/database/tnsnames.ora
export SQLNET=$HOME/database/sqlnet.ora
export CWALLET=$HOME/database/cwallet.sso
export EWALLET=$HOME/database/ewallet.p12
export KEYSTORE=$HOME/database/keystore.jks
export TRUSTSTORE=$HOME/database/truststore.jks

for c in c1 c2; do
  kubectx $c
  kubectl -n todo create secret generic tasksdb-wallet --from-file=ojdbc.properties=$OJDBC --from-file=tnsnames.ora=$TNSNAMES \
  --from-file=sqlnet.ora=$SQLNET --from-file=cwallet.sso=$CWALLET --from-file=ewallet.p12=$EWALLET --from-file=keystore.jks=$KEYSTORE \
  --from-file=truststore.jks=$TRUSTSTORE
done
```

## D. Deploy todo Application

1. Edit the hibernate.cfg.xml file in `/home/opc/todo` and update the password.

2. Store the Hibernate configuration in a secret:

```shell
export HIBERNATE_CFG_XML=$HOME/todo/hibernate.cfg.xml
for c in c1 c2; do
  kubectx $c
  kubectl -n todo create secret generic hibernate-cfg --from-file=hibernate.cfg.xml=$HIBERNATE_CFG_XML
done
```

3. Create the WKA headless service:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/wka.yaml
done
```
4. Deploy Coherence cluster with storage enabled
```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/coherence-$c.yaml
done
```
5. Deploy backend service
```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/backend.yaml
done
```
6. Deploy frontend service
```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/frontend.yaml
done
```
7. Port-forward to the backend:
```shell
kubectx c1
kubectl -n todo port-forward svc/backend 8080:8080
```
8. In another terminal, run the command to preload the data:

```shell
curl http://localhost:8080/api/backend/preload
```
9. Stop the port-forwarding to the backend and instead set up port-forwarding to frontend:
```shell
kubectx c1
kubectl -n todo port-forward svc/frontend 8080:8080
```
10. Use your browser to access the todo application http://localhost:8080/. Verify the keys have been loaded properly.

11. Setup port-forwarding to the frontend in the c2 cluster to verify the data is being shared across clusters:

```shell
kubectx c2
kubectl -n todo port-forward svc/frontend 8080:8080
```

12. Set up public access by creating an Istio Gateway and a VirtualService for the frontend:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/frontend-vs.yaml
done
```

13. Obtain the Gateway URLs:

```shell
export INGRESS_NAME=istio-ingressgateway
export INGRESS_NS=istio-system

for c in c1 c2; do
  kubectx $c
  export INGRESS_HOST=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
  export INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')
  export SECURE_INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="https")].port}')
  export TCP_INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="tcp")].port}')
  export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT
  echo "http://${GATEWAY_URL}/"
done
```
14. Access the todo application in your browser using the Gateway URLs printed in (10).

15. Configure locality failover using DestinationRules in each region:

```shell
for c in c1 c2; do
  kubectx $c
  kubectl apply -f $HOME/todo/todo-dr-$c.yaml
done
```

### Configure OCI DNS
This assumes the DNS domain (domain.tld) is already being managed by OCI DNS.

1. Use the IP addresses obtained in Step 7 to create 'A' records for the ingress gateways e.g.  c1.domain.tld, c2.domain.tld.
 
### Configure Traffic Steering

1. In OCI Console, navigate to Networking > DNS Management> Traffic Management Steering Policies

2. Create a Traffic Steering Policy and select Load Balancer

3. In the Answer(s) section, add 2 entries of type A record with the IP addresses of the ingress gateways as Rdata and equal weight

4. Add a new Health Check and select HTTP as protocol

5. In Attached Domain(s) section, add todo in Subdomain. Select the compartment in which the DNS Zone for your DNS domain has been created and then select the zone. 

6. You can now access the application as todo.domain.tld