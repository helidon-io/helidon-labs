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

resource "null_resource" "tools" {
  depends_on = [module.c1]

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
    content     = local.token_helper_template
    destination = "/home/opc/token_helper.sh"
  }

  provisioner "file" {
    content = local.tools_template
    destination = "/home/opc/tools.sh"
  }

  provisioner "file" {
    content     = local.cilium_delete_pods_template
    destination = "/home/opc/cilium_delete_pods.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "mkdir /home/opc/bin; mv token_helper.sh /home/opc/bin; chmod +x /home/opc/bin/token_helper.sh",
      "if [ -f \"$HOME/setup.sh\" ]; then bash \"$HOME/setup.sh\";fi",
      "if [ -f \"$HOME/install_istioctl.sh\" ]; then bash \"$HOME/install_istioctl.sh\";fi",
      "if [ -f \"$HOME/install_cilium_cli.sh\" ]; then bash \"$HOME/install_cilium_cli.sh\";fi",
    ]
  }
}

resource "null_resource" "set_contexts" {
  depends_on = [module.c1, module.c2]
#   depends_on = [module.c1, module.c2, module.c3]
  for_each = local.all_cluster_ids
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
    content = lookup(local.kubeconfig_templates, each.key)
    destination = "/home/opc/generate_kubeconfig_${each.key}.sh"
  }

  provisioner "file" {
    content = lookup(local.set_credentials_templates, each.key)
    destination = "/home/opc/kubeconfig_set_credentials_${each.key}.sh"
  }

  provisioner "file" {
    content = lookup(local.set_alias_templates, each.key)
    destination = "/home/opc/set_alias_${each.key}.sh"
  }

  provisioner "remote-exec" {
    inline = [
      "if [ -f \"$HOME/generate_kubeconfig_${each.key}.sh\" ]; then bash \"$HOME/generate_kubeconfig_${each.key}.sh\";fi",
      "if [ -f \"$HOME/kubeconfig_set_credentials_${each.key}.sh\" ]; then bash \"$HOME/kubeconfig_set_credentials_${each.key}.sh\";fi",
      "if [ -f \"$HOME/set_alias_${each.key}.sh\" ]; then bash \"$HOME/set_alias_${each.key}.sh\";fi",
    ]
  }

  triggers = {
    clusters = length(var.clusters)
  }

  lifecycle {
    create_before_destroy = true
  }
}
