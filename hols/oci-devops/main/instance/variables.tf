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

variable "resource_name_suffix" {
  default = ""
}

#
variable "devops_project_id" {
  default = ""
}

variable "artifact_repository_id" {
  default = ""
}

variable "devops_repo_name" {
  default = ""
}

variable "devops_repo_id" {
  default = ""
}

variable "devops_repo_http_url" {
  default = ""
}
#

variable "compartment_ocid" {
  default = ""
}

variable "private_key_path" {
  default = ""
}

# Allows provisioned compute instance to be ssh'd with corresponding private key. If empty, a public/private ssh key pair
# will be generated and private key can be extracted from the TF state.
variable "ssh_public_key" {
  default = ""
}

variable "availablity_domain_name" {
  default = ""
}
variable "VCN-CIDR" {
  default = "10.0.0.0/16"
}

variable "Subnet-CIDR" {
  default = "10.0.0.0/24"
}

variable "instance_shape" {
  description = "Instance Shape"
  default     = "VM.Standard.E4.Flex"
}

variable "instance_ocpus" {
  default = 1
}

variable "instance_shape_config_memory_in_gbs" {
  default = 16
}

variable "instance_os" {
  description = "Operating system for compute instances"
  default     = "Oracle Linux"
}

variable "linux_os_version" {
  description = "Operating system version for all Linux instances"
  default     = "8"
}
