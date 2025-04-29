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

locals {
  resource_name_suffix        = "-helidon_oci_basic_setup"
  resource_name_random_suffix = "${local.resource_name_suffix}-${random_string.random_value.result}"
}

#random id generation
resource "random_string" "random_value" {
  length  = 4
  special = false
}
