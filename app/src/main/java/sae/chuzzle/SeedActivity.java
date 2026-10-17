package sae.chuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;


public class SeedActivity extends Activity {

    private EditText etGraine;
    private Button btnJouer;
    private Button btnRetour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seed);

        etGraine = findViewById(R.id.etGraine);
        btnJouer = findViewById(R.id.btnJouer);
        btnRetour = findViewById(R.id.btnBack);
        SeedControleur controleur = new SeedControleur(this, etGraine);
        btnRetour.setOnClickListener(controleur);
        btnJouer.setOnClickListener(controleur);
    }

    public void lancerPartie(long graine) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("graine", graine);
        startActivity(intent);
    }
    public void retourMenu() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}