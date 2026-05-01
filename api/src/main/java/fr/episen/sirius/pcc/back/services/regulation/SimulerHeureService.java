package fr.episen.sirius.pcc.back.services.regulation;

import java.util.Calendar;
import java.util.Date;

public class SimulerHeureService {

    private static final int FACTEUR = 30; // 1s réelle = 30min simulées

    // Non-final pour pouvoir être réinitialisés après la génération
    private static volatile long tempsReelDebut = System.currentTimeMillis();
    private static volatile Date heureDebutSimulation;

    static {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MARCH, 20, 6, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        heureDebutSimulation = cal.getTime();
    }

    public static Date now() {
        long tempsEcoule = System.currentTimeMillis() - tempsReelDebut;
        long tempsSimule = tempsEcoule * FACTEUR;
        return new Date(heureDebutSimulation.getTime() + tempsSimule);
    }

    /**

     * À appeler après la fin de generateTrajets().
     */
    public static void reset(Date heureVoulue) {
        heureDebutSimulation = heureVoulue;
        tempsReelDebut = System.currentTimeMillis();
    }
}