# Deploying Helidon OCI MP Application on a Basic OCI Infrastructure Setup

This example will demonstrate how to create a basic OCI infrastructure setup that can be used to deploy a Helidon MP OCI application.

## Objective
1. Use Terraform to create automation of OCI resources provisioning that can build a basic OCI infrastructure setup composed of the following:
   1. OCI Compute Instance with firewall opened at port 8080.
   2. OCI Virtual Cloud Network with Security List containing an Ingress at port 8080.
   3. Policies to allow OCI Logging and Monitoring services to be accessed from the provisioned OCI Compute Instance.
2. Generate a Helidon MP OCI project using Helidon cli. 
3. Use OCI cloud-shell to run this example through. 

## Prerequisite
- An OCI tenancy that has enough capacity to provision an OCI Compute Instance and a Virtual Cloud Network. This will also work on the free trial of the [OCI Free Tier](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier.htm).

## Tasks
### Set up Cloud Shell access.
1. If you are already a super-user with administrator rights that has access to all resources in the tenancy, skip to the next section. Otherwise, continue to the next step.
2. Create a new group and add your user as a member of that group.
3. Create a new policy and provide that group a cloud-shell access:
   ```
   Allow group '<my_cloud_shell_access_group>' to use cloud-shell in tenancy
   ```
4. Verify that it works by opening Cloud Shell from the OCI Console.

