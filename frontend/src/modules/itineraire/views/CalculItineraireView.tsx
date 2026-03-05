import {useEffect, useRef, useState} from "react";
import {calculerItineraire, Itineraire} from "../api/itineraireApi.ts";
import {stationApi} from '../../station/api/stationApi';
import {Station} from "../../station/components/stationComponent.ts";
import Timeline from "../components/Timeline.tsx";
import "./CalculerItineraire.css";
import {enregistrerItineraireFavori} from "../api/itineraireApi.ts";
import AuthPopup from "../../auth/components/AuthPopup.tsx";
import {isAuthenticated} from "../../auth/api/authApi.ts";

/* nom ligne = classe CSS couleur */
export const CLASSE_LIGNE: Record<string, string> = {
    "Ligne 1": "ligne-1",
    "Ligne 8": "ligne-8"
};

export default function CalculItineraireView() {
    const [stations, setStations] = useState<Station[]>([]);
    const [depart, setDepart] = useState<number|"">("");
    const [arrivee, setArrivee] = useState<number|"">("");
    const [itineraire, setItineraire] = useState<Itineraire|null>(null);
    const [erreur, setErreur] = useState<string>("");
    const [chargement, setChargement] = useState<boolean>(false);
    const enregistrerItineraireBtnRef = useRef<HTMLButtonElement>(null);
    const [showAuthPopup, setShowAuthPopup] = useState<boolean>(false);

    /* chargement des stations*/
    useEffect(() => {

        stationApi.getStations()
            .then(res => setStations(res))
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
            .then(res => setItineraire(res))
            .catch(() => setErreur("Erreur pendant le calcul"))
            .finally(() => setChargement(false));
    };

    const clickBtnItineraireFavori = () => {
        if (itineraire === null) return;
        if (enregistrerItineraireBtnRef.current === null) return;

        const button = enregistrerItineraireBtnRef.current;
        button.textContent = "Enregistrement...";
        button.className = "bg-gray-500 px-2.5 py-1 rounded-lg text-white animate-pulse";
        button.disabled = true;

        if (!isAuthenticated()) {
            setShowAuthPopup(true);
        } else {
            enregistrerItineraireFavoriAuthOK();
        }
    }

    const enregistrerItineraireFavoriAuthOK = () => {
        setShowAuthPopup(false);
        if (itineraire === null) return enregistrerItineraireFavoriAuthFail();
        if (enregistrerItineraireBtnRef.current === null) return enregistrerItineraireFavoriAuthFail();

        const button = enregistrerItineraireBtnRef.current;
        enregistrerItineraireFavori(itineraire.stationDepart.id, itineraire.stationArrivee.id).then(() => {
            button.textContent = "Enregistré";
            button.className = "bg-green-500 px-2.5 py-1 rounded-lg text-white";
        }).catch(() => {
            button.textContent = "Erreur lors de l'enregistrement";
            button.className = "bg-red-500 px-2.5 py-1 rounded-lg text-white";

            setTimeout(() => {
                button.textContent = "⭐️";
                button.className = "bg-blue-950 px-2.5 py-1 rounded-lg";
                button.disabled = false;
            }, 3000);
        })
    }

    const enregistrerItineraireFavoriAuthFail = () =>  {
        setShowAuthPopup(false);

        if (itineraire === null) return;
        if (enregistrerItineraireBtnRef.current === null) return;

        const button = enregistrerItineraireBtnRef.current;
        button.textContent = "⭐️";
        button.className = "bg-blue-950 px-2.5 py-1 rounded-lg";
        button.disabled = false;
    }

    return (

        <div className="itineraire-container">

            <h2>Calculer un itinéraire</h2>

            {erreur && <p className="erreur">{erreur}</p>}

            <label>Station de départ</label>

            <select value={depart} onChange={e => setDepart(parseInt(e.target.value))}>
                <option value="">Choisir</option>

                {stations.map(station => (
                    <option key={station.id} value={station.id}>
                        {station.nom}
                    </option>
                ))}
            </select>

            <label>Station d'arrivée</label>

            <select value={arrivee} onChange={e => setArrivee(parseInt(e.target.value))}>
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

                        <div className="flex justify-between">
                            <h3>Résultat</h3>
                            <button
                                ref={enregistrerItineraireBtnRef}
                                onClick={clickBtnItineraireFavori}
                                className="bg-blue-950 px-2.5 py-1 rounded-lg"
                            >
                                ⭐️
                            </button>
                        </div>

                        <div className="resultat-trajet">
                            {itineraire.stationDepart.nom} → {itineraire.stationArrivee.nom}
                        </div>

                        <div className="resultat-infos">
                            <span>🚇 {itineraire.nombreStations} stations</span>
                            <span>
                                🔄 {itineraire.nombreChangements} correspondance
                                {itineraire.nombreChangements > 1 ? "s" : ""}
                            </span>
                        </div>

                    </div>

                    <Timeline itineraire={itineraire}/>

                </div>

            )}

            {showAuthPopup && <AuthPopup
                message="Un compte est requis pour utiliser la fonctionnalité itinéraires favoris."
                onSuccess={enregistrerItineraireFavoriAuthOK}
                onDismiss={enregistrerItineraireFavoriAuthFail}
            />}
        </div>
    );
}