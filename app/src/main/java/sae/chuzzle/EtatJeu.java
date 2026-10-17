package sae.chuzzle;

import android.os.Bundle;
import java.util.List;
import java.util.Random;

/**
 * Gère la session de jeu (score, coups, état de la partie) et délègue la
 * manipulation des pièces de la grille à la classe Plateau.
 */
public class EtatJeu {

    private final Plateau plateau;
    private final Random aleatoire;
    private final boolean hardMode;
    private int score = 0;
    private int nbCoups = 0;
    private boolean partieTerminee = false;

    private final int[] seriesParCouleurDernierCoup = new int[Plateau.NB_TYPES];

    public EtatJeu(long graine, boolean hardMode) {
        this.aleatoire = new Random(graine);
        this.hardMode = hardMode;
        this.plateau = new Plateau(this.aleatoire);
    }

    // --- Getters

    public int obtenirScore() {
        return score;
    }

    public int obtenirNbCoups() {
        return nbCoups;
    }

    public boolean estTerminee() {
        return partieTerminee;
    }

    public int[] getSeriesParCouleurDernierCoup() {
        return seriesParCouleurDernierCoup;
    }

    public int[][] obtenirGrille() {
        return plateau.copierGrille();
    }

    /**
     * Retourne l'état des verrous (cases bloquées)
     * On crée une copie pour que personne ne puisse modifier les verrous
     * directement sans passer par les règles de cette classe
     */
    public boolean[][] obtenirVerrous() {
        // Copie manuelle pour respecter l'encapsulation
        boolean[][] v = plateau.getVerrous();
        boolean[][] copie = new boolean[Plateau.NB_LIGNES][Plateau.NB_COLONNES];
        for (int l = 0; l < Plateau.NB_LIGNES; l++) {
            System.arraycopy(v[l], 0, copie[l], 0, Plateau.NB_COLONNES);
        }
        return copie;
    }

    // --- Actions

    /**
     * Tente de déplacer une ligne ou une colonne.
     * Si le mouvement ne crée aucune série de 3 couleurs, il est annulé.
     */
    public boolean appliquerCoup(boolean estLigne, int index, int sens) {
        if (partieTerminee || plateau.estBloque(estLigne, index)) {
            return false;
        }

        int[][] sauvegarde = plateau.copierGrille();
        if (estLigne) {
            plateau.decalerLigne(index, sens);
        } else {
            plateau.decalerColonne(index, sens);
        }

        if (plateau.trouverSeries().isEmpty()) {
            plateau.restaurerGrille(sauvegarde);
            return false;
        }

        nbCoups++;
        resetSeriesDernierCoup();
        score += resoudreCascades();

        plateau.ajouterVerrou(nbCoups);
        if (hardMode) {
            plateau.ajouterVerrou(nbCoups);
        }

        if (!aUnCoupValide()) {
            partieTerminee = true;
        }
        return true;
    }

    /**
     * Gère les "cascades" : quand des pièces disparaissent, d'autres tombent
     * et peuvent créer de nouvelles séries automatiquement.
     */
    private int resoudreCascades() {
        int baseTotal = 0;
        int nbSeriesTotal = 0;
        List<int[]> series = plateau.trouverSeries();

        while (!series.isEmpty()) {
            int[] result = traiterSeries(series);
            baseTotal += result[0];
            nbSeriesTotal += result[1];

            boolean[][] masque = creerMasque(series);
            plateau.libererVerrous(masque);
            plateau.faireTomber(masque);

            series = plateau.trouverSeries();
        }

        if (nbSeriesTotal == 0) {
            return 0;
        }
        return (int) (baseTotal * (1.0 + (nbSeriesTotal - 1) * 0.5));
    }

    // --- Aide à la résolution

    /**
     * Parcourt les séries détectées, calcule les points de base et compte le nombre
     * de séries.
     * Vérifie la couleur des cellules pour délimiter correctement les séries.
     * 
     * int[2] : [0] = points de base, [1] = nombre de séries
     */
    private int[] traiterSeries(List<int[]> series) {
        boolean[][] masque = creerMasque(series);
        int[][] g = plateau.getGrille();
        int pts = 0;
        int count = 0;

        // Horizontal
        for (int l = 0; l < Plateau.NB_LIGNES; l++) {
            int c = 0;
            while (c < Plateau.NB_COLONNES) {
                if (masque[l][c]) {
                    int type = g[l][c];
                    int fin = c + 1;
                    while (fin < Plateau.NB_COLONNES && masque[l][fin] && g[l][fin] == type) {
                        fin++;
                    }
                    if (fin - c >= 3) {
                        pts += pointsPourLongueur(fin - c);
                        seriesParCouleurDernierCoup[type]++;
                        count++;
                    }
                    c = fin;
                } else {
                    c++;
                }
            }
        }
        // Vertical
        for (int c = 0; c < Plateau.NB_COLONNES; c++) {
            int l = 0;
            while (l < Plateau.NB_LIGNES) {
                if (masque[l][c]) {
                    int type = g[l][c];
                    int fin = l + 1;
                    while (fin < Plateau.NB_LIGNES && masque[fin][c] && g[fin][c] == type) {
                        fin++;
                    }
                    if (fin - l >= 3) {
                        pts += pointsPourLongueur(fin - l);
                        seriesParCouleurDernierCoup[type]++;
                        count++;
                    }
                    l = fin;
                } else {
                    l++;
                }
            }
        }
        return new int[] { pts, count };
    }

