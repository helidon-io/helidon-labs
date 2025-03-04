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

output "dev" {
  value = "Made with \u2764 by Oracle Developers"
}

output "comments" {
  value = "The application URL will be unavailable for a few minutes after provisioning while the application is configured and deployed to Kubernetes"
}

# output "deploy_id" {
#   value = random_string.deploy_id.result
# }
#
# output "deployed_to_region" {
#   value = var.region
# }

output "deployed_oke_kubernetes_version" {
  value = (var.k8s_version == "Latest") ? local.cluster_k8s_latest_version : var.k8s_version
}

output "kubeconfig_for_kubectl" {
  value       = "export KUBECONFIG=./generated/kubeconfig"
  description = "If using Terraform locally, this command set KUBECONFIG environment variable to run kubectl locally"
}
