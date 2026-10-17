package sae.chuzzle;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Contrôleur pour la saisie de la graine.
 * Gère la validation de la saisie et la navigation.
 */
public class SeedControleur implements View.OnClickListener {

    private final SeedActivity activite;
    private final EditText etGraine;

    public SeedControleur(SeedActivity activite, EditText etGraine) {
        this.activite = activite;
        this.etGraine = etGraine;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.btnJouer) {
            traiterGraine();
        } else if (id == R.id.btnBack) {
            activite.retourMenu();
        }
    }

    /**
     * Valide le texte saisi et demande à l'activité de lancer la partie.
     */
    private void traiterGraine() {
        String texte = etGraine.getText().toString().trim();

        if (texte.isEmpty()) {
            Toast.makeText(activite, "Veuillez entrer une graine.", Toast.LENGTH_SHORT).show();
            return;
        }

        long graine;
        try {
            graine = Long.parseLong(texte);
        } catch (NumberFormatException e) {

            graine = texte.hashCode();
        }

        activite.lancerPartie(graine);
    }
}
