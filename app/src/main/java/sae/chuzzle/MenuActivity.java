package sae.chuzzle;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class MenuActivity extends Activity {

    private Button btnNouvellePartie;
    private Button btnPartieGraine;
    private Button btnOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnNouvellePartie = findViewById(R.id.btnNouvellePartie);
        btnPartieGraine = findViewById(R.id.btnPartieGraine);
        btnOptions = findViewById(R.id.btnOptions);

        MenuController controller = new MenuController(this);
        btnNouvellePartie.setOnClickListener(controller);
        btnPartieGraine.setOnClickListener(controller);
        btnOptions.setOnClickListener(controller);
    }
}