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

  all_cluster_ids = merge(
    { c1 = one(element([module.c1[*].cluster_id], 0)) },
    { c2 = one(element([module.c2[*].cluster_id], 0)) },
    #     { c3 = one(element([module.c3[*].cluster_id], 0)) },
  )

  kubeconfig_templates = {
    for cluster_name, cluster_id in local.all_cluster_ids :
    cluster_name => templatefile("${path.module}/scripts/generate_kubeconfig.tftpl",
      {
        cluster_id = cluster_id
        endpoint   = var.oke_control_plane == "public" ? "PUBLIC_ENDPOINT" : "PRIVATE_ENDPOINT"
        region = lookup(local.regions, lookup(lookup(var.clusters, cluster_name), "region"))
      }
    )
  }

  set_credentials_templates = {
    for cluster_name, cluster_id in local.all_cluster_ids :
    cluster_name => templatefile("${path.module}/scripts/kubeconfig_set_credentials.tftpl",
      {
        cluster_id = cluster_id
        cluster_id_11 = substr(cluster_id, (length(cluster_id) - 11), length(cluster_id))
        region = lookup(local.regions, lookup(lookup(var.clusters, cluster_name), "region"))
      }
    )
  }

  set_alias_templates = {
    for cluster_name, cluster_id in local.all_cluster_ids :
    cluster_name => templatefile("${path.module}/scripts/set_alias.tftpl",
      {
        cluster = cluster_name
        cluster_id_11 = substr(cluster_id, (length(cluster_id) - 11), length(cluster_id))
      }
    )
  }

  token_helper_template = templatefile("${path.module}/scripts/token_helper.tftpl", {})

  tools_template = templatefile("${path.module}/scripts/tools.tftpl", {
    istio_version      = var.istio_version
    CLI_ARCH           = "amd64"
  })

}
