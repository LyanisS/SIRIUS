import { Incident } from "../api/incidentApi.ts";
import BadgeLigne from "../../itineraire/components/BadgeLigne.tsx";

interface IncidentRowProps {
    incident: Incident;
}

export default function IncidentRow({ incident }: IncidentRowProps) {
    return (
        <tr className="odd:bg-white even:bg-gray-50 border-b border-gray-200">
            <th scope="row" className="px-6 py-4 font-medium text-gray-900 whitespace-nowrap">{incident.id}</th>
            <td className="px-6 py-4">{incident.message}</td>
            <td className="px-6 py-4">{incident.dateDebut.toLocaleDateString('fr-fr', { hour: '2-digit', minute: '2-digit' })}</td>
            <td className="px-6 py-4">{incident.trajet.train.id}</td>
            <td className="px-6 py-4"><BadgeLigne ligne={incident.trajet.ligne} /></td>
        </tr>
    )
}