    /**
     * Définit combien de points rapporte une série selon sa taille (3, 4, 5...).
     */
    private int pointsPourLongueur(int n) {
        if (n == 3)
            return 8;
        if (n == 4)
            return 16;
        if (n == 5)
            return 32;
        return 64;
    }

    /**
     * Transforme une liste de positions en une "carte" (masque) de la grille
     * pour savoir rapidement quelles cases doivent être supprimées.
     */
    private boolean[][] creerMasque(List<int[]> positions) {
        boolean[][] m = new boolean[Plateau.NB_LIGNES][Plateau.NB_COLONNES];
        for (int[] p : positions) {
            m[p[0]][p[1]] = true;
        }
        return m;
    }

    private void resetSeriesDernierCoup() {
        for (int i = 0; i < Plateau.NB_TYPES; i++) {
            seriesParCouleurDernierCoup[i] = 0;
        }
    }

    /**
     * Parcourt l'intégralité des possibilités pour vérifier si le joueur
     * peut encore effectuer au moins un mouvement créant une série.
     */
    public boolean aUnCoupValide() {
        for (int i = 0; i < Plateau.NB_LIGNES; i++) {
            if (plateau.estBloque(true, i)) {
                continue;
            }
            for (int s = 1; s < Plateau.NB_COLONNES; s++) {
                if (simulerCoup(true, i, s)) {
                    return true;
                }
            }
        }
        for (int j = 0; j < Plateau.NB_COLONNES; j++) {
            if (plateau.estBloque(false, j)) {
                continue;
            }
            for (int s = 1; s < Plateau.NB_LIGNES; s++) {
                if (simulerCoup(false, j, s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Simule un déplacement pour voir s'il créerait une série,
     * sans modifier la "vraie" grille de jeu du joueur.
     */
    private boolean simulerCoup(boolean estLigne, int idx, int s) {
        int[][] save = plateau.copierGrille();
        if (estLigne) {
            plateau.decalerLigne(idx, s);
        } else {
            plateau.decalerColonne(idx, s);
        }
        boolean ok = !plateau.trouverSeries().isEmpty();
        plateau.restaurerGrille(save);
        return ok;
    }


    public void forcerFinDePartie() {
        partieTerminee = true;
    }

    /**
     * Prépare les données du jeu pour les sauvegarder (en cas de fermeture de l'app).
     */
    public void sauvegarderEtat(Bundle b) {
        int[] g = new int[36];
        boolean[] v = new boolean[36];
        int[][] grid = plateau.getGrille();
        boolean[][] lk = plateau.getVerrous();
        for (int i = 0; i < Plateau.NB_LIGNES; i++) {
            for (int j = 0; j < Plateau.NB_COLONNES; j++) {
                g[i * Plateau.NB_LIGNES + j] = grid[i][j];
                v[i * Plateau.NB_LIGNES + j] = lk[i][j];
            }
        }
        b.putIntArray("grille", g);
        b.putBooleanArray("verrous", v);
        b.putInt("score", score);
        b.putInt("nbCoups", nbCoups);
        b.putBoolean("partieTerminee", partieTerminee);
    }

    /**
     * Recharge les données du jeu sauvegardées précédemment.
     */
    public void restaurerEtat(Bundle b) {
        if (!b.containsKey("grille")) return;

        int[] g = b.getIntArray("grille");
        boolean[] v = b.getBooleanArray("verrous");
        int[][] grid = plateau.getGrille();
        boolean[][] lk = plateau.getVerrous();
        for (int i = 0; i < Plateau.NB_LIGNES; i++) {
            for (int j = 0; j < Plateau.NB_COLONNES; j++) {
                grid[i][j] = g[i * Plateau.NB_LIGNES + j];
                lk[i][j] = v[i * Plateau.NB_LIGNES + j];
            }
        }
        score = b.getInt("score");
        nbCoups = b.getInt("nbCoups");
        partieTerminee = b.getBoolean("partieTerminee");
    }
}
