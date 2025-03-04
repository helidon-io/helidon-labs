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

resource "oci_containerengine_cluster" "oci_oke_cluster" {
  compartment_id     = var.compartment_ocid
  kubernetes_version = (var.k8s_version == "Latest") ? local.cluster_k8s_latest_version : var.k8s_version
  name               = "oke-cluster${var.resource_name_suffix}"
  vcn_id             = oci_core_virtual_network.oke_vcn.id

  dynamic "endpoint_config" {
    for_each = var.vcn_native ? [1] : []
    content {
      is_public_ip_enabled = (var.cluster_endpoint_visibility == "Private") ? false : true
      subnet_id            = oci_core_subnet.oke_k8s_endpoint_subnet.id
    }
  }

  options {
    service_lb_subnet_ids = [oci_core_subnet.oke_lb_subnet.id]
    add_ons {
      is_kubernetes_dashboard_enabled = var.cluster_options_add_ons_is_kubernetes_dashboard_enabled
      is_tiller_enabled               = false
    }

    admission_controller_options {
      is_pod_security_policy_enabled = var.cluster_options_admission_controller_options_is_pod_security_policy_enabled
    }

    kubernetes_network_config {
      pods_cidr     = lookup(var.network_cidrs, "PODS-CIDR")                 # var.pods_cidr
      services_cidr = lookup(var.network_cidrs, "KUBERNETES-SERVICE-CIDR")   # var.services_cidr
    }
  }
  # defined_tags = var.defined_tags
}

resource "oci_containerengine_node_pool" "oci_oke_node_pool" {
  cluster_id         = oci_containerengine_cluster.oci_oke_cluster.id
  compartment_id     = var.compartment_ocid
  kubernetes_version = (var.k8s_version == "Latest") ? local.cluster_k8s_latest_version : var.k8s_version
  name               = var.node_pool_name
  node_shape         = var.node_pool_shape

  # initial_node_labels {
  #   key   = var.node_pool_initial_node_labels_key
  #   value = var.node_pool_initial_node_labels_value
  # }

  node_source_details {
    image_id                = element([for source in data.oci_containerengine_node_pool_option.oke.sources : source.image_id if length(regexall("Oracle-Linux-${var.node_pool_image_operating_system_version}-20[0-9]*.*", source.source_name)) > 0], 0)
    source_type             = "IMAGE"
    boot_volume_size_in_gbs = var.node_pool_boot_volume_size_in_gbs
  }

  ssh_public_key = var.ssh_public_key

  node_config_details {
    placement_configs {
      availability_domain = var.availability_domain_name
      subnet_id           = oci_core_subnet.oke_nodes_subnet.id
    }
    size = var.node_pool_workers     # var.node_count
  }

  dynamic "node_shape_config" {
    for_each = length(regexall("Flex", var.node_pool_shape)) > 0 ? [1] : []
    content {
      ocpus         = var.node_pool_node_shape_config_ocpus          # var.node_ocpus
      memory_in_gbs = var.node_pool_node_shape_config_memory_in_gbs  # var.node_memory
    }
  }
}

# Local kubeconfig for when using Terraform locally. Not used by Oracle Resource Manager
resource "local_file" "kubeconfig" {
  content  = data.oci_containerengine_cluster_kube_config.oke_cluster_kube_config.content
  filename = "generated/kubeconfig"
}

# Get OKE options
locals {
  cluster_k8s_latest_version   = reverse(sort(data.oci_containerengine_cluster_option.oke.kubernetes_versions))[0]
  node_pool_k8s_latest_version = reverse(sort(data.oci_containerengine_node_pool_option.oke.kubernetes_versions))[0]
}
