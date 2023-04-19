package com.example.entregaindividual_2_anelopezmena.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;
import android.widget.Toast;

import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.databinding.ActivityActMapa2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

/*************************************************************************/
/** ---------------------------- ACT_MAPA ----------------------------- **/
/*************************************************************************/
// Actividad correspondiente al mapa de Google. Mediante esta actividad de mapa
// se mostrarán varios marcadores que se corresponden con diferentes instalaciones
// de los cines "BlockBuster!" a nivel nacional (Marcadores naranjas), así como la
// localización actual del usuario (Marcador azul, servicio de GEOLOCALIZACIÓN).
// Esta actividad extenderá de 'FragmentActivity' e implementará al mismo tiempo
// los métodos de 'OnMapReadyCallback'

public class Act_Mapa extends FragmentActivity implements OnMapReadyCallback {

    // Atributos privados
    private GoogleMap mMap;
    private ActivityActMapa2Binding binding;

    // Variables globales para almacenar Coordenadas
    double lat_previa  = 0.0;
    double long_previa = 0.0;
    // Guardar posición previa para poder eliminarla cuando el usuario se
    // desplace a otro punto del mapa
    Marker marcador_previo;

    // Para utilizar los servicios de Google Play y obtener la localización del usuario,
    // se debe crear un Proveedor de posiciones (Clase FusedLocationProviderClient)
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    // Código para solicitar permisos
    int LOCATION_REQUEST_CODE = 100;
    boolean primerZoom = false;

