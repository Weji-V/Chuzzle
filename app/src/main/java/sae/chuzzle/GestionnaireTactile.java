package sae.chuzzle;

import android.view.MotionEvent;
import android.view.View;

/**
 * Gère les interactions tactiles sur la grille de jeu.
 * Traduit les mouvements de doigt en déplacements de lignes ou colonnes.
 */
public class GestionnaireTactile implements View.OnTouchListener {

    private static final int NB_LIGNES = Plateau.NB_LIGNES;
    private static final int NB_COLONNES = Plateau.NB_COLONNES;

    // Distance minimale (en pixels) avant de décider si le joueur déplace une ligne ou une colonne.
    // Cela évite les déclenchements accidentels lors d'un simple clic.
    private static final float SEUIL_DIRECTION_PX = 10f;

    private final VueGrille vueGrille;
    private final EtatJeu etatJeu;
    private final Controleur controleur;

    private float touchDebutX, touchDebutY; // Position initiale du doigt
    private int ligneTouchee, colonneTouchee; // Indices de la case située sous le doigt au départ
    private Boolean estLigne; // null = direction inconnue, true = horizontal, false = vertical
    private int indexGlissement; // Numéro de la ligne ou colonne que l'on fait glisser
    private float decalagePx; // Distance parcourue par le doigt depuis le début
    private float tailleCase;

    public GestionnaireTactile(VueGrille vueGrille,
                               EtatJeu etatJeu,
                               Controleur controleur) {
        this.vueGrille = vueGrille;
        this.etatJeu = etatJeu;
        this.controleur = controleur;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (etatJeu.estTerminee()) {
            return false;
        }

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                tailleCase = calculerTailleCase();
                gererDebut(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                gererMouvement(event);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                gererFin(event.getAction() == MotionEvent.ACTION_UP);
                return true;
        }

        return false;
    }

    private void gererDebut(MotionEvent event) {
        touchDebutX = event.getX();
        touchDebutY = event.getY();
        estLigne = null;
        decalagePx = 0f;

        // Calcule le décalage pour que le doigt vise bien les cases centrées
        float margeGauche = (vueGrille.getWidth() - tailleCase * NB_COLONNES) / 2f;
        float margeHaut = (vueGrille.getHeight() - tailleCase * NB_LIGNES) / 2f;

        // Conversion des coordonnées pixels en indices de tableau (0 à 5)
        ligneTouchee = (int) ((touchDebutY - margeHaut) / tailleCase);
        colonneTouchee = (int) ((touchDebutX - margeGauche) / tailleCase);

        // Sécurité pour éviter de sortir des limites du tableau si on touche les bords
        ligneTouchee = Math.max(0, Math.min(NB_LIGNES - 1, ligneTouchee));
        colonneTouchee = Math.max(0, Math.min(NB_COLONNES - 1, colonneTouchee));
    }

    private void gererMouvement(MotionEvent event) {
        float dx = event.getX() - touchDebutX;
        float dy = event.getY() - touchDebutY;

        // on détermine si le joueur glisse vers le côté ou vers le haut/bas
        if (estLigne == null) {
            if (Math.abs(dx) > SEUIL_DIRECTION_PX || Math.abs(dy) > SEUIL_DIRECTION_PX) {
                // On verrouille l'axe : celui qui a la plus grande distance parcourue gagne
                estLigne = Math.abs(dx) >= Math.abs(dy);

                if (estLigne) {
                    indexGlissement = ligneTouchee;
                } else {
                    indexGlissement = colonneTouchee;
                }
            } else {
                return;
            }
        }

        if (estLigne) {
            decalagePx = dx;
        } else {
            decalagePx = dy;
        }

        // Affiche le glissement réel du doigt (permet la rotation multi-cases)
        vueGrille.definirGlissement(estLigne, indexGlissement, decalagePx);
    }

    private void gererFin(boolean estRelachement) {

        if (estLigne == null) {
            vueGrille.annulerGlissement();
            return;
        }

        if (tailleCase <= 1f) {
            vueGrille.annulerGlissement();
            estLigne = null;
            return;
        }

        // Arrondi au nombre de cases le plus proche (permet ±1, ±2, ±3…)
        int nbCases = (int) Math.round(decalagePx / tailleCase);

        if (nbCases != 0 && estRelachement) {
            boolean accepte = etatJeu.appliquerCoup(estLigne, indexGlissement, nbCases);
            if (accepte) {
                // MISE A JOUR DES OBJECTIFS APRES UN COUP TACTILE
                controleur.gererFinDeCoup();
            }
        }

        vueGrille.annulerGlissement();
        estLigne = null;
    }

    private float calculerTailleCase() {
        float l = vueGrille.getWidth();
        float h = vueGrille.getHeight();

        if (l == 0 || h == 0) {
            return 1f;
        }

        return Math.min(l / NB_COLONNES, h / NB_LIGNES);
    }
}