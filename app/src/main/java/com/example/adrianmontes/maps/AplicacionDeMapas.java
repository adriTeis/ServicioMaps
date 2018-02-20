package com.example.adrianmontes.maps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class AplicacionDeMapas extends FragmentActivity implements OnMapReadyCallback,View.OnClickListener {

    //------- Primero hay que coger una clave de la api de google--------
    //-------Despues ponemos la clave recivida en el XML Res/Values/Google_maps_api.com
    private GoogleMap mMap;
    private Button mapa;
    private Button terreno;
    private Button hibrido;
    LocationManager locationManager;
    Location location;
    LocationListener locationListener;
    AlertDialog Alert;
    ObtenerWebService hiloconexion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aplicacion_de_mapas);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        AlertaNoGPS();
        mapFragment.getMapAsync(this);
        hibrido = (Button)findViewById(R.id.hibrido);
        mapa = (Button) findViewById(R.id.Mapa);
        terreno = (Button) findViewById(R.id.terreno);
        hibrido.setOnClickListener(this);
        mapa.setOnClickListener(this);
        terreno.setOnClickListener(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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


    public void onClick(View v){
        switch (v.getId()){

            case R.id.Mapa:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.terreno:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.hibrido:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;




        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //En este metodo de google, cuando inicia la aplicacion nos carga en la variable nMap el mapa de google
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


    }
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
                Toast.makeText(AplicacionDeMapas.this,"la Aplicacion no funcionara",Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        Alert=builder.create();
        Alert.show();

    }
    private void Mostrarlocalizacion(Location location) {
        //En este metodo a traves del mapa de google cargo la posicion en una variable LatLng, que se le pasa
        //a un metodo para que nos muestre esa posicion en el mapa
        if (location != null) {
            // Add a marker in Sydney and move the camera
            //le paso mi posicion a una variable
            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            //Cargo mi posicion en el puntero del mapa
            mMap.addMarker(new MarkerOptions().position(pos).title("Marker in Sydney"));
            //Muevo la camara a esa posicion
            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            Log.i("GPSAA", "LAStPosition: " + location.getLatitude() + ", " + location.getLongitude());
            //añado Zoom
            mMap.moveCamera(CameraUpdateFactory.zoomBy(8));

        } else {
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

            //super.onPostExecute(aVoid);
        }

        @Override

        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }


    }
    //En este metodo que sobreescribimos Cada vez que vuelve a pausa se ejecuta de nuevo, para no quedarnos sin bateria
    @Override
    protected void onPause(){
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
