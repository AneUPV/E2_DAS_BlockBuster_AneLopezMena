package com.example.entregaindividual_2_anelopezmena.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_DB;
import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_Idioma;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_CambiarCaratula;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_FaltaTituloDirector;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_PeliculaYaExiste;
import com.example.entregaindividual_2_anelopezmena.interfaces.Interfaz_Dial;
import com.example.entregaindividual_2_anelopezmena.java.Pelicula;

import java.util.HashMap;
import java.util.Map;

/*************************************************************************/
/** --------------------ACTIVITY_AÑADIR_PELICULA ---------------------- **/
/*************************************************************************/
// Para acceder a la actividad de añadir una película, se debe pulsar el Floating
// Action Button de la parte inferior derecha del menú principal de la app (+)
// Pulsando sobre el botón, se muestra la actividad desde la que añadir una película
// al registro. En esta segunda entrega, las nuevas peículas se añadirán en una base de
// datos remota.
//
// En esta nueva pantalla, el usuario podrá indicar todos los detalles
// de la película (titulo, año, director, actores...) y guardarlos fácilmente pulsando
// el botón rojo de 'GUARDAR'. Esta clase extiende a 'AppCompatActivity'


public class Act_AñadirPelicula extends AppCompatActivity implements Interfaz_Dial {

