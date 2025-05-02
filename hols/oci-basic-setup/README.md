# Deploying a Helidon OCI MP Application on a Basic OCI Setup

This example demonstrates how to create a basic OCI infrastructure setup that can be used to deploy a Helidon MP OCI application.

## Objective
1. Use Terraform to automate the provisioning of OCI resources that can build a basic OCI setup composed of the following:
   1. OCI Compute instance with firewall opened at port 8080.
   2. OCI Virtual Cloud Network with a Security List containing an Ingress at port 8080.
   3. Policies to allow OCI Logging and Monitoring services to be accessed from the provisioned OCI Compute instance.
2. Generate a Helidon MP OCI project using Helidon CLI. 
3. Use OCI Cloud Shell to run this example through. 

## Prerequisite
- An OCI tenancy that has enough capacity to provision an OCI Compute Instance,  a Virtual Cloud Network (VCN) and a Custom Log. At a minimum, this will also  work on an [Always Free Resources](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm) type of Tenancy.
- If this is your first time using OCI, consider going though some of these references to learn more about it:
  - [Getting Started](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/baremetalintro.htm)
  - [Learn About Oracle Cloud Basics](https://docs.oracle.com/en-us/iaas/Content/GSG/Concepts/concepts.htm#concepts-start)
  - [Signing In for the First Time](https://docs.public.oneportal.content.oci.oraclecloud.com/en-us/iaas/Content/GSG/Tasks/signingin_topic-Signing_In_for_the_First_Time.htm) 
  - [Launching Your First Linux Instance](https://docs.oracle.com/en-us/iaas/Content/Compute/tutorials/first-linux-instance/overview.htm)
- If this is your first time using Terraform, please check out the link below to learn more about it:  
  - [Getting Started with the Terraform Provider](https://docs.oracle.com/en-us/iaas/Content/dev/terraform/getting-started.htm)

## Tasks
### Set up Cloud Shell access
1. If you are already a super-user with administrator rights that has access to all resources in the tenancy, skip to the next section. Otherwise, continue to the next step.
2. Create a new group and add your user as a member of that group.
3. Create a new policy and provide that group a Cloud Shell access:
   ```
   Allow group '<my_cloud_shell_access_group>' to use cloud-shell in tenancy
   ```
4. Verify that it works by opening Cloud Shell from the OCI Console.

### Retrieve the oci-basic-setup subdirectory from the helidon-labs repository
The materials for this exercise are located in the [oci-basic-setup](https://github.com/helidon-io/helidon-labs/tree/main/hols/oci-basic-setup) subdirectory of the  [helidon-labs](https://github.com/helidon-io/helidon-labs) repository, so only that specific subdirectory needs to be cloned.
1. Open a Cloud Shell terminal and make sure you are in the home directory
   ```shell
   cd ~
   ```
2. Use `git sparsecheckout` to check out only the `oci-basic-setup` directory from the `helidon-labs` repository.
   ```shell
   git init helidon-labs
   cd helidon-labs 
   git remote add origin https://github.com/helidon-io/helidon-labs.git
   git config core.sparsecheckout true
   echo "hols/oci-basic-setup/*" >> .git/info/sparse-checkout
   echo ".gitignore" >> .git/info/sparse-checkout
   git pull --depth=1 origin main
   ```
3. The previous step pulls all the required Terraform and Bash script files into the  `~/helidon-labs/hols/oci-basic-setup` directory that is required to perform the various operations in this exercise.

### Prepare the OCI infrastructure environment
The goal of this task is to prepare a basic infrastructure environment comprised of a Compartment, Dynamic Groups, Policies, Compute and Virtual Cloud Network. This section requires a user with administrator privilege.
1. Open the newly cloned repository directory `oci-basic-setup` from the a Cloud Shell terminal.
   ```shell
   cd ~/helidon-labs/hols/oci-basic-setup
   ```
2. From the root directory of the repository, edit `terraform.tfvars` and set the values of the following variables: 
   * `tenancy_ocid` - This can be retrieved by opening the `Profile` menu (click the User icon on the top most right corner of the console window) and click `Tenancy: <your_tenancy_name>`. The tenancy OCID is shown under `Tenancy Information`. Click `Show` to display the entire ID or click `Copy` to copy it to the clipboard and paste the value into `terraform.tfvars`.
   * `region` - From OCI Console's home screen, open the `Region` menu, and then click `Manage Regions`. Locate the Home Region and copy the Region identifier to the clipboard and paste the value into `terraform.tfvars`.
   * `instance_*` - Use these parameters to specify the size and shape of the OCI Compute instance that you would like to be provisioned. Currently defaults to an available shape from an [Always Free Resources of an OCI Free Tier Tenancy](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm).
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
    
### Prepare the Helidon OCI Application
1. Go to the home directory from a Cloud Shell console:
   ```shell
   cd ~
   ```
2. Make sure that JDK 21 exists.
   1. Run the following command to validate:
      ```shell
      java -version
      ```
   2. If it doesn't exist, download and install the appropriate binaries relevant to your OS platform. Below is an example of downloading and extracting a Linux Arm64 JDK compressed archive that will work with the default platform of Cloud Shell.
      ```shell
      curl -O https://download.oracle.com/java/21/latest/jdk-21_linux-aarch64_bin.tar.gz
      tar -xvzf jdk-21_linux-aarch64_bin.tar.gz
      ```
   3. Set the JAVA_HOME and PATH environment variables to include the corresponding directories from the extracted JDK binaries. 
      ```shell
      export JAVA_HOME=~/jdk-21.0.7/
      export PATH=$JAVA_HOME/bin/:$PATH
      ```
      **Note:** If you want the above environment variable settings to persist between Cloud Shell sessions, add the definitions to your `~/.bashrc` file.
3. Make sure that Maven 3.8 or higher exists.
    1. Run the following command to validate:
       ```shell
       mvn --version
       ```
    2. If it doesn't exist, download and install the binary archive of your choice from the [Apache Maven Download page](https://maven.apache.org/download.cgi). Below is an example of downloading and extracting a  Maven 3.9.9 binary tar.gz archive.
       ```shell
       curl -O https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
       tar -xvzf apache-maven-3.9.9-bin.tar.gz
       ```
    3. Add the extracted mvn bin subdirectory into the PATH environment variable
       ```shell
       export PATH=~/apache-maven-3.9.9/bin/:$PATH
       ```
       **Note:** If you want the above environment variable setting to persist between Cloud Shell sessions, add the definition to your `~/.bashrc` file.
4. Install the Helidon CLI generic distribution.
    1. Download and extract the Helidon CLI generic distribution
       ```shell
       curl -L -O https://github.com/helidon-io/helidon-build-tools/releases/download/3.0.6/helidon-cli.zip
       unzip helidon-cli.zip
       ```
    2. Set the PATH environment variable to include the bin directory of the extracted Helidon CLI.
       ```shell
       export PATH=~/helidon-3.0.6/bin/:$PATH
       ```
       **Note:** If you want the above environment variable settings to persist between Cloud Shell sessions, add the definitions to your `~/.bashrc` file.   
5. Use the CLI to generate a Helidon Microprofile application project.
   1. Start the CLI.
      ```shell
      helidon init
      ```
   2. When it prompts for `Helidon version`, choose the default (or the latest Helidon 4 version).
      ```shell
      Helidon versions
      (1) 4.2.1
      (2) 3.2.12
      (3) 2.6.11
      (4) Show all versions
      Enter selection (default: 1):
      ```
   3. When prompted to `Select a Flavor`, choose `Helidon MP`.
      ```shell
      | Helidon Flavor
   
      Select a Flavor
        (1) se | Helidon SE
        (2) mp | Helidon MP
      Enter selection (default: 1): 2
      ```
   4. When prompted to `Select an Application Type`, choose `oci`.
      ```shell
      Select an Application Type
      (1) quickstart | Quickstart
      (2) database   | Database
      (3) custom     | Custom
      (4) oci        | OCI
      Enter selection (default: 1): 4
      ```
   5. When prompted for `Project groupId`, `Project artifactId`, `Project version` and `Java package name`, just accept the default values. Once completed, this will generate an `oci-mp` project.
6. Go to the generated `oci-mp` directory.
   ```shell
   cd ~/oci-mp
   ```
7. Run the `update_config_values.sh` utility script from the `oci-basic-setup` local repository to update the config parameters. Specify the directory location of the Helidon application as an argument to this script.
   ```shell
   ~/helidon-labs/hols/oci-basic-setup/update_config_values.sh ~/oci-mp
   ```
   Invoking this script performs the following:
   1. Updates the `~/oci-mp/server/src/main/resources/application.yaml` config file to set up a Helidon feature that sends Helidon generated metrics to the OCI monitoring service.
      1. compartmentId - The Compartment OCID used for this demo.
      2. namespace - This can be any string but for this demo, will be set to `helidon_metrics`.
   2. Updates the `~/oci-mp/server/src/main/resources/META-INF/microprofile-config.properties` config file to set up configuration parameters used by the Helidon app code to perform integration with OCI Logging and Metrics service.
      1. oci.monitoring.compartmentId - Compartment ocid that is used for this demo
      2. oci.monitoring.namespace - This can be any string but for this demo, this will be set to `helidon_application`.
      3. oci.logging.id - Application log id that was provisioned by the terraform scripts.

   **Note:** Make sure to validate that `application.yaml` and `microprofile-config.properties` were updated by checking that the mentioned config parameters were properly populated.
8. Build the application.  
   ```shell
   mvn clean package
   ```

### Deploy the Helidon OCI application to your Compute instance
1. Go back to `oci-basic-setup` local repository directory.
   ```shell
   cd ~/helidon-labs/hols/oci-basic-setup
   ```
2. Open up `deploy.sh` script and use the JDK_TAR_GZ_INSTALLER variable to specify the download path of JDK 21 based on the OS flavor currently installed on Compute instance. The current value used is the Linux x64 version of JDK 21. 
3. Run `deploy.sh` utility script and provide the directory of the OCI MP application as the argument. This script will zip up the server binaries, upload it to the Compute instance, install JDK 21, create a systemctl service for the application to ensure that the application is persistent across reboots, and start the service.
   ```shell
   ./deploy.sh ~/oci-mp
   ```

### Exercise the deployed Helidon application:
1. Access the application by using curl to perform GET & PUT HTTP requests.
    1. Set the endpoint using the instance's public ip which can be retrieved using `get.sh` utility script from the `oci-basic-setup` local repository directory.
       ```shell
       export ENDPOINT_IP=$(~/helidon-labs/hols/oci-basic-setup/get.sh public_ip)
       echo "Instance public ip is $ENDPOINT_IP"
       ```
    2. Test the Hello world request.
       ```shell
       curl http://$ENDPOINT_IP:8080/greet
       ```
       results in:
       ```shell
       {"message":"Hello World!","date":[2025,4,29]}
       ```
    3. Test Hello to a name, i.e. to `Joe`.
       ```shell
       curl http://$ENDPOINT_IP:8080/greet/Joe
       ```
       results in:
       ```shell
       {"message":"Hello Joe!","date":[2025,4,29]}
       ```
    4. Replace Hello with another greeting word, i.e. `Hola`.
       ```shell
       curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://$ENDPOINT_IP:8080/greet/greeting 
       curl http://$ENDPOINT_IP:8080/greet
       ```
       results in:
       ```shell
       {"message":"Hola World!","date":[2025,4,29]}
       ```
2. Validate that the Helidon Metrics are pushed to the OCI Monitoring Service using the OCI metric integration that was added in the Helidon application:
   1. From the OCI Console, go to `Observability & Management` -> `Metrics Explorer (under Monitoring)`.
   2. On `Query 1`, choose the `Compartment` value with the format of `compartment-helidon_oci_basic_setup-<4 char random value>`.
   3. Select `helidon_metrics` under `Metric namespace`.
   4. Select `requests.count_counter` under `Metric Name`.
   5. Above the empty graph, you can choose values for `Start time/End Time` or choose the time duration under `Quick Selects`.
   6. Click `Update Chart` at the bottom of `Query 1` to show all the metric data for the `request count`. You can also toggle to enable `Show Data Table` on the right upper portion of the graph to show the list of data for the chosen metric.
   7. You can also explore other Metrics by going back to step 4 and choosing a new value. 
3. Validate OCI Logging SDK integration that was added in the Helidon application. This will push log messages to the OCI Logging Service:
   1. From the OCI Console, go to `Observability & Management` -> `Logs (under Logging)`.
   2. Change `Compartment` with a value that has the format of `compartment-helidon_oci_basic_setup-<4 char random value>`.
   3. Under `Filters`, change `Log Group` to `app-log-group-helidon-demo`.
   4. Choose and click on `app-log-helidon-demo` from the Logs table.
   5. Choose `Filter by time` value within the scope of your last request. For example, you can choose `Today` to see all requests that were made today.
   6. The `Explore Log` display should output graphs of the logging activity. Below the graphs, it will also show a list of the logs that have been captured.

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
   
### Troubleshooting Tips
1. There may be times when the Compute instance becomes unresponsive. This may happen when using the `All Free Resources` default Compute shape which has very low ocpu and memory capacity and is most evident when the Helidon application is deployed using `deploy.sh` utility and it freezes. 
   1. To further validate, you can use either of the following steps: 
      1. Run `ssh.sh` utility script from `~/helidon-labs/hols/oci-basic-setup` to remotely access the server. If it also freezes, then you are encountering the problem.
         ```shell
         ~/helidon-labs/hols/oci-basic-setup/ssh.sh
         ```
      2. Check the health of the Compute instance on the OCI console by navigating to `Compute->Instances`, filter the search by choosing the compartment name created by this exercise, which can be retrieved by using `get.sh` utility script:
         ```shell
         ~/helidon-labs/hols/oci-basic-setup/get.sh compartment_name
         ```
         Once the Compute instance shows up, click on it and go to the `Monitoring` tab and verify that there is activity on the various metrics graphs that are shown.
   2. To resolve the issue, you can either:
      1. Wait until the Compute instance stabilizes as there might be a system process that is taking up all the resources, or
      2. From the same Compute instance window from the OCI Console, click on the `Actions` pull down menu and choose `Reboot`. To reboot the instance immediately, without waiting for the OS to respond, select the `Force reboot the instance by immediately powering off, then powering back on` option.
2. If requests to the application are failing, check the application log for errors. Use `ssh.sh` utility script to remotely access the server and the application log can be found in `~/oci-mp/log/oci-mp-server.log`.
3. If OCI Log or OCI Metrics Explorer do not show any results, check that the application configuration has the correct values:
   1. Use `get.sh` utility script to retrieve the values of the `compartment id` and the `custom log id`  that will be used for validation purposes in the next steps to follow:
      ```shell
      ~/helidon-labs/hols/oci-basic-setup/get.sh compartment_id
      ~/helidon-labs/hols/oci-basic-setup/get.sh custom_log_id
      ```
   2. In `~/oci-mp/server/src/main/resources/application.yaml`, validate that the `ocimetrics.compartmentId` reflects the retrieved compartment id and `ocimetrics.namespace` has a value of `helidon_metrics`.
   3. In `~/oci-mp/server/src/main/resources/META-INF/microprofile-config.properties`, validate that the `oci.monitoring.compartmentId` reflects the retrieved compartment id, `oci.monitoring.namespace` has a value of `helidon_application` and `oci.logging.id` has the retrieved custom log id.
   4. If values are incorrect, you can manually replace them with the correct ones, or just simply run the `update_config_values.sh` script again to automatically replace those values:
      ```shell
      ~/helidon-labs/hols/oci-basic-setup/update_config_values.sh ~/oci-mp
      ```
   5. Once the config property values are updated, build and redeploy the application again.
