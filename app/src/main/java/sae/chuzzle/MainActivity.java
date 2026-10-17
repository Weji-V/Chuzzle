package sae.chuzzle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Controleur controleur;
    private boolean hardMode;
    private EtatJeu etatJeu;
    private Button btnMenu;
    private VueGrille vueGrille;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Modèle
        long graine = getIntent().getLongExtra("graine", System.currentTimeMillis());
        hardMode = getSharedPreferences("chuzzle_prefs", MODE_PRIVATE).getBoolean("hard_mode", false);

        etatJeu = new EtatJeu(graine, hardMode);

        // --- Restauration si retour de pause
        if (savedInstanceState != null) {
            etatJeu.restaurerEtat(savedInstanceState);
        }

        // --- Vue
        vueGrille = findViewById(R.id.vueGrille);

        // --- Controleur métier
        controleur = new Controleur(
                this,
                etatJeu,
                vueGrille,
                graine,
                (TextView) findViewById(R.id.tvScore),
                (TextView) findViewById(R.id.tvCoups),
                hardMode
        );

        // --- INITIALISATION DE L'OBJECTIF
        controleur.initialiserObjectif(savedInstanceState);

        // --- Gestion tactile
        GestionnaireTactile gestionnaireTactile =
                new GestionnaireTactile(vueGrille, etatJeu, controleur);
        vueGrille.setOnTouchListener(gestionnaireTactile);

        // --- Controleur 
        btnMenu = findViewById(R.id.btnMenu);
        MainController mainController = new MainController(this, etatJeu);
        btnMenu.setOnClickListener(mainController);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (etatJeu != null) {
            etatJeu.sauvegarderEtat(outState);
        }
        if (controleur != null) {
            controleur.sauvegarderEtat(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
