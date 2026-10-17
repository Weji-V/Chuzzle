package sae.chuzzle;

import android.os.Bundle;

/**
 * Représente un objectif dynamique pour le mode Hard.
 */
public class Objectif {
    private final int couleur;         // Index de la couleur cible (0-6)
    private final int nbSeriesCible;   // Nombre de séries à réaliser
    private final int nbCoupsMax;      // Limite de coups à faire
    private final int poids;           // Probabilité d'apparition (poids pour le tirage)

    private int seriesRealisees = 0;
    private int coupsRestants;

    public Objectif(int couleur, int nbSeries, int nbCoupsMax, int poids) {
        this.couleur = couleur;
        this.nbSeriesCible = nbSeries;
        this.nbCoupsMax = nbCoupsMax;
        this.coupsRestants = nbCoupsMax;
        this.poids = poids;
    }

    // Getters
    public int getCouleur() {
        return couleur;
    }
    public int getNbSeriesCible() {

        return nbSeriesCible;
    }
    public int getNbCoupsMax() {
        return nbCoupsMax;
    }

    public int getCoupsRestants() {
        return coupsRestants;

    }
    public int getPoids() {
        return poids;

    }

    public void decrementerCoups() {

        if (coupsRestants > 0) coupsRestants--;
    }

    public void ajouterSeries(int n) {

        this.seriesRealisees += n;
    }

    public boolean estReussi() {

        return seriesRealisees >= nbSeriesCible;
    }

    public boolean estEchoue() {

        return coupsRestants <= 0 && !estReussi();
    }

    public String getDescription() {
        String nomCouleur = obtenirNomCouleur(couleur);
        return "Éclatez " + nbSeriesCible + " fois des Chuzzle " + nomCouleur + 
               " en moins de " + nbCoupsMax + " coups (" + seriesRealisees + "/" + nbSeriesCible + 
               ").\nCoups restants : " + coupsRestants;
    }

    private String obtenirNomCouleur(int c) {
        switch (c) {
            case 0: return "Gris";
            case 1: return "Rose";
            case 2: return "Vert";
            case 3: return "Bleu";
            case 4: return "Jaune";
            case 5: return "Orange";
            case 6: return "Rouge";
            default: return "Inconnue";
        }
    }

    // Persistance pour la rotation d'écran
    public void sauvegarder(Bundle out) {
        out.putInt("obj_couleur", couleur);
        out.putInt("obj_cible", nbSeriesCible);
        out.putInt("obj_max", nbCoupsMax);
        out.putInt("obj_restants", coupsRestants);
        out.putInt("obj_realise", seriesRealisees);
        out.putInt("obj_poids", poids);
    }

    public static Objectif restaurer(Bundle in) {
        if (in == null || !in.containsKey("obj_couleur")) return null;
        Objectif obj = new Objectif(
            in.getInt("obj_couleur"),
            in.getInt("obj_cible"),
            in.getInt("obj_max"),
            in.getInt("obj_poids")
        );
        obj.coupsRestants = in.getInt("obj_restants");
        obj.seriesRealisees = in.getInt("obj_realise");
        return obj;
    }
}
