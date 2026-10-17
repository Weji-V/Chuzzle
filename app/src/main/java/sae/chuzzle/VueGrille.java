package sae.chuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class VueGrille extends View {

    //  Constantes

    private static final int NB_LIGNES   = 6;
    private static final int NB_COLONNES = 6;
    private static final int NB_TYPES    = 7;

    /** Symboles pour les daltoniens, un par type. */
    private static final String[] SYMBOLES = {"●", "■", "▲", "✚", "★", "♦", "✿"};

    //  Données nécessaires au dessin

    private int[][] grille  = new int[NB_LIGNES][NB_COLONNES];
    private boolean[][] verrous = new boolean[NB_LIGNES][NB_COLONNES];

    private Boolean animEstLigne = null;
    private int animIndex = 0;
    private float animDecalagePx = 0f;

    //  Outils de dessin

    private final Paint pinceauCase = new Paint();
    private final Paint pinceauSymbole = new Paint();
    private final Paint pinceauSelection = new Paint();
    private Bitmap imageChaine;

    //  Constructeurs

    public VueGrille(Context contexte) {
        super(contexte);
        initPinceaux();
    }

    public VueGrille(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPinceaux();
    }

    private void initPinceaux() {
        pinceauCase.setAntiAlias(true);
        pinceauCase.setStyle(Paint.Style.FILL);

        pinceauSymbole.setAntiAlias(true);
        pinceauSymbole.setColor(0xFF000000);
        pinceauSymbole.setTextAlign(Paint.Align.CENTER);

        pinceauSelection.setAntiAlias(true);
        pinceauSelection.setStyle(Paint.Style.STROKE);
        pinceauSelection.setStrokeWidth(12f);
        pinceauSelection.setColor(0xFFFFFFFF);

        imageChaine = BitmapFactory.decodeResource(getResources(), R.drawable.chaine);
    }

    //  -

    public void definirGrille(int[][] nouvelleGrille) {
        for (int l = 0; l < NB_LIGNES; l++) {
            System.arraycopy(nouvelleGrille[l], 0, grille[l], 0, NB_COLONNES);
        }
        invalidate();
    }

    public void definirVerrous(boolean[][] nouveauxVerrous) {
        for (int l = 0; l < NB_LIGNES; l++) {
            System.arraycopy(nouveauxVerrous[l], 0, verrous[l], 0, NB_COLONNES);
        }
        invalidate();
    }

    //  animation de glissement

    public void definirGlissement(boolean estLigne, int index, float decalagePx) {
        this.animEstLigne   = estLigne;
        this.animIndex      = index;
        this.animDecalagePx = decalagePx;
        invalidate();
    }

    public void annulerGlissement() {
        this.animEstLigne = null;
        invalidate();
    }

    //  Dessin

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Mise à jour de la couleur du contour de sélection si bloqué
        if (animEstLigne != null) {
            if (estSelectionBloquee()) {
                pinceauSelection.setColor(0xFF000000); // Noir
            } else {
                pinceauSelection.setColor(0xFFFFFFFF); // Blanc
            }
        }

        int largeur = getWidth()  - getPaddingLeft() - getPaddingRight();
        int hauteur = getHeight() - getPaddingTop()  - getPaddingBottom();

        float tailleCase = Math.min(
                largeur / (float) NB_COLONNES,
                hauteur / (float) NB_LIGNES
        );

        float margeGauche = getPaddingLeft() + (largeur - tailleCase * NB_COLONNES) / 2f;
        float margeHaut   = getPaddingTop()  + (hauteur - tailleCase * NB_LIGNES)   / 2f;

        // Limite le dessin à la zone de la grille
        canvas.save();
        canvas.clipRect(margeGauche, margeHaut,
                        margeGauche + NB_COLONNES * tailleCase,
                        margeHaut + NB_LIGNES * tailleCase);

        pinceauSymbole.setTextSize(tailleCase * 0.4f);

        // On parcourt chaque case de la grille pour la dessiner
        for (int ligne = 0; ligne < NB_LIGNES; ligne++) {
            for (int colonne = 0; colonne < NB_COLONNES; colonne++) {

                float offsetX = 0f;
                float offsetY = 0f;

                // Si un glissement est en cours, on calcule le décalage visuel
                if (animEstLigne != null) {
                    // On convertit les pixels du doigt en nombre de cases de décalage
                    int nbCases = Math.round(animDecalagePx / tailleCase);
                    // On applique le décalage uniquement à la ligne ou colonne que le joueur bouge
                    if (animEstLigne && ligne == animIndex) {
                        offsetX = nbCases * tailleCase;
                    } else if (!animEstLigne && colonne == animIndex) {
                        offsetY = nbCases * tailleCase;
                    }
                }

                float x1 = margeGauche + colonne * tailleCase + 6 + offsetX;
                float y1 = margeHaut   + ligne   * tailleCase + 6 + offsetY;
                float x2 = margeGauche + (colonne + 1) * tailleCase - 6 + offsetX;
                float y2 = margeHaut   + (ligne   + 1) * tailleCase - 6 + offsetY;

                // On dessine la case (et ses éventuelles copies pour l'effet de boucle)
                dessinerCase(canvas, ligne, colonne, x1, y1, x2, y2,
                             tailleCase, margeGauche, margeHaut, offsetX, offsetY);
            }
        }
        canvas.restore();
    }

    private void dessinerCase(Canvas canvas,
                               int ligne, int colonne,
                               float x1, float y1, float x2, float y2,
                               float tailleCase,
                               float margeGauche, float margeHaut,
                               float offsetX, float offsetY) {

        float largeurGrille = NB_COLONNES * tailleCase;
        float hauteurGrille = NB_LIGNES   * tailleCase;

        // Dessin case principale
        dessinerRectCase(canvas, ligne, colonne, x1, y1, x2, y2);

        // Réapparition de l'autre côté (effet boucle infini)
        if (offsetX != 0f) {
            float bordD = margeGauche + largeurGrille;
            float bordG = margeGauche;
            if (x2 > bordD) {
                dessinerRectCase(canvas, ligne, colonne, x1 - largeurGrille, y1, x2 - largeurGrille, y2);
            } else if (x1 < bordG) {
                dessinerRectCase(canvas, ligne, colonne, x1 + largeurGrille, y1, x2 + largeurGrille, y2);
            }
        }
        if (offsetY != 0f) {
            float bordB = margeHaut + hauteurGrille;
            float bordH = margeHaut;
            if (y2 > bordB) {
                dessinerRectCase(canvas, ligne, colonne, x1, y1 - hauteurGrille, x2, y2 - hauteurGrille);
            } else if (y1 < bordH) {
                dessinerRectCase(canvas, ligne, colonne, x1, y1 + hauteurGrille, x2, y2 + hauteurGrille);
            }
        }
    }

    /**
     * Dessine le rectangle coloré d'une case + verrou + symbole daltonien.
     * Ajoute un contour gras si la case est sélectionnée.
     */
    private void dessinerRectCase(Canvas canvas,
                                   int ligne, int colonne,
                                   float x1, float y1, float x2, float y2) {

        int type = grille[ligne][colonne];

        definirCouleur(type);
        canvas.drawRoundRect(x1, y1, x2, y2, 20, 20, pinceauCase);

        if (animEstLigne != null) {
            if ((animEstLigne && ligne == animIndex) || (!animEstLigne && colonne == animIndex)) {
                canvas.drawRoundRect(x1, y1, x2, y2, 20, 20, pinceauSelection);
            }
        }

        // Assombrir si verrouillée
        if (verrous[ligne][colonne]) {
            pinceauCase.setARGB(120, 0, 0, 0);
            canvas.drawRoundRect(x1, y1, x2, y2, 20, 20, pinceauCase);
        }

        float cx = (x1 + x2) / 2f;
        float cy = (y1 + y2) / 2f - (pinceauSymbole.descent() + pinceauSymbole.ascent()) / 2f;

        canvas.drawText(SYMBOLES[type % NB_TYPES], cx, cy, pinceauSymbole);

        // Dessin de l'image de la chaine si verrouillée
        if (verrous[ligne][colonne] && imageChaine != null) {
            float size     = (x2 - x1);
            float chainSize = size * 0.55f;
            canvas.drawBitmap(imageChaine, null, new RectF(x1, y1, x1 + chainSize, y1 + chainSize), null);
            canvas.drawBitmap(imageChaine, null, new RectF(x2 - chainSize, y2 - chainSize, x2, y2), null);
        }
    }

    private void definirCouleur(int type) {
        switch (type % NB_TYPES) {
            case 0: pinceauCase.setARGB(255, 200, 200, 200); break;
            case 1: pinceauCase.setARGB(255, 255, 105, 180); break;
            case 2: pinceauCase.setARGB(255, 90,  230, 200); break;
            case 3: pinceauCase.setARGB(255, 100, 170, 255); break;
            case 4: pinceauCase.setARGB(255, 255, 220, 90);  break;
            case 5: pinceauCase.setARGB(255, 255, 140, 90);  break;
            case 6: pinceauCase.setARGB(255, 255, 90,  90);  break;
        }
    }

    /** Vérifie si la ligne/colonne en cours de glissement contient un verrou. */
    private boolean estSelectionBloquee() {
        if (animEstLigne == null) return false;
        if (animEstLigne) {
            for (int c = 0; c < NB_COLONNES; c++)
                if (verrous[animIndex][c]) return true;
        } else {
            for (int l = 0; l < NB_LIGNES; l++)
                if (verrous[l][animIndex]) return true;
        }
        return false;
    }
}
