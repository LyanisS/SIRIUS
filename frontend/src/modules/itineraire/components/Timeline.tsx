import SegmentMetro, {Segment} from "./SegmentMetro.tsx";
import {EtapeItineraire, Itineraire} from "../api/itineraireApi.ts";

function grouperParLigne(etapes: EtapeItineraire[]) {

    const segments: Segment[] = [];
    let segmentActuel: Segment|null = null;

    etapes.forEach(item => {

        const ligne = item.ligne;

        if (!segmentActuel || segmentActuel.ligne.id !== ligne.id) {

            segmentActuel = {
                ligne: ligne,
                stations: []
            };

            segments.push(segmentActuel);
        }

        segmentActuel.stations.push(item.station);

    });

    return segments;
}

interface TimelineProps {
    itineraire: Itineraire;
}

export default function Timeline({ itineraire }: TimelineProps) {

    const etapes = itineraire.etapes || [];

    /* on enlève la dernière station */
    const stationsSansDerniere = etapes.slice(0, etapes.length - 1);

    const segments = grouperParLigne(stationsSansDerniere);

    const derniereStation =
        etapes.length > 0
            ? etapes[etapes.length - 1].station
            : itineraire.stationArrivee;

    return (

        <div className="timeline">

            {segments.map((segment, index) => (

                <SegmentMetro
                    key={index}
                    segment={segment}
                    isLast={index === segments.length - 1}
                />

            ))}

            {/* station d'arrivée */}
            <div className="tl-etape">

                <div className="tl-track">
                    <div className="tl-dot arrivee" />
                </div>

                <div className="tl-content">
                    <div className="tl-station-nom">
                        {derniereStation.nom}
                    </div>
                </div>

            </div>

        </div>
    );
}