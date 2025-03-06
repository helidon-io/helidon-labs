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

module "oke-deployment" {
  count                    = length(regexall("(?i)^(OKE|ALL)$", var.deployment_target)) > 0 ? 1 : 0
  source                   = "./oke"
  region                   = var.region
  tenancy_namespace        = data.oci_objectstorage_namespace.object_storage_namespace.namespace
  availability_domain_name = var.availablity_domain_name == "" ? data.oci_identity_availability_domains.ads.availability_domains[0]["name"] : var.availablity_domain_name
  compartment_ocid         = var.compartment_ocid
  artifact_repository_id   = oci_artifacts_repository.artifact_repo.id
  devops_project_id        = oci_devops_project.devops_project.id
  devops_repo_name         = oci_devops_repository.devops_repo.name
  devops_repo_id           = oci_devops_repository.devops_repo.id
  devops_repo_http_url     = oci_devops_repository.devops_repo.http_url
  k8s_version              = "Latest"
  node_pool_workers        = 1
  resource_name_suffix     = local.resource_name_suffix
  ssh_public_key           = var.ssh_public_key == "" ? tls_private_key.public_private_key_pair.public_key_openssh : var.ssh_public_key
}
