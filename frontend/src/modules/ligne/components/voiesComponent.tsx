import React, { useEffect, useRef } from 'react';
import { LigneStation } from '../../station/components/stationComponent.ts';
import { Train } from '../../train/components/trainComponent.ts';
import { ConfigCanvas } from '../components/ligneComponent.ts';

interface VoiesComponentProps {
    stations: LigneStation[];
    trains: Train[];
    direction: 'DIRECT' | 'INDIRECT';
    config: ConfigCanvas;
    onTrainClique?: (train: Train) => void;
}

export const VoiesComponent: React.FC<VoiesComponentProps> = (props) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);

    // Récupérer les données passées au composant
    const stations = props.stations;
    const trains = props.trains;
    const direction = props.direction;
    const config = props.config;
    const onTrainClique = props.onTrainClique;

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
        const margeHaut = 150;
        const espacementHorizontal = (config.largeur - 150) / Math.max(stations.length - 1, 1);

        const positions = stations.map((station, index) => ({
            x: 80 + index * espacementHorizontal,
            y: margeHaut,
            station: station.station,
            id: station.id,
            ordre: station.ordre
        }));

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
        positions.forEach((pos) => {
            // Cercle de la station
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
        });

        trains.forEach((train) => {
            // Trouver où est le train
            const pos = positions.find(p => p.id === train.position?.ligneStation?.id);
            if (!pos) return;

            const trainX = pos.x;
            const trainY = pos.y - 60;

            const couleur = train.vitesse > 0 ? '#10B981' : '#EF4444';

            // Carré du train
            ctx.fillStyle = couleur;
            ctx.fillRect(trainX - 15, trainY - 10, 30, 20);

            // bordure noire
            ctx.strokeStyle = '#000';
            ctx.lineWidth = 2;
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
            ctx.moveTo(pos.x, pos.y - 10);
            ctx.lineTo(trainX, trainY + 12);
            ctx.stroke();
            ctx.setLineDash([]);
        });

        // lègende
        ctx.fillStyle = '#FFF';
        ctx.fillRect(20, config.hauteur - 40, 200, 30);
        ctx.strokeStyle = '#DDD';
        ctx.lineWidth = 1;
        ctx.strokeRect(20, config.hauteur - 40, 200, 30);

        // si Arrêté
        ctx.fillStyle = '#EF4444';
        ctx.fillRect(30, config.hauteur - 30, 15, 15);
        ctx.fillStyle = '#000';
        ctx.font = '12px Arial';
        ctx.textAlign = 'left';
        ctx.fillText('Arrêté', 50, config.hauteur - 18);

        // En circulation
        ctx.fillStyle = '#10B981';
        ctx.fillRect(120, config.hauteur - 30, 15, 15);
        ctx.fillText('En route', 140, config.hauteur - 18);

    }, [stations, trains, direction, config]);

    // pop up lors du clic train
    const gererClic = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!onTrainClique) return;

        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        const clickX = e.clientX - rect.left;
        const clickY = e.clientY - rect.top;

        const margeHaut = 150;
        const espacementHorizontal = (config.largeur - 150) / Math.max(stations.length - 1, 1);

        for (let i = 0; i < trains.length; i++) {
            const train = trains[i];

            // recup station du train
            let stationIndex = -1;
            for (let j = 0; j < stations.length; j++) {
                const station = stations[j];
                if (station.id === train.position?.ligneStation?.id) {
                    stationIndex = j;
                    break;
                }
            }

            if (stationIndex === -1) {
                continue;
            }

            // Calculer où est dessiné ce train
            const trainX = 80 + stationIndex * espacementHorizontal;
            const trainY = margeHaut - 60;

            const clicDansLaLargeur = clickX >= trainX - 15 && clickX <= trainX + 15;
            const clicDansLaHauteur = clickY >= trainY - 10 && clickY <= trainY + 25;

            if (clicDansLaLargeur && clicDansLaHauteur) {
                onTrainClique(train);
                break;
            }
        }
    };

    return (
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
    );
};

export default VoiesComponent;