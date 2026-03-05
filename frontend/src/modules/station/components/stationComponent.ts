
export interface Station {
  id: number;
  nom: string;
}

export interface LigneStation {
  id: number;
  ordre: number;
  station: Station;
  ligne: {
    id: number;
    nom: string;
  };
}
