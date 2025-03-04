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

variable "tenancy_ocid" {}
variable "compartment_ocid" {
  default = ""
}
variable "ssh_public_key" {
  default = ""
}
variable "availablity_domain_name" {
  default = ""
}
variable "home_region" {
  default = ""
}
variable "region" {
  default = ""
}

# Best to set values for below variables in terraform.tfvars under the following conditions:
# 1. If using user principal authentication.
# 2. If user needs additional policy to access the created compartment and add cloud shell, which in this
#    scenario, needs only "user_ocid" to be set up.
variable "user_ocid" {
  default = ""
}
variable "fingerprint" {
  default = ""
}

variable "project_logging_config_retention_period_in_days" {
  default = 30
}

variable "project_description" {
  default = "DevOps Project for Instance Group deployment of a Helidon Application"
}

variable "use_oke_cluster" {
  default     = true
  description = "Creates a new OKE cluster, node pool and network resources"
}

variable "deployment_target" {
  type    = string
  default = "ALL"
  validation {
    condition     = contains(["OKE", "INSTANCE", "ALL"], upper(var.deployment_target))
    error_message = "Must be either \"OKE\", \"INSTANCE\" or \"ALL\"."
  }
}
