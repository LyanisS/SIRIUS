import {useEffect, useRef, useState} from "react";
import {calculerItineraire, getItinerairesFavoris, Itineraire, ItineraireFavori} from "../api/itineraireApi.ts";
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
    const [onSuccessAuthCallback, setOnSuccessAuthCallback] = useState<() => void>(() => {});
    const [onDismissAuthCallback, setOnDismissAuthCallback] = useState<() => void>(() => {});
    const [itinerairesFavoris, setItinerairesFavoris] = useState<ItineraireFavori[]>([]);
    const [showFavorisPopup, setShowFavorisPopup] = useState<boolean>(false);
    const [chargementFavoris, setChargementFavoris] = useState<boolean>(false);

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

    const clickBtnListeItinerairesFavoris = () => {
        if (!isAuthenticated()) {
            console.log("not authenticated");
            setOnSuccessAuthCallback(() => listerItinerairesFavorisAuthOK);
            setOnDismissAuthCallback(() => {});
            setShowAuthPopup(true);
        } else {
            console.log("authenticated");
            listerItinerairesFavorisAuthOK();
        }
    }

    const listerItinerairesFavorisAuthOK = () => {
        setShowAuthPopup(false);
        setShowFavorisPopup(true);
        setChargementFavoris(true);

        getItinerairesFavoris()
            .then(res => setItinerairesFavoris(res))
            .catch(() => setErreur("Impossible de charger les favoris"))
            .finally(() => setChargementFavoris(false));
    }

    const selectionnerFavori = (favori: ItineraireFavori) => {
        setShowFavorisPopup(false);
        setDepart(favori.stationDepart.id);
        setArrivee(favori.stationArrivee.id);
        setErreur("");
        setChargement(true);
        setItineraire(null);

        calculerItineraire(favori.stationDepart.id, favori.stationArrivee.id)
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
            setOnSuccessAuthCallback(() => enregistrerItineraireFavoriAuthOK);
            setOnDismissAuthCallback(() => enregistrerItineraireFavoriAuthFail);
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

            <button onClick={clickBtnListeItinerairesFavoris} className="bg-blue-950 px-2.5 py-1 rounded-lg text-white">Mes itinéraires favoris</button>

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
                onSuccess={onSuccessAuthCallback}
                onDismiss={onDismissAuthCallback}
            />}

            {showFavorisPopup && (
                <div
                    className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
                    onClick={() => setShowFavorisPopup(false)}
                >
                    <div
                        className="bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 max-h-[70vh] flex flex-col overflow-hidden"
                        onClick={e => e.stopPropagation()}
                    >
                        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-200">
                            <h3 className="text-base font-semibold m-0">Mes itinéraires favoris</h3>
                            <button
                                className="text-gray-400 hover:text-gray-700 hover:bg-gray-100 rounded-md px-2 py-1 transition-colors"
                                onClick={() => setShowFavorisPopup(false)}
                            >
                                ✕
                            </button>
                        </div>

                        {chargementFavoris && (
                            <p className="text-gray-500 text-sm text-center py-6">Chargement…</p>
                        )}

                        {!chargementFavoris && itinerairesFavoris.length === 0 && (
                            <p className="text-gray-500 text-sm text-center py-6">Aucun itinéraire favori enregistré.</p>
                        )}

                        {!chargementFavoris && itinerairesFavoris.length > 0 && (
                            <ul className="list-none m-0 p-0 overflow-y-auto divide-y divide-gray-100">
                                {itinerairesFavoris.map(favori => (
                                    <li key={favori.id}>
                                        <button
                                            className="w-full flex items-center justify-between gap-4 px-5 py-3 text-left hover:bg-blue-50 transition-colors"
                                            onClick={() => selectionnerFavori(favori)}
                                        >
                                            <span className="font-medium text-gray-900 text-sm">
                                                {favori.stationDepart.nom} → {favori.stationArrivee.nom}
                                            </span>
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}