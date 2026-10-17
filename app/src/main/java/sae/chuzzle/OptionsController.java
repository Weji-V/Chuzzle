package sae.chuzzle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;

public class OptionsController
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private final OptionsActivity activity;
    private final SharedPreferences prefs;

    public OptionsController(OptionsActivity activity, SharedPreferences prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    @Override
    public void onCheckedChanged(CompoundButton bouton, boolean estCoche) {
        int id = bouton.getId();
        if (id == R.id.checkHard) {
            prefs.edit().putBoolean("hard_mode", estCoche).apply();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnBack) {
            Intent intent = new Intent(activity, MenuActivity.class);
            activity.startActivity(intent);
        }
    }
}
