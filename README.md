# Projet SIRIUS - PCC : Poste de Commande Centralisé
### Ce système consiste à développer une application
### simulant un Poste de Commande Centralisé (PCC)
### pour la gestion et l'automatisation d'une ligne de métro.
### Elle permettra de surveiller le trafic en temps réel.


## Auteurs

- CHALA El yasmine
- GHOMRAS ROZALIA
- SOUIDI Lyanis


## Les features branchs :
- WI : Mocker la position des trains, fait par ROZALIA :
  - dèfinir la position d'un train sur la ligne de métro en temps reel en tenant en compte 
  - des frequences de passage (heure creuse/ pointe/normale) ,les stations,  sur quel element de voie se trouve et
  - en dèduire la vitesse du train.
- WI : mocker les trajets ainsi que les horaires des trains, fait par LYANIS :
  - ainsi que gènerer les incidents. 
- WI : gèrer les itinèraires , fait par YASMINE :
  - calculer les itinèraires à partir d'un point de dèpart et d'un point d'arrivé .
  ## Livraison R3:
  -Rozalia :

    -Affichage des éléments de voie occupés par les trains, avec visualisation des trains circulant entre les stations
    -Utilisation de l'heure simulée pour la génération des trajets
    -Test unitaire validant la règle métier : un seul train autorisé par élément de voie

-Lyanis :

   -Sauvegarde et consultation des itinéraires favoris
   -Gestion de la déconnexion du compte utilisateur
   -Consultation de la liste des incidents
