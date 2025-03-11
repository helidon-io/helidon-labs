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
  kubedns_c1 = templatefile("${path.module}/resources/kubedns.template.yaml",
    {
      int_lb_subnet_id = one(element([module.c1[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c1[*].int_lb_nsg_id], 0))
    }
  )

  kubedns_c2 = templatefile("${path.module}/resources/kubedns.template.yaml",
    {
      int_lb_subnet_id = one(element([module.c2[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c2[*].int_lb_nsg_id], 0))
    }
  )

#   kubedns_c3 = templatefile("${path.module}/resources/kubedns.template.yaml",
#     {
#       int_lb_subnet_id = one(element([module.c3[*].int_lb_subnet_id], 0))
#       int_nsg_id = one(element([module.c3[*].int_lb_nsg_id], 0))
#     }
#   )

  coredns_c1 = templatefile("${path.module}/resources/coredns-custom-c1.template.yaml", {})

  coredns_c2 = templatefile("${path.module}/resources/coredns-custom-c2.template.yaml", {})

#   coredns_c3 = templatefile("${path.module}/resources/coredns-custom-c3.template.yaml", {})
}

resource "null_resource" "coredns" {
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
    content     = local.kubedns_c1
    destination = "/home/opc/coredns/kubedns-c1.yaml"
  }
  provisioner "file" {
    content     = local.kubedns_c2
    destination = "/home/opc/coredns/kubedns-c2.yaml"
  }
#   provisioner "file" {
#     content     = local.kubedns_c3
#     destination = "/home/opc/coredns/kubedns-c3.yaml"
#   }

  provisioner "file" {
    content     = local.coredns_c1
    destination = "/home/opc/coredns/coredns-c1.yaml"
  }
  provisioner "file" {
    content     = local.coredns_c2
    destination = "/home/opc/coredns/coredns-c2.yaml"
  }
#   provisioner "file" {
#     content     = local.coredns_c3
#     destination = "/home/opc/coredns/coredns-c3.yaml"
#   }
}