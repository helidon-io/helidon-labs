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

module "tenancy" {
  source               = "./tenancy"
  tenancy_ocid         = var.tenancy_ocid
  resource_name_suffix = local.resource_name_random_suffix
}

module "instance" {
  source                              = "./instance"
  availablity_domain_name             = var.availablity_domain_name
  ssh_public_key                      = var.ssh_public_key
  tenancy_ocid                        = var.tenancy_ocid
  compartment_ocid                    = module.tenancy.compartment_id
  resource_name_suffix                = local.resource_name_random_suffix
  instance_shape                      = var.instance_shape
  instance_ocpus                      = var.instance_ocpus
  instance_shape_config_memory_in_gbs = var.instance_shape_config_memory_in_gbs
  instance_os                         = var.instance_os
  instance_os_version                 = var.instance_os_version
}
