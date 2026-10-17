package sae.chuzzle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

public class OptionsActivity extends Activity {

    private SharedPreferences prefs;
    private CheckBox checkHard;
    private Button btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        prefs = getSharedPreferences("chuzzle_prefs", MODE_PRIVATE);

        checkHard = findViewById(R.id.checkHard);
        btnRetour = findViewById(R.id.btnBack);

        checkHard.setChecked(prefs.getBoolean("hard_mode", false));

        OptionsController controller = new OptionsController(this, prefs);
        checkHard.setOnCheckedChangeListener(controller);
        btnRetour.setOnClickListener(controller);
    }
}
