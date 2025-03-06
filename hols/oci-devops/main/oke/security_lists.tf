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
  http_port_number                        = "80"
  https_port_number                       = "443"
  application_port_number                 = "8080"
  k8s_api_endpoint_port_number            = "6443"
  k8s_worker_to_control_plane_port_number = "12250"
  k8s_kube_proxy_port_number              = "10256"
  k8s_node_port_low_number                = "27500"
  k8s_node_port_high_number               = "32767"
  ssh_port_number                         = "22"
  tcp_protocol_number                     = "6"
  icmp_protocol_number                    = "1"
  all_protocols                           = "all"
}

resource "oci_core_security_list" "oke_nodes_security_list" {
  compartment_id = var.compartment_ocid
  display_name   = "oke-nodes-wkr-seclist${var.resource_name_suffix}"
  vcn_id         = oci_core_virtual_network.oke_vcn.id

  # Ingresses
  ingress_security_rules {
    description = "Allow pods on one worker node to communicate with pods on other worker nodes"
    source      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.all_protocols
    stateless   = false
  }
  ingress_security_rules {
    description = "Inbound SSH traffic to worker nodes"
    source      = lookup(var.network_cidrs, (var.cluster_workers_visibility == "Private") ? "VCN-CIDR" : "ALL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.ssh_port_number
      min = local.ssh_port_number
    }
  }
  ingress_security_rules {
    description = "TCP access from Kubernetes Control Plane"
    source      = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false
  }
  ingress_security_rules {
    description = "Path discovery"
    source      = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.icmp_protocol_number
    stateless   = false

    icmp_options {
      type = "3"
      code = "4"
    }
  }
  ingress_security_rules {
    description = "Inbound access from LB to kube-proxy"
    source      = lookup(var.network_cidrs, "LB-SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.k8s_kube_proxy_port_number
      min = local.k8s_kube_proxy_port_number
    }
  }
  ingress_security_rules {
    description = "Inbound access from LB to a Node Port"
    source      = lookup(var.network_cidrs, "LB-SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.k8s_node_port_high_number
      min = local.k8s_node_port_low_number
    }
  }


  # Egresses
  egress_security_rules {
    description      = "Allow pods on one worker node to communicate with pods on other worker nodes"
    destination      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.all_protocols
    stateless        = false
  }
  egress_security_rules {
    description      = "Worker Nodes access to Internet"
    destination      = lookup(var.network_cidrs, "ALL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.all_protocols
    stateless        = false
  }
  egress_security_rules {
    description      = "Allow nodes to communicate with OKE to ensure correct start-up and continued functioning"
    destination      = lookup(data.oci_core_services.all_services.services[0], "cidr_block")
    destination_type = "SERVICE_CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.https_port_number
      min = local.https_port_number
    }
  }
  egress_security_rules {
    description      = "ICMP Access from Kubernetes Control Plane"
    destination      = lookup(var.network_cidrs, "ALL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.icmp_protocol_number
    stateless        = false

    icmp_options {
      type = "3"
      code = "4"
    }
  }
  egress_security_rules {
    description      = "Access to Kubernetes API Endpoint"
    destination      = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.k8s_api_endpoint_port_number
      min = local.k8s_api_endpoint_port_number
    }
  }
  egress_security_rules {
    description      = "Kubernetes worker to control plane communication"
    destination      = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.k8s_worker_to_control_plane_port_number
      min = local.k8s_worker_to_control_plane_port_number
    }
  }
  egress_security_rules {
    description      = "Path discovery"
    destination      = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.icmp_protocol_number
    stateless        = false

    icmp_options {
      type = "3"
      code = "4"
    }
  }
}

resource "oci_core_security_list" "oke_lb_security_list" {
  compartment_id = var.compartment_ocid
  display_name   = "oke-lb-seclist${var.resource_name_suffix}"
  vcn_id         = oci_core_virtual_network.oke_vcn.id

  # Ingresses

  ingress_security_rules {
    description = "Access to application port"
    source      = lookup(var.network_cidrs, "ALL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.application_port_number
      min = local.application_port_number
    }
  }

  # Egresses

  egress_security_rules {
    description      = "Access from LB to kube-proxy"
    destination      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.k8s_kube_proxy_port_number
      min = local.k8s_kube_proxy_port_number
    }
  }
  egress_security_rules {
    description      = "Access from LB to NodePort"
    destination      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.k8s_node_port_high_number
      min = local.k8s_node_port_low_number
    }
  }
}

resource "oci_core_security_list" "oke_endpoint_security_list" {
  compartment_id = var.compartment_ocid
  display_name   = "oke-k8s-api-endpoint-seclist${var.resource_name_suffix}"
  vcn_id         = oci_core_virtual_network.oke_vcn.id

  # Ingresses

  ingress_security_rules {
    description = "External access to Kubernetes API endpoint"
    source      = lookup(var.network_cidrs, (var.cluster_endpoint_visibility == "Private") ? "VCN-CIDR" : "ALL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.k8s_api_endpoint_port_number
      min = local.k8s_api_endpoint_port_number
    }
  }
  ingress_security_rules {
    description = "Kubernetes worker to Kubernetes API endpoint communication"
    source      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.k8s_api_endpoint_port_number
      min = local.k8s_api_endpoint_port_number
    }
  }
  ingress_security_rules {
    description = "Kubernetes worker to control plane communication"
    source      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.tcp_protocol_number
    stateless   = false

    tcp_options {
      max = local.k8s_worker_to_control_plane_port_number
      min = local.k8s_worker_to_control_plane_port_number
    }
  }
  ingress_security_rules {
    description = "Path discovery"
    source      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    source_type = "CIDR_BLOCK"
    protocol    = local.icmp_protocol_number
    stateless   = false

    icmp_options {
      type = "3"
      code = "4"
    }
  }

  # Egresses

  egress_security_rules {
    description      = "Allow Kubernetes Control Plane to communicate with OKE"
    destination      = lookup(data.oci_core_services.all_services.services[0], "cidr_block")
    destination_type = "SERVICE_CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false

    tcp_options {
      max = local.https_port_number
      min = local.https_port_number
    }
  }
  egress_security_rules {
    description      = "All traffic to worker nodes"
    destination      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.tcp_protocol_number
    stateless        = false
  }
  egress_security_rules {
    description      = "Path discovery"
    destination      = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
    destination_type = "CIDR_BLOCK"
    protocol         = local.icmp_protocol_number
    stateless        = false

    icmp_options {
      type = "3"
      code = "4"
    }
  }
}
