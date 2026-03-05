export interface Utilisateur {
    id: number;
    nom: string;
    email: string;
}

export interface SessionUtilisateur {
    token: string;
    utilisateur: Utilisateur;
}

export function getToken(): string | null {
    return sessionStorage.getItem("auth_token");
}

export function getUtilisateur(): Utilisateur | null {
    try {
        const raw = sessionStorage.getItem("auth_utilisateur");
        return raw ? (JSON.parse(raw) as Utilisateur) : null;
    } catch {
        return null;
    }
}

export function isAuthenticated(): boolean {
    return getToken() !== null;
}

export function saveSession({ token, utilisateur }: SessionUtilisateur): void {
    sessionStorage.setItem("auth_token", token);
    sessionStorage.setItem("auth_utilisateur", JSON.stringify(utilisateur));
}

export function clearSession(): void {
    sessionStorage.removeItem("auth_token");
    sessionStorage.removeItem("auth_utilisateur");
}

export async function connexion(email: string, motDePasse: string): Promise<SessionUtilisateur> {
    const res = await fetch(`/api/utilisateurs/connexion`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({ email, motDePasse }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message ?? `Erreur ${res.status}`);
    saveSession(data);
    return data;
}

export async function inscription(nom: string, email: string, motDePasse: string): Promise<SessionUtilisateur> {
    const res = await fetch(`/api/utilisateurs/inscription`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({ nom, email, motDePasse }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message ?? `Erreur ${res.status}`);
    saveSession(data);
    return data;
}