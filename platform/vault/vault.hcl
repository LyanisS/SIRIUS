# /etc/vault.d/vault.hcl
ui            = true
disable_mlock = true
api_addr      = "https://vault01.pcc.internal:8200"
cluster_addr  = "https://vault01.pcc.internal:8201"

storage "raft" {
  path    = "/opt/vault/data"
  node_id = "vault01"
}

listener "tcp" {
  address       = "0.0.0.0:8200"
  tls_cert_file = "/etc/pcc-tls/vault.crt"
  tls_key_file  = "/etc/pcc-tls/vault.key"
  x_forwarded_for_authorized_addrs  = "192.168.20.0/24"
  x_forwarded_for_reject_not_present = false
}
