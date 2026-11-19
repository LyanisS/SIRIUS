# /etc/vault-agent/agent.hcl sur chaque nœud data : remplacer __HOST__ (data01, data02…).
# Un certificat par nœud (90 jours, renouvelé à 90 % de sa durée), copié pour chaque service puis rechargé.
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
  # Copie de travail : Vault Agent y relit le certificat pour savoir quand le renouveler
  destination = "/etc/vault-agent/cert.pem"
  perms       = "0600"
  # Noms : le nœud, le nom commun des nœuds data (DNS) et le nom court (RabbitMQ entre nœuds : rabbit@__HOST__)
  contents    = <<-EOT
    {{- with pkiCert "pki_int/issue/data" "common_name=__HOST__.pcc.internal" "alt_names=data.pcc.internal,__HOST__" "ttl=2160h" "private_key_format=pkcs8" -}}
    {{ .Cert }}{{ .CA }}{{ .Key }}
    {{ print .CA "\n" (file "/usr/local/share/ca-certificates/pcc-ca.crt") | writeToFile "/etc/pcc-tls/ca.pem" "root" "root" "0644" }}
    {{ print .Cert "\n" .CA "\n" .Key | writeToFile "/etc/pcc-tls/mongodb.pem" "mongodb" "mongodb" "0400" }}
    {{ print .Cert "\n" .CA | writeToFile "/etc/pcc-tls/redis.crt" "redis" "redis" "0644" }}
    {{ .Key | writeToFile "/etc/pcc-tls/redis.key" "redis" "redis" "0400" }}
    {{ .Cert | writeToFile "/etc/pcc-tls/rabbitmq.crt" "rabbitmq" "rabbitmq" "0644" }}
    {{ .Key | writeToFile "/etc/pcc-tls/rabbitmq.key" "rabbitmq" "rabbitmq" "0400" }}
    {{- end -}}
  EOT
  # Services déjà démarrés seulement. MongoDB et Redis relisent le certificat au démarrage (un redémarrage = une
  # courte bascule si le nœud est primaire ou maître) ; RabbitMQ le relit pour les nouvelles connexions.
  exec {
    command = ["/bin/sh", "-c", "systemctl try-restart mongod redis-server redis-sentinel; systemctl is-active -q rabbitmq-server && rabbitmqctl eval 'ssl:clear_pem_cache().' >/dev/null; true"]
  }
}
