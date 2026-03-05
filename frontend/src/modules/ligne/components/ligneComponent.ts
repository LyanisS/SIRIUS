
export interface Ligne {
    id: number;
    nom: string;
}

export interface ConfigCanvas {
    largeur: number;
    hauteur: number;
    margeHaut: number;
    margeBasAfter: number;
    margeGauche: number;
    margeDroite: number;
    espacementStations: number;
    rayonStation: number;
    rayonTrain: number;
}