package sae.chuzzle;

import android.app.Activity;
import android.view.View;

/**
 * Contrôleur pour l'écran de fin de partie.
 * Gère les interactions utilisateur
 */
public class FinPartieControleur implements View.OnClickListener {

    private final Activity activity;

    public FinPartieControleur(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnFinMenu) {
            ((FinPartieActivity) activity).retourMenu();
        }
    }
}
