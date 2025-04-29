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

# Create VCN
resource "oci_core_virtual_network" "vcn" {
  cidr_block     = var.VCN-CIDR
  compartment_id = var.compartment_ocid
  display_name   = "vcn${var.resource_name_suffix}"
}

# Create internet gateway to allow public internet traffic
resource "oci_core_internet_gateway" "ig" {
  compartment_id = var.compartment_ocid
  display_name   = "internet-gateway${var.resource_name_suffix}"
  vcn_id         = oci_core_virtual_network.vcn.id
}

# Create route table to connect vcn to internet gateway
resource "oci_core_route_table" "rt" {
  compartment_id = var.compartment_ocid
  vcn_id         = oci_core_virtual_network.vcn.id
  display_name   = "route-table${var.resource_name_suffix}"
  route_rules {
    destination       = "0.0.0.0/0"
    network_entity_id = oci_core_internet_gateway.ig.id
  }
}

# Create security list to allow internet access, ssh access and port 8080 access
resource "oci_core_security_list" "sl" {
  compartment_id = var.compartment_ocid
  display_name   = "security-list${var.resource_name_suffix}"
  vcn_id         = oci_core_virtual_network.vcn.id

  egress_security_rules {
    destination = "0.0.0.0/0"
    protocol    = "6"
  }

  ingress_security_rules {
    protocol = "6"
    source   = "0.0.0.0/0"
    tcp_options {
      max = 22
      min = 22
    }
  }

  ingress_security_rules {
    protocol = "6"
    source   = "0.0.0.0/0"
    tcp_options {
      max = 8080
      min = 8080
    }
  }
}

# Create regional subnets in vcn
resource "oci_core_subnet" "subnet" {
  cidr_block        = var.Subnet-CIDR
  display_name      = "subnet${var.resource_name_suffix}"
  compartment_id    = var.compartment_ocid
  vcn_id            = oci_core_virtual_network.vcn.id
  dhcp_options_id   = oci_core_virtual_network.vcn.default_dhcp_options_id
  route_table_id    = oci_core_route_table.rt.id
  security_list_ids = [oci_core_security_list.sl.id]

  provisioner "local-exec" {
    command = "sleep 5"
  }
}
