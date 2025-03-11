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

  all_ports = -1

  # Protocols
  # See https://www.iana.org/assignments/protocol-numbers/protocol-numbers.xhtml
  all_protocols = "all"
  icmp_protocol = 1
  tcp_protocol  = 6
  udp_protocol  = 17

  anywhere          = "0.0.0.0/0"
  rule_type_nsg     = "NETWORK_SECURITY_GROUP"
  rule_type_cidr    = "CIDR_BLOCK"
  rule_type_service = "SERVICE_CIDR_BLOCK"

  bastion_ip = one(element([module.c1[*].bastion_public_ip], 0))

  operator_ip = one(element([module.c1[*].operator_private_ip], 0))

  # TODO: check when is 15021 required for public
  public_lb_allowed_ports = [80, 443, 15021]

  # ports required to be opened for inter-cluster communication between for Istio
  service_mesh_ports = [15012, 15017, 15021, 15443]

  cilium_ports = {
    4240 = {
      port = 4240,
      protocol = "tcp",
      description = "cluster health checks (cilium-health)"
    }
    4244 = {
      port = 4244,
      protocol = "tcp",
      description = "Hubble server"
    }
    4245 = {
      port = 4245,
      protocol = "tcp",
      description = "Hubble Relay"
    }
    4250 = {
      port = 4250,
      protocol = "tcp",
      description = "Mutual Authentication port"
    }
    4251 = {
      port = 4251,
      protocol = "tcp",
      description = "Spire Agent health check port"
    }
    6060 = {
      port = 6060,
      protocol = "tcp",
      description = "cilium-agent pprof server"
    }
    6061 = {
      port = 6061,
      protocol = "tcp",
      description = "cilium-operator pprof server"
    }
    6062 = {
      port = 6062,
      protocol = "tcp",
      description = "Hubble Relay pprof server"
    }
    9878 = {
      port = 9878,
      protocol = "tcp",
      description = "cilium-envoy health listener"
    }
    9879 = {
      port = 9879,
      protocol = "tcp",
      description = "cilium-agent health status API"
    }
    9890 = {
      port = 9890,
      protocol = "tcp",
      description = "cilium-agent gops server"
    }
    9891 = {
      port = 9891,
      protocol = "tcp",
      description = "operator gops server"
    }
    9893 = {
      port = 9893,
      protocol = "tcp",
      description = "Hubble Relay gops server"
    }
    9901 = {
      port = 9901,
      protocol = "tcp",
      description = "cilium-envoy Admin API"
    }
    9962 = {
      port = 9962,
      protocol = "tcp",
      description = "cilium-agent Prometheus metrics"
    }
    9963 = {
      port = 9963,
      protocol = "tcp",
      description = "cilium-operator Prometheus metrics"
    }
    9964 = {
      port = 9964,
      protocol = "tcp",
      description = "cilium-envoy Prometheus metrics"
    }
    51871 = {
      port = 51871,
      protocol = "udp",
      description = "WireGuard encryption tunnel endpoint"
    }
  }

  regions = {
    # Africa
    johannesburg = "af-johannesburg-1"

    # Asia
    chuncheon   = "ap-chuncheon-1"
    hyderabad   = "ap-hyderabad-1"
    mumbai      = "ap-mumbai-1"
    osaka       = "ap-osaka-1"
    seoul       = "ap-seoul-1"
    singapore-1 = "ap-singapore-1"
    singapore-2 = "ap-singapore-2"
    tokyo = "ap-tokyo-1"

    # Europe
    amsterdam  = "eu-amsterdam-1"
    frankfurt  = "eu-frankfurt-1"
    kragujevac = "eu-kragujevac-1"
    london     = "uk-london-1"
    madrid     = "eu-madrid-1"
    marseille  = "eu-marseille-1"
    milan      = "eu-milan-1"
    newport    = "uk-cardiff-1"
    paris      = "eu-paris-1"
    stockholm  = "eu-stockholm-1"
    zurich = "eu-zurich-1"

    # Middle East
    abudhabi = "me-abudhabi-1"
    dubai    = "me-dubai-1"
    jeddah   = "me-jeddah-1"
    neom     = "me-neom-1"
    riyadh   = "me-riyadh-1"
    jerusalem = "il-jerusalem-1"

    # Oceania
    melbourne = "ap-melbourne-1"
    sydney = "ap-sydney-1"


    # South America
    bogota     = "sa-bogota-1"
    santiago   = "sa-santiago-1"
    saupaulo   = "sa-saupaulo-1"
    valparaiso = "sa-valparaiso-1"
    vinhedo = "sa-vinhedo-1"

    # North America
    ashburn   = "us-ashburn-1"
    chicago   = "us-chicago-1"
    monterrey = "mx-monterrey-1"
    montreal  = "ca-montreal-1"
    phoenix   = "us-phoenix-1"
    queretaro = "mx-queretaro-1"
    sanjose   = "us-sanjose-1"
    toronto = "ca-toronto-1"

    # US Gov FedRamp
    us-gov-ashburn = "us-langley-1"
    us-gov-phoenix = "us-luke-1"

    # US Gov DISA L5
    us-dod-east  = "us-gov-ashburn-1"
    us-dod-north = "us-gov-chicago-1"
    us-dod-west = "us-gov-phoenix-1"

    # UK Gov
    uk-gov-south = "uk-gov-london-1"
    uk-gov-west = "uk-gov-cardiff-1"

    # Australia Gov
    au-gov-cbr = "ap-dcc-canberra-1"

  }

  worker_cloud_init = [
    {
      content      = <<-EOT
    runcmd:
    - 'echo "Kernel module configuration for Istio and worker node initialization"'
    - 'modprobe br_netfilter'
    - 'modprobe nf_nat'
    - 'modprobe xt_REDIRECT'
    - 'modprobe xt_owner'
    - 'modprobe iptable_nat'
    - 'modprobe iptable_mangle'
    - 'modprobe iptable_filter'
    - '/usr/libexec/oci-growfs -y'
    - 'timedatectl set-timezone Australia/Sydney'
    - 'curl --fail -H "Authorization: Bearer Oracle" -L0 http://169.254.169.254/opc/v2/instance/metadata/oke_init_script | base64 --decode >/var/run/oke-init.sh'
    - 'bash -x /var/run/oke-init.sh'
    EOT
      content_type = "text/cloud-config",
    }
  ]
}
