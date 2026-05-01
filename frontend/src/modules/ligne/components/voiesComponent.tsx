import React, { useEffect, useRef, useState } from 'react';
import { LigneStation } from '../../station/components/stationComponent.ts';
import { Train, ElementVoie } from '../../train/components/trainComponent.ts';
import { ConfigCanvas } from '../components/ligneComponent.ts';

interface VoiesComponentProps {
    stations: LigneStation[];
    trains: Train[];
    elementVoies: ElementVoie[];
    direction: 'DIRECT' | 'INDIRECT';
    config: ConfigCanvas;
    onTrainClique?: (train: Train) => void;
    incidents?: any[];
}

interface Popup {
    train: Train;
    x: number;
    y: number;
}

export const VoiesComponent: React.FC<VoiesComponentProps> = (props) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const [popup, setPopup] = useState<Popup | null>(null);

    const stations = props.stations;
    const trains = props.trains;
    const elementVoies = props.elementVoies;
    const direction = props.direction;
    const config = props.config;
    const onTrainClique = props.onTrainClique;
    const incidents = props.incidents || [];

    const margeHaut = 150;
    const espacementHorizontal = (config.largeur - 150) / Math.max(stations.length - 1, 1);

    // Liste des Ids de trains qui ont un incident actif
    const trainsAvecIncident = new Set<number>();
    for (let i = 0; i < incidents.length; i++) {
        const inc = incidents[i];
        const pasEncoreTermine = !inc.dateFin || new Date(inc.dateFin) > new Date();
        if (pasEncoreTermine && inc.trajet && inc.trajet.train) {
            trainsAvecIncident.add(inc.trajet.train.id);
        }
    }

    // position X du train
    const calculerXTrain = function(train: Train, positions: { x: number; id: number }[]) {
        if (!train.position) return null;


        let evTrain = null;
        for (let i = 0; i < elementVoies.length; i++) {
            if (elementVoies[i].id === train.position.id) {
                evTrain = elementVoies[i];
                break;
            }
        }
        if (!evTrain) return null;

        // Si le train est arrete sur une station
        if (evTrain.ligneStation && train.vitesse === 0) {
            for (let i = 0; i < positions.length; i++) {
                if (positions[i].id === evTrain.ligneStation.id) {
                    return positions[i].x;
                }
            }
            return null;
        }

        // Si le train est en mouvement : trouver la station d'arrivee
        let evCourant = evTrain;
        let idxArrivee = -1;

        while (idxArrivee === -1) {
            if (evCourant.ligneStation) {
                for (let i = 0; i < positions.length; i++) {
                    if (positions[i].id === evCourant.ligneStation.id) {
                        idxArrivee = i;
                        break;
                    }
                }
                break;
            }
            if (!evCourant.elementSuivant) break;

            let evSuivant = null;
            for (let i = 0; i < elementVoies.length; i++) {
                if (elementVoies[i].id === evCourant.elementSuivant.id) {
                    evSuivant = elementVoies[i];
                    break;
                }
            }
            if (!evSuivant) break;
            evCourant = evSuivant;
        }

        if (idxArrivee === -1) return null;

        const xArrivee = positions[idxArrivee].x;
        const xDepart = idxArrivee > 0 ? positions[idxArrivee - 1].x : xArrivee;

        // Milieu entre les deux stations
        return (xDepart + xArrivee) / 2;
    };

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        //le fond
        ctx.fillStyle = '#FAFAFA';
        ctx.fillRect(0, 0, config.largeur, config.hauteur);

        ctx.fillStyle = '#333';
        ctx.font = 'bold 16px Arial';
        ctx.textAlign = 'left';

        // positionner les stations
        const positions = stations.map(function(station, index) {
            return {
                x: 80 + index * espacementHorizontal,
                y: margeHaut,
                station: station.station,
                id: station.id,
                ordre: station.ordre
            };
        });

        // les voies
        ctx.strokeStyle = '#888';
        ctx.lineWidth = 4;
        ctx.beginPath();
        positions.forEach((pos, index) => {
            if (index === 0) {
                ctx.moveTo(pos.x, pos.y);
            } else {
                ctx.lineTo(pos.x, pos.y);
            }
        });
        ctx.stroke();

        // Statons
        for (let i = 0; i < positions.length; i++) {
            const pos = positions[i];

            ctx.fillStyle = '#2563EB';
            ctx.beginPath();
            ctx.arc(pos.x, pos.y, 8, 0, Math.PI * 2);
            ctx.fill();

            // Bordure blanche
            ctx.strokeStyle = '#FFF';
            ctx.lineWidth = 2;
            ctx.stroke();

            ctx.save();
            ctx.translate(pos.x, pos.y + 15);
            ctx.rotate(-Math.PI / 4);
            ctx.fillStyle = '#000';
            ctx.font = '11px Arial';
            ctx.textAlign = 'left';
            ctx.fillText(pos.station.nom, 0, 0);
            ctx.restore();
        }

        // les trains
        for (let i = 0; i < trains.length; i++) {
            const train = trains[i];
            if (!train.position) continue;

            const trainX = calculerXTrain(train, positions);
            if (trainX === null) continue;

            const trainY = margeHaut - 60;
            const aIncident = trainsAvecIncident.has(train.id);

            let couleur = '#FFA500'; // arrete
            if (aIncident) {
                couleur = '#EF4444'; // incident
            } else if (train.vitesse > 0) {
                couleur = '#10B981'; // en mouvement
            }

            // rouge clignotant si incident
            if (aIncident) {
                const pulse = Math.sin(Date.now() / 300) * 0.5 + 0.5;
                ctx.fillStyle = 'rgba(239,68,68,' + (pulse * 0.3) + ')';
                ctx.beginPath();
                ctx.arc(trainX, trainY, 26, 0, Math.PI * 2);
                ctx.fill();
            }

            // Carré du train
            ctx.fillStyle = couleur;
            ctx.fillRect(trainX - 15, trainY - 10, 30, 20);

            // bordure
            if (aIncident) {
                ctx.strokeStyle = '#B91C1C';
                ctx.lineWidth = 3;
            } else {
                ctx.strokeStyle = '#000';
                ctx.lineWidth = 2;
            }
            ctx.strokeRect(trainX - 15, trainY - 10, 30, 20);

            // identifiant train
            ctx.fillStyle = '#FFF';
            ctx.font = 'bold 10px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(`T${train.id}`, trainX, trainY + 3);

            // Ligne pointillée vers la station
            ctx.strokeStyle = '#CCC';
            ctx.lineWidth = 1;
            ctx.setLineDash([5, 5]);
            ctx.beginPath();
            ctx.moveTo(trainX, margeHaut - 10);
            ctx.lineTo(trainX, trainY + 12);
            ctx.stroke();
            ctx.setLineDash([]);
        }

        // lègende
        ctx.fillStyle = '#FFF';
        ctx.fillRect(20, config.hauteur - 40, 300, 30);
        ctx.strokeStyle = '#DDD';
        ctx.lineWidth = 1;
        ctx.strokeRect(20, config.hauteur - 40, 300, 30);

        ctx.fillStyle = '#FFA500';
        ctx.fillRect(30, config.hauteur - 30, 15, 15);
        ctx.fillStyle = '#000';
        ctx.font = '12px Arial';
        ctx.textAlign = 'left';
        ctx.fillText('Arrêté', 50, config.hauteur - 18);

        // En circulation
        ctx.fillStyle = '#10B981';
        ctx.fillRect(120, config.hauteur - 30, 15, 15);
        ctx.fillText('En route', 140, config.hauteur - 18);

        ctx.fillStyle = '#EF4444';
        ctx.fillRect(210, config.hauteur - 30, 15, 15);
        ctx.fillText('Incident', 230, config.hauteur - 18);

    }, [stations, trains, elementVoies, direction, config, incidents]);

    // Gerer le clic sur le canvas
    const gererClic = function(e: React.MouseEvent<HTMLCanvasElement>) {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const clickX = e.clientX - rect.left;
        const clickY = e.clientY - rect.top;

        const positions = stations.map(function(station, index) {
            return {
                x: 80 + index * espacementHorizontal,
                y: margeHaut,
                id: station.id,
            };
        });

        for (let i = 0; i < trains.length; i++) {
            const train = trains[i];
            if (!train.position) continue;

            const trainX = calculerXTrain(train, positions);
            if (trainX === null) continue;

            const trainY = margeHaut - 60;

            const clicDansLaLargeur = clickX >= trainX - 15 && clickX <= trainX + 15;
            const clicDansLaHauteur = clickY >= trainY - 10 && clickY <= trainY + 25;

            if (clicDansLaLargeur && clicDansLaHauteur) {
                if (popup && popup.train.id === train.id) {
                    setPopup(null);
                } else {
                    setPopup({ train: train, x: trainX, y: trainY });
                    if (onTrainClique) onTrainClique(train);
                }
                return;
            }
        }
        setPopup(null);
    };

    // Calculer la position CSS du popup
    const canvas = canvasRef.current;
    const canvasRect = canvas ? canvas.getBoundingClientRect() : null;
    const scaleX = canvasRect ? config.largeur / canvasRect.width : 1;
    const scaleY = canvasRect ? config.hauteur / canvasRect.height : 1;
    const popupCssX = popup ? popup.x / scaleX : 0;
    const popupCssY = popup ? popup.y / scaleY : 0;

    // Trouver l incident actif du train clique
    let incidentActif = null;
    if (popup) {
        for (let i = 0; i < incidents.length; i++) {
            const inc = incidents[i];
            const pasEncoreTermine = !inc.dateFin || new Date(inc.dateFin) > new Date();
            if (inc.trajet && inc.trajet.train && inc.trajet.train.id === popup.train.id && pasEncoreTermine) {
                incidentActif = inc;
                break;
            }
        }
    }

    return (
        <div style={{ position: 'relative', display: 'inline-block' }}>
            <canvas
                ref={canvasRef}
                width={config.largeur}
                height={config.hauteur}
                onClick={gererClic}
                style={{
                    border: '1px solid #DDD',
                    cursor: 'pointer',
                    backgroundColor: '#FAFAFA',
                }}
            />

            {popup && (
                <div style={{
                    position: 'absolute',
                    left: popupCssX,
                    top: popupCssY - 10,
                    transform: 'translate(-50%, -100%)',
                    backgroundColor: '#fff',
                    border: '1px solid ' + (incidentActif ? '#EF4444' : '#CBD5E1'),
                    borderRadius: '8px',
                    padding: '12px',
                    minWidth: '180px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    zIndex: 20,
                    fontSize: '13px',
                }}>
                    <button
                        onClick={function(e) { e.stopPropagation(); setPopup(null); }}
                        style={{
                            position: 'absolute', top: 6, right: 8,
                            background: 'none', border: 'none',
                            cursor: 'pointer', fontSize: '16px',
                        }}
                    >
                        x
                    </button>

                    <div style={{ fontWeight: 'bold', marginBottom: 6 }}>
                        Train {popup.train.id}
                        {incidentActif && (
                            <span style={{ color: '#EF4444', marginLeft: 6 }}>
                                ! Incident
                            </span>
                        )}
                    </div>

                    <div>Vitesse : <strong>{popup.train.vitesse.toFixed(1)} km/h</strong></div>

                    <div>
                        Statut :
                        <strong style={{ color: popup.train.vitesse > 0 ? '#10B981' : '#FFA500' }}>
                            {popup.train.vitesse > 0 ? ' En mouvement' : ' Arrete'}
                        </strong>
                    </div>

                    {incidentActif && (
                        <div style={{
                            marginTop: 8,
                            padding: '6px',
                            backgroundColor: '#FEF2F2',
                            borderRadius: 4,
                            color: '#B91C1C',
                            fontSize: '12px',
                        }}>
                            <strong>{incidentActif.message}</strong>
                            {incidentActif.dateFin && (
                                <div>
                                    Fin : {new Date(incidentActif.dateFin).toLocaleTimeString()}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default VoiesComponent;