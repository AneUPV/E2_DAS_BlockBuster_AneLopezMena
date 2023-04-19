package com.example.entregaindividual_2_anelopezmena.fragments.ui.todos;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.entregaindividual_2_anelopezmena.activities.Act_AñadirPelicula;
import com.example.entregaindividual_2_anelopezmena.activities.Act_InfoPelicula;
import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_Idioma;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_Sinopsis;
import com.example.entregaindividual_2_anelopezmena.interfaces.InterfazRV;
import com.example.entregaindividual_2_anelopezmena.java.Pelicula;
import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.AdapterRV_Peliculas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.example.entregaindividual_2_anelopezmena.databinding.FragmentTodosDrawerBinding;

import org.json.JSONException;
import org.json.JSONObject;

/*******************************************************************/
/** ------------------------- FRAG_TODOS ------------------------ **/
/*******************************************************************/
// Este Fragment contiene la lista completa de películas guardadas en
// la aplicación. Además, está asignado como pantalla predeterminada
// a mostrar cuando el usuario accede a la aplicación, por lo que muestra
// todas las películas en el menú principal. Se corresponde con la sección
// de ‘Todas las películas’ del menú lateral.

public class Frag_Todos extends Fragment implements InterfazRV{

    // Atributos privados
    private ArrayList<Pelicula> listaPelis;
    private ArrayList<Pelicula> listaFiltrada;
    private AdapterRV_Peliculas adaptador;
    private FloatingActionButton b_addPelicula;
    private RecyclerView recyclerPeliculas;
    private String usuario;
    private String email;
    private String idiomaActual;
    //private Gestor_DB peliculasDB;
    private Gestor_Idioma gestorIdioma;
    private FragmentTodosDrawerBinding vista;
    private int PeliVista;
    private RequestQueue rq;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL_server="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1) Método constructor (vacío)
    public Frag_Todos(){}

    //---------------------------------------------------------------------------------
    // 2) Método ON_CREATE
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rq = Volley.newRequestQueue(getActivity());
        // Crear gestores: DB e IDIOMA
        gestorIdioma = new Gestor_Idioma(getActivity());
        //peliculasDB= new Gestor_DB(getActivity());

        //------------------------------------------------------------------------------------------
        // Obtener intent lanzado desde el Login hasta la
        // Act_MenuPrincipal.
        Bundle extras = getActivity().getIntent().getExtras();
        // Si tiene datos, recogerlos
        if (extras !=null) {
            email = extras.getString("var_email");
            usuario = extras.getString("var_usuario");
            idiomaActual = extras.getString("var_idioma");
            gestorIdioma.setIdioma(this.idiomaActual);
        }
        // Settear el idioma recibido desde el intent
        this.idiomaActual = gestorIdioma.getIdiomaActual();

        //------------------------------------------------------------------------------------------
        // Gestionar pérdida de información al girar la pantalla:
        // GUARDAR:
        //      * IDIOMA
        //      * EMAIL
        //      * USUARIO
        //      * CARGADOS
        if (savedInstanceState != null){
            this.idiomaActual = savedInstanceState.getString("var_idioma");
            this.email = savedInstanceState.getString("var_email");
            this.usuario = savedInstanceState.getString("var_usuario");
            gestorIdioma.setIdioma(this.idiomaActual);
        }
        // Una vez conseguidos los datos, actualizar idioma en el gestor
        this.idiomaActual=gestorIdioma.getIdiomaActual();
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_CREATE_VIEW: Este método contiene una la mayor carga de peticiones
    // contra la base de datos remota, puesto que debe cargar todas las películas en la
    // lista del menú principal.
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        // "Inflar" el layout del fragment. El fragment es el elemento 'vista'
        vista = FragmentTodosDrawerBinding.inflate(inflater, container, false);
        View root = vista.getRoot();

        // Conseguir View del Recycler
        recyclerPeliculas = (RecyclerView) vista.getRoot().findViewById(R.id.rv_todos);
        // Recoger orientación
        int orientacion = getResources().getConfiguration().orientation;

