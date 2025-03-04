#
# Copyright (c) 2025 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Create policies for devops service
resource "oci_identity_policy" "devops_policy" {
  name           = "devops-policy${local.resource_name_random_suffix}"
  description    = "Policy created for devops"
  compartment_id = var.tenancy_ocid

  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to manage cluster-family in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to manage devops-family in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to manage repos in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to manage all-artifacts in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to read instance-family in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to use instance-agent-command-family in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.devops_dynamic_group.name} to use ons-topics in compartment ${oci_identity_compartment.devops_demo_compartment.name}"
  ]
}

# Create policies for deployment host instance
resource "oci_identity_policy" "instance_policy" {
  name           = "instance-policy${local.resource_name_random_suffix}"
  description    = "Policy created for compute instance that will host the deployment"
  compartment_id = var.tenancy_ocid

  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use instance-agent-command-execution-family in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to read generic-artifacts in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use log-content in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use metrics in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to manage objects in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use buckets in compartment ${oci_identity_compartment.devops_demo_compartment.name}"
  ]
}

# Create policies for user group
resource "oci_identity_policy" "user_group_policy" {
  name           = "user-policy${local.resource_name_random_suffix}"
  description    = "Policy to allow user full access to the created compartment and cloud shell"
  compartment_id = var.tenancy_ocid

  statements = [
    "Allow group ${oci_identity_group.user_group.name} to manage all-resources in compartment ${oci_identity_compartment.devops_demo_compartment.name}",
    "Allow group ${oci_identity_group.user_group.name} to use cloud-shell in tenancy"
  ]
}
