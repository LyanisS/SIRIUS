import React, { useState, useRef, useEffect } from "react";
import { connexion, inscription } from "../api/authApi";

interface AuthFormProps {
    onSuccess: () => void;
}

type Mode = "connexion" | "inscription";

interface FormState {
    nom: string;
    email: string;
    motDePasse: string;
}

export default function AuthForm({ onSuccess }: AuthFormProps) {
    const [mode, setMode] = useState<Mode>("connexion");
    const [form, setForm] = useState<FormState>({ nom: "", email: "", motDePasse: "" });
    const [error, setError] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [fading, setFading] = useState<boolean>(false);
    const emailRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        setTimeout(() => emailRef.current?.focus(), 50);
    }, [mode]);

    function switchMode(next: Mode): void {
        if (next === mode) return;
        setFading(true);
        setError("");
        setTimeout(() => {
            setMode(next);
            setForm({ nom: "", email: "", motDePasse: "" });
            setFading(false);
        }, 180);
    }

    async function handleSubmit(): Promise<void> {
        setError("");
        setLoading(true);
        try {
             if (mode === "connexion") {
                 await connexion(form.email, form.motDePasse);
             } else {
                 await inscription(form.nom, form.email, form.motDePasse);
             }
            onSuccess();
        } catch (e) {
            setError(e instanceof Error ? e.message : "Erreur inconnue");
        } finally {
            setLoading(false);
        }
    }

    const labels: Record<keyof FormState, string> = {
        nom: "Nom",
        email: "Email",
        motDePasse: "Mot de passe",
    };

    function renderField(
        key: keyof FormState,
        type: string,
        placeholder: string,
        ref?: React.RefObject<HTMLInputElement>
    ) {
        return (
            <label key={key} className="flex flex-col gap-1.5">
            <span className="text-[11px] font-bold uppercase tracking-widest text-slate-500">
                {labels[key]}
            </span>
                <input
                    ref={ref}
                    type={type}
                    placeholder={placeholder}
                    value={form[key]}
                    onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                    className="bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-slate-100 placeholder-slate-600 outline-none transition-all duration-150 focus:border-violet-500 focus:ring-2 focus:ring-violet-500/20"
                />
            </label>
        );
    }

    return (
        <div>
            <div className="flex border-b border-slate-800 mb-6">
                {(["connexion", "inscription"] as Mode[]).map((m) => (
                    <button
                        key={m}
                        onClick={() => switchMode(m)}
                        className={`flex-1 pb-2.5 text-[11px] font-bold uppercase tracking-widest border-b-2 -mb-px transition-all duration-150 cursor-pointer ${
                            mode === m
                                ? "border-violet-500 text-violet-400"
                                : "border-transparent text-slate-600 hover:text-slate-400"
                        }`}
                    >
                        {m === "connexion" ? "Connexion" : "Inscription"}
                    </button>
                ))}
            </div>

            <div className={`flex flex-col gap-4 transition-opacity duration-150 ${fading ? "opacity-0" : "opacity-100"}`}>
                {mode === "inscription" && renderField("nom", "text", "Prénom Nom")}
                {renderField("email", "email", "prenom.nom@etu.u-pec.fr", emailRef)}
                {renderField("motDePasse", "password", "••••••••")}

                {error && (
                    <p className="text-xs text-pink-400 bg-pink-400/10 rounded-lg px-3 py-2.5 font-medium">
                        {error}
                    </p>
                )}

                <button
                    onClick={handleSubmit}
                    disabled={loading}
                    className="mt-1 py-3 rounded-lg text-[11px] font-bold uppercase tracking-widest bg-violet-600 hover:bg-violet-500 text-white transition-all duration-150 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-violet-500/40"
                >
                    {loading ? "…" : mode === "connexion" ? "Se connecter" : "Créer un compte"}
                </button>
            </div>
        </div>
    );
}