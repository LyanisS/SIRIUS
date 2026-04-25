import React, { useEffect, useRef, useState } from 'react';
import { Ligne, ConfigCanvas } from '../components/ligneComponent.ts';
import { LigneStation } from '../../station/components/stationComponent.ts';
import { Train } from '../../train/components/trainComponent.ts';
import { ligneApi } from '../api/ligneApi';
import { stationApi } from '../../station/api/stationApi';
import { trainApi } from '../../train/api/trainApi';
import { elementVoieApi } from '../../train/api/elementVoieApi';
import { ElementVoie } from '../../train/components/trainComponent.ts';
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
    const [elementVoies, setElementVoies] = useState<ElementVoie[]>([]);
    const [incidents, setIncidents] = useState<any[]>([]);
    const [chargement, setChargement] = useState(true);
    const [erreur, setErreur] = useState<string | null>(null);

    const elementVoiesRef = useRef<ElementVoie[]>([]);
    const incidentsRef = useRef<any[]>([]);
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

    const filtrerTrainsParLigne = (trainsData: Train[], evs: ElementVoie[]): Train[] => {
        const idsEvLigne = new Set(evs.map(ev => ev.id));
        return trainsData.filter(train => train.position && idsEvLigne.has(train.position.id));
    };

    useEffect(() => {
        const charger = async () => {
            try {
                setChargement(true);
                const [ligneData, stationsD, stationsI, evs, trainsData, incidentsData] = await Promise.all([
                    ligneApi.obtenirLigneParId(ligneId),
                    stationApi.obtenirStationsDirect(ligneId),
                    stationApi.obtenirStationsIndirect(ligneId),
                    elementVoieApi.obtenirEvParLigne(ligneId),
                    trainApi.obtenirTousLesTrains(),
                    fetch('/api/incidents').then(function(r) { return r.json(); })
                ]);

                setLigne(ligneData);
                setStationsDirect(stationsD);
                setStationsIndirect(stationsI);
                setElementVoies(evs);
                elementVoiesRef.current = evs;
                setIncidents(incidentsData);
                incidentsRef.current = incidentsData;
                setTrains(filtrerTrainsParLigne(trainsData, evs));

            } catch (err) {
                setErreur(err instanceof Error ? err.message : 'Erreur réseau');
            } finally {
                setChargement(false);
            }
        };

        charger();

        // Actualiser les trains toutes les 3 secondes
        const intervalle = setInterval(async function() {
            try {
                const trainsData = await trainApi.obtenirTousLesTrains();
                const incidentsResponse = await fetch('/api/incidents');
                const incidentsData = await incidentsResponse.json();

                const filtres = filtrerTrainsParLigne(trainsData, elementVoiesRef.current);
                incidentsRef.current = incidentsData;
                setIncidents(incidentsData);

                if (filtres.length > 0) {
                    setTrains(filtres);
                }
            } catch (e) {
                console.error(e);
            }
        }, 3000);

        return function() {
            clearInterval(intervalle);
        };

    }, [ligneId]);


    const gererTrainClique = function(train: Train) {
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
    const trainsSensDirect = trains.filter(t => t.sens === true);
    const trainsSensIndirect = trains.filter(t => t.sens !== true);

    return (
        <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>

            {/* En-tête avec le nom de la ligne */}
            <div style={{ marginBottom: '20px' }}>
                <h1 style={{ margin: 0, fontSize: '24px' }}>
                    {ligne?.nom || 'Ligne'}
                </h1>
                <p style={{ margin: '5px 0', color: '#666' }}>
                    {nombreTotalStations} stations • {trains.length} train(s) • {incidents.filter(i => !i.dateFin || new Date(i.dateFin) > new Date()).length} incidents actifs
                </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

                {/* Voie sens direct  */}
                <div>
                    <VoiesComponent
                        stations={stationsDirect}
                        trains={trainsSensDirect}
                        elementVoies={elementVoies}
                        direction="DIRECT"
                        config={config}
                        onTrainClique={gererTrainClique}
                        incidents={incidents}
                    />
                </div>

                {/* Voie sens indirect */}
                <div>
                    <VoiesComponent
                        stations={stationsIndirect}
                        trains={trainsSensIndirect}
                        elementVoies={elementVoies}
                        direction="INDIRECT"
                        config={config}
                        onTrainClique={gererTrainClique}
                        incidents={incidents}
                    />
                </div>
            </div>
        </div>
    );
};

export default LigneView;