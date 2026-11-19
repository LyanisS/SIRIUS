#!/usr/bin/env python3
"""
Simulateur de trains du PCC : publie en MQTT (vers RabbitMQ)
  pcc/trains/<trainId>/position  (QoS 0)
  pcc/trains/<trainId>/vitesse   (QoS 0)
  pcc/trains/<trainId>/incident  (QoS 1)
"""

import argparse
import json
import logging
import os
import random
import signal
import sys
import time
import uuid
from datetime import datetime, timedelta, timezone
from pathlib import Path

log = logging.getLogger("train-mock")

# motif : (durée min, durée max en minutes, bloquant)
MOTIFS = {
    "Problème de signalisation": (10, 60, False),
    "Conditions météorologiques difficiles": (5, 45, False),
    "Perturbation du trafic": (5, 30, False),
    "Régulation du trafic": (2, 20, False),
    "Incident voyageur": (3, 20, True),
    "Obstacle sur la voie": (5, 40, True),
    "Panne de courant": (10, 90, True),
    "Défaillance du matériel roulant": (8, 60, True),
    "Problème de porte": (2, 15, True),
    "Colis suspect signalé": (20, 90, True),
}
ESPACEMENT_M = 200  # distance de sécurité entre deux trains


class Ligne:
    def __init__(self, fichier):
        d = json.loads(Path(fichier).read_text(encoding="utf-8"))
        self.id, self.nom = d["id"], d["nom"]
        self.stations = sorted(d["stations"], key=lambda s: s["pk"])
        self.longueur = self.stations[-1]["pk"]
        self.vmax = d["vitesseMaxKmh"] / 3.6  # m/s
        self.accel, self.frein = d["accelerationMs2"], d["freinageMs2"]
        self.arret_quai, self.arret_terminus = d["arretQuaiSec"], d["arretTerminusSec"]

    def station_suivante(self, t):
        devant = [s for s in self.stations if (s["pk"] - t.pk) * t.direction > 0]
        if not devant:
            return self.stations[-1 if t.direction > 0 else 0]
        return min(devant, key=lambda s: abs(s["pk"] - t.pk))

    def station_precedente(self, t):
        derriere = [s for s in self.stations if (t.pk - s["pk"]) * t.direction >= 0]
        if not derriere:
            return self.stations[0 if t.direction > 0 else -1]
        return min(derriere, key=lambda s: abs(s["pk"] - t.pk))


class Train:
    def __init__(self, id, pk, direction):
        self.id, self.pk, self.direction = id, pk, direction  # direction : +1 aller, -1 retour
        self.v = self.a = self.attente = 0.0
        self.etat = "EN_MARCHE"  # EN_MARCHE, A_QUAI, ARRET_ESPACEMENT, ARRET_INCIDENT
        self.incident = None

    def vmax(self, ligne):
        if self.incident is None:
            return ligne.vmax
        return 0.0 if self.incident["bloquant"] else ligne.vmax / 2


