import { Link } from 'react-router-dom'
import { routes } from './router'
import AccountMenu from "./modules/auth/components/AccountMenu.tsx";
import { getUtilisateur, Utilisateur } from "./modules/auth/api/authApi.ts";
import { useState } from "react";

export default function Navbar() {
    const [utilisateur, setUtilisateur] = useState<Utilisateur|null>(getUtilisateur());

    return (
    <nav className="bg-white shadow-md p-4 flex">
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
      {utilisateur != null && (
          <div className="flex items-center gap-3">
            <AccountMenu utilisateur={utilisateur} onLogout={() => {setUtilisateur(null);}} />
          </div>
      )}

    </nav>
  )
}
