package com.argentuino.ingreso;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AlarmaActivity extends AppCompatActivity {

    private EditText editTextAlarma;
    private Button alarmaON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        editTextAlarma = (EditText) findViewById(R.id.editTextAlarma);
        alarmaON = (Button) findViewById(R.id.buttonAlarma);

        alarmaON.setOnClickListener(new View.OnClickListener(){
                                       @Override
                                       public void onClick(View v){
                                           Bundle bundleIdUsuario = getIntent().getExtras();
                                           String messageIdUsuario = bundleIdUsuario.getString("id");
                                           Bundle bundleDispositivo = getIntent().getExtras();
                                           int messageDispositivo = bundleDispositivo.getInt("dispositivo");

                                           String messageAlarma = editTextAlarma.getText().toString();
                                           Intent intent = new Intent(AlarmaActivity.this, AstunaActivity.class);
                                           intent.putExtra("alarma", messageAlarma);
                                           intent.putExtra("id", messageIdUsuario);
                                           intent.putExtra("dispositivo", messageDispositivo);
                                           startActivity(intent);
                                       }
                                   }
        );

    }
}