    // -----------------------------------------------------------------------------------
    // 1) Método LOCATION_CALLBACK: Aquí se obtiene la ubicación actual del usuario y
    // marca su posición actual en el mapa. Para ello se utiliza el locationResult
    // que llega de parámetro de entrada
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            // Si las cordenadas recibidas no son válidas, no hará nada
            return;
        }
        // Si son válidas, obtener latitud y longitud de la última posición conocida
        double lat = locationResult.getLastLocation().getLatitude();
        double lng = locationResult.getLastLocation().getLongitude();

        // Actualizar marcador
            marcarUbicacionActual(new LatLng(lat, lng));
        }
    };

    // -----------------------------------------------------------------------------------
    // 2) Método MARCAR_UBICACION_ACTUAL: Método que configura el marcador a dibujar en el mapa
    // y elimina el anterior en el caso de que el usuario se haya movido de posición.
    private void marcarUbicacionActual(LatLng latLng) {

        MarkerOptions markerOption = new MarkerOptions();
        markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Si hay un punto anterior, eliminarlo del mapa
        if (marcador_previo != null){
            marcador_previo.remove();
        }
        //Niveles de ZOOM para la cámara:
        // 1: Mundo
        // 5: Continente
        // 10: Ciudad
        // 15: Calles
        // 20: Edificios
        if (!primerZoom) {
            primerZoom = true;
            // Desplazar cámara hasta el nuevo punto y hacer zoom)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

    }
        // Añadir el marcador azul del usuario
        marcador_previo = mMap.addMarker(markerOption.position(latLng).title(getResources().getString(R.string.miUbicacion)));
        // Guardar nuevas coordenadas para poder comparar si el usuario se ha movido
        lat_previa = latLng.latitude;
        long_previa = latLng.longitude;

        // Imprimir coordenadas en la pantalla.
        // Esto se hace para verificar la funcionalidad de geolocalización
        TextView tv_latitud = binding.getRoot().findViewById(R.id.latitud);
        TextView tv_longitud = binding.getRoot().findViewById(R.id.longitud);
        tv_latitud.setText("LAT: "+lat_previa);
        tv_longitud.setText("LONG: "+long_previa);
    }

    // -----------------------------------------------------------------------------------
    // 3) Método ON_CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Inicializar locationRequest
        locationRequest= LocationRequest.create();

        // Indicar un periodo de actualización de la localización
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(1000);

        // Settear prioridad de la geolocalización
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Obtener útlima ubicación conocida del usuario
        askLocationPermission();

        // Obtener vista y setContentView
        binding = ActivityActMapa2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para usar.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // -----------------------------------------------------------------------------------
    // 4) Método ON_START: Este método se ejecutará cuando el mapa esté preparado para usarse
    //  Aquí se preguntará al usuario por los permisos de localización.
    @Override
    protected void onStart() {
        super.onStart();
        // Si los permisos de localización han sido concedidos, obtener la ubicación
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtener útlima ubicación conocida del usuario
            getLastLocation();
        } else {
            // Si no se tienen los permisos, pedirlos
            askLocationPermission();
        }
    }

    // -----------------------------------------------------------------------------------
    // 5) Método ASK_LOCATION_PERMISION: Método para pedir permisos al usuario en caso de no
    // tenerlos. Aquí se preguntará al usuario por los permisos de localización.
    private void askLocationPermission() {
        // Si los permisos no son concedidos, volver
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Si se tienen, obtener última localización conocida del usuario
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Si ha habido éxito, llamar a actualizar la posición en el mapa
                actualizarPosicion();
                if (location != null){
                    // La locaclización es distinta de null, es VÁLIDA, imprimirla
                    // (Monitorización por LOGS)
                    Log.d("GEO", "onSuccess: "+ location.toString());
                    Log.d("GEO", "onSuccess: LAT"+ location.getLatitude());
                    Log.d("GEO", "onSuccess: LONG"+ location.getLongitude());
                }
                else{
                    Log.d("GEO", "Localización es NULL");
                }
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Si no ha habido éxito, escribir en el Log sobre el error
                Log.e("GEO", "onFailure:"+e.getLocalizedMessage());
            }
        });

    }
    // -----------------------------------------------------------------------------------
    // 6) Método GET_LAST_LOCATION: Método que obtendrá la localización actual del usuario.
    private void getLastLocation() {
        // Si no se tienen los permisos necesarios
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Pedir los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, getResources().getString(R.string.permisosMaps), Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }
    }
    // -----------------------------------------------------------------------------------
    // 7) Método ON_REQUEST_PERMISSIONS_RESULT: Verifica el resultado del proceso de petición de
    // permisos para el mapa y la geolocalización.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permiso concedido
                getLastLocation();
            }else{
                // Permiso NO concedido
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // 8) Método ON_MAP_READY: En este método, se configura el mapa y se dibujan marcadores
    // para señalar distintos cines a lo largo del mapa
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Cambiar el tipo de mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = mMap.getUiSettings();

        // Añadir coordenadas de varios cines
        Pair<LatLng, String> c1 = new Pair(new LatLng(43.2678, -2.9407), "BLOCKBUSTER ZUBIARTE, Bilbao");
        Pair<LatLng, String> c2 = new Pair(new LatLng(43.2873, -3.0074), "BLOCKBUSTER MAX OCIO, Barakaldo");
        Pair<LatLng, String> c3 = new Pair(new LatLng(43.2934, -3.0051), "BLOCKBUSTER CINES, Barakaldo");
        Pair<LatLng, String> c4 = new Pair(new LatLng(43.2603, -2.9409), "BLOCKBUSTER, Bilbao");
        Pair<LatLng, String> c5 = new Pair(new LatLng(41.4072, 2.1386), "BLOCKBUSTER Balbes, Barcelona");
        Pair<LatLng, String> c6 = new Pair(new LatLng(41.3938, 2.1362), "BLOCKBUSTER diagonal, Barcelona");
        Pair<LatLng, String> c7 = new Pair(new LatLng(40.4250, -3.7129), "BLOCKBUSTER Renoir Princesa, Madrid");
        Pair<LatLng, String> c8 = new Pair(new LatLng(40.4204, -3.7066), "BLOCKBUSTER Gran Via, Madrid");
        Pair<LatLng, String> c9 = new Pair(new LatLng(37.3411, -5.9866), "BLOCKBUSTER Premium Lagoh, Sevilla");
        Pair<LatLng, String> c10 = new Pair(new LatLng(39.4698, -0.3573), "BLOCKBUSTER Babel, Valencia");
        Pair<LatLng, String> c11 = new Pair(new LatLng(38.9051, -6.3631), "BLOCKBUSTER Victoria, Mérida, Badajoz");
        Pair<LatLng, String> c12 = new Pair(new LatLng(36.7219, -4.4170), "BLOCKBUSTER Albéniz, Málaga");
        Pair<LatLng, String> c13 = new Pair(new LatLng(40.4204, -3.7066), "BLOCKBUSTER Capitol Gran Vía, Madrid");
        Pair<LatLng, String> c14 = new Pair(new LatLng(42.3427, -3.6887), "BLOCKBUSTER Van Golem, Burgos");
        Pair<LatLng, String> c15 = new Pair(new LatLng(43.0739, -7.5886), "BLOCKBUSTER Cines Cristal, Lugo");
        Pair<LatLng, String> c16 = new Pair(new LatLng(39.8728, -7.2866), "BLOCKBUSTER Cinebox, Portugal");
        Pair<LatLng, String> c17 = new Pair(new LatLng(43.3047, -2.8901), "BLOCKBUSTER Gure Aretoa, Derio");
        Pair<LatLng, String> c18 = new Pair(new LatLng(28.6223, -16.2562), "BLOCKBUSTER Multicines, Tenerife");
        Pair<LatLng, String> c19 = new Pair(new LatLng(21.30261319791836, -157.85474549420528), "BLOCKBUSTER TITAN LUXE, Honolulu");

        // Añadir los pares a la lista de Cines
        ArrayList<Pair<LatLng, String>> listaCines = new ArrayList<Pair<LatLng, String>>();
        listaCines.add(c1);   listaCines.add(c2);   listaCines.add(c3);   listaCines.add(c4);
        listaCines.add(c5);   listaCines.add(c6);   listaCines.add(c7);   listaCines.add(c8);
        listaCines.add(c9);   listaCines.add(c10);  listaCines.add(c11);  listaCines.add(c12);
        listaCines.add(c13);  listaCines.add(c14);  listaCines.add(c15);  listaCines.add(c16);
        listaCines.add(c17);  listaCines.add(c18);  listaCines.add(c19);

        // Para cada par en la lista de cines, crear un marcador con la configuraciñon indicada
        for (Pair<LatLng, String> punto : listaCines) {
            MarkerOptions markerOption = new MarkerOptions();
            markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mMap.addMarker(markerOption.position(punto.first).title(punto.second).snippet("Cines BLOCKBUSTER!"));
        }

    }
    // -----------------------------------------------------------------------------------
    // 9) Método ACTUALIZAR_POSICIÓN: Si se tienen los permisos, en este método se le
    // solicita la actualización del mapa
    private void actualizarPosicion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {;
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

}