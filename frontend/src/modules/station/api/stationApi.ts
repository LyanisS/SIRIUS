import {LigneStation, Station} from '../components/stationComponent.ts';

class StationApi {

    /**
     * Récupère les stations d'une ligne (sens direct)
     */
    async obtenirStationsDirect(idLigne: number): Promise<LigneStation[]> {
        try {
            let reponse = await fetch(`/api/ligneStations/ligne/${idLigne}/asc`);
            const premierEssaiOK = reponse.ok;
            if (!premierEssaiOK) {
                reponse = await fetch(`/api/ligneStations/ligne/${idLigne}`);
            }
            
            if (!reponse.ok) {
                throw new Error(`Erreur HTTP ${reponse.status}`);
            }
            
            const donnees = await reponse.json();
            const donneesTriees = Array.isArray(donnees);

            if (donneesTriees) {
                // Trier les stations par ordre croissant
                const stationsTriees = donnees.sort(function(stationA, stationB) {
                    const ordreA = stationA.ordre || 0;
                    const ordreB = stationB.ordre || 0;
                    return ordreA - ordreB;
                });
                return stationsTriees;
            } else {
                return donnees;
            }

        } catch (erreur) {
            console.error('Erreur lors de la récupération des stations (sens direct):', erreur);
            throw erreur;
        }
    }

    /**
     * Récupère les stations d'une ligne (sens indirect)
     */
    async obtenirStationsIndirect(idLigne: number): Promise<LigneStation[]> {
        try {
            let reponse = await fetch(`/api/ligneStations/ligne/${idLigne}/desc`);
            const premierEssaiOK = reponse.ok;
            if (!premierEssaiOK) {
                reponse = await fetch(`/api/ligneStations/ligne/${idLigne}`);
            }

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP ${reponse.status}`);
            }

            const donnees = await reponse.json();
            const donneesTriees = Array.isArray(donnees);

            if (donneesTriees) {
                // Trier les stations par ordre décroissant
                const stationsTriees = donnees.sort(function(stationA, stationB) {
                    const ordreA = stationA.ordre || 0;
                    const ordreB = stationB.ordre || 0;
                    return ordreB - ordreA;
                });
                return stationsTriees;
            } else {
                return donnees;
            }

        } catch (erreur) {
            console.error('Erreur lors de la récupération des stations (sens indirect):', erreur);
            throw erreur;
        }
    }

    async getStations(): Promise<Station[]> {
        try {
            const response = await fetch(
                `/api/stations`,
                { headers: { Accept: "application/json" } }
            );
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return response.json();
        } catch (error) {
            console.error("Erreur stations", error);
            throw error;
        }
    }
}

export const stationApi = new StationApi();