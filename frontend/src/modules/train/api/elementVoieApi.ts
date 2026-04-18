import { ElementVoie } from '../components/trainComponent.ts';

class ElementVoieApi {
  async obtenirEvParLigne(ligneId: number): Promise<ElementVoie[]> {
    try {
      const reponse = await fetch(`/api/elementVoies/ligne/${ligneId}`);
      if (!reponse.ok) throw new Error(`HTTP ${reponse.status}`);
      return await reponse.json();
    } catch (erreur) {
      console.error('nno recup des  elementVoies:', erreur);
      throw erreur;
    }
  }
}

export const elementVoieApi = new ElementVoieApi();
