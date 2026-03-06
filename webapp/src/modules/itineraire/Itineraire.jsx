import { useEffect, useState } from "react";
import "./Itineraire.css";
import { recupererStations, calculerItineraire } from "../../api/itineraire.api";

/* nom ligne = classe CSS couleur */
const CLASSE_LIGNE = {
    "Ligne 1": "ligne-1",
    "Ligne 8": "ligne-8"
};

/* petit emoji qui affiche la ligne metro (M1/M8) */
function BadgeLigne({ ligne }) {

    if (!ligne) return null;

    const numero = ligne.replace("Ligne ", "");
    const classe = CLASSE_LIGNE[ligne] || "";

    return (
        <span className={`tl-badge ${classe}`}>
            <span className="badge-m">M</span>
            {numero}
        </span>
    );
}

function grouperParLigne(details) {

    const segments = [];
    let segmentActuel = null;

    details.forEach(item => {

        const ligne = item.ligne;

        if (!segmentActuel || segmentActuel.ligne !== ligne) {

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
/
function SegmentMetro({ segment, isLast }) {

    const [ouvert, setOuvert] = useState(false);

    const classe = CLASSE_LIGNE[segment.ligne] || "";

    const stationDepart = segment.stations[0];
    const stationsIntermediaires = segment.stations.slice(1);

    return (

        <div className="tl-etape">

            <div className="tl-track">
                <div className={`tl-dot ${classe}`} />
                {!isLast && <div className={`tl-line ${classe}`} />}
            </div>

            <div className="tl-content">

                <div className="tl-station-nom">
                    {stationDepart}
                </div>

                {segment.ligne && (
                    <BadgeLigne ligne={segment.ligne} />
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
                                        <span className={`tl-mini-dot ${classe}`} />
                                        {station}
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

/* timeline du trajet */
function Timeline({ itineraire }) {

    const details = itineraire.details || [];

    /* on enlève la dernière station */
    const stationsSansDerniere = details.slice(0, details.length - 1);

    const segments = grouperParLigne(stationsSansDerniere);

    const derniereStation =
        details.length > 0
            ? details[details.length - 1].station
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
                        {derniereStation}
                    </div>
                </div>

            </div>

        </div>
    );
}

/* composant principal de la page itineraire */
function Itineraire() {

    const [stations, setStations] = useState([]);
    const [depart, setDepart] = useState("");
    const [arrivee, setArrivee] = useState("");
    const [itineraire, setItineraire] = useState(null);
    const [erreur, setErreur] = useState("");
    const [chargement, setChargement] = useState(false);

    /* chargement des stations*/
    useEffect(() => {

        recupererStations()
            .then(res => setStations(res.data))
            .catch(() => setErreur("Impossible de charger les stations"));

    }, []);

    const lancerCalcul = () => {

        if (!depart || !arrivee) {
            setErreur("Choisir un départ et une destination");
            return;
        }

        if (depart === arrivee) {
            setErreur("Départ et arrivée identiques");
            return;
        }

        setErreur("");
        setChargement(true);
        setItineraire(null);

        calculerItineraire(depart, arrivee)
            .then(res => setItineraire(res.data))
            .catch(() => setErreur("Erreur pendant le calcul"))
            .finally(() => setChargement(false));
    };

    return (

        <div className="itineraire-container">

            <h2>Calculer un itinéraire</h2>

            {erreur && <p className="erreur">{erreur}</p>}

            <label>Station de départ</label>

            <select value={depart} onChange={e => setDepart(e.target.value)}>
                <option value="">Choisir</option>

                {stations.map(station => (
                    <option key={station.id} value={station.id}>
                        {station.nom}
                    </option>
                ))}
            </select>

            <label>Station d'arrivée</label>

            <select value={arrivee} onChange={e => setArrivee(e.target.value)}>
                <option value="">Choisir</option>

                {stations.map(station => (
                    <option key={station.id} value={station.id}>
                        {station.nom}
                    </option>
                ))}
            </select>

            <button
                className="btn-calculer"
                onClick={lancerCalcul}
            >
                Calculer
            </button>

            {chargement && <p>Calcul en cours…</p>}

            {itineraire && (

                <div className="resultat">

                    <div className="resultat-header">

                        <h3>Résultat</h3>

                        <div className="resultat-trajet">
                            {itineraire.stationDepart} → {itineraire.stationArrivee}
                        </div>

                        <div className="resultat-infos">
                            <span>🚇 {itineraire.nombreStations} stations</span>
                            <span>
                                🔄 {itineraire.nombreChangements} correspondance
                                {itineraire.nombreChangements > 1 ? "s" : ""}
                            </span>
                        </div>

                    </div>

                    <Timeline itineraire={itineraire} />

                </div>

            )}

        </div>
    );
}

export default Itineraire;