# Projet SIRIUS - PCC : Poste de Commande Centralisé

Supervision en temps réel des lignes de métro : suivi des trains, réaction aux événements (ex. malaise voyageur → régulation du trafic), calcul des horaires et des itinéraires, et diffusion de l'information aux opérateurs, aux voyageurs et aux consoles de quai (SIEL).

## Focus sur l'architecture: 
- OPNsense sépare le réseau EPISEN de l'infrastructure (3 VLAN : INFRA, K8S, TRAINS) et sert de point d'entrée HTTPS et SSH.
- Cluster Kubernetes : Traefik (API gateway, auth OIDC), 3 IHM React (Supervision, Voyageur, SIEL) et les microservices.
- Bus et données sur 3 VM redondantes : RabbitMQ, MongoDB, Redis Sentinel.
- Services communs : Keycloak (IAM), Vault (PKI / mTLS), Graylog (logs via OpenTelemetry).
- Trains simulés qui publient leur télémétrie en MQTTS vers RabbitMQ.

## Auteurs

- GHOMRAS ROZALIA
- SOUIDI Lyanis
