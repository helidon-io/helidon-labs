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

# Provisions a compute instance that will be used as the deployment target for OCI DevOps service
resource "oci_core_instance" "compute_instance" {
  availability_domain = var.availablity_domain_name
  compartment_id      = var.compartment_ocid
  display_name        = "instance${var.resource_name_suffix}"
  shape               = var.instance_shape
  fault_domain        = "FAULT-DOMAIN-1"

  shape_config {
    ocpus         = var.instance_ocpus
    memory_in_gbs = var.instance_shape_config_memory_in_gbs
  }

  metadata = {
    ssh_authorized_keys = var.ssh_public_key
    user_data           = base64encode(file("./instance/cloud_init"))
  }

  create_vnic_details {
    subnet_id                 = oci_core_subnet.subnet.id
    display_name              = "primaryvnic${var.resource_name_suffix}"
    assign_public_ip          = true
    assign_private_dns_record = true
  }

  source_details {
    source_type             = "image"
    source_id               = lookup(data.oci_core_images.compute_instance_images.images[0], "id")
    boot_volume_size_in_gbs = "50"
  }

  timeouts {
    create = "60m"
  }
}
