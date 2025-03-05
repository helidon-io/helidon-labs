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

  cilium_delete_pods_template = templatefile("${path.module}/scripts/cilium_delete_pods.tftpl", {})

  cilium_c1 = templatefile("${path.module}/resources/cilium.template.yaml",
    {
      cluster = "c1"
      id      = "1"
      int_lb_subnet_id = one(element([module.c1[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c1[*].int_lb_nsg_id], 0))
    }
  )

  cilium_c2 = templatefile("${path.module}/resources/cilium.template.yaml",
    {
      cluster = "c2"
      id      = "2"
      int_lb_subnet_id = one(element([module.c2[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c2[*].int_lb_nsg_id], 0))
    }
  )

#   cilium_c3 = templatefile("${path.module}/resources/cilium.template.yaml",
#     {
#       cluster = "c3"
#       id      = "3"
#       int_lb_subnet_id = one(element([module.c3[*].int_lb_subnet_id], 0))
#       int_nsg_id = one(element([module.c3[*].int_lb_nsg_id], 0))
#     }
#   )
}

resource "null_resource" "cilium" {
  depends_on = [module.c1, module.c2, null_resource.tools]
#   depends_on = [module.c1, module.c2, module.c3, null_resource.tools]
  connection {
    host    = local.operator_ip
    private_key = file(var.ssh_private_key_path)
    timeout = "40m"
    type    = "ssh"
    user    = "opc"

    bastion_host = local.bastion_ip
    bastion_user = "opc"
    bastion_private_key = file(var.ssh_private_key_path)
  }

  provisioner "file" {
    content     = local.cilium_c1
    destination = "/home/opc/cilium/cilium-c1.yaml"
  }

  provisioner "file" {
    content     = local.cilium_c2
    destination = "/home/opc/cilium/cilium-c2.yaml"
  }
#   provisioner "file" {
#     content     = local.cilium_c3
#     destination = "/home/opc/cilium/cilium-c3.yaml"
#   }
}