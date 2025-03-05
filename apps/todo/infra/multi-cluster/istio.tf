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
  install_istio_template = templatefile("${path.module}/scripts/install_istio.tftpl", {
    version = var.istio_version
  })

  istio_c1 = templatefile("${path.module}/resources/istio.template.yaml",
    {
      mesh_id      = var.istio_mesh_id
      cluster      = "c1"
      mesh_network = "c1"
      pub_nsg_id = one(element([module.c1[*].pub_lb_nsg_id], 0))
      int_lb_subnet_id = one(element([module.c1[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c1[*].int_lb_nsg_id], 0))
    }
  )

  istio_c2 = templatefile("${path.module}/resources/istio.template.yaml",
    {
      mesh_id      = var.istio_mesh_id
      cluster      = "c2"
      mesh_network = "c2"
      pub_nsg_id = one(element([module.c2[*].pub_lb_nsg_id], 0))
      int_lb_subnet_id = one(element([module.c2[*].int_lb_subnet_id], 0))
      int_nsg_id = one(element([module.c2[*].int_lb_nsg_id], 0))
    }
  )

#   istio_c3 = templatefile("${path.module}/resources/istio.template.yaml",
#     {
#       mesh_id      = var.istio_mesh_id
#       cluster      = "c3"
#       mesh_network = "c3"
#       pub_nsg_id = one(element([module.c3[*].pub_lb_nsg_id], 0))
#       int_lb_subnet_id = one(element([module.c3[*].int_lb_subnet_id], 0))
#       int_nsg_id = one(element([module.c3[*].int_lb_nsg_id], 0))
#     }
#   )

  istio_east_west_gateway_template = templatefile("${path.module}/resources/east-west-gateway.template.yaml", {})
}

resource "null_resource" "istio" {
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
    content     = local.istio_c1
    destination = "/home/opc/istio/istio-c1.yaml"
  }

  provisioner "file" {
    content     = local.istio_c2
    destination = "/home/opc/istio/istio-c2.yaml"
  }

#   provisioner "file" {
#     content     = local.istio_c3
#     destination = "/home/opc/istio/istio-c3.yaml"
#   }

  provisioner "file" {
    content = local.install_istio_template
    destination = "/home/opc/istio/install_istio.sh"
  }

  provisioner "file" {
    content = local.istio_east_west_gateway_template
    destination = "/home/opc/istio/east-west-gateway.yaml"
  }
}