    // Atributos privados
    private EditText et_titulo,et_anyo, et_genero, et_duracion, et_director, et_actores, et_sinopsis, et_trailer;
    private RatingBar rb_valoracion;
    private CheckBox cb_visto;
    private ImageView iv_caratula;
    private String visto = "0";
    private Uri caratulaUri;
    private Gestor_Idioma gestorIdioma;
    private String idiomaActual, email;
    private String caratulaTemp;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1) Método ON_CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se indica el idioma por defecto, que será el ESPAÑOL
        idiomaActual = "es";

        // Crear gestores: IDIOMA
        gestorIdioma = new Gestor_Idioma(this);

        //---------------------------------------------------------------------------------
        // Recoger intent lanzado desde el menú principal
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Obtener email y nombre del usuario loggeado
            idiomaActual = extras.getString("var_idioma");
            email = extras.getString("var_email");
            gestorIdioma.setIdioma(idiomaActual);
        }
        //---------------------------------------------------------------------------------
        //  Gestionar pérdida de información al girar la pantalla:
        //  --> GUARDAR:
        //        * EMAIL
        //        * IDIOMA
        //        * CARÁTULA
        if (savedInstanceState != null){
            idiomaActual = savedInstanceState.getString("var_idioma");
            email = savedInstanceState.getString("var_email");
            caratulaTemp = savedInstanceState.getString("var_caratula");
            // Settear idioma actual en la aplicación
            gestorIdioma.setIdioma(idiomaActual);
        }
        //---------------------------------------------------------------------------------
        // Inflar la vista con el layout (act_anadir_pelicula_layout.xml)
        setContentView(R.layout.act_anadir_pelicula_layout);

        // Tras ello, obtener elementos del layout
        et_titulo = findViewById(R.id.add_titulo);

        iv_caratula = findViewById(R.id.add_caratula);
        et_anyo = findViewById(R.id.add_anyo);
        et_genero = findViewById(R.id.add_genero);
        et_duracion = findViewById(R.id.add_duracion);
        et_director = findViewById(R.id.add_director);
        et_actores = findViewById(R.id.add_actores);
        et_sinopsis = findViewById(R.id.add_trama);
        et_trailer = findViewById(R.id.add_url);
        rb_valoracion = findViewById(R.id.add_valoracion);
        cb_visto = findViewById(R.id.add_visto);

        // Imagen genérica, para las películas sin carátula
        if (caratulaTemp == null){
            caratulaUri =  Uri.parse("android.resource://com.example.entregaindividual_2_anelopezmena/drawable/vacio");
        }else{
            caratulaUri =  Uri.parse(caratulaTemp);
        }
        Glide.with(this).load(caratulaUri).into(iv_caratula);
    }

    //---------------------------------------------------------------------------------
    // 2) Método VOLVER: Se ejecuta al pulsar el pequeño botón negro con el icono de
    // la fecha (<--). Hace 'finish()' de la activity
    public void volver(View v) {
        finish();
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_CLICK: Se ejecuta al pulsar el botón ROJO 'GUARDAR'.
    // Este método gestionará la recogida de datos de los Views de la Activity, y el
    // envío de los datos actualizados, bien a la activity anterior, como a la base de datos
    public void onClick(View v){

        // Obtener texto de los elementos del layout
        String tit = et_titulo.getText().toString();
        String anyo = et_anyo.getText().toString();
        String genero = et_genero.getText().toString();
        String dur = et_duracion.getText().toString();
        String dir =  et_director.getText().toString();
        String act = et_actores.getText().toString();
        String trama = et_sinopsis.getText().toString();
        float val2 = rb_valoracion.getRating();
        String val = String.valueOf(val2);
        String url = et_trailer.getText().toString();

        // Obtener valor del checkbox de 'visto'
        if ( cb_visto.isChecked() ){ visto = "1";}
        else{ visto="0";}

        // Si se han proporcionado los datos obligatorios (Titulo y director)
        if ( !tit.equals("") && !dir.equals("")){

            // 1ERA PETICIÓN A DB REMOTA: Comprobar si la película existe
            RequestQueue rq = Volley.newRequestQueue(this);
            String ruta = URL + "peliculas.php";
            Context contexto = this;

            StringRequest sr = new StringRequest(
            Request.Method.POST, ruta, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                if (response.length()<=0) {
                    // Si la película NO está registrada en la DB, añadirla y notificar a
                    // los demás usuarios por mensajería FCM de que hay novedades en la cartelera
                    RequestQueue rq = Volley.newRequestQueue(contexto);
                    // Indicar recurso PHP del servidor
                    String ruta = URL + "enviarNotificacion.php";
                    StringRequest sr = new StringRequest(
                    Request.Method.POST, ruta, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                        // Devolver los datos a la pantalla del menú principal (CODE:50)
                            Intent i = new Intent();
                            i.putExtra("var_titulo", tit);
                            i.putExtra("var_caratula", caratulaTemp);
                            i.putExtra("var_director", dir);
                            i.putExtra("var_actores", act);
                            i.putExtra("var_genero", genero);
                            i.putExtra("var_trama", trama);
                            i.putExtra("var_anyo", anyo);
                            i.putExtra("var_visto", visto);
                            i.putExtra("var_valoracion", val);
                            i.putExtra("var_duracion", dur);
                            i.putExtra("var_trailer", url);

                            /**    # (CODE:50) #    **/
                            // Settear resultado para atrapar el intent en la otra actividad
                            setResult(50, i);
                            // Hacer finish de la Activity para que la otra reciba los datos del Intent
                            finish();

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Ha habido un error enviando la notificación PUSH por FCM
                            Log.e("DB_REMOTA", "Error al enviar notificación PUSH con FCM");}
                    }){
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            // Preparar parámetros para petición PHP
                            Map<String, String> parametros = new HashMap<String, String>();
                            parametros.put("tituloN", getResources().getString(R.string.tituloN));
                            parametros.put("mensajeN", getResources().getString(R.string.mensajeN)+" "+tit+" - "+dir);
                            parametros.put("tituloP", tit);
                            parametros.put("directorP",dir);
                            parametros.put("actoresP", act);
                            parametros.put("generoP", genero);
                            parametros.put("duracionP", dur);
                            parametros.put("tramaP", trama);
                            parametros.put("anyoP", anyo);
                            parametros.put("trailerP", url);
                            parametros.put("caratulaP", caratulaTemp );
                            parametros.put("valoracionP",val);
                            parametros.put("idioma", idiomaActual);
                            // Enviar
                            return parametros;
                        }
                    };
                    // Añadir solicitud a la cola (RequestQueue)
                    rq.add(sr);

                }
                else{
                    // Si la película SÍ está registrada en la DB, mostrar un dialog que
                    // informe al usuario de la situación, y no insertar la película
                    Dial_PeliculaYaExiste d_existe = new Dial_PeliculaYaExiste();
                    d_existe.show(getSupportFragmentManager(), "PeliculaYaExiste");
                }

            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Ha habido un error comprobando la película
                    Log.e("DB_REMOTA", "Error al comprobar si la película a añadir existe");}
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Preparar parámetros para petición PHP
                    Map<String, String> parametros = new HashMap<String, String>();
                    // RECURSO --> peliculas.php> conseguirPelicula
                    parametros.put("id_recurso", "conseguirPelicula");
                    parametros.put("titulo", tit);
                    parametros.put("director", dir);
                    // Enviar
                    return parametros;
                }
            };
            // Añadir solicitud a la cola (RequestQueue)
            rq.add(sr);
        }
        else{
            // Si el campo de 'título' y/o 'director' está(n) vacíos, lanzar
            // un dialog que avise al usuario del problema
            Dial_FaltaTituloDirector d = new Dial_FaltaTituloDirector();
            d.show(getSupportFragmentManager(), "FaltanDatos");
        }

    }

    //---------------------------------------------------------------------------------
    // 4) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy() de la
    //    aplicación, por lo que es aquí dónde se deben guardar los datos que se quieran
    //    mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil, interrupciones, etc
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar datos en el objeto outState para evitar la
        // pérdida al girar el móvil, interrupciones, etc.
        outState.putString("var_idioma", this.idiomaActual);
        outState.putString("var_email", this.email);
        outState.putString("var_caratula", caratulaTemp);

    }

    // ------------------------------------------------------------------------------------------------
    // Métodos de la interfaz 'INTERFAZ_DIAL', a implementar en la clase.
    // En este caso, 'cerrarSesion' no será necesario
    @Override
    public void cerrarSesion() {}

    //---------------------------------------------------------------------------------
    // 5) Método CAMBIAR_CARATULA: Este método cargará las URI de los recursos multimedia
    // sobre los ImageView de las carátulas. Emplea la librería Glide
    // FUENTE: https://devexperto.com/glide-android/
    @Override
    public void cambiarCaratula(String url) {

        caratulaTemp = url;
        caratulaUri = Uri.parse(caratulaTemp);
        Glide.with(this).load(caratulaUri).into(iv_caratula);
        Log.e("CARATULA", "URI:"+caratulaTemp);

    }

    //---------------------------------------------------------------------------------
    // 6) Método ON_CLICK_CAMBIAR_CARATULA: Este método llamará al dialog
    // donde el usuario debe indicar la URL de la imagen que pondrá como
    // carátula de la nueva película a añadir
    // FUENTE: https://devexperto.com/glide-android/
    public void onClickcambiarCaratula(View v) {
        Dial_CambiarCaratula d = new Dial_CambiarCaratula((Interfaz_Dial) this);
        d.show(getSupportFragmentManager(), "CambiarCaratula");
    }
}