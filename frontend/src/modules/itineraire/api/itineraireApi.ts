import {Station} from "../../station/components/stationComponent.ts";
import {Ligne} from "../../ligne/components/ligneComponent.ts";
import {getToken} from "../../auth/api/authApi.ts";

export interface EtapeItineraire {
    station: Station;
    ligne: Ligne;
}

export interface Itineraire {
    stationDepart: Station;
    stationArrivee: Station;
    etapes: EtapeItineraire[];
    nombreChangements: number;
    nombreStations: number;
}

export interface ItineraireFavori {
    id: number;
    date: Date;
    depart: boolean;
    stationDepart: Station;
    stationArrivee: Station;
}

/**
 * Calcule un intinéraire
 */
export async function calculerItineraire(depart: number, arrivee: number): Promise<Itineraire> {
    try {
        const response = await fetch(
            `/api/itineraires/calculer?depart=${depart}&arrivee=${arrivee}&arrivee=${arrivee}`,
            { headers: { Accept: "application/json" } }
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error('Erreur calcul itineraire', error);
        throw error;
    }
}

/**
 * Enregistre un itinéraire en favori
 */
export async function enregistrerItineraireFavori(depart: number, arrivee: number): Promise<ItineraireFavori> {
    try {
        const response = await fetch(
            `/api/itineraires/favoris`,
            {
                method: 'POST',
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${getToken()}`
                },
                body: JSON.stringify({ stationDepartId: depart, stationArriveeId: arrivee }),
            }
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error('Erreur enregistrement itineraire favori', error);
        throw error;
    }
}