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

terraform {
  required_version = ">= 0.14"
}

# For more information on how to Configure the OCI Provider, refer to
# https://docs.oracle.com/en-us/iaas/Content/dev/terraform/configuring.htm#api-key-auth
provider "oci" {
  tenancy_ocid = var.tenancy_ocid
  region       = var.region

  # Uncomment below parameters and set the corresponding values if using User Principal Authentication
  # user_ocid        = "ocid1.user.oc1.."
  # fingerprint      = "1c.."
  # private_key_path = "~/.oci/oci_api_key.pem"
}