### Retrieve the oci-basic-setup subdirectory from the helidon-labs repository
The materials for this exercise will be located in the [oci-basic-setup](https://github.com/helidon-io/helidon-labs/tree/main/hols/oci-basic-setup) subdirectory of the  [helidon-labs](https://github.com/helidon-io/helidon-labs) repository, hence only that specific subdirectory will be cloned.
1. Open a Cloud Shell terminal and make sure you are in the home directory
   ```shell
   cd ~
   ```
2. Use `git sparsecheckout` to check out only the `oci-basic-setup` directory from the helidon-labs repository.
   ```shell
   git init helidon-labs
   cd helidon-labs 
   git remote add origin https://github.com/helidon-io/helidon-labs.git
   git config core.sparsecheckout true
   echo "hols/oci-basic-setup/*" >> .git/info/sparse-checkout
   echo ".gitignore" >> .git/info/sparse-checkout
   git pull --depth=1 origin main
   ```
3. The previous step will pull all the required Terraform and Bash script files into the  `~/helidon-labs/hols/oci-basic-setup` directory that will be needed to perform various operations to complete this exercise.

### Prepare the OCI infrastructure environment
The goal of this task is to prepare a basic infrastructure environment comprised of a Compartment, Dynamic Groups, Policies, Compute Instance and Virtual Cloud Network. This section requires a user with administrator privilege.
1. Open the newly cloned repository directory `oci-basic-setup` from the a Cloud Shell terminal.
   ```shell
   cd ~/helidon-labs/hols/oci-basic-setup
   ```
2. From the root directory of the repository, open `terraform.tfvars` and set the values of the following variables: 
   * `tenancy_ocid` - This can be retrieved by opening the `Profile` menu (click the User icon on the top most right corner of the console window) and click `Tenancy: <your_tenancy_name>`. The tenancy OCID is shown under `Tenancy Information`. Click `Show` to display the entire ID or click `Copy` to copy it to your clipboard.
   * `region` - From OCI Console's home screen, open the `Region` menu, and then click `Manage Regions`. Locate the Home Region and copy the Region identifier.
   * `instance_*` - Use these parameters to specify the size and shape of the OCI compute instance that you would like to be provisioned. Currently defaults to an available shape from an [Always Free Resources of an OCI Free Tier Tenancy](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm).
     ```terraform
     instance_shape                      = "VM.Standard.E2.1.Micro"
     instance_ocpus                      = 1
     instance_shape_config_memory_in_gbs = 1
     instance_os                         = "Oracle Linux"
     instance_os_version                 = "8"
     ```
4. Use Terraform to execute the scripts to provision the OCI resources.
   ```bash
   terraform init
   terraform plan
   terraform apply -auto-approve
   ```
    
### Prepare the Helidon OCI Application:
1. Go to the home directory from a Cloud Shell console:
   ```shell
   cd ~
   ```  
2. Download and unzip the helidon cli generic distribution
   ```shell
   curl -L -O https://github.com/helidon-io/helidon-build-tools/releases/download/3.0.6/helidon-cli.zip
   unzip helidon-cli.zip
   ```
3. Make sure that JDK 21 exist in the path.
4. Execute the cli to generate a Helidon Microprofile application project.
   ```shell
   ~/helidon-3.0.6/bin/helidon init
   ```
5. When it prompts for `Helidon version`, choose the default (or the latest Helidon 4 version)
   ```shell
   Helidon versions
   (1) 4.2.1
   (2) 3.2.12
   (3) 2.6.11
   (4) Show all versions
   Enter selection (default: 1):
   ```
6. When prompted to `Select a Flavor`, choose `Helidon MP`
   ```shell
   | Helidon Flavor
   
   Select a Flavor
     (1) se | Helidon SE
     (2) mp | Helidon MP
   Enter selection (default: 1): 2
   ```
7. When prompted to `Select an Application Type`, choose `oci`  
   ```shell
   Select an Application Type
   (1) quickstart | Quickstart
   (2) database   | Database
   (3) custom     | Custom
   (4) oci        | OCI
   Enter selection (default: 1): 4
   ```
8. When prompted for `Project groupId`, `Project artifactId`, `Project version` and `Java package name`, just accept the default values. Once completed, this will generate an `oci-mp` project.
10. Go to the `oci-mp` directory.
    ```shell
    cd ~/oci-mp
    ```
11. Run the `update_config_values.sh` utility script from the `oci-basic-setup` local repository to update the config parameters. Specify the directory location of the Helidon application as an argument to this script.
    ```shell
    ~/helidon-labs/hols/oci-basic-setup/update_config_values.sh ~/oci-mp
    ```
    Invoking this script will perform the following:
    1. Updates in `~/oci-mp/server/src/main/resources/application.yaml` config file to set up a Helidon feature that sends Helidon generated metrics to the OCI monitoring service.
       1. compartmentId - Compartment ocid that is used for this demo
       2. namespace - This can be any string but for this demo, will be set to `helidon_metrics`.
    2. Updates in `~/oci-mp/server/src/main/resources/META-INF/microprofile-config.properties` config file to set up configuration parameters used by the Helidon app code to perform integration with OCI Logging and Metrics service.
       1. oci.monitoring.compartmentId - Compartment ocid that is used for this demo
       2. oci.monitoring.namespace - This can be any string but for this demo, this will be set to `helidon_application`.
       3. oci.logging.id - Application log id that was provisioned by the terraform scripts.

    **Note:** Make sure to validate that `application.yaml` and `microprofile-config.properties` have been updated by checking that the mentioned config parameters were properly populated.
12. To prepare for the build, ensure that JDK 21 and Maven 3.8+ exist and are set in the PATH environment variable of the Cloud Shell terminal.
13. Build the application.  
    ```shell
    mvn clean package
    ```

### Deploy the Helidon OCI application:
1. Go back to `oci-basic-setup` local repository directory.
   ```shell
   cd ~/helidon-labs/hols/oci-basic-setup
   ```
2. Open up `deploy.sh` script and use the JDK_TAR_GZ_INSTALLER variable to specify the download path of jdk 21 based on the OS flavor currently installed on Compute instance. The current value used is the Linux x64 version of jdk 21. 
3. Run `deploy.sh` utility script and provide the directory of the OCI MP application as the argument. This script will zip up the server binaries, upload it to the compute instance, install jdk 21, create a systemctl service for the application to ensure that the application is persistent across reboots, and start the service.
   ```shell
   ./deploy.sh ~/oci-mp
   ```

### Exercise the deployed Helidon application:
1. Access the application by using curl to do a GET & PUT http requests.
    1. Set the endpoint using the instance's public ip which can be retrieved using `get.sh` utility script from the `oci-basic-setup` local repository directory.
       ```shell
       export ENDPOINT_IP=$(~/helidon-labs/hols/oci-basic-setup/get.sh public_ip)
       echo "Instance public ip is $ENDPOINT_IP"
       ```
    2. Test Hello world request.
       ```shell
       curl http://$ENDPOINT_IP:8080/greet
       ```
       results to:
       ```shell
       {"message":"Hello World!","date":[2025,4,29]}
       ```
    3. Test Hello to a name, i.e. to `Joe`.
       ```shell
       curl http://$ENDPOINT_IP:8080/greet/Joe
       ```
       results to:
       ```shell
       {"message":"Hello Joe!","date":[2025,4,29]}
       ```
    4. Replace Hello with another greeting word, i.e. `Hola`.
       ```shell
       curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://$ENDPOINT_IP:8080/greet/greeting 
       curl http://$ENDPOINT_IP:8080/greet
       ```
       results to:
       ```shell
       {"message":"Hola World!","date":[2025,4,29]}
       ```
2. Validate that the Helidon Metrics are pushed to the OCI Monitoring Service using the OCI metric integration that was added in the Helidon application:
   1. From the OCI Console, go to `Observability & Management` -> `Metrics Explorer (under Monitoring)`.
   2. On `Query 1`, choose the `Compartment` value with the format of `devops-compartment-helidon-demo-<4 char random value>`.
   3. Select `helidon_metrics` under `Metric namespace`.
   4. Select `requests.count_counter` under `Metric Name`.
   5. Above the empty graph, you can choose values for `Start time/End Time` or choose the time duration under `Quick Selects`.
   6. Click `Update Chart` at the bottom of `Query 1` to show all the metric data for the `request count`. You can also toggle to enable `Show Data Table` on the right upper portion of the graph to show the list of data for the chosen metric.
   7. You can also explore other Metrics by going back to step 4 and choosing a new value. 
3. Validate OCI Logging SDK integration that was added in the Helidon application. This will push log messages to the OCI Logging Service:
   1. From the OCI Console, go to `Observability & Management` -> `Logs (under Logging)`.
   2. Change `Compartment` with a value that has the format of `devops-compartment-helidon-demo-<4 char random value>`.
   3. Under `Filters`, change `Log Group` to `app-log-group-helidon-demo`.
   4. Choose and click on `app-log-helidon-demo` from the Logs table.
   5. Choose `Filter by time` value within the scope of your last request. For example, you can choose `Today` to see all request that was made today.
   6. The `Explore Log` display should output some graphs of the logging activity and below it will also show a list of the logs that has been captured.

### Cleanup
When the environment is no longer needed, all the OCI resources can be cleaned up by following these steps:
1. Go back to oci-basic-setup local repository directory.
   ```shell
   cd ~/helidon-labs/hols/oci-basic-setup
   ```  
2. Execute the `destroy` option from terraform.
   ```shell
   terraform destroy -auto-approve
   ```   
