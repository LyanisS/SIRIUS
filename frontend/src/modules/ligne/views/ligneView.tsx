import React, { useEffect, useState } from 'react';
import { Ligne, ConfigCanvas } from '../components/ligneComponent.ts';
import { LigneStation } from '../../station/components/stationComponent.ts';
import { Train } from '../../train/components/trainComponent.ts';
import { ligneApi } from '../api/ligneApi';
import { stationApi } from '../../station/api/stationApi';
import { trainApi } from '../../train/api/trainApi';
import VoiesComponent from '../components/voiesComponent.tsx';

interface LigneViewProps {
    ligneId: number;
    configCanvas?: Partial<ConfigCanvas>;
    onTrainSelectionne?: (train: Train) => void;
}

export const LigneView: React.FC<LigneViewProps> = (props) => {

    // récupérer les données passées au composant
    const ligneId = props.ligneId;
    const configCanvas = props.configCanvas;
    const onTrainSelectionne = props.onTrainSelectionne;

    const [ligne, setLigne] = useState<Ligne | null>(null);
    const [stationsDirect, setStationsDirect] = useState<LigneStation[ ]>([ ]);
    const [stationsIndirect, setStationsIndirect] = useState<LigneStation[ ]>([ ]);
    const [trains, setTrains] = useState<Train[]>([]);
    const [trainSelectionne, setTrainSelectionne] = useState<Train | null>(null);
    const [chargement, setChargement] = useState(true);
    const [erreur, setErreur] = useState<string | null>(null);

    // Configuration du canvas
    const config: ConfigCanvas = {
        largeur: 1200,
        hauteur: 350,
        margeHaut: 50,
        margeBasAfter: 50,
        margeGauche: 20,
        margeDroite: 20,
        espacementStations: 60,
        rayonStation: 6,
        rayonTrain: 12,
        ...configCanvas,
    };

    useEffect(function() {

        const charger = async function() {
            try {
                setChargement(true);
                const [ligneData, stationsD, stationsI] = await Promise.all([
                    ligneApi.obtenirLigneParId(ligneId),
                    stationApi.obtenirStationsDirect(ligneId),
                    stationApi.obtenirStationsIndirect(ligneId),
                ]);

                setLigne(ligneData);
                setStationsDirect(stationsD);
                setStationsIndirect(stationsI);

                const trainsData = await trainApi.obtenirTousLesTrains();

                const trainsDeLaLigne = [];
                for (let i = 0; i < trainsData.length; i++) {
                    const train = trainsData[i];
                    const trainAppartientALaLigne = train.position?.ligneStation?.ligne?.id === ligneId;

                    if (trainAppartientALaLigne) {
                        trainsDeLaLigne.push(train);
                    }
                }

                setTrains(trainsDeLaLigne);

            } catch (err) {
                const messageErreur = err instanceof Error ? err.message : 'Erreur réseau';
                setErreur(messageErreur);
            } finally {
                setChargement(false);
            }
        };

        charger();

        // Actualiser les trains toutes les 3 secondes
        const intervalle = setInterval(async function() {
            const trainsData = await trainApi.obtenirTousLesTrains();

            // Filtrer ppar ligne
            const trainsDeLaLigne = [];
            for (let i = 0; i < trainsData.length; i++) {
                const train = trainsData[i];
                const trainAppartientALaLigne = train.position?.ligneStation?.ligne?.id === ligneId;

                if (trainAppartientALaLigne) {
                    trainsDeLaLigne.push(train);
                }
            }

            setTrains(trainsDeLaLigne);
        }, 3000);

        return function() {
            clearInterval(intervalle);
        };

    }, [ligneId]);


    const gererTrainClique = function(train: Train) {
        setTrainSelectionne(train);

        if (onTrainSelectionne) {
            onTrainSelectionne(train);
        }
    };

    if (chargement) {
        return <div style={{ padding: '20px' }}> Veuillez patienter...</div>;
    }

    if (erreur) {
        return <div style={{ padding: '20px', color: 'red' }}> Erreur: {erreur}</div>;
    }

    const nombreTotalStations = stationsDirect.length + stationsIndirect.length;

    // Filtrer les trains par sens
    const trainsSensDirect = [];
    const trainsSensIndirect = [];

    for (let i = 0; i < trains.length; i++) {
        const train = trains[i];

        if (train.sens === true) {
            trainsSensDirect.push(train);
        } else {
            trainsSensIndirect.push(train);
        }
    }

    // l'interface
    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>

            {/* En-tête avec le nom de la ligne */}
            <div style={{ marginBottom: '20px' }}>
                <h1 style={{ margin: 0, fontSize: '24px' }}>
                    {ligne?.nom || 'Ligne'}
                </h1>
                <p style={{ margin: '5px 0', color: '#666' }}>
                    {nombreTotalStations} stations • {trains.length} train(s)
                </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

                {/* Voie sens direct  */}
                <div>
                    <VoiesComponent
                        stations={stationsDirect}
                        trains={trainsSensDirect}
                        direction="DIRECT"
                        config={config}
                        onTrainClique={gererTrainClique}
                    />
                </div>

                {/* Voie sens indirect */}
                <div>
                    <VoiesComponent
                        stations={stationsIndirect}
                        trains={trainsSensIndirect}
                        direction="INDIRECT"
                        config={config}
                        onTrainClique={gererTrainClique}
                    />
                </div>
            </div>

            {/* info sur train sélectionné */}
            {trainSelectionne && (
                <div style={{
                    marginTop: '20px',
                    padding: '15px',
                    backgroundColor: '#F0F9FF',
                    border: '2px solid #3B82F6',
                    borderRadius: '8px',
                }}>
                    <h3 style={{ margin: '0 0 10px 0' }}>
                         Train #{trainSelectionne.id}
                    </h3>

                    <p style={{ margin: '5px 0' }}>
                        <strong>Vitesse:</strong> {trainSelectionne.vitesse.toFixed(1)} km/h
                    </p>

                    <p style={{ margin: '5px 0' }}>
                        <strong>Direction:</strong> {' '}
                        {(() => {
                            const toutes = trainSelectionne.sens ? stationsDirect : stationsIndirect;
                            const debut = toutes[0]?.station.nom ?? '?';
                            const fin = toutes[toutes.length - 1]?.station.nom ?? '?';
                            return `${debut} → ${fin}`;
                        })()}
                    </p>

                    <p style={{ margin: '5px 0' }}>
                        <strong>Station:</strong> {' '}
                        {(() => {
                            const ligneStationId = trainSelectionne.position?.ligneStation?.id;
                            if (ligneStationId == null) return 'Inconnue';
                            const trouvee = [...stationsDirect, ...stationsIndirect].find(ls => ls.id === ligneStationId);
                            return trouvee?.station.nom ?? 'Inconnue';
                        })()}
                    </p>

                    <p style={{ margin: '5px 0' }}>
                        <strong>Statut:</strong> {' '}
                        {trainSelectionne.vitesse > 0 ? ' En mouvement' : ' Arrêté'}
                    </p>

                    <button
                        onClick={function() {
                            setTrainSelectionne(null);
                        }}
                        style={{
                            marginTop: '10px',
                            padding: '8px 16px',
                            backgroundColor: '#3B82F6',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                        }}
                    >
                        Fermer
                    </button>
                </div>
            )}
        </div>
    );
};

export default LigneView;