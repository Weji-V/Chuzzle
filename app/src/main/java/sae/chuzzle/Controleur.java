package sae.chuzzle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Controleur implements View.OnClickListener {

    private final Activity activite;
    private final EtatJeu etatJeu;
    private final VueGrille vueGrille;
    private final long graine;
    private final TextView tvScore;
    private final TextView tvCoups;

    // Hard Mode logic
    private final boolean hardMode;
    private GestionnaireObjectifs gestionnaireObjectifs;
    private Objectif objectifActuel;
    private TextView tvObjectif;
    private TextView tvNbObjectifs;
    private TextView tvCles;
    private Button  btnReinitialisationObjectif;
    private View layoutCles;

    public Controleur(Activity activite, EtatJeu etatJeu, VueGrille vueGrille,
                      long graine, TextView tvScore, TextView tvCoups,
                      boolean hardMode) {
        this.activite    = activite;
        this.etatJeu     = etatJeu;
        this.vueGrille   = vueGrille;
        this.graine      = graine;
        this.tvScore     = tvScore;
        this.tvCoups     = tvCoups;
        this.hardMode    = hardMode;

        if (hardMode) {
            this.gestionnaireObjectifs = new GestionnaireObjectifs(graine);
            this.tvObjectif    = activite.findViewById(R.id.tvObjectif);
            this.tvNbObjectifs = activite.findViewById(R.id.tvNbObjectifs);
            this.tvCles        = activite.findViewById(R.id.tvCles);
            this.layoutCles    = activite.findViewById(R.id.layoutCles);
            this.btnReinitialisationObjectif = activite.findViewById(R.id.btnReinitialisationObjectif);

            if (tvObjectif != null) {
                tvObjectif.setVisibility(View.VISIBLE);
            }
            if (tvNbObjectifs != null) {
                tvNbObjectifs.setVisibility(View.VISIBLE);
            }
            if (layoutCles != null) {
                layoutCles.setVisibility(View.VISIBLE);
            }

            if (btnReinitialisationObjectif != null) {
                btnReinitialisationObjectif.setOnClickListener(this);
            }
        }

        rafraichirAffichage();
    }



    /**
     * Initialise l'objectif au début de la partie ou lors de la restauration.
     */
    public void initialiserObjectif(Bundle savedState) {
        if (!hardMode) return;
        
        gestionnaireObjectifs.restaurer(savedState);
        objectifActuel = Objectif.restaurer(savedState);
        
        if (objectifActuel == null) {
            objectifActuel = gestionnaireObjectifs.genererObjectif();
        }
        rafraichirAffichage();
    }

    /**
     * Gère la validation d'un coup réussi (mise à jour objectif, score, affichage).
     * Centralise la logique pour être appelé par le bouton ET par le tactile.
     */
    public void gererFinDeCoup() {

        // 1 clé tous les 4  coups
        if (hardMode && etatJeu.obtenirNbCoups() % 4 == 0) {
            gestionnaireObjectifs.ajouterCle();
        }
        // --- Logique Hard Mode
        if (hardMode && objectifActuel != null) {
            // 1. Décrémenter les coups de l'objectif
            objectifActuel.decrementerCoups();
            
            // 2. Compter les séries de la couleur cible
            int[] series = etatJeu.getSeriesParCouleurDernierCoup();
            objectifActuel.ajouterSeries(series[objectifActuel.getCouleur()]);

            // 3. Vérifier réussite ou échec
            if (objectifActuel.estReussi()) {

                gestionnaireObjectifs.incrementerReussites();
                objectifActuel = gestionnaireObjectifs.genererObjectif();
            } else if (objectifActuel.estEchoue()) {
                // Plus de coups disponibles et objectif non réussi -> fin de partie immédiate
                Toast.makeText(activite, "Objectif échoué !",
                        Toast.LENGTH_SHORT
                ).show();

                etatJeu.forcerFinDePartie();

            }
        }

        rafraichirAffichage();
        verifierFinDePartie();
    }

    /**
     * Réinitialiser l'objectif en cours si la clé est consommé.
     * Affiche un Toast si le joueur n'a pas assez de clés.
     */
    public void reinitialiserObjectif() {
        if (!hardMode || gestionnaireObjectifs == null) return;

        if (gestionnaireObjectifs.consommerCles()) {
            objectifActuel = gestionnaireObjectifs.genererObjectif();
            rafraichirAffichage();
        } else {
            Toast.makeText(activite, "Pas assez de clés !", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Lancer l'écran de fin.
     */
    public void verifierFinDePartie() {
        int nbARetenir;
        if (hardMode) {
            nbARetenir = gestionnaireObjectifs.getNbObjectifsRealises();
        } else {
            nbARetenir = 0;
        }

        String description;
        if (hardMode == true && objectifActuel != null) {
            description = objectifActuel.getDescription();
        } else {
            description = null;
        }

        if (etatJeu.estTerminee()) {
            FinPartieActivity.demarrer(
                    activite,
                    etatJeu.obtenirScore(),
                    etatJeu.obtenirNbCoups(),
                    graine,
                    nbARetenir,
                    description,
                    etatJeu.obtenirGrille(),
                    etatJeu.obtenirVerrous()
            );
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnReinitialisationObjectif) {
            reinitialiserObjectif();
        }
    }


    public void rafraichirAffichage() {
        tvScore.setText("Score : " + etatJeu.obtenirScore());
        tvCoups.setText("Coups : " + etatJeu.obtenirNbCoups());
        vueGrille.definirGrille(etatJeu.obtenirGrille());
        vueGrille.definirVerrous(etatJeu.obtenirVerrous());

        if (hardMode && objectifActuel != null) {
            if (tvObjectif != null){
                tvObjectif.setText(objectifActuel.getDescription());
            }
            if (tvNbObjectifs != null){
                tvNbObjectifs.setText("Objectifs réussis : " + gestionnaireObjectifs.getNbObjectifsRealises());
            }

            int nbCles = gestionnaireObjectifs.getNbCles();
            if (tvCles != null) {
                tvCles.setText("🗝️ Clés : " + nbCles + " (Max 1)");
            }
            if (btnReinitialisationObjectif != null) {
                // Grisé si 0 clé
                float alpha;
                if (nbCles >= 1) {
                    alpha = 1f; //  visible
                } else {
                    alpha = 0.4f; // grisé
                }
                btnReinitialisationObjectif.setAlpha(alpha);
            }
        }
    }

    public void sauvegarderEtat(Bundle out) {
        if (hardMode) {
            gestionnaireObjectifs.sauvegarder(out);
            if (objectifActuel != null) {
                objectifActuel.sauvegarder(out);
            }
        }
    }
}