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

# Create policies for deployment host instance
resource "oci_identity_policy" "instance_policy" {
  name           = "instance-policy${var.resource_name_suffix}"
  description    = "Policy created for compute instance that will host the deployment"
  compartment_id = var.tenancy_ocid

  statements = [
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use log-content in compartment ${oci_identity_compartment.compartment.name}",
    "Allow dynamic-group ${oci_identity_dynamic_group.instance_dynamic_group.name} to use metrics in compartment ${oci_identity_compartment.compartment.name}"
  ]
}
