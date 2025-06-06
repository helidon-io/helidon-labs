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

# module "c3" {
#
#   source  = "oracle-terraform-modules/oke/oci"
#   version = "5.2.1"
#
#   count = lookup(lookup(var.clusters, "c3"), "enabled") ? 1 : 0
#
#   home_region = lookup(local.regions, var.home_region)
#
#   region = lookup(local.regions, lookup(lookup(var.clusters, "c3"), "region"))
#
#   tenancy_id = var.tenancy_id
#
#   # general oci parameters
#   compartment_id = var.compartment_id
#
#   # ssh keys
#   ssh_private_key_path = var.ssh_private_key_path
#   ssh_public_key_path = var.ssh_public_key_path
#
#   # networking
#   create_drg       = var.oke_control_plane == "private" ? true : false
#   drg_display_name = "c3-drg"
#
#   remote_peering_connections = var.oke_control_plane == "private" ? {
#     for k, v in var.clusters : "rpc-to-${k}" => {} if k != "c3"
#   } : {}
#
#   nat_gateway_route_rules = var.oke_control_plane == "private" ? [
#     for k, v in var.clusters :
#     {
#       destination = lookup(v, "vcn")
#       destination_type  = "CIDR_BLOCK"
#       network_entity_id = "drg"
#       description       = "Routing to allow connectivity to ${title(k)} cluster"
#     } if k != "c3"
#   ] : []
#
#   vcn_cidrs = [lookup(lookup(var.clusters, "c3"), "vcn")]
#   vcn_dns_label = "c3"
#   vcn_name = "c3"
#
#   #subnets
#   subnets = {
#     bastion = { newbits = 13, netnum = 0, dns_label = "bastion" }
#     operator = { newbits = 13, netnum = 1, dns_label = "operator" }
#     cp = { newbits = 13, netnum = 2, dns_label = "cp" }
#     int_lb = { newbits = 11, netnum = 16, dns_label = "ilb" }
#     pub_lb = { newbits = 11, netnum = 17, dns_label = "plb" }
#     workers = { newbits = 2, netnum = 1, dns_label = "workers" }
#   }
#
#   # bastion host
#   create_bastion = true
#   bastion_allowed_cidrs = ["0.0.0.0/0"]
#   bastion_upgrade = false
#
#   # operator host
#   create_operator            = true
#   operator_upgrade           = false
#   create_iam_resources       = true
#   create_iam_operator_policy = "always"
#   operator_install_k9s = true
#
#   # oke cluster options
#   cluster_name            = "c3"
#   cluster_type            = var.cluster_type
#   cni_type                = var.preferred_cni
#   control_plane_is_public = var.oke_control_plane == "public"
#   control_plane_allowed_cidrs = [local.anywhere]
#   kubernetes_version      = var.kubernetes_version
#   pods_cidr = lookup(lookup(var.clusters, "c3"), "pods")
#   services_cidr = lookup(lookup(var.clusters, "c3"), "services")
#
#   # node pools
#   allow_worker_ssh_access = true
#   kubeproxy_mode          = "iptables"
#   worker_pool_mode        = "node-pool"
#   worker_pools            = var.nodepools
#   worker_cloud_init       = local.worker_cloud_init
#   worker_image_type = "oke"
#
#   # oke load balancers
#   load_balancers          = "both"
#   preferred_load_balancer = "public"
#
#   allow_rules_internal_lb = merge({
#     for p in local.service_mesh_ports :
#     format("Allow ingress to port %v from cluster c2 for Istio", p) => {
#       protocol    = local.tcp_protocol, port = p, source = lookup(lookup(var.clusters, "c2"), "vcn"),
#       source_type = local.rule_type_cidr,
#     }
#   },
#     {
#       for c in var.clusters : format("Allow TCP ingress from cluster %v for Cilium clustermesh", lookup(c, "name")) => {
#       protocol = local.tcp_protocol, port = 2379, source = lookup(c, "vcn"), source_type = local.rule_type_cidr,
#     } if lookup(c, "name") != "c3"
#     },
#     {
#       for c in var.clusters :
#       format("Allow UDP ingress from cluster %v for cross-cluster DNS lookup via NLB for Coherence WKA", lookup(c, "name"))
#       => {
#       protocol = local.udp_protocol, port = 53, source = lookup(c, "vcn"), source_type = local.rule_type_cidr,
#     } if lookup(c, "name") != "c3"
#     },
#   )
#
#   allow_rules_public_lb = merge({
#     for p in local.public_lb_allowed_ports :
#     format("Allow ingress to port %v", p) => {
#       protocol = local.tcp_protocol, port = p, source = "0.0.0.0/0", source_type = local.rule_type_cidr,
#     }
#   },
#   )
#
#   allow_rules_workers = merge(
#     {
#       for c in var.clusters :
#       format("Allow UDP ingress to workers from cluster %v for default VXLAN", lookup(c, "name")) => {
#       protocol = local.udp_protocol, port = 8472, source = lookup(c, "vcn"), source_type = local.rule_type_cidr,
#     } if lookup(c, "name") != "c3"
#     },
#   )
#
#   user_id = var.user_id
#
#   providers = {
#     oci      = oci.c3
#     oci.home = oci.home
#   }
# }
