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

# Create all logging resources required by the Helidon OCI application
resource "oci_logging_log_group" "custom_log_group" {
  compartment_id = module.tenancy.compartment_id
  display_name   = "custom-log-group${local.resource_name_random_suffix}"
}

resource "oci_logging_log" "custom_log" {
  display_name = "custom-log${local.resource_name_random_suffix}"
  log_group_id = oci_logging_log_group.custom_log_group.id
  log_type     = "CUSTOM"
}
