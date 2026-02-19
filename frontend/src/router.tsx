import { ReactNode } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import HelloView from './modules/train/views/HelloView'
import EditMotdView from './modules/train/views/EditMotdView'
import TimeView from './modules/time/views/TimeView'

interface RouteConfig {
  path: string
  label: string
  element: ReactNode
}

export const routes: RouteConfig[] = [
  { path: '/hello', label: 'Hello', element: <HelloView /> },
  { path: '/hello/edit', label: 'Edit MOTD', element: <EditMotdView /> },
  { path: '/time', label: 'Time', element: <TimeView /> },
]

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/hello" replace />} />
      {routes.map((route) => (
        <Route key={route.path} path={route.path} element={route.element} />
      ))}
    </Routes>
  )
}
