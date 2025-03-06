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

# Create group for compartment and cloud shell
resource "oci_identity_group" "user_group" {
  name           = "devops-group${local.resource_name_random_suffix}"
  description    = "Group for users to be able to access all resources on created compartment and cloud shell"
  compartment_id = var.tenancy_ocid
}

# Create user-group membership if user_ocid is set
resource "oci_identity_user_group_membership" "user_group_membership" {
  count    = length(var.user_ocid) > 0 ? 1 : 0
  group_id = oci_identity_group.user_group.id
  user_id  = var.user_ocid
}
