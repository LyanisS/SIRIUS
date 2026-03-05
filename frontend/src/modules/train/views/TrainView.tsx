
import { useState, useEffect } from 'react';
import LigneDisplay from '../../ligne/views/ligneView.tsx';
import { ligneApi } from '../../ligne/api/ligneApi';

export default function TrainView() {
    const [selectedLigneId, setSelectedLigneId] = useState<number | null>(null);
    const [lignes, setLignes] = useState<any[]>([]);
    const [chargement, setChargement] = useState(true);

    // Charger les lignes
    useEffect(() => {
        const chargerLignes = async () => {
            try {
                setChargement(true);
                const data = await ligneApi.obtenirToutesLignes();
                setLignes(data);

                // ligne 8 par défaut
                if (data.length > 0) {
                    setSelectedLigneId(data[0].id);
                }
            } catch (error) {
                console.error('Erreur chargement lignes:', error);
            } finally {
                setChargement(false);
            }
        };

        chargerLignes();
    }, []);

    if (chargement) {
        return (
            <div style={{ padding: '30px', textAlign: 'center' }}>
                 Chargement des lignes...
            </div>
        );
    }

    return (
        <div style={{ padding: '20px' }}>
            <div style={{
                backgroundColor: 'white',
                borderRadius: '8px',
                padding: '20px',
                marginBottom: '20px',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
            }}>
                <label
                    htmlFor="ligne-select"
                    style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        color: '#374151',
                        marginBottom: '8px'
                    }}
                >
                    Sélectionner une ligne:
                </label>
                <select
                    id="ligne-select"
                    value={selectedLigneId || ''}
                    onChange={(e) => setSelectedLigneId(parseInt(e.target.value))}
                    style={{
                        width: '100%',
                        padding: '10px 12px',
                        border: '1px solid #D1D5DB',
                        borderRadius: '6px',
                        fontSize: '14px',
                        backgroundColor: 'white',
                        cursor: 'pointer'
                    }}
                >
                    {lignes.map((ligne) => (
                        <option key={ligne.id} value={ligne.id}>
                            {ligne.nom}
                        </option>
                    ))}
                </select>
            </div>

            {selectedLigneId && <LigneDisplay ligneId={selectedLigneId} />}
        </div>
    );
}