        // Si es la orientación es VERTICAL
        if (orientacion == Configuration.ORIENTATION_PORTRAIT) {
            // Obtener textView donde se saluda al usuario, que solo existe en la vista vertical
            TextView saludo = vista.getRoot().findViewById(R.id.saludaUsuario);

            // Si el mail ha llegado
            if (email !=null){
                // Comprobar las preferencias sobre el nombre de usuario
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                // Si el usuario ha indicado un nickname o apodo, actualizarlo en el menú principal
                if (sp.contains("nombre")){
                    // Obtener nombre desde las preferencias
                    usuario = sp.getString("nombre", usuario);
                    //CONSULTA A DB_REMOTA
                    //  * Indicar URL del recurso del servidor
                    String ruta = URL_server + "usuarios.php";
                    StringRequest sr = new StringRequest(
                    Request.Method.POST, ruta, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Si ha habido un error actualizando el nombre de usuario, indicarlo en el Log de errores
                            Log.e("DB_REMOTA", "Error al actualizar preferencias del nombre de usuario");
                        }
                    }){
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            // Preparar parámetros para PHP
                            Map<String, String> parametros = new HashMap<String, String>();
                            // RECURSO: usuarios.php>actualizarUsuario
                            parametros.put("id_recurso", "actualizarUsuario");
                            parametros.put("nombre", usuario);
                            parametros.put("email", email);
                            // Enviar
                            return parametros;
                        }
                    };
                    // Añadir solicitud a la cola (RequestQueue)
                    rq.add(sr);
                }

                // Actualizar nombre en DB y mensaje de la vista
                //      -> Obtener nombre de usuario a partir del email desde DB REMOTA
                String ruta = URL_server + "usuarios.php";
                StringRequest sr = new StringRequest(
                        Request.Method.POST, ruta, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si ha habido respuesta
                        if (response.length()>0) {
                            usuario = response;
                            saludo.setText(getResources().getString(R.string.hola) +" "+usuario+" !");
                        } else {// Asignar un nombre por defecto, no debería ocurrir
                            usuario = "Default";
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener nombre de usuario");}
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        // Preparar parámetros para PHP
                        Map<String, String> parametros = new HashMap<String, String>();
                        // RECURSO: usuarios.php>getNombreUsuario
                        parametros.put("id_recurso", "getNombreUsuario");
                        parametros.put("email", email);
                        // Enviar
                        return parametros;
                    }
                };
                // Añadir solicitud a la cola (RequestQueue)
                rq.add(sr);
            }
            // Settar layout
            recyclerPeliculas.setLayoutManager(new GridLayoutManager(getActivity(), 3, GridLayoutManager.VERTICAL, false));
        }
        else{
            // Settear la disposición StaggeredGridLayout, con 2 columnas en orientación VERTICAL
            recyclerPeliculas.setLayoutManager(new GridLayoutManager(getActivity(), 1, GridLayoutManager.VERTICAL, false));
        }

        //-------------------------------------------------------------------------------------------
        // CONSULTA A DB_REMOTA: Cargar películas de la DB en la app

        // Indicar ruta del recurso en la URL
        String ruta = URL_server + "peliculas.php";
        Frag_Todos contexto = this;
        ArrayList<Pelicula> miLista = new ArrayList<Pelicula>();

        // Crear petición con StringRequest
        StringRequest sr = new StringRequest(
        Request.Method.POST, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.length()>0) {
                    // Si ha habido respuesta, obtener listado de películas en JSON
                    JSONObject json = null;
                    // Dividir el resultado por películas
                    String[] pelis_aux = response.split("\t");
                    // Para cada película, transformarla en JSON
                    for (String peli : pelis_aux) {
                        try {
                            json = new JSONObject(peli);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        String titulo, caratula, director, actores, genero, trama, anyo, duracion, trailer;
                        Double valoracion;

                        try {
                            // Extraer los datos del JSON
                            titulo = (String) json.get("titulo");
                            caratula = (String) json.get("caratula");
                            director = (String) json.get("director");
                            actores = (String) json.get("actores");
                            genero = (String) json.get("genero");
                            trama = (String) json.get("trama");
                            anyo = (String) json.get("anyo");
                            valoracion = (Double) json.getDouble("valoracion");
                            duracion = (String) json.get("duracion");
                            trailer = (String) json.get("trailer");
                            PeliVista=0;

                            // Indicar ruta del recurso en la URL
                            String ruta = URL_server + "guardar.php";
                            StringRequest sr = new StringRequest( Request.Method.POST, ruta, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Si ha habido respuesta y ha sido exitosa
                                    if (response.contains("200") ) {PeliVista = 1;}else {PeliVista = 0;}
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener si la pelicula fue vista");}}){
                                @Nullable
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    // Preparar parámetros para PHP
                                    Map<String, String> parametros = new HashMap<String, String>();
                                    // RECURSO:
                                    parametros.put("id_recurso", "getPeliculas");
                                    parametros.put("titulo", titulo);
                                    parametros.put("director", director);
                                    return parametros;
                                }
                            };
                            // Añadir solicitud a la cola (RequestQueue)
                            rq.add(sr);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Pelicula p = new Pelicula(titulo, caratula, director, actores, genero, trama,anyo, PeliVista, Float.parseFloat(String.valueOf(valoracion)), duracion, trailer);
                        miLista.add(p);
                        listaPelis = miLista;
                        // Instanciar adaptador y cargar los datos
                        adaptador = new AdapterRV_Peliculas(getActivity(), listaPelis, contexto);
                        recyclerPeliculas.setAdapter(adaptador);
                    }
                } else {// Asignar un nombre por defecto, no debería ocurrir
                   Log.d("DB_REMOTA", "No hay películas");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener nombre de usuario");}
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Preparar recursos para PHP
                Map<String, String> parametros = new HashMap<String, String>();
                // RECURSO: guardar.php>getPeliculas
                parametros.put("id_recurso", "getPeliculas");
                // Enviar
                return parametros;
            }
        };
        // Añadir solicitud a la cola (RequestQueue)
        rq.add(sr);
        return root;
    }

    //---------------------------------------------------------------------------------
    // 4) Método ON_VIEW_CREATED
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //------------------------------------------------
        // Obtener orientación de la pantalla para cargar diferentes elementos en la pantalla
        int orientacion = getResources().getConfiguration().orientation;

        //  **** Si es VERTICAL
        if (orientacion == Configuration.ORIENTATION_PORTRAIT) {

            // Cargar la imagen de perfil, obtenida desde el servidor
            String direccion = "";
            URL destino = null;

            if (email !=null) {
                direccion = URL_server + "PERFILES/" + "profile_" + email.replace("@", "").replace(".", "") + ".jpeg";
                destino = null;
            }
            try {
                // Crear objeto URL, método GET
                destino = new URL(direccion);
                HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = 0;
                // Lanzar petición
                conn.connect();

                try {
                    // Recoger código de respuesta
                    responseCode = conn.getResponseCode();
                    // Si code 200 (= Ha ido OK)
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Obtener ImageView y settear Bitmap en él
                        ImageView iv_fotoperfil = view.findViewById(R.id.iv_perfil_inicio);
                        Bitmap bitmapPerfil = BitmapFactory.decodeStream(conn.getInputStream());
                        iv_fotoperfil.setImageBitmap(Bitmap.createScaledBitmap(bitmapPerfil, 120, 120, false));
                    }
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
        }
    }
