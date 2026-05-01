import { useEffect, useState } from "react";
import { getIncidents, Incident } from "../api/incidentApi.ts";
import IncidentRow from "../components/IncidentRow.tsx";
import './IncidentView.css'

export default function IncidentView() {
    const [incidents, setIncidents] = useState<Incident[]>([]);
    const [chargement, setChargement] = useState<boolean>(true);

    // Charger les incidents
    useEffect(() => {
        const chargerIncidents = async () => {
            try {
                setChargement(true);
                const data = await getIncidents();
                setIncidents(data);
            } catch (error) {
                console.error('Erreur chargement incidents:', error);
            } finally {
                setChargement(false);
            }
        };

        chargerIncidents();
    }, []);

    if (chargement) {
        return (
            <div style={{ padding: '30px', textAlign: 'center' }}>
                Chargement des incidents...
            </div>
        );
    }

    return (
        <div>
            <table className="w-full text-left">
                <thead className="text-gray-700 bg-gray-50 border-b rounded-base border-default">
                    <tr>
                        <th scope="col" className="px-6 py-3">ID</th>
                        <th scope="col" className="px-6 py-3">Message</th>
                        <th scope="col" className="px-6 py-3">Heure</th>
                        <th scope="col" className="px-6 py-3">Train</th>
                        <th scope="col" className="px-6 py-3">Ligne</th>
                    </tr>
                </thead>
                <tbody>
                {incidents.map((incident: Incident) => (
                    <IncidentRow key={incident.id} incident={incident}/>
                ))}
                </tbody>
            </table>
        </div>
    )
}