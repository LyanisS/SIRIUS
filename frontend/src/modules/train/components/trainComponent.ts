
export interface Train {
  id: number;
  vitesse: number;
  dateArriveePosition: string;
  position: ElementVoie | null;
  sens: boolean | null;
}

export interface ElementVoie {
  id: number;
  longueur: number;
  elementSuivant?: ElementVoie | null;
  ligneStation?: {
    id: number;
    ordre: number;
    station: { id: number; nom: string };
    ligne: { id: number; nom: string };
  } | null;
}
