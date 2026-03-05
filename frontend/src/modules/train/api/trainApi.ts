import { Train } from '../components/trainComponent.ts';

class TrainApi {
  /**
   * Récupère tous les trains
   */
  async obtenirTousLesTrains(): Promise<Train[]> {
    try {
      const reponse = await fetch(`/api/trains`);
      if (!reponse.ok) throw new Error(`HTTP ${reponse.status}`);
      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur trains:', erreur);
      throw erreur;
    }
  }
}

export const trainApi = new TrainApi();