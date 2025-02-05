# todo with Helidon and Coherence

## Configure database

### Oracle Database
1. Create a table
```
CREATE TABLE TASKS(ID VARCHAR2(255), TITLE VARCHAR2(255), COMPLETED BOOLEAN)
```
2. Download the wallet to a known directory and extract
3. Create a file `hibernate.cfg.xml` file in a known location

```xml
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 5.3//EN"
        "http://hibernate.org/dtd/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>
        <property name="hbm2ddl.auto">update</property>
        <property name="connection.url">jdbc:oracle:thin:@tasksdb_medium?TNS_ADMIN=/wallets/task_db</property>
        <property name="connection.username">task_db_user</property>
        <property name="connection.password">changeme</property>
        <property name="connection.driver_class">oracle.jdbc.OracleDriver</property>
        <property name="hibernate.oracleucp.connectionFactoryClassName">oracle.jdbc.datasource.impl.OracleDataSource
        </property>

        <mapping class="io.helidon.labs.todo.coherence.Task"/>
    </session-factory>
</hibernate-configuration>
                            
## Build modules

### Build everything locally
 
```bash
mvn clean package
```   
 
### Build individual module

* coherence

```bash
mvn -pl coherence clean package -Dmaven.test.skip
```    
  
* backend

```bash
mvn -pl coherence,backend install -Dmaven.test.skip
```   
  
* frontend

```bash
mvn -pl frontend package -Dmaven.test.skip
```


### Run

1. Set Environment variable

```bash
export HIBERNATE_CFG_XML=/path/to/hibernate.cfg.xml
export WALLET=/path/to/extracted/wallet
```
2. Run one or more Coherence Cache Servers
   
```bash
./run.sh coherence
```   

3. Run the backend

```bash
./run.sh backend
```

4. Run the frontend

```bash
./run.sh frontend
```
5. Preload data
```bash
curl http://localhost:8080/api/backend/preload
```
6. Access
Use your browser to access http://localhost:7001/

## Run with Helidon Dev 

1. Run the backend

```bash
cd backend
export COHERENCE_HIBERNATE_CONFIG=/path/to/hibernate.cfg.xml
export COHERENCE_WKA=127.0.0.1
export COHERENCE_POF_ENABLED=true
export COHERENCE_TTL=0
export COHERENCE_CLUSTER=todo
helidon dev
```
2. Run the frontend

```bash
cd frontend
helidon dev
```

## Build Containers

### Build containers locally

1. coherence
```bash
docker buildx build --platform=linux/amd64 -t coherence -f docker/Dockerfile.coherence .
```
2. backend
````bash
docker buildx build --platform=linux/amd64 -t backend -f docker/Dockerfile.backend .
````
3. frontend
```bash
docker buildx build --platform=linux/amd64 -t frontend -f docker/Dockerfile.frontend .
```

### Run containers

1. Set Environment variable

```bash
export HIBERNATE_CFG_XML=/path/to/hibernate.cfg.xml
export WALLET=/path/to/extracted/wallet
```

2. Run Coherence container
```bash
docker run --rm -it -v $HIBERNATE_CFG_XML:/hibernate/hibernate.cfg.xml -v $WALLET:/wallets/task_db coherence
```
3. Run backend container
```bash
docker run --rm -it -v $HIBERNATE_CFG_XML:/hibernate/hibernate.cfg.xml -v $WALLET:/wallets/task_db  -p 8080:8080 backend
```
4. Run frontend container
```bash
docker run --rm -it -e HELIDON_CONFIG_PROFILE=docker -p 7001:7001 frontend
```
5. Preload data
```bash
curl http://localhost:8080/api/backend/preload
```
6. Access 
Use your browser to access http://localhost:7001/

## Deployment on Kubernetes

### Push container images to OCIR

1. Tag container images:

```bash
docker tag coherence:latest ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/coherence:v3
docker tag backend:latest ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/backend:v3
docker tag frontend:latest ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/frontend:v3
```
2. Create 3 container repositories in OCIR:
- todo/coherence
- todo/backend
- todo/frontend

3. Push container images to OCIR:
```
docker push ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/coherence:v3
docker push ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/backend:v3
docker push ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/frontend:v3
```

### Setup Oracle Database Operator
1. Install cert-manager:
```bash
helm repo add jetstack https://charts.jetstack.io
helm repo update
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.15.3/cert-manager.crds.yaml
helm install cert-manager --namespace cert-manager --version v1.15.3 jetstack/cert-manager --create-namespace
```
2. Install Oracle Database Operator:
```bash
kubectl apply -f https://raw.githubusercontent.com/oracle/oracle-database-operator/main/oracle-database-operator.yaml
```

3. Create Role Bindings for Access Management:
```
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: oracle-database-operator-oracle-database-operator-manager-rolebinding
  namespace: oracle-database-operator-system
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: oracle-database-operator-manager-role
subjects:
- kind: ServiceAccount
  name: default
  namespace: oracle-database-operator-system