class Simulation:
    def __init__(self, ligne, nb_trains, incidents_par_heure, rng):
        self.ligne, self.incidents_par_heure, self.rng = ligne, incidents_par_heure, rng
        self.horloge = datetime.now(timezone.utc)
        self.evenements = []  # (train, "OUVERT"/"RESOLU", incident) à publier
        self.trains = []
        for i in range(nb_trains):  # répartis régulièrement sur l'aller-retour
            s = i * 2 * ligne.longueur / nb_trains
            pk, direction = (s, 1) if s < ligne.longueur else (2 * ligne.longueur - s, -1)
            self.trains.append(Train(f"T{i + 1:02d}", pk, direction))

    def pas(self, dt):
        self.horloge += timedelta(seconds=dt)
        if self.rng.random() < self.incidents_par_heure * dt / 3600:
            libres = [t for t in self.trains if t.incident is None]
            if libres:
                self.declencher_incident(self.rng.choice(libres))
        for t in self.trains:
            if t.incident and self.horloge >= t.incident["fin"]:
                self.evenements.append((t, "RESOLU", t.incident))
                t.incident = None
            self.avancer(t, dt)

    def declencher_incident(self, t, motif=None):
        motif = motif or self.rng.choice(list(MOTIFS))
        dmin, dmax, bloquant = MOTIFS[motif]
        duree = timedelta(minutes=self.rng.randint(dmin, dmax))
        t.incident = {"id": str(uuid.uuid4()), "motif": motif, "bloquant": bloquant,
                      "debut": self.horloge, "fin": self.horloge + duree}
        self.evenements.append((t, "OUVERT", t.incident))
        log.info("Incident sur %s : %s", t.id, motif)

    def avancer(self, t, dt):
        L = self.ligne
        if t.etat == "A_QUAI":  # si incident bloquant
            if t.vmax(L) > 0:
                t.attente -= dt
                if t.attente <= 0:
                    t.etat = "EN_MARCHE"
            return

        # point d'arrêt : prochaine station, ou 200 m derrière le train de devant
        station = L.station_suivante(t)
        distance = abs(station["pk"] - t.pk)
        for o in self.trains:
            ecart = (o.pk - t.pk) * t.direction
            if o is not t and o.direction == t.direction and ecart > 0:
                distance = min(distance, max(ecart - ESPACEMENT_M, 0))

        vmax = t.vmax(L)
        doit_freiner = t.v ** 2 / (2 * L.frein) + t.v * dt >= distance
        if t.v > 0 and (doit_freiner or t.v > vmax):
            t.a = -L.frein
        elif t.v < vmax and not doit_freiner:
            t.a = min(L.accel, (vmax - t.v) / dt)
        else:
            t.a = 0.0
        t.v = max(0.0, t.v + t.a * dt)
        t.pk += t.direction * min(t.v * dt, distance)

        if abs(station["pk"] - t.pk) < 1:  # arrivée en station
            t.pk, t.v, t.a, t.etat = float(station["pk"]), 0.0, 0.0, "A_QUAI"
            if station in (L.stations[0], L.stations[-1]):  # terminus : demi-tour
                t.direction = 1 if station is L.stations[0] else -1
                t.attente = L.arret_terminus
            else:
                t.attente = self.rng.uniform(*L.arret_quai)
        elif t.v > 0:
            t.etat = "EN_MARCHE"
        else:
            t.etat = "ARRET_INCIDENT" if vmax == 0 else "ARRET_ESPACEMENT"


def iso(date):
    return date.isoformat(timespec="milliseconds").replace("+00:00", "Z")


def entete(sim, t, type_msg):
    return {"type": type_msg, "trainId": t.id, "ligneId": sim.ligne.id, "ts": iso(sim.horloge)}


def msg_position(sim, t):
    return entete(sim, t, "position") | {
        "pkM": round(t.pk, 1), "direction": "ALLER" if t.direction > 0 else "RETOUR",
        "stationPrecedente": sim.ligne.station_precedente(t)["id"],
        "stationSuivante": sim.ligne.station_suivante(t)["id"],
        "aQuai": t.etat == "A_QUAI", "etat": t.etat}


def msg_vitesse(sim, t):
    return entete(sim, t, "vitesse") | {
        "vitesseKmh": round(t.v * 3.6, 1), "accelerationMs2": round(t.a, 2),
        "vitesseMaxAutoriseeKmh": round(t.vmax(sim.ligne) * 3.6, 1)}


def msg_incident(sim, t, statut, inc):
    return entete(sim, t, "incident") | {
        "incidentId": inc["id"], "statut": statut, "motif": inc["motif"],
        "bloquant": inc["bloquant"], "gravite": "MAJEURE" if inc["bloquant"] else "MINEURE",
        "pkM": round(t.pk, 1), "stationProche": sim.ligne.station_precedente(t)["id"],
        "debut": iso(inc["debut"]), "finPrevue": iso(inc["fin"])}


