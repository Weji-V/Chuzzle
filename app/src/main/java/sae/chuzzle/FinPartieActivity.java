package sae.chuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class FinPartieActivity extends Activity {

    private FinPartieControleur controleur;
    private Button btnMenu;

    /**
     * Méthode statique pour démarrer l'activité avec toutes les données nécessaires.
     */
    public static void demarrer(Activity source, int score, int nbCoups, long graine,
                                 int objectifsReussis, String objectifDescription,
                                 int[][] grille, boolean[][] verrous) {
        Intent intent = new Intent(source, FinPartieActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("nbCoups", nbCoups);
        intent.putExtra("graine", graine);
        intent.putExtra("objectifsReussis", objectifsReussis);
        intent.putExtra("objectifDescription", objectifDescription);

        // Aplatissement de la grille
        int nbLignes = grille.length;
        int nbCols = grille[0].length;
        int[] grillePlat = new int[nbLignes * nbCols];
        for (int l = 0; l < nbLignes; l++)
            for (int c = 0; c < nbCols; c++)
                grillePlat[l * nbCols + c] = grille[l][c];
        
        intent.putExtra("grille", grillePlat);
        intent.putExtra("nbLignes", nbLignes);
        intent.putExtra("nbCols", nbCols);

        // Aplatissement des verrous
        boolean[] verrousPlat = new boolean[nbLignes * nbCols];
        for (int l = 0; l < nbLignes; l++)
            for (int c = 0; c < nbCols; c++)
                verrousPlat[l * nbCols + c] = verrous[l][c];
        
        intent.putExtra("verrous", verrousPlat);

        source.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_partie);

        // Récupérer les données passées
        int score    = getIntent().getIntExtra("score", 0);
        int nbCoups  = getIntent().getIntExtra("nbCoups", 0);
        long graine  = getIntent().getLongExtra("graine", 0L);

        // Afficher les données textuelles
        TextView tvScore  = findViewById(R.id.tvFinScore);
        TextView tvCoups  = findViewById(R.id.tvFinCoups);
        TextView tvGraine = findViewById(R.id.tvFinGraine);

        tvScore.setText(String.valueOf(score));
        tvCoups.setText(String.valueOf(nbCoups));
        tvGraine.setText(String.valueOf(graine));

        // Objectif en cours (hard mode)
        String objectifDesc = getIntent().getStringExtra("objectifDescription");
        TextView tvLabelObjectif = findViewById(R.id.tvLabelObjectif);
        TextView tvFinObjectif   = findViewById(R.id.tvFinObjectif);
        if (objectifDesc != null) {
            tvLabelObjectif.setVisibility(android.view.View.VISIBLE);
            tvFinObjectif.setVisibility(android.view.View.VISIBLE);
            tvFinObjectif.setText(objectifDesc);
        }

        // Reconstruire et afficher la grille finale
        int nbLignes = getIntent().getIntExtra("nbLignes", 6);
        int nbCols   = getIntent().getIntExtra("nbCols", 6);
        int[] grillePlat   = getIntent().getIntArrayExtra("grille");
        boolean[] verrousPlat = getIntent().getBooleanArrayExtra("verrous");

        VueGrille vueGrilleFinale = findViewById(R.id.vueGrilleFinale);

        if (grillePlat != null && grillePlat.length == nbLignes * nbCols) {
            int[][] grille = new int[nbLignes][nbCols];
            for (int l = 0; l < nbLignes; l++)
                for (int c = 0; c < nbCols; c++)
                    grille[l][c] = grillePlat[l * nbCols + c];
            vueGrilleFinale.definirGrille(grille);
        }

        if (verrousPlat != null && verrousPlat.length == nbLignes * nbCols) {
            boolean[][] verrous = new boolean[nbLignes][nbCols];
            for (int l = 0; l < nbLignes; l++)
                for (int c = 0; c < nbCols; c++)
                    verrous[l][c] = verrousPlat[l * nbCols + c];
            vueGrilleFinale.definirVerrous(verrous);
        }


        controleur = new FinPartieControleur(this);
        btnMenu = findViewById(R.id.btnFinMenu);
        btnMenu.setOnClickListener(controleur);
    }

    @Override
    public void onBackPressed() {
        retourMenu();
    }

    public void retourMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
