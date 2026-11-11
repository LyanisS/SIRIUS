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
    {{- with pkiCert "pki_int/issue/datalake" "common_name=datalake01.pcc.internal" "ttl=2160h" "private_key_format=pkcs8" -}}
    {{ .Cert }}{{ .CA }}{{ .Key }}
    {{ print .Cert "\n" .CA | writeToFile "/etc/pcc-tls/datalake.crt" "root" "root" "0644" }}
    {{ .Key | writeToFile "/etc/pcc-tls/datalake.key" "root" "root" "0600" }}
    {{- end -}}
  EOT
  exec {
    command = ["systemctl", "try-reload-or-restart", "nginx"]
  }
}
