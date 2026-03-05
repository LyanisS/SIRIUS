import React, { useEffect, useRef } from "react";
import AuthForm from "./AuthForm";

export interface AuthPopupProps {
    message?: string;
    onSuccess: () => void;
    onDismiss: () => void;
}

export default function AuthPopup({ message, onSuccess, onDismiss }: AuthPopupProps) {
    const overlayRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        document.body.style.overflow = "hidden";
        return () => { document.body.style.overflow = ""; };
    }, []);

    function handleOverlayClick(e: React.MouseEvent<HTMLDivElement>): void {
        if (e.target === overlayRef.current) onDismiss();
    }

    return (
        <div
            ref={overlayRef}
            onClick={handleOverlayClick}
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm"
        >
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-7 w-[380px] max-w-[calc(100vw-2rem)] shadow-2xl font-sans text-slate-100">

                <div className="flex items-center gap-2.5 mb-6">
                    <div className="w-8 h-8 rounded-lg bg-violet-500/10 flex items-center justify-center shrink-0">
                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none"
                             stroke="#8b5cf6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                        </svg>
                    </div>
                    <span className="flex-1 text-sm font-bold tracking-tight">
                        Authentification requise
                    </span>
                    {onDismiss && (
                        <button
                            onClick={onDismiss}
                            className="text-slate-600 hover:text-slate-300 transition-colors text-sm leading-none cursor-pointer px-1"
                        >
                            ✕
                        </button>
                    )}
                </div>

                {message && (
                    <div className="flex items-start gap-2 bg-violet-500/5 border border-violet-500/20 rounded-lg px-3.5 py-3 text-[13px] text-slate-300 mb-6 leading-relaxed">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none"
                             stroke="#8b5cf6" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
                             className="shrink-0 mt-0.5">
                            <circle cx="12" cy="12" r="10" />
                            <line x1="12" y1="8" x2="12" y2="12" />
                            <line x1="12" y1="16" x2="12.01" y2="16" />
                        </svg>
                        <span>{message}</span>
                    </div>
                )}

                <AuthForm onSuccess={onSuccess} />
            </div>
        </div>
    );
}