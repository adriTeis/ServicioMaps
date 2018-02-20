package com.example.adrianmontes.maps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    Button datos;
    EditText latitud;
    EditText longitud;
    TextView resultado;
    ObtenerWebService hiloconexion;
    LocationManager locationManager;
    Location location;
    LocationListener locationListener;
    AlertDialog Alert;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("GPSaa","Inicio del GPS");
        //doy la opcion para activar el GPS
        AlertaNoGPS();
        datos = (Button) findViewById(R.id.datos);
        latitud = (EditText) findViewById(R.id.latitud);
        longitud = (EditText) findViewById(R.id.longitud);
        resultado = (TextView) findViewById(R.id.resultado);
        datos.setOnClickListener(this);
        // devuelve un objeto location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Saco la lista deproobeadores que tenemos
        List<String> listaProobedores = locationManager.getAllProviders();
        StringBuilder listaProviders = new StringBuilder();
        for (int i = 0; i < listaProobedores.size(); i++) {
            listaProviders.append("\n" + listaProobedores.get(i) + "\n");
        }
        resultado.setText(listaProviders.toString());
        //comprobamos si la version de android es superior a 23
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
        } else {
            //-------OBTENGO LA ULTIMA POSICION QUE ME DA EL GPS---
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.i("GPSaa", String.valueOf(location));
            Mostrarlocalizacion(location);



        }


        locationListener = new LocationListener() {
            //se ejecuta cuando cambia la localizacion
            @Override
            public void onLocationChanged(Location location) {
                Mostrarlocalizacion(location);

                Log.i("GPSAA","La localizacion ha cambiado");





            }

            //cuando cambia el estado del gps, ya sea desactivado o algo
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("GPSAA", "cambio de estado");
            }

            @Override
            public void onProviderEnabled(String provider) {

                Log.i("GPSAA", "Se ha activado");

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("GPSAA", "Se ha desactivado");
            }

        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    private void Mostrarlocalizacion(Location location) {
        if (location != null) {
            hiloconexion = new ObtenerWebService();
            hiloconexion.execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            Log.i("GPSAA", "LAStPosition: " + location.getLatitude() + ", " + location.getLongitude());

        } else {
            resultado.setText("NO HAY POSICION");
        }


    }


    @Override

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.datos:
                hiloconexion = new ObtenerWebService();
                hiloconexion.execute(latitud.getText().toString(), longitud.getText().toString());   // Parámetros que recibe doInBackground
                break;


            default:
                break;

        }

    }


    //recive una cadena de String, onPorogressUpdate recive un Integer, y el ultmimo String es del OnPostExecute
    public class ObtenerWebService extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            String cadena = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";

            //http://maps.googleapis.com/maps/api/geocode/json?latlng=42.2311895,-8.7339075&sensor=false
            cadena = cadena + params[0];
            cadena = cadena + ",";
            cadena = cadena + params[1];
            cadena = cadena + "&sensor=false";
            String devuelve = "";

            URL url = null; // Url de donde queremos obtener información

            try {
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Abrir la conexión
                connection.setRequestProperty("User-Agent", "Mozilla/5.0" +
                        " (Linux; Android 1.5; es-ES) Ejemplo HTTP");
                //connection.setHeader("content-type", "application/json");

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();
                if (respuesta == HttpURLConnection.HTTP_OK) {

                    InputStream in = new BufferedInputStream(connection.getInputStream());  // preparo la cadena de entrada
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    // El siguiente proceso lo hago porque el JSONOBject necesita un String y tengo
                    // que tranformar el BufferedReader a String. Esto lo hago a traves de un
                    // StringBuilder.

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);        // Paso toda la entrada al StringBuilder
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados
                    JSONArray resultJSON = respuestaJSON.getJSONArray("results");   // results es el nombre del campo en el JSON
                    //Vamos obteniendo todos los campos que nos interesen.
                    //En este caso obtenemos la primera dirección de los resultados.

                    String direccion = "SIN DATOS PARA ESA LONGITUD Y LATITUD";
                    if (resultJSON.length() > 0) {
                        direccion = resultJSON.getJSONObject(0).getString("formatted_address");    // dentro del results pasamos a Objeto la seccion formated_address
                    }
                    devuelve = "Dirección: " + direccion;   // variable de salida que mandaré al onPostExecute para que actualice la UI

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return devuelve;
        }

        @Override
        protected void onCancelled(String aVoid) {
            super.onCancelled(aVoid);
        }

        @Override

        protected void onPostExecute(String aVoid) {
            resultado.setText(aVoid);
            //super.onPostExecute(aVoid);
        }

        @Override

        protected void onPreExecute() {
            resultado.setText("");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }


    }

    //En este metodo que sobreescribimos Cada vez que vuelve a pausa se ejecuta de nuevo
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.removeUpdates(locationListener);
            }
        } else {
            locationManager.removeUpdates(locationListener);
        }
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    // Me pregunta en esta linea si quiero activar el GPS
    private  void AlertaNoGPS(){
        //En esta linea me pregunta sio quiero activar el GPS
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El GPS esta desactivado, ¿Desea Activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"la Aplicacion no funcionara",Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        Alert=builder.create();
        Alert.show();

    }


    //En este metodo que sobreescribimos Cada vez que vuelve del estado de pausa se ejecuta de nuevo
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }

    }
}