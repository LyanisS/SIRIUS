import { Train } from "../../train/components/trainComponent.ts";
import { Ligne } from "../../ligne/components/ligneComponent.ts";

export interface Trajet {
    id: number;
    ligne: Ligne;
    train: Train;
    sens: boolean;
}

export interface Incident {
    id: number;
    message: string;
    dateDebut: Date;
    dateFin: Date;
    trajet: Trajet;
}

export interface IncidentDTO {
    id: number;
    message: string;
    dateDebut: string;
    dateFin: string;
    trajet: Trajet;
}

const toIncident = (dto: IncidentDTO): Incident => ({
    id: dto.id,
    message: dto.message,
    dateDebut: new Date(dto.dateDebut),
    dateFin: new Date(dto.dateFin),
    trajet: dto.trajet,
});

export async function getIncidents(): Promise<Incident[]> {
    try {
        const response = await fetch(
            "/api/incidents",
            { headers: { "Accept": "application/json" } }
        );
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data: IncidentDTO[] = await response.json();

        return data.map(toIncident);
    } catch (error) {
        console.error("Erreur récupération incidents", error);
        throw error;
    }
}