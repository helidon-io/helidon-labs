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

# Create all resources required by the Helidon application
resource "oci_logging_log_group" "application_log_group" {
  compartment_id = var.compartment_ocid
  display_name   = "app-log-group${local.resource_name_suffix}"
}

resource "oci_logging_log" "application_log" {
  display_name = "app-log${local.resource_name_suffix}"
  log_group_id = oci_logging_log_group.application_log_group.id
  log_type     = "CUSTOM"
}

# Create Object Storage that will be used by the application
resource "oci_objectstorage_bucket" "application_bucket" {
  compartment_id = var.compartment_ocid
  name           = "app-bucket${local.resource_name_random_suffix}"
  namespace      = data.oci_objectstorage_namespace.object_storage_namespace.namespace
}
