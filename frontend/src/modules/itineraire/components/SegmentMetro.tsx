import {useState} from "react";
import BadgeLigne from "./BadgeLigne.tsx"
import {Ligne} from "../../ligne/components/ligneComponent.ts";
import {Station} from "../../station/components/stationComponent.ts";
import {CLASSE_LIGNE} from "../views/CalculItineraireView.tsx";

export interface Segment {
    ligne: Ligne;
    stations: Station[]
}

interface SegmentMetroProps {
    segment: Segment;
    isLast: boolean;
}

export default function SegmentMetro({ segment, isLast }: SegmentMetroProps) {

    const [ouvert, setOuvert] = useState(false);

    const classe = CLASSE_LIGNE[segment.ligne.nom] || "";

    const stationDepart = segment.stations[0];
    const stationsIntermediaires = segment.stations.slice(1);

    return (

        <div className="tl-etape">

            <div className="tl-track">
                <div className={`tl-dot ${classe}`}/>
                {!isLast && <div className={`tl-line ${classe}`}/>}
            </div>

            <div className="tl-content">

                <div className="tl-station-nom">
                    {stationDepart.nom}
                </div>

                {segment.ligne && (
                    <BadgeLigne ligne={segment.ligne}/>
                )}

                {stationsIntermediaires.length > 0 && (

                    <div className="tl-accordion">

                        <button
                            className="tl-accordion-btn"
                            onClick={() => setOuvert(prev => !prev)}
                        >
                            {stationsIntermediaires.length} arrêts
                            <span className={`tl-chevron ${ouvert ? "ouvert" : ""}`}>
                                ›
                            </span>
                        </button>

                        {ouvert && (
                            <ul className={`tl-stations-list ${classe}`}>

                                {stationsIntermediaires.map((station, index) => (
                                    <li key={index}>
                                        <span className={`tl-mini-dot ${classe}`}/>
                                        {station.nom}
                                    </li>
                                ))}

                            </ul>
                        )}

                    </div>

                )}

            </div>

        </div>
    );
}