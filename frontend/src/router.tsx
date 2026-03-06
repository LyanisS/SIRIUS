import { ReactNode } from 'react'
import { Routes, Route } from 'react-router-dom'
import TrainView from './modules/train/views/TrainView.tsx'



interface RouteConfig {
  path: string
  label: string
  element: ReactNode
}

export const routes: RouteConfig[] = [
  {path: '/train', label: 'Régulation trafic',  element: <TrainView /> },
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
