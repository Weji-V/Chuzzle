package sae.chuzzle;

import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gère la génération aléatoire et le comptage des objectifs.
 */
public class GestionnaireObjectifs {
    private final Random random;
    private int nbObjectifsRealises = 0;
    private int nbCles = 0;

    public GestionnaireObjectifs(long graine) {

        this.random = new Random(graine);
    }

    /**
     * Génère un nouvel objectif en respectant les probabilités de difficulté.
     */
    public Objectif genererObjectif() {
        List<Objectif> pool = new ArrayList<>();
        int totalPoids = 0;

        // M (nbCoupsMax) entre 2 et 5.
        // N (nbSeriesCible) tel que M - N >= 1 et N <= 3.
        for (int m = 2; m <= 5; m++) {
            for (int n = 1; n <= m - 1 && n <= 3; n++) {
                // Plus l'objectif offre une marge de coups importante (M-N), plus il a de chances de tomber.
                // Plus (M-N) est petit, plus c'est dur.
                int diff = m - n;
                int poids = diff * diff; // On utilise le carré pour accentuer la différence de probabilité

                for (int color = 0; color < 7; color++) {
                    pool.add(new Objectif(color, n, m, poids));
                    totalPoids += poids;
                }
            }
        }

        if (totalPoids == 0) return null;

        int tirage = random.nextInt(totalPoids);
        int cumul = 0;
        for (Objectif obj : pool) {
            cumul += obj.getPoids();
            if (tirage < cumul) {
                return new Objectif(obj.getCouleur(), obj.getNbSeriesCible(), obj.getNbCoupsMax(), obj.getPoids());
            }
        }
        return pool.get(0);
    }

    public void incrementerReussites() {
        nbObjectifsRealises++;
        // 1 clé gagnée tous les 2 objectifs réussis, maximum 1 clés en stock
        if (nbObjectifsRealises % 2 == 0) {
            ajouterCle();
        }
    }

    public int getNbCles() {
        return nbCles;
    }

    public void ajouterCle() {
        // Ajoute une clé maximum 1 en stock
        nbCles = Math.min(nbCles + 1, 1);
    }

    /**
     * Consomme 1 clé pour réinitialiser l'objectif.
     */
    public boolean consommerCles() {
        if (nbCles >= 1) {
            nbCles -= 1;
            return true;
        }
        return false;
    }

    public int getNbObjectifsRealises() {

        return nbObjectifsRealises;
    }

    public void sauvegarder(Bundle out) {
        out.putInt("nb_objectifs_total", nbObjectifsRealises);
        out.putInt("nb_cles", nbCles);
    }

    public void restaurer(Bundle in) {
        if (in != null) {
            nbObjectifsRealises = in.getInt("nb_objectifs_total", 0);
            nbCles = in.getInt("nb_cles", 0);
        }
    }
}
