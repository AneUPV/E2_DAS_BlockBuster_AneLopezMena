package com.example.entregaindividual_2_anelopezmena.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_Idioma;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_Reinicio;

import java.util.HashMap;
import java.util.Map;


/*******************************************************************/
/** -------------------- FRAG_PREFERENCIAS ---------------------- **/
/*******************************************************************/
// Clase JAVA que se corresponde con el fragmento del menú de AJUSTES/PREFERENCIAS.
// Se puede acceder al menú de preferencias desde el toolbar superior derecho del
// menú principal. Esta clase extenderá de 'PreferenceFragmentCompat' e implementará
// 'SharedPreferences' para notificar de los cambios en las preferencias del usuario

public class Frag_Preferencias extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Atributos privados de la clase
    private String idiomaActual, usuario, email;
    private Gestor_Idioma gestorIdioma;
    private RequestQueue rq;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL_server="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1) Método constructor (vacío)
    public Frag_Preferencias() { }

    //---------------------------------------------------------------------------------
    // 2) Método ON_CREATE_PREFERENCES: Se ejecutará cuando se cree el menu de preferencias
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        // Inicializar el gestor de idiomas
        gestorIdioma = new Gestor_Idioma(getActivity());
        // Instanciar el objeto RequestQueue para realizar consultas contra la DB
        rq = Volley.newRequestQueue(getActivity());
    //------------------------------------------------------------------------------------------
        // Recoger el intent lanzado desde el menú principal,
        // con el objteivo de recoger los datos del intent
        Intent i = getActivity().getIntent();
        Bundle extras = i.getExtras();

        // Recibir datos del intent enviado desde el menú principal de la aplicación
        if ( extras != null){
            // OBTENER: idiomaActual, email y usuario
            idiomaActual = extras.getString("var_idioma");
            email = extras.getString("var_email");
            usuario = extras.getString("var_usuario");
        }
    //------------------------------------------------------------------------------------------
        // Gestionar pérdida de información al girar la pantalla:
        // GUARDAR:
        //      * IDIOMA
        if (savedInstanceState != null){
            idiomaActual = savedInstanceState.getString("var_idioma");
        }
    //------------------------------------------------------------------------------------------

        // Una vez conseguidos los datos, actualizar idioma en el gestor
        gestorIdioma.setIdioma(idiomaActual);

        //En esta línea se indica cuál es el fichero XML donde están definidas las preferencias
        addPreferencesFromResource(R.xml.preferencias);
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_RESUME
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    //---------------------------------------------------------------------------------
    // 4) Método ON_PAUSE
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //---------------------------------------------------------------------------------
    // 5) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy() de la
    //    aplicación, por lo que es aquí dónde se deben guardar los datos que se quieran
    //    mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil, interrupciones, etc
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar datos en el objeto outState
        outState.putString("var_idioma", this.idiomaActual);
        outState.putString("var_email", this.email);
        outState.putString("var_usuario", this.usuario);
    }

    //---------------------------------------------------------------------------------
    // 6) Método ON_SHARED_PREFERENCE_CHANGED: Este método se ejecuta cada vez que se
    //    detecta un cambio en las preferencias del usuario. En el caso de BlockBuster!,
    //    las preferencias que se han implementado comprenden:
    //          1) Preferencia de idioma en la aplicación (general)
    //          2) Nombre de usuario personalizable
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            // El el caso de que se detecte un cambio en las preferencias del idioma
            case "miidioma":
                // Obtener selección
                sharedPreferences.getString(key, "es");
                // Lanzar dialog de alerta de Reinicio de app
                Dial_Reinicio d = new Dial_Reinicio(getActivity());
                d.show(getActivity().getSupportFragmentManager(), "ReiniciarApp");
                break;
            case "nombre":
                // Obtener selección
                String nombreNuevo = sharedPreferences.getString(key, "Usuario");

                // CONSULTA A LA DB REMOTA: Actualizar nombre de usuario
                String ruta = URL_server + "usuarios.php";
                //
                StringRequest sr = new StringRequest(
                Request.Method.POST, ruta, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("200")){
                            //Si ha ido bien
                            usuario = nombreNuevo;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Si ha habido algún error, indicarlo en el log de errores de la app
                        Log.e("DB_REMOTA", "Error al actualizar nombre de usuario");}
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        // Preparar parámetros para PHP
                        Map<String, String> parametros = new HashMap<String, String>();
                        // RECURSO: usuarios.php>actualizarUsuario
                        parametros.put("id_recurso", "actualizarUsuario");
                        parametros.put("nombre", nombreNuevo);
                        parametros.put("email", email);
                        // Enviar
                        return parametros;
                    }
                };
                // Añadir solicitud a la cola (RequestQueue)
                rq.add(sr);
                break;
            // Caso por defecto
            default:
                break;
        }
    }
}