```

4. Create a ConfigMap for OCI Credentials to use API keys:
```bash
kubectl create --namespace oracle-database-operator-system configmap oci-cred \
--from-literal=tenancy=ocid1.tenancy.oc1..................67iypsmea \
--from-literal=user=ocid1.user.oc1..aaaaaaaaxw3i...............ce6qzdrnmq \
--from-literal=fingerprint=b2:7c:a8:d5:44:f5.....................:9a:55 \
--from-literal=region=us-phoenix-1
```

5. Create a secret to store the private key:
```bash
kubectl create secret --namespace oracle-database-operator-system generic oci-privatekey --from-file=privatekey=~/.oci/oci_rsa.pem
```

### Setup Coherence Operator

1. Add Helm chart repository for Coherence Operator:
```bash
helm repo add coherence https://oracle.github.io/coherence-operator/charts

helm repo update
```

2. Install Coherence Operator:
```bash
helm install --namespace coherence-operator coherence coherence/coherence-operator --create-namespace
```

3. Create a secret to authenticate with OCIR:

```bash
kubectl create secret -n todo docker-registry ocir-secret --docker-server=<your-registry-server> --docker-username=<your-name> --docker-password=<your-pword> --docker-email=<your-email>
```

### Deploy to Kubernetes
1. Create a namespace for the todo application in Kubernetes:

```bash
kubectl create ns todo
``` 
2. Create a password for the wallet:

```bash 
kubectl create --namespace todo secret generic tasksdb-wallet-password --from-literal=tasksdb-wallet-password='<replace_me>'
```
3. Create the following manifest and replace the OCID value for the Autonomous Database:

```
apiVersion: database.oracle.com/v1alpha1
kind: AutonomousDatabase
metadata:
  name: tasksdb
  namespace: todo
spec:
  details:
    autonomousDatabaseOCID: ocid1.autonomousdatabase.oc1.....
    wallet:
      name: tasksdb-wallet
      password:
        k8sSecret:
          name: tasksdb-wallet-password
  ociConfig:
    configMapName: oci-cred
    secretName: oci-privatekey
```
4. Bind the Autonomous Database to the cluster:

```bash
kubectl apply -f taskdb.yaml
```

5. Store the Hibernate configuration in a secret:

```bash
kubectl -n todo create secret generic hibernate-cfg --from-file=hibernate.cfg.xml=/path/to/hibernate.cfg.xml
```

6. Edit the manifests in the manifests directory and ensure the image location is correct e.g.

```yaml
          image: ocir.<region>.oci.oraclecloud.com/<tenancy_namespace>/todo/frontend:v3
```

### Deploy to Kubernetes

1. Deploy Coherence cluster with storage enabled
```bash
kubectl apply -f coherence-c1.yaml
```
2. Deploy backend service
```bash
kubectl apply -f backend.yaml
```
3. Deploy frontend service
```bash
kubectl apply -f frontend.yaml
```
4. Set up port-forward to the backend:

```bash
kubectl -n todo port-forward svc/backend 8080:8080
```
5. In another terminal, run the command to preload the data:
```bash
curl http://localhost:8080/api/backend/preload
```
6. Stop the port-forwarding to the backend and instead set up port-forwarding to frontend:
```bash
kubectl -n todo port-forward svc/frontend 8080:8080
```
7. Use your browser to access the todo application http://localhost:8080/

## Set up Istio

1. Create the following manifest and replace the NSG OCID with an appropriate value:

```yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    ingressGateways:
      - name: istio-ingressgateway
        enabled: true
        k8s:
          serviceAnnotations:
            service.beta.kubernetes.io/oci-load-balancer-shape: "flexible"
            service.beta.kubernetes.io/oci-load-balancer-shape-flex-min: "50"
            service.beta.kubernetes.io/oci-load-balancer-shape-flex-max: "100"
            service.beta.kubernetes.io/oci-load-balancer-security-list-management-mode: "None"
            oci.oraclecloud.com/oci-network-security-groups: "ocid1.networksecuritygroup...."
```
2. Install Istio:

```bash
istioctl install -f istio.yaml
```

3. Label the `todo` namespace that with istio-injection=enabled:

```bash
kubectl label namespace todo istio-injection=enabled
```

4. Create an Istio Gateway using the following manifest. If you are going to use a FQDN, add a new entry before the '*' under hosts:

```
apiVersion: networking.istio.io/v1
kind: Gateway
metadata:
  name: todo-gateway
spec:
  # The selector matches the ingress gateway pod labels.
  # If you installed Istio using Helm following the standard documentation, this would be "istio=ingress"
  selector:
    istio: ingressgateway # use istio default controller
  servers:
    - port:
        number: 8080
        name: http
        protocol: HTTP
      hosts:
        - "*"
---
```

5. Create a VirtualService for the frontend using the following manifest:

```bash
apiVersion: networking.istio.io/v1
kind: VirtualService
metadata:
  name: frontend
spec:
  hosts:
    - "*"
  gateways:
    - todo-gateway
  http:
    - match:
        - uri:
            prefix: /api/todo
        - uri:
            prefix: /css
        - uri:
            prefix: /js
        - uri:
            exact: /
      route:
        - destination:
            host: frontend
            port:
              number: 8080
```
6. Obtain the Ingress IP and Port:

```bash
export INGRESS_HOST=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
export INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')
export SECURE_INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="https")].port}')
export TCP_INGRESS_PORT=$(kubectl -n "$INGRESS_NS" get service "$INGRESS_NAME" -o jsonpath='{.spec.ports[?(@.name=="tcp")].port}')
```
7. Print the Gateway URL:

```bash
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT
echo "http://${GATEWAY_URL}/"
```

8. Access the todo application in your browser using the Gateway URL printed in (7).