package sae.chuzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gère les données de la grille et la logique de manipulation du plateau.
 */
public class Plateau {
    public static final int NB_LIGNES = 6;
    public static final int NB_COLONNES = 6;
    public static final int NB_TYPES = 7;

    private final int[][] grille = new int[NB_LIGNES][NB_COLONNES];
    private final boolean[][] verrous = new boolean[NB_LIGNES][NB_COLONNES];
    private final Random aleatoire;

    public Plateau(Random aleatoire) {
        this.aleatoire = aleatoire;
        initialiserGrilleSansTriples();
    }

    /**
     * Remplit la grille aléatoirement au début de la partie.
     * On s'assure qu'aucune série de 3 n'est créée automatiquement au démarrage.
     */
    private void initialiserGrilleSansTriples() {
        for (int l = 0; l < NB_LIGNES; l++) {
            for (int c = 0; c < NB_COLONNES; c++) {
                int valeur;
                do {
                    valeur = aleatoire.nextInt(NB_TYPES);
                } while (creeTriple(l, c, valeur));
                grille[l][c] = valeur;
            }
        }
    }

    /**
     * Vérifie si placer une couleur à une position précise créerait un alignement de 3.
     */
    private boolean creeTriple(int l, int c, int val) {
        if (c >= 2 && grille[l][c - 1] == val && grille[l][c - 2] == val) {
            return true;
        }
        if (l >= 2 && grille[l - 1][c] == val && grille[l - 2][c] == val) {
            return true;
        }
        return false;
    }

    /**
     * Fait défiler une colonne horizontalement. C'est un mouvement circulaire :
     * les cases qui sortent par la gauche reviennent par la droite (comme un rouleau)
     */
    public void decalerLigne(int ligne, int sens) {
        int s = ((sens % NB_COLONNES) + NB_COLONNES) % NB_COLONNES;
        for (int etape = 0; etape < s; etape++) {
            int tmp = grille[ligne][NB_COLONNES - 1];
            for (int c = NB_COLONNES - 1; c > 0; c--) {
                grille[ligne][c] = grille[ligne][c - 1];
            }
            grille[ligne][0] = tmp;
        }
    }

    /**
     * Fait défiler une colonne verticalement. C'est un mouvement circulaire :
     * les cases qui sortent par le bas reviennent par le haut.
     */
    public void decalerColonne(int col, int sens) {
        int s = ((sens % NB_LIGNES) + NB_LIGNES) % NB_LIGNES;
        for (int etape = 0; etape < s; etape++) {
            int tmp = grille[NB_LIGNES - 1][col];
            for (int l = NB_LIGNES - 1; l > 0; l--) {
                grille[l][col] = grille[l - 1][col];
            }
            grille[0][col] = tmp;
        }
    }

    /**
     * Parcourt toute la grille pour identifier les alignements de 3 couleurs ou plus.
     * Retourne nne liste de coordonnées [ligne, colonne] des cases à supprimer.
     */
    public List<int[]> trouverSeries() {
        boolean[][] aSupprimer = new boolean[NB_LIGNES][NB_COLONNES];
        // Analyse horizontale
        for (int l = 0; l < NB_LIGNES; l++) {
            int c = 0;
            while (c < NB_COLONNES) {
                int type = grille[l][c];
                int fin = c + 1;
                while (fin < NB_COLONNES && grille[l][fin] == type) {
                    fin++;
                }
                if (fin - c >= 3) {
                    for (int k = c; k < fin; k++) {
                        aSupprimer[l][k] = true;
                    }
                }
                c = fin;
            }
        }
        // Analyse verticale
        for (int col = 0; col < NB_COLONNES; col++) {
            int l = 0;
            while (l < NB_LIGNES) {
                int type = grille[l][col];
                int fin = l + 1;
                while (fin < NB_LIGNES && grille[fin][col] == type) {
                    fin++;
                }
                if (fin - l >= 3) {
                    for (int k = l; k < fin; k++) {
                        aSupprimer[k][col] = true;
                    }
                }
                l = fin;
            }
        }
        List<int[]> positions = new ArrayList<>();
        for (int l = 0; l < NB_LIGNES; l++) {
            for (int c = 0; c < NB_COLONNES; c++) {
                if (aSupprimer[l][c]) {
                    positions.add(new int[]{l, c});
                }
            }
        }
        return positions;
    }

    /**
     * Vérifie si une ligne ou une colonne contient au moins une case verrouillée.
     * Si c'est le cas, elle ne peut pas être déplacée par le joueur.
     */
    public boolean estBloque(boolean estLigne, int index) {
        if (estLigne) {
            for (int c = 0; c < NB_COLONNES; c++) {
                if (verrous[index][c]) {
                    return true;
                }
            }
        } else {
            for (int l = 0; l < NB_LIGNES; l++) {
                if (verrous[l][index]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ajoute un nouveau verrou sur une case libre au hasard.
     * La fréquence d'apparition augmente au fur et à mesure que la partie avance.
     */
    public void ajouterVerrou(int nbCoups) {
        int intervalle = Math.max(1, 5 - nbCoups / 10);
        if (nbCoups % intervalle != 0) {
            return;
        }
        List<int[]> libres = new ArrayList<>();
        for (int l = 0; l < NB_LIGNES; l++) {
            for (int c = 0; c < NB_COLONNES; c++) {
                if (!verrous[l][c]) {
                    libres.add(new int[]{l, c});
                }
            }
        }
        if (libres.isEmpty()) {
            return;
        }
        int[] choix = libres.get(aleatoire.nextInt(libres.size()));
        verrous[choix[0]][choix[1]] = true;
    }

    /**
     * Libère les verrous sur les cases qui viennent d'être complétées dans une série.
     */
    public void libererVerrous(boolean[][] masque) {
        for (int l = 0; l < NB_LIGNES; l++) {
            for (int c = 0; c < NB_COLONNES; c++) {
                if (masque[l][c]) {
                    verrous[l][c] = false;
                }
            }
        }
    }

    /**
     * Création d'un double de la grille actuelle (utile pour tester des mouvements).
     */
    public int[][] copierGrille() {
        int[][] copie = new int[NB_LIGNES][NB_COLONNES];
        for (int l = 0; l < NB_LIGNES; l++) {
            System.arraycopy(grille[l], 0, copie[l], 0, NB_COLONNES);
        }
        return copie;
    }

    public void restaurerGrille(int[][] source) {
        for (int l = 0; l < NB_LIGNES; l++) {
            System.arraycopy(source[l], 0, grille[l], 0, NB_COLONNES);
        }
    }

    public int[][] getGrille() {
        return grille;
    }

    public boolean[][] getVerrous() {
        return verrous;
    }

    /**
     * Logique de gravité : les cases vides (supprimées) sont comblées par les cases du dessus.
     * De nouvelles couleurs sont générées au sommet du plateau.
     */
    public void faireTomber(boolean[][] aSupprimer) {
        for (int col = 0; col < NB_COLONNES; col++) {
            List<Integer> survivants = new ArrayList<>();
            for (int l = NB_LIGNES - 1; l >= 0; l--) {
                if (!aSupprimer[l][col]) {
                    survivants.add(grille[l][col]);
                }
            }
            int li = NB_LIGNES - 1;
            for (int val : survivants) {
                grille[li][col] = val;
                li--;
            }
            while (li >= 0) {
                grille[li][col] = aleatoire.nextInt(NB_TYPES);
                li--;
            }
        }
    }
}
