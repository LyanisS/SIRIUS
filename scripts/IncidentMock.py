#!/usr/bin/env python3

import requests
import random
import sys
import argparse
from datetime import datetime, timedelta

# Configuration
API_BASE_URL = "http://localhost:8080/api"

MOTIFS_INCIDENTS = [
    "Problème de signalisation",
    "Conditions météorologiques difficiles",
    "Perturbation du trafic",
    "Incident voyageur",
    "Obstacle sur la voie",
    "Panne de courant",
    "Défaillance du matériel roulant",
    "Régulation du trafic",
    "Problème de porte",
    "Colis suspect signalé"
]

# Délais de résoluton du problème (entre 5 min et 2h)
RESOLUTION_DELAY_RANGE = (5, 120)

def get_active_trajets(api_base_url):
    """Récupère les trajets en cours via l'API"""
    try:
        response = requests.get(f"{api_base_url}/trajets?statut=actif")
        response.raise_for_status()
        return response.json()
    except Exception as e:
        print(f"Erreur lors de la récupération des trajets: {e}")
        return []

def create_incident(api_base_url, trajet_id, message):
    """Créée un incident via l'API"""

    resolution_delay_minutes = random.randint(*RESOLUTION_DELAY_RANGE)
    date_fin = datetime.now() + timedelta(minutes=resolution_delay_minutes)

    incident_data = {
        "message": message,
        "trajetId": trajet_id,
        "dateFin": date_fin.isoformat()
    }

    try:
        response = requests.post(
            f"{api_base_url}/incidents",
            json=incident_data,
            headers={"Content-Type": "application/json"}
        )
        response.raise_for_status()
        incident = response.json()
        print(f"Incident créé (id : {incident['id']}) pour le trajet {trajet_id}, fin de l'incident dans {resolution_delay_minutes} minutes avec le motif : {message}")
        return incident
    except Exception as e:
        print(f"Erreur lors de la création de l'incident : {e}")
        return None

def main():
    parser = argparse.ArgumentParser(description="Créer un incident aléatoire")
    parser.add_argument(
        "--api-url",
        default="http://localhost:8080/api"
    )
    args = parser.parse_args()

    trajets = get_active_trajets(args.api_url)

    if not trajets:
        print("Aucun trajet actif trouvé")
        sys.exit(1)

    trajet = random.choice(trajets)
    trajet_id = trajet['id']
    ligne_nom = trajet['ligne']['nom']

    message = random.choice(MOTIFS_INCIDENTS)

    incident = create_incident(args.api_url, trajet_id, message)

    if incident:
        print(f"Incident créé sur la {ligne_nom}")
        sys.exit(0)
    else:
        print("Échec de la création de l'incident")
        sys.exit(1)


if __name__ == "__main__":
    main()