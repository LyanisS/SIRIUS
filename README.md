# Projet SIRIUS

## Auteurs

- CHALA El yasmine
- GHOMRAS ROZALIA
- SOUIDI Lyanis

# Execution du Back sur la VM :

## Créer le package

mvn package

## Envoyer le JAR du backend

scp ./xmart-city-backend/target/xmart-zity-backend-1.0-SNAPSHOT-jar-with-dependencies.jar pcc-backend.e.lyanis.net:backend.jar

## Se connecter à la vm et lancer l'execution du backend

ssh pcc-backend.e.lyanis.net
java -jar backend.jar 2>&1 > backend.log &

## Arrêter le programme

pkill java
