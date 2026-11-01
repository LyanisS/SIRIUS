# /etc/vault-agent/agent.hcl
auto_auth {
  method "approle" {
    config = {
      role_id_file_path                   = "/etc/vault-agent/role-id"
      secret_id_file_path                 = "/etc/vault-agent/secret-id"
      remove_secret_id_file_after_reading = false
    }
  }
}

template {
  destination = "/etc/vault-agent/cert.pem"
  perms       = "0600"
  contents    = <<-EOT
    {{- with pkiCert "pki_int/issue/vault" "common_name=vault01.pcc.internal" "ttl=2160h" "private_key_format=pkcs8" -}}
    {{ .Cert }}{{ .CA }}{{ .Key }}
    {{ print .Cert "\n" .CA | writeToFile "/etc/pcc-tls/vault.crt" "vault" "vault" "0644" }}
    {{ .Key | writeToFile "/etc/pcc-tls/vault.key" "vault" "vault" "0400" }}
    {{- end -}}
  EOT
  exec {
    command = ["systemctl", "try-reload-or-restart", "vault"]
  }
}
