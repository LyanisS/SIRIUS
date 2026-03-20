import { useState } from "react";
import { clearSession, Utilisateur } from "../api/authApi";

interface AccountMenuProps {
    utilisateur: Utilisateur;
    onLogout: () => void;
}

export default function AccountMenu({ utilisateur, onLogout }: AccountMenuProps) {
    const [open, setOpen] = useState<boolean>(false);

    function handleLogout(): void {
        setOpen(false);
        clearSession()
        onLogout();
    }

    return (
        <div className="relative">
            <button
                onClick={() => setOpen((v) => !v)}
                className="flex items-center gap-2 pl-1 pr-3 py-1 rounded-full border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-all duration-150 cursor-pointer group"
            >
                <span className="text-sm font-medium text-slate-700 group-hover:text-slate-900 transition-colors max-w-[120px] truncate">
                     {utilisateur.nom}
                </span>
                <svg
                    width="12"
                    height="12"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className={`text-slate-400 transition-transform duration-200 ${open ? "rotate-180" : ""}`}
                >
                    <polyline points="6 9 12 15 18 9" />
                </svg>
            </button>

            {open && (
                <div className="absolute right-0 mt-2 w-56 bg-white border border-slate-200 rounded-xl shadow-lg overflow-hidden z-50">
                    <div className="px-4 py-3 border-b border-slate-100">
                        <p className="text-xs font-bold text-slate-800 truncate">{utilisateur.nom}</p>
                        <p className="text-xs text-slate-400 truncate mt-0.5">{utilisateur.email}</p>
                    </div>

                    <div className="p-1">
                        <button
                            onClick={handleLogout}
                            className="w-full flex items-center gap-2.5 px-3 py-2 text-sm text-red-500 hover:bg-red-50 rounded-lg transition-colors duration-100 cursor-pointer"
                        >
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                                 stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                                <polyline points="16 17 21 12 16 7" />
                                <line x1="21" y1="12" x2="9" y2="12" />
                            </svg>
                            Se déconnecter
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}