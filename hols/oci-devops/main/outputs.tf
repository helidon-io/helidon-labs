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

# Output private key used for ssh connection to the provisioned instance
output "generated_ssh_private_key" {
  value     = tls_private_key.public_private_key_pair.private_key_pem
  sensitive = true
}

# Output compute instance public ip
output "deployment_instance_public_ip" {
  value = length(regexall("(?i)^(INSTANCE|ALL)$", var.deployment_target)) > 0  ? module.instance-deployment[0].deployment_instance_public_ip : "No compute instance exist"
}

# Output code repository https url
output "application_code_repository_https_url" {
  value = oci_devops_repository.devops_repo.http_url
}

# Output object storage application bucket
output "application_bucket_name" {
  value = oci_objectstorage_bucket.application_bucket.name
}

# Output object storage application bucket
output "application_log_id" {
  value = oci_logging_log.application_log.id
}
