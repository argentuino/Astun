package com.argentuino.ingreso;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class AstunaActivity extends AppCompatActivity {

    private int alarma;
    private String id_usuario;
    private TextView temperatura,textViewAlarma;
    private ImageButton alarmaButton;
    private String urlTemp;
    private final static int INTERVAL = 1000 * 5; //5 segundos
    private Handler mHandler;
    private static Boolean alarmaBool = false;
    private Boolean alarmaSeteadaBool=false;
    private Spinner spinnerDispositivos;
    private String urlDispositivos;
    private String[] values;
    private Boolean posicionBool=false;
    private Boolean spinnerBool=false;
    private int dispositivoSeleccionado; //ubicacion del dispositivo seleccionado
    private ArrayAdapter dataAdapter;
    private String dispositivos; //lista de dispositivos separado por ","
    ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_astuna);

        temperatura = (TextView) findViewById(R.id.TextViewTemperatura);
        textViewAlarma = (TextView) findViewById(R.id.textViewAlarma);
        alarmaButton = (ImageButton) findViewById(R.id.ImageButtonAlarma);
        spinnerDispositivos = (Spinner) findViewById(R.id.spinnerDispositivos);

        Bundle bundleId = getIntent().getExtras();
        id_usuario=bundleId.getString("id");

        urlDispositivos = "http://astun.com.ar/astuna01/jsonDispositivosId.php?id_usuario=" + bundleId.getString("id");
        new AstunaActivity.JSONTaskDispositivos().execute(urlDispositivos);

        if(alarmaBool){
            Bundle bundleAlarma = getIntent().getExtras();
            textViewAlarma.setText(bundleAlarma.getString("alarma"));

            bundleId = getIntent().getExtras();
            id_usuario=bundleId.getString("id");

            Bundle bundlePosicion = getIntent().getExtras();
            dispositivoSeleccionado=bundlePosicion.getInt("dispositivo");
            posicionBool=true;

            urlDispositivos = "http://astun.com.ar/astuna01/jsonDispositivosId.php?id_usuario=" + bundleId.getString("id");
            new AstunaActivity.JSONTaskDispositivos().execute(urlDispositivos);

            alarmaSeteadaBool=true;
        }

        ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        final Runnable tempRun = new Runnable() {
            public void run() {
                if(spinnerBool){
                    urlTemp="http://www.astun.com.ar/astuna01/jsonDescDispositivo.php?descripcion=\"" + spinnerDispositivos.getSelectedItem() + "\"";
                    new JSONTaskTemperatura().execute(urlTemp);
                }
            }
        };
        final ScheduledFuture<?> tempHandle =
                scheduler.scheduleAtFixedRate(tempRun, 0, 2, TimeUnit.SECONDS);
        scheduler.schedule(new Runnable() {
            public void run() { tempHandle.cancel(true); }
        }, 60 * 60, TimeUnit.SECONDS);


        alarmaButton.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View v){
                                                alarmaBool=true;
                                                setAlarma();
                                            }
                                        }
        );

    }

    public void setAlarma() {
        Intent intent = new Intent(this, AlarmaActivity.class);
        intent.putExtra("id", id_usuario);
        intent.putExtra("dispositivo", spinnerDispositivos.getSelectedItemPosition());
        startActivity(intent);
    }

    Runnable mHandlerTaskTemperatura = new Runnable()
    {
            @Override
            public void run() {

                new JSONTaskTemperatura().execute(urlTemp);
                mHandler.postDelayed(mHandlerTaskTemperatura, INTERVAL);
           }
    };

    void startRepeatingTaskTemperatura()
    {
        mHandlerTaskTemperatura.run();
    }

    void stopRepeatingTaskTemperatura()
    {
        mHandler.removeCallbacks(mHandlerTaskTemperatura);
    }

    public class JSONTaskTemperatura extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line="";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                return finalJson;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } /*catch (JSONException e) {
                e.printStackTrace();
            } */finally {
                if(connection!=null){
                    connection.disconnect();
                }
                try {
                    if(reader!=null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String resultEnt=result.substring(0,2);
            String resultDec=result.substring(3,4);
            String resultTun=resultEnt+","+resultDec;
            temperatura.setText(resultTun);

            if(alarmaSeteadaBool) {
                if (Integer.parseInt(textViewAlarma.getText().toString()) > Integer.parseInt(resultEnt)) {
                    temperatura.setTextColor(Color.RED);
                }
            }
        }
    }

    Runnable mHandlerTaskDispositivos = new Runnable()
    {
        @Override
        public void run() {
            new AstunaActivity.JSONTaskDispositivos().execute(urlDispositivos);
            mHandler.postDelayed(mHandlerTaskDispositivos, INTERVAL);
        }
    };

    void startRepeatingTaskDispositivos()
    {
        mHandlerTaskDispositivos.run();
    }

    void stopRepeatingTaskDispositivos()
    {
        mHandler.removeCallbacks(mHandlerTaskDispositivos);
    }

    public class JSONTaskDispositivos extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line="";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                return finalJson;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(connection!=null){
                    connection.disconnect();
                }
                try {
                    if(reader!=null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
                dispositivos=result;
                values = stringToArray(dispositivos);
                cargarSpinner(values);
        }

    }

    public String[] stringToArray(String result){

        values = result.split(",");
        return values;
    }

    public void cargarSpinner(String[] values) {

        dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, values);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDispositivos.setAdapter(dataAdapter);
        spinnerBool=true;
        if(posicionBool){
            spinnerDispositivos.setSelection(dispositivoSeleccionado);
        }
    }

}