// --------------------------------***### AÑADIR PELÍCULA ###***---------------------------------- //

        // Obtener orientación de la pantalla
        orientacion = getResources().getConfiguration().orientation;

        if (orientacion == Configuration.ORIENTATION_PORTRAIT) {  // Si es VERTICAL

        // 1.- BOTÓN AÑADIR PELÍCULA (+)  ---> RESULT CODE = 0
            //Obtener view del botón
            b_addPelicula = view.findViewById(R.id.botonAñadir);
            // Añadir listener al Floating Action Button (+)
            b_addPelicula.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Crear un intent, que envíe datos a la Act_AñadirPelicula
                    Intent i = new Intent(getActivity(), Act_AñadirPelicula.class);
                    // Añadir usuario, email e idioma al intent
                    i.putExtra("var_usuario", usuario);
                    i.putExtra("var_email", email);
                    i.putExtra("var_idioma", idiomaActual);
                    // Lanzar intent
                    startActivityIntent.launch(i);
                }
            });
        }
    }

    //---------------------------------------------------------------------------------
    // 5) Método ON_ACTIVITY_RESULT:
    // Este método espera a recibir resultado desde la activity 'Act_AñadirPelicula' ,
    // con el código de resultado 50. Una vez obtenidos los datos, añadirá la película en la DB
    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if ( result.getData() != null && result.getResultCode() == 50) {
                        // Obtener datos del ActivityResult
                        String titulo = result.getData().getStringExtra("var_titulo");
                        String caratula = result.getData().getStringExtra("var_caratula");
                        String director = result.getData().getStringExtra("var_director");
                        String actores = result.getData().getStringExtra("var_actores");
                        String trama = result.getData().getStringExtra("var_trama");
                        String genero = result.getData().getStringExtra("var_genero");
                        String anyo = result.getData().getStringExtra("var_anyo");
                        String visto = result.getData().getStringExtra("var_visto");
                        String valoracion = result.getData().getStringExtra("var_valoracion");
                        String duracion = result.getData().getStringExtra("var_duracion");
                        String trailer = result.getData().getStringExtra("var_trailer");
                        int vt=0;
                        if (Boolean.valueOf(visto)){vt=1;}else{vt=0;}

                        // Instanciar una nueva película con los datos obtenidos
                        Pelicula p = new Pelicula(titulo, caratula, director, actores, genero, trama, anyo, vt, Float.parseFloat(valoracion), duracion, trailer);

                        // Insertar película en DB
                        // DB_REMOTA: Insertar película en DB
                        String ruta = URL_server + "peliculas.php";
                        StringRequest sr = new StringRequest(Request.Method.POST, ruta, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Si ha habido respuesta
                                if (response.length()>0) {
                                    Log.d("DB_REMOTA", response);
                                    Toast.makeText(getActivity(), "SE HA AÑADIDO LA PELI", Toast.LENGTH_LONG).show();

                                    // Notificar del insert al adapter
                                    notificarAnadido(p);
                                } else {// Asignar un nombre por defecto, no debería ocurrir
                                    Log.d("DB_REMOTA", "No hay películas");
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // Si ha habido algún error insertando la película, indicarlo en el log de errores
                                Log.e("DB_REMOTA", "Error al insertar película en DB");
                            }
                        }){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                // Preparar parámetros para PHP
                                Map<String, String> parametros = new HashMap<String, String>();
                                // RECURSO: peliculas.php>insertarPelicula
                                parametros.put("id_recurso", "insertarPelicula");
                                parametros.put("titulo",titulo);
                                parametros.put("caratula", caratula);
                                parametros.put("director",director);
                                parametros.put("actores",actores);
                                parametros.put("genero",genero);
                                parametros.put("trama", trama);
                                parametros.put("anyo",anyo );
                                parametros.put("valoracion", valoracion);
                                parametros.put("duracion",duracion);
                                parametros.put("trailer", trailer);
                                // Enviar
                                return parametros;
                            }
                        };
                        // Añadir solicitud a la cola (RequestQueue)
                        rq.add(sr);
                    }
                }
            });

    //---------------------------------------------------------------------------------
    // 6) Método NOTIFICAR_AÑADIDO: Notificar al adapter para actualizar la lista
    private void notificarAnadido(Pelicula p) {
        // Añadir película
        listaPelis.add(p);
        // Notificar al adaptador
        adaptador.notifyItemInserted(listaPelis.size()-1);
    }

    //---------------------------------------------------------------------------------
    // 7) Método NOTIFICAR_ACTUALIZADO: Notificar al adapter para actualizar los
    // datos sobre la lista
    public void notificarActualizado(int pos, Pelicula p) {

        if (listaPelis != null) {
            Pelicula p_original = listaPelis.get(pos);
            p_original.setTitulo(p.getTitulo());
            p_original.setCaratula(p.getCaratula());
            p_original.setDirector(p.getDirector());
            p_original.setActores(p.getActores());
            p_original.setGenero(p.getGenero());
            p_original.setValoracion(p.getValoracion());
            p_original.setDuracion(p.getDuracion());
            p_original.setTrailer(p.getTrailer());
            p_original.setAnyo(p.getAnyo());
            p_original.setTrama(p.getTrama());

            // ACTUALIZAR DATOS
            adaptador.notifyItemChanged(pos, p_original);
        }

    }

    //---------------------------------------------------------------------------------
    // 8) Método ON_RESUME
    @Override
    public void onResume() {
        // Recoger orientación
        int orientacion = getResources().getConfiguration().orientation;
        //  ** Si es al orientación es VERTICAL
        if (orientacion == Configuration.ORIENTATION_PORTRAIT) {
            // Recoger TextView del saludo al usuario
            TextView saludo = vista.getRoot().findViewById(R.id.saludaUsuario);
            // Si el email se ha mantenido bien, es decir, el dato no se ha perdido
            if (email !=null){
                // Comprobar las preferencias sobre el nombre de usuario
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                // Si el usuario ha indicado un nickname o apodo, actualizarlo en el menú principal
                if (sp.contains("nombre")){
                    usuario = sp.getString("nombre", usuario);

                    // * DB_REMOTA: Actualizar nombre de usuario
                    // Indicar URL del recurso en el servidor
                    String ruta = URL_server + "usuarios.php";

                    // Crear StringRequest para ejecutar la petición
                    StringRequest sr = new StringRequest(
                    Request.Method.POST, ruta, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {}
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Si ha habido algún error, indicarlo en el log de errores
                            Log.e("DB_REMOTA", "Error al actualizar preferencias del nombre de usuario");
                        }
                    }){
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            // Preparar parámetros para PHP
                            Map<String, String> parametros = new HashMap<String, String>();
                            // RECURSO: usuarios.php>actualizarUsuario
                            parametros.put("id_recurso", "actualizarUsuario");
                            parametros.put("nombre", usuario);
                            parametros.put("email", email);
                            // ENVIAR
                            return parametros;
                        }
                    };
                    // Añadir solicitud a la cola (RequestQueue)
                    rq.add(sr);
                }

                //*************************************************************
                // DB_REMOTA: Actualizar mensaje del menú principal.
                // Obtener nombre de usuario a partir del email desde DB REMOTA
                String ruta = URL_server + "usuarios.php";
                StringRequest sr = new StringRequest(
                        Request.Method.POST, ruta, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Si ha habido respuesta
                        if (response.length()>0) {
                            usuario = response;
                            saludo.setText(getResources().getString(R.string.hola) +" "+usuario+" !");
                        } else {// Asignar un nombre por defecto, no debería ocurrir
                            usuario = "Default";
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener nombre de usuario");}
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parametros = new HashMap<String, String>();
                        parametros.put("id_recurso", "getNombreUsuario");
                        parametros.put("email", email);
                        return parametros;
                    }
                };
                // Añadir solicitud a la cola (RequestQueue)
                rq.add(sr);
                saludo.setText(getResources().getString(R.string.hola) +" "+usuario+" !");
            }
            saludo.setText(getResources().getString(R.string.hola) +" "+usuario+" !");
        }
        super.onResume();
    }

