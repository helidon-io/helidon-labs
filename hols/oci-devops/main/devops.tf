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

# Create OCI Notification
resource "oci_ons_notification_topic" "devops_notification_topic" {
  compartment_id = var.compartment_ocid
  name           = "devops-topic${local.resource_name_random_suffix}"
}

# Create devops project
resource "oci_devops_project" "devops_project" {
  compartment_id = var.compartment_ocid
  name           = "devops-project${local.resource_name_random_suffix}"
  notification_config {
    topic_id = oci_ons_notification_topic.devops_notification_topic.id
  }
  description = var.project_description
}

# Create OCI Code Repository
resource "oci_devops_repository" "devops_repo" {
  name            = local.application_repo_name
  description     = "Will host Helidon OCI MP template app generated via the archetype tool"
  project_id      = oci_devops_project.devops_project.id
  repository_type = "HOSTED"
  default_branch  = "main"
}

# Create log group that will serve as the logical container for the devops log
resource "oci_logging_log_group" "devops_log_group" {
  compartment_id = var.compartment_ocid
  display_name   = "devops-log-group${local.resource_name_suffix}"
}

# Create log to store devops logging
resource "oci_logging_log" "devops_log" {
  display_name = "devops-log${local.resource_name_suffix}"
  log_group_id = oci_logging_log_group.devops_log_group.id
  log_type     = "SERVICE"
  configuration {
    source {
      category    = "all"
      resource    = oci_devops_project.devops_project.id
      service     = "devops"
      source_type = "OCISERVICE"
    }
    compartment_id = var.compartment_ocid
  }
  is_enabled         = true
  retention_duration = var.project_logging_config_retention_period_in_days
}
