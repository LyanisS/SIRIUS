
const URL_API ='http://localhost:8080/api';

import { Ligne } from '../components/ligneComponent.ts';

class LigneApi {
    /**
     * Récupère toutes les lignes
     */
    async obtenirToutesLignes(): Promise<Ligne[]> {
        try {
            const reponse = await fetch(`${URL_API}/lignes`);
            if (!reponse.ok) throw new Error(`HTTP ${reponse.status}`);
            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lignes:', erreur);
            throw erreur;
        }
    }

    /**
     * Récupèrer une ligne par ID
     */
    async obtenirLigneParId(id: number): Promise<Ligne> {
        try {
            const reponse = await fetch(`${URL_API}/lignes/${id}`);
            if (!reponse.ok) throw new Error(`HTTP ${reponse.status}`);
            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur ligne:', erreur);
            throw erreur;
        }
    }
}

export const ligneApi = new LigneApi();