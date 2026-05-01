import { ReactNode } from 'react'
import { Routes, Route } from 'react-router-dom'
import TrainView from './modules/train/views/TrainView.tsx'
import CalculItineraireView from "./modules/itineraire/views/CalculItineraireView.tsx";
import IncidentView from "./modules/incident/views/IncidentView.tsx";



interface RouteConfig {
  path: string
  label: string
  element: ReactNode
}

export const routes: RouteConfig[] = [
  {path: '/train', label: 'Régulation trafic',  element: <TrainView /> },
  {path: '/itineraire', label: 'Calcul itinéraire',  element: <CalculItineraireView /> },
  {path: '/incident', label: 'Incidents',  element: <IncidentView /> },
]

export default function AppRouter() {
  return (
    <Routes>
      {routes.map((route) => (
        <Route key={route.path} path={route.path} element={route.element} />
      ))}
    </Routes>
  )
}
