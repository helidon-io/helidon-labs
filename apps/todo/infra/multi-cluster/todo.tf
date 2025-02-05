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

  hibernate_cfg_xml = templatefile("${path.module}/resources/hibernate.cfg.template.xml", {})

  wka = templatefile("${path.module}/resources/wka.template.yaml", {})

  coherence_c1 = templatefile("${path.module}/resources/coherence-c1.template.yaml",
    {
      registry_region   = var.registry_region
      tenancy_namespace = var.tenancy_namespace
      todo_version      = var.todo_version
    }
  )
  coherence_c2 = templatefile("${path.module}/resources/coherence-c2.template.yaml",
    {
      registry_region   = var.registry_region
      tenancy_namespace = var.tenancy_namespace
      todo_version      = var.todo_version
    }
  )
  #   coherence_c3 = templatefile("${path.module}/resources/coherence-c3.template.yaml",
  #     {
  #       registry_region            = var.registry_region
  #       tenancy_namespace = var.tenancy_namespace
  #       todo_version      = var.todo_version
  #     }
  #   )
  backend = templatefile("${path.module}/resources/backend.template.yaml",
    {
      registry_region   = var.registry_region
      tenancy_namespace = var.tenancy_namespace
      todo_version      = var.todo_version
    }
  )
  frontend = templatefile("${path.module}/resources/frontend.template.yaml",
    {
      registry_region   = var.registry_region
      tenancy_namespace = var.tenancy_namespace
      todo_version      = var.todo_version
    }
  )

  frontend_vs = templatefile("${path.module}/resources/frontend-vs.template.yaml", {})

  dr_c1 = templatefile("${path.module}/resources/todo-dr-c1.template.yaml",
    {
      REGION_1 = upper(lookup(local.regions, lookup(lookup(var.clusters, "c1"), "region")))
      REGION_2 = upper(lookup(local.regions, lookup(lookup(var.clusters, "c2"), "region")))
    }
  )
  dr_c2 = templatefile("${path.module}/resources/todo-dr-c2.template.yaml",
    {
      REGION_1 = upper(lookup(local.regions, lookup(lookup(var.clusters, "c1"), "region")))
      REGION_2 = upper(lookup(local.regions, lookup(lookup(var.clusters, "c2"), "region")))
    }
  )
  #   dr_c3 = templatefile("${path.module}/resources/todo-dr-c3.template.yaml",
  #     {
  #       REGION_1      = upper(lookup(local.regions, lookup(lookup(var.clusters, "c1"), "region")))
  #       REGION_2      = upper(lookup(local.regions, lookup(lookup(var.clusters, "c2"), "region")))
  #       REGION_3      = upper(lookup(local.regions, lookup(lookup(var.clusters, "c3"), "region")))
  #     }
  #   )
  tnsnames = templatefile("${path.module}/resources/tnsnames.template.ora",
    {
      profile = lookup(var.autonomous_db, "profile")
      region_primary = lookup(local.regions,lookup(var.autonomous_db, "region_primary"))
      region_peer = lookup(local.regions,lookup(var.autonomous_db, "region_peer"))
      service_name = lookup(var.autonomous_db, "service_name")
    }
  )
}

resource "null_resource" "todo" {
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
    content     = local.tnsnames
    destination = "/home/opc/database/tnsnames.ora.ha"
  }

  provisioner "file" {
    content     = local.hibernate_cfg_xml
    destination = "/home/opc/todo/hibernate.cfg.xml"
  }

  provisioner "file" {
    content     = local.wka
    destination = "/home/opc/todo/wka.yaml"
  }

  provisioner "file" {
    content     = local.coherence_c1
    destination = "/home/opc/todo/coherence-c1.yaml"
  }

  provisioner "file" {
    content     = local.coherence_c2
    destination = "/home/opc/todo/coherence-c2.yaml"
  }

  #   provisioner "file" {
  #     content     = local.coherence_c3
  #     destination = "/home/opc/todo/coherence-c3.yaml"
  #   }

  provisioner "file" {
    content     = local.backend
    destination = "/home/opc/todo/backend.yaml"
  }

  provisioner "file" {
    content     = local.frontend
    destination = "/home/opc/todo/frontend.yaml"
  }

  provisioner "file" {
    content     = local.frontend_vs
    destination = "/home/opc/todo/frontend-vs.yaml"
  }

  provisioner "file" {
    content     = local.dr_c1
    destination = "/home/opc/todo/todo-dr-c1.yaml"
  }

  provisioner "file" {
    content     = local.dr_c2
    destination = "/home/opc/todo/todo-dr-c2.yaml"
  }

  #   provisioner "file" {
  #     content     = local.dr_c3
  #     destination = "/home/opc/todo/todo-dr-c3.yaml"
  #   }
}