// -------------------------------- INFO + MODIFICAR PELICULA --------------------------------- //
    //====================================================================================
    // ....****#### Métodos onClick de la interfaz 'InterfazRV' implementados ####****....

    // Método de la Interfaz - No es necesario en esta clase
    @Override
    public void onitemLongClick(int pos) { }

    //---------------------------------------------------------------------------------
    // 9) Método ON_CLICK: ---> RESULT CODE = 1
    // Este método envía todos los datos a la activity Act_infoPelicula
    // para visualizar los datos sobre la película que se haya seleccionado
    @Override
    public void onClick(int position) {
        Pelicula p = listaPelis.get(position);
        comprobarVista(p, position);

    }

    // 10) Método COMPROBAR_VISTA: ---> RESULT CODE = 1
    //  Este método envía todos los datos a la activity Act_infoPelicula
    //  para visualizar los datos sobre la película que se haya seleccionado
    private void comprobarVista(Pelicula p, int position) {
        // Indicar URL del recurso en el servidor
        String ruta = URL_server + "guardar.php";
        // Crear objeto StringRequest para petición
        StringRequest sr = new StringRequest(
        Request.Method.POST, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.contains("200")){
                    // Si ha habido éxito, marcar la película como VISTA
                    PeliVista=1;
                }
                else{
                    // Si NO ha habido éxito, marcar la película como NO VISTA
                    PeliVista = 0;
                }
                p.setVisto(PeliVista);

                // Según la orientación
                int orientacion = getResources().getConfiguration().orientation;
                //   *** Si la orientación es VERTICAL
                if (orientacion == Configuration.ORIENTATION_PORTRAIT) {
                    // Enviar los datos a Act_InfoPelicula desde un Intent
                    //  * Crear intent hacia Act_InfoPelicula
                    Intent i1 = new Intent(getActivity(), Act_InfoPelicula.class);
                    // Pasar datos: FRAGMENTO_TODOS ---> ACT_INFO_PELICULA
                    i1.putExtra("var_email", email);
                    i1.putExtra("var_titulo", p.getTitulo());
                    i1.putExtra("var_caratula", p.getCaratula());
                    i1.putExtra("var_director", p.getDirector());
                    i1.putExtra("var_actores", p.getActores());
                    i1.putExtra("var_genero", p.getGenero());
                    i1.putExtra("var_trama", p.getTrama());
                    i1.putExtra("var_anyo", p.getAnyo());
                    i1.putExtra("var_valoracion", String.valueOf(p.getValoracion()));
                    i1.putExtra("var_duracion", p.getDuracion());
                    i1.putExtra("var_trailer", p.getTrailer());
                    i1.putExtra("var_posicion", String.valueOf(position));
                    i1.putExtra("var_idioma", idiomaActual);
                    if (PeliVista ==1){i1.putExtra("var_visto", true);}
                    else{i1.putExtra("var_visto", false);}

                    // Lanzar intent
                    startActivityIntent2.launch(i1);
                }
                else{
                    //   *** Si la orientación es HORIZONTAL
                    // Obtener los elementos exclusivos del fragment que aparece en la orientación horizontal
                    TextView tv_titulo_s = getActivity().findViewById(R.id.s_titulo);
                    TextView tv_director_s = getActivity().findViewById(R.id.s_director);
                    ImageView iv_caratula_s = getActivity().findViewById(R.id.s_caratula);
                    ImageView info = getActivity().findViewById(R.id.s_info);

                    // Settear los valores
                    tv_titulo_s.setText(p.getTitulo());
                    tv_director_s.setText(p.getDirector());
                    Glide.with(getActivity()).load(p.getCaratula()).into(iv_caratula_s);

                    // Listener sobre el botón de información, que mostrará el dialogo de la sinopsis
                    info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Llamar al dialog (Dial_Sinopsis)
                            Dial_Sinopsis d = new Dial_Sinopsis(p.getTitulo(), p.getDirector(), p.getTrama());
                            d.show(getActivity().getSupportFragmentManager(), "MostrarInfo");
                        }
                    });
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener peli vista");}
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Preparar parámetros para PHP
                Map<String, String> parametros = new HashMap<String, String>();
                // RECURSO: guardar.php>getVisto
                parametros.put("id_recurso", "getVisto");
                parametros.put("titulo", p.getTitulo());
                parametros.put("director", p.getDirector());
                parametros.put("email", email);
                // Enviar
                return parametros;
            }
        };
        // Añadir solicitud a la cola (RequestQueue)
        rq.add(sr);

    }

    //---------------------------------------------------------------------------------
    // 11) Método ELIMINAR_CONFIRMADO: Se ejecutará cuando el usuario confirme la
    // acción de eliminar la película desde el dialog. Se eliminará el registro de
    // la película y se notificará al adaptador para que actualice la lista
    @Override
    public void eliminarConfirmado(int pos) {
        if(listaPelis != null) {
            listaPelis.remove(pos);
            adaptador.notifyItemRemoved(pos);
        }
    }

    //---------------------------------------------------------------------------------
    // Método de la Interfaz - No es necesario en esta clase
    @Override
    public void notificarActualizado() {}

    //---------------------------------------------------------------------------------
    // 12) Método ON_ACTIVITY_RESULT:
    // Este método espera a recibir resultado desde la activity 'Act_InfoPelicula' ,
    // con el código de resultado 1. Una vez obtenidos los datos, añadirá o actualizará
    // la película en la DB
    ActivityResultLauncher<Intent> startActivityIntent2 = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                // Le ha pulsado a 'MODIFICAR'
                public void onActivityResult(ActivityResult result) {
                    if ( result.getData() != null && result.getResultCode() == 1) {
                        // Obtener datos
                        String titulo = result.getData().getStringExtra("var_titulo");
                        String caratula = result.getData().getStringExtra("var_caratula");
                        String director = result.getData().getStringExtra("var_director");
                        String actores = result.getData().getStringExtra("var_actores");
                        String trama = result.getData().getStringExtra("var_trama");
                        String genero = result.getData().getStringExtra("var_genero");
                        String anyo = result.getData().getStringExtra("var_anyo");
                        String visto = result.getData().getStringExtra("var_visto");
                        String valoracion = result.getData().getStringExtra("var_valoracion");
                        String duracion = result.getData().getStringExtra("var_duracion");
                        String trailer = result.getData().getStringExtra("var_trailer");

                        // Obtener posición para notificar al adapter
                        String pos = result.getData().getStringExtra("var_posicion");
                        // Obtener equivalente al valor de visto en int
                        int v=0;
                        if (Boolean.valueOf(visto)){ v=1;}

                        // Crear la peLícula
                        Pelicula p = new Pelicula(titulo, caratula, director, actores, trama, genero, anyo, v, Float.parseFloat(valoracion), duracion, trailer);
                        notificarActualizado(Integer.parseInt(pos), p);

                        //DB_REMOTA: Añadir / Actualizar pelicula de DB
                        // Indicar URL del recurso en el servidor
                        String ruta = URL_server + "peliculas.php";
                        StringRequest sr = new StringRequest( Request.Method.POST, ruta, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) { // Si ha habido respuesta
                                if (response.length()==0 ) {
                                    // Si la pelicula es nueva (ES NULL), añadirla
                                    // a la lista y notificar al adapter
                                    notificarAnadido(p);
                                }
                                else{
                                    // Si la película se ha modificado, notificar al adapter del cambio
                                    notificarActualizado(Integer.parseInt(pos), p);
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) { }}){
                            @Nullable
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                // Preparar parámetros para PHP
                                Map<String, String> parametros = new HashMap<String, String>();
                                // RECURSO: peliculas.php>conseguirPelicula
                                parametros.put("id_recurso", "conseguirPelicula");
                                parametros.put("titulo", titulo);
                                parametros.put("director", director);
                                // Enviar
                                return parametros;
                            }
                        };
                        // Añadir solicitud a la cola (RequestQueue)
                        rq.add(sr);

                    }
                    // Le ha pulsado a 'ELIMINAR'
                    if ( result.getData() != null && result.getResultCode() == 404) {
                        // Obtener identificadores de la película a eliminar
                        int pos = result.getData().getExtras().getInt("var_posicion");
                        eliminarConfirmado(pos);
                    }
                }
            });

    //---------------------------------------------------------------------------------
    // 13) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy() de la
    //    aplicación, por lo que es aquí dónde se deben guardar los datos que se quieran
    //    mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil, interrupciones,
    //    etc.
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar datos para evitar la pérdida al girar el móvil, interrupciones, etc.
        outState.putString("var_idioma", this.idiomaActual);
        outState.putString("var_email", this.email);
        outState.putString("var_usuario", this.usuario);

    }

    //---------------------------------------------------------------------------------
    // 14) Método ON_DESTROY_VIEW
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vista = null;
    }

}