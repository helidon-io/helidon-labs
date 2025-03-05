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

provider "oci" {
  fingerprint         = var.api_fingerprint
  private_key_path    = var.api_private_key_path
  region              = lookup(local.regions,var.home_region)
  tenancy_ocid        = var.tenancy_id
  user_ocid           = var.user_id
  alias               = "home"
  ignore_defined_tags = ["Oracle-Tags.CreatedBy", "Oracle-Tags.CreatedOn"]
}

provider "oci" {
  fingerprint         = var.api_fingerprint
  private_key_path    = var.api_private_key_path
  region              = lookup(local.regions,lookup(lookup(var.clusters,"c1"),"region"))
  tenancy_ocid        = var.tenancy_id
  user_ocid           = var.user_id
  alias               = "c1"
  ignore_defined_tags = ["Oracle-Tags.CreatedBy", "Oracle-Tags.CreatedOn"]
}

provider "oci" {
  fingerprint         = var.api_fingerprint
  private_key_path    = var.api_private_key_path
  region              = lookup(local.regions,lookup(lookup(var.clusters,"c2"),"region"))
  tenancy_ocid        = var.tenancy_id
  user_ocid           = var.user_id
  alias               = "c2"
  ignore_defined_tags = ["Oracle-Tags.CreatedBy", "Oracle-Tags.CreatedOn"]
}

provider "oci" {
  fingerprint         = var.api_fingerprint
  private_key_path    = var.api_private_key_path
  region              = lookup(local.regions,lookup(lookup(var.clusters,"c3"),"region"))
  tenancy_ocid        = var.tenancy_id
  user_ocid           = var.user_id
  alias               = "c3"
  ignore_defined_tags = ["Oracle-Tags.CreatedBy", "Oracle-Tags.CreatedOn"]
}