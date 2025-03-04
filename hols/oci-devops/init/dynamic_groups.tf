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

# Create group, user and polcies for devops service
resource "oci_identity_dynamic_group" "devops_dynamic_group" {
  name           = "devops-dynamic-group${local.resource_name_random_suffix}"
  description    = "DevOps pipeline dynamic group"
  compartment_id = var.tenancy_ocid
  matching_rule = format("Any {%s, %s, %s}",
    "ALL {resource.type = 'devopsbuildpipeline', resource.compartment.id = '${oci_identity_compartment.devops_demo_compartment.id}'}",
    "ALL {resource.type = 'devopsdeploypipeline', resource.compartment.id = '${oci_identity_compartment.devops_demo_compartment.id}'}",
    "ALL {resource.type = 'devopsrepository', resource.compartment.id = '${oci_identity_compartment.devops_demo_compartment.id}'}"
  )
}

resource "oci_identity_dynamic_group" "instance_dynamic_group" {
  name           = "instance-dynamic-group${local.resource_name_random_suffix}"
  description    = "Compute instance dynamic group"
  compartment_id = var.tenancy_ocid
  matching_rule  = "ALL {instance.compartment.id = '${oci_identity_compartment.devops_demo_compartment.id}'}"
}
