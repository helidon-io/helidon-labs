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

# Tenancy
tenancy_ocid = "ocid1.tenancy.oc1.."

# Region - Set home_region variable only if it is different from region. Only used when running scripts under init.
#          If home_region is not set, region variable will also be considered as the home_region in init.
# home_region = ""
region = "us-ashburn-1"

# Compartment - Will only be used in scripts under main. Fill this up with the compartment id created in init or
#               an already existing compartment that you wish to use for this demo.
compartment_ocid = "ocid1.compartment.oc1.."

# Set values for below variables only under the following conditions:
# 1. If using user principal authentication. Set the proper user credentials and uncomment corresponding provider
#    parameters in providers.tf.
# 2. If user needs additional policy to access the created compartment and cloud shell, which in this scenario, needs
#    only "user_ocid" to be set up.
#
user_ocid        = "ocid1.user.oc1.."
# fingerprint      = "1c.."
# private_key_path = "~/.oci/oci_api_key.pem"

# Deployment target destination. Allowed values are the following (not case sensitive):
# 1. INSTANCE - Deploys to a provisioned instance
# 2. OKE      - Deploys to a provisioned OKE Cluster
# 3. ALL      - Deploys to both OKE and INSTANCE.
# Default is ALL
deployment_target = "ALL"
