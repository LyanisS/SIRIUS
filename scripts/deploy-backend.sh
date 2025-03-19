#!/bin/bash

# Variables
ssh_key="$(pwd)/key"
backend_vm="pcc-backend.e.lyanis.net"
jar_file="./xmart-city-backend/target/xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar"

# 1-  Construire le package Maven
echo " Construction du package Maven.."
mvn package

# 2- Copier le JAR sur la VM Backend
echo "Transfert du JAR vers la VM Backend.."
scp -i "$ssh_key" "$jar_file" "$backend_vm":backend.jar

# 3- Lancer le backend sur la VM
echo "Lancement du backend sur la VM..."
ssh -i "$ssh_key" "$backend_vm" << EOF
    # Lancer le backend et enregistrer les logs
     java -jar backend.jar > backend.log 2>&1 &
    echo "Backend lancé avec succès."
EOF