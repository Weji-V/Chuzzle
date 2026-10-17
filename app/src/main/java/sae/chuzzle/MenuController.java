package sae.chuzzle;

import android.content.Intent;
import android.view.View;

public class MenuController implements View.OnClickListener {

    private final MenuActivity activity;

    public MenuController(MenuActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnNouvellePartie) {
            long graine = System.currentTimeMillis();
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("graine", graine);
            activity.startActivity(intent);
        } else if (id == R.id.btnPartieGraine) {
            Intent intent = new Intent(activity, SeedActivity.class);
            activity.startActivity(intent);
        } else if (id == R.id.btnOptions) {
            Intent intent = new Intent(activity, OptionsActivity.class);
            activity.startActivity(intent);
        }
    }
}
