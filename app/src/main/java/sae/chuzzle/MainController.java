package sae.chuzzle;

import android.content.Intent;
import android.view.View;

public class MainController implements View.OnClickListener {

    private final MainActivity activity;
    private final EtatJeu etatJeu;

    public MainController(MainActivity activity, EtatJeu etatJeu) {
        this.activity = activity;
        this.etatJeu = etatJeu;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnMenu) {
            etatJeu.forcerFinDePartie();
            Intent intent = new Intent(activity, MenuActivity.class);
            activity.startActivity(intent);
        }
    }
}
