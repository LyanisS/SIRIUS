import { Link } from 'react-router-dom'
import { routes } from './router'

export default function Navbar() {
  return (
    <nav className="bg-white shadow-md p-4">
      <div className="container mx-auto flex gap-6">
        {routes.map((route) => (
          <Link
            key={route.path}
            to={route.path}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            {route.label}
          </Link>
        ))}
      </div>
    </nav>
  )
}