def connecter_mqtt(args):
    import paho.mqtt.client as mqtt
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, client_id=args.client_id)
    if args.username:
        client.username_pw_set(args.username, args.password)
    if args.tls:
        # mTLS : avec un certificat client, RabbitMQ prend son CN comme nom de compte (sans mot de passe)
        client.tls_set(ca_certs=args.ca_cert, certfile=args.client_cert, keyfile=args.client_key)
    client.on_connect = lambda *a: log.info("Connexion à %s : %s", args.host, a[3])
    client.on_disconnect = lambda *a: log.warning("Déconnecté (%s), reconnexion…", a[3])
    client.connect_async(args.host, args.port, keepalive=30)  # paho se reconnecte tout seul
    client.loop_start()
    for _ in range(150):  # attend la connexion 15 s max
        if client.is_connected():
            break
        time.sleep(0.1)
    return client


def parse_args():
    env = os.environ.get
    p = argparse.ArgumentParser(description="Simulateur de trains du PCC")
    p.add_argument("--ligne", default=env("LIGNE_FILE", str(Path(__file__).with_name("ligne.json"))))
    p.add_argument("--trains", type=int, default=int(env("NB_TRAINS", 6)))
    p.add_argument("--periode", type=float, default=float(env("PERIODE_S", 1)))
    p.add_argument("--acceleration", type=float, default=float(env("ACCELERATION", 1)))
    p.add_argument("--incidents-par-heure", type=float, default=float(env("INCIDENTS_PAR_HEURE", 2)))
    p.add_argument("--duree", type=float, default=0, help="en secondes, 0 = sans fin")
    p.add_argument("--dry-run", action="store_true", help="affiche au lieu de publier")
    p.add_argument("--host", default=env("MQTT_HOST", "data.pcc.internal"))
    p.add_argument("--port", type=int, default=int(env("MQTT_PORT", 8883)))
    p.add_argument("--username", default=env("MQTT_USERNAME"))
    p.add_argument("--password", default=env("MQTT_PASSWORD"))
    p.add_argument("--client-id", default=env("MQTT_CLIENT_ID", "train-mock"))
    p.add_argument("--no-tls", dest="tls", action="store_false")
    p.add_argument("--ca-cert", default=env("MQTT_CA_CERT"))
    p.add_argument("--client-cert", default=env("MQTT_CLIENT_CERT"), help="certificat client (mTLS)")
    p.add_argument("--client-key", default=env("MQTT_CLIENT_KEY"))
    return p.parse_args()


def main():
    args = parse_args()
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    ligne = Ligne(args.ligne)
    sim = Simulation(ligne, args.trains, args.incidents_par_heure, random.Random())
    client = None if args.dry_run else connecter_mqtt(args)
    log.info("%d trains sur %s", args.trains, ligne.nom)

    def publier(t, type_msg, msg, qos=0):
        topic, texte = f"pcc/trains/{t.id}/{type_msg}", json.dumps(msg, ensure_ascii=False)
        if client:
            client.publish(topic, texte, qos=qos)
        else:
            print(topic, texte, flush=True)

    # Le certificat client n'est lu qu'à la connexion : quand cert-manager le renouvelle, on s'arrête
    # et Kubernetes relance le mock avec le nouveau
    cert_lu = os.stat(args.client_cert).st_mtime if args.client_cert else None

    signal.signal(signal.SIGTERM, lambda *_: sys.exit(0))
    nb_pas = max(1, round(args.periode * args.acceleration / 0.5))  # pas de ~0,5 s simulée
    debut = time.monotonic()
    try:
        while not args.duree or time.monotonic() - debut < args.duree:
            if cert_lu and os.stat(args.client_cert).st_mtime != cert_lu:
                log.info("Certificat client renouvelé : arrêt pour le recharger")
                break
            for _ in range(nb_pas):
                sim.pas(args.periode * args.acceleration / nb_pas)
            for t, statut, inc in sim.evenements:
                publier(t, "incident", msg_incident(sim, t, statut, inc), qos=1)
            sim.evenements.clear()
            for t in sim.trains:
                publier(t, "position", msg_position(sim, t))
                publier(t, "vitesse", msg_vitesse(sim, t))
            time.sleep(args.periode)
    except KeyboardInterrupt:
        pass
    finally:
        if client:
            client.disconnect()
            client.loop_stop()


if __name__ == "__main__":
    main()
