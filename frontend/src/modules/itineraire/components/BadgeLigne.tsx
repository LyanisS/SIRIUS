import {CLASSE_LIGNE} from "../views/CalculItineraireView.tsx";
import {Ligne} from "../../ligne/components/ligneComponent.ts";

interface BadgeLigneProps {
    ligne: Ligne;
}

export default function BadgeLigne({ ligne }: BadgeLigneProps) {

    if (!ligne) return null;

    const numero = ligne.nom.replace("Ligne ", "");
    const classe = CLASSE_LIGNE[ligne.nom] || "";

    return (
        <span className={`tl-badge ${classe}`}>
            <span className="badge-m">M</span>
            {numero}
        </span>
    );
}