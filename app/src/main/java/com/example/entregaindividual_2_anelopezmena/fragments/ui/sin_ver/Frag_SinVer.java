package com.example.entregaindividual_2_anelopezmena.fragments.ui.sin_ver;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.AdapterRV_Peliculas;
import com.example.entregaindividual_2_anelopezmena.listasRecyclerView.Adapter_SinVer;
import com.example.entregaindividual_2_anelopezmena.R;

import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_Idioma;
import com.example.entregaindividual_2_anelopezmena.databinding.FragmentSlideshowBinding;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_EliminarPelicula;
import com.example.entregaindividual_2_anelopezmena.interfaces.InterfazRV;
import com.example.entregaindividual_2_anelopezmena.java.Pelicula;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/********************************************************************/
/** ----------------------- FRAG_SIN_VER ------------------------- **/
/********************************************************************/
// Este fragmento mostrará la sección del perfil del usuario, donde verá sus
// datos personales y su imagen de perfil además de las películas que no haya
// marcado como vistas. La idea de este Fragment, consiste guardar de forma
// centralizada, las películas que el usuario no ha visto aún y quisiera ver
// en algún momento. Se corresponde con la sección de ‘Mi perfil’ del menú lateral.

public class Frag_SinVer extends Fragment implements InterfazRV {
    // Atributos privados
    private FragmentSlideshowBinding vista;
    private RecyclerView recyclerSinVer;
    private ArrayList<Pelicula> listaPelis;
    private Adapter_SinVer adaptador;
    private Gestor_Idioma gestorIdioma;
    private String usuario;
    private String email;

    // Objeto RequestQueue para peticiones al servidor AWS
    private RequestQueue rq;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL_server="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1.1) Método constructor (vacío)
    public Frag_SinVer() {
    }

    //---------------------------------------------------------------------------------
    // 1.2) Método constructor
    public Frag_SinVer(String email, String usuario) {
        this.email = email;
        this.usuario=usuario;
    }

    //---------------------------------------------------------------------------------
    // 2) Método ON_VIEW_CREATED
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Obtener la orientación para cargar diferentes elementos
        int orientacion = getResources().getConfiguration().orientation;

        // Si es la orientación es VERTICAL
        if (orientacion == Configuration.ORIENTATION_PORTRAIT) {
            // CONSULTA A DB REMOTA - Indicar recurso en la URL
            String ruta = URL_server + "usuarios.php";
            StringRequest sr = new StringRequest(
            Request.Method.POST, ruta, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.length()>0){
                        // Si ha habido respuesta, actualizar nombre de usuario
                        usuario = response;
                        // Obtener elementos de la vista, y actualizar datos
                        TextView miEmail = vista.getRoot().findViewById(R.id.perfil_email);
                        TextView miNombre = vista.getRoot().findViewById(R.id.perfil_nombre);
                        miEmail.setText(email);
                        miNombre.setText(usuario);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Si ha habido un error, indicar sobre el mismo en el log de errores de la app
                    Log.e("DB_REMOTA", "Error al obtener nombre de usuario");}
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Preparar parámetros para petición por PHP
                    Map<String, String> parametros = new HashMap<String, String>();
                    // RECURSO -> guardar.php > getNombreUsuario
                    parametros.put("id_recurso", "getNombreUsuario");
                    parametros.put("email", email);
                    // Enviar
                    return parametros;
                }
            };
            // Añadir solicitud a la cola (RequestQueue)
            rq.add(sr);

            // .......~~~~****###### CARGAR IMAGEN DE PERFIL #######******~~~~~~......... //

            // Preparar URL (genérica) al recurso, generada a partir del email del usuario
            String direccion = URL_server + "PERFILES/" + "profile_"+email.replace("@", "").replace(".", "")+".jpeg";
            URL destino = null;

            try {
                // Crear objeto URL
                destino = new URL(direccion);
                // Establecer conexión y realizar una petición por GET
                HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = 0;
                // Lanza petición
                conn.connect();

                try {
                    // Recoger código de respuesta
                    responseCode = conn.getResponseCode();
                    // Si code 200 = SI HA IDO OK
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Obtener ImageView de la vista
                        ImageView iv_fotoperfil = view.findViewById(R.id.iv_perfil);
                        // Convertir imagen a Bitmap
                        Bitmap bitmapPerfil = BitmapFactory.decodeStream(conn.getInputStream());
                        //Colocar la imagen
                        iv_fotoperfil.setImageBitmap(Bitmap.createScaledBitmap(bitmapPerfil, 120, 120, false));
                        Log.e("PERFIL", "---> IMAGEN SETTEADA EN CAMPO CON ÉXITO");
                    }
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            catch (IOException e) {
            }

            // .......~~~~****###### CAMBIAR IMAGEN DE PERFIL #######******~~~~~~......... //

            // Preparar método ON_CLICK del pequeño icono de la cámara. Si se pulsa, se
            // iniciarán las acciones relativas al cambio de imagen de perfil
            ImageView iv_perfil = view.findViewById(R.id.camara_small);
            iv_perfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Si la versión es superior a Marshmallow, comprobar permisos
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // Comprobar si el permiso ha sido concedido
                        if (ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                                ContextCompat.checkSelfPermission(v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                            // Si no está permitido, pedirlo
                            String[] permission = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
                            requestPermissions(permission, 600);
                        } else {
                            // Si está permitido, lanzar el intent a la aplicación de cámara
                            mostrarCuadroDialogo();
                        }
                    } else {
                        // Si es inferior a MarshMallow, no solicitar permisos
                        mostrarCuadroDialogo();
                    }
                }
            });
        }
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_CREATE
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instanciar RequestQueue
        rq = Volley.newRequestQueue(getActivity());

        // Obtener desde el fragment, el intent lanzado desde el
        // login y recogido por Act_MenuPrincipal.
        Bundle extras = getActivity().getIntent().getExtras();

        // Instanciar gestor: Idiomas
        gestorIdioma = new Gestor_Idioma(getActivity());

        // Si el intent tenía datos en los extras
        if (extras !=null) {
            // recoger esos datos en los atributos de la clase
            email = extras.getString("var_email");

            // Obtener nombre de usuario a partir del email desde DB REMOTA
            String ruta = URL_server + "usuarios.php";
            StringRequest sr = new StringRequest(
                    Request.Method.POST, ruta, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Si ha habido respuesta
                    if (response.length()>0) {
                        usuario = response;
                    }else{ //Nunca debería darse el caso
                        usuario = "default";
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Si ha habido un error, inidicarlo en el Log de errores
                    Log.e("DB_REMOTA", "Error al obtener nombre de usuario");
                }
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Preparar parámetros para PHP
                    Map<String, String> parametros = new HashMap<String, String>();
                    // RECURSO -> usuarios.php>getNombreUsuario
                    parametros.put("id_recurso", "getNombreUsuario");
                    parametros.put("email", email);
                    // Enviar
                    return parametros;
                }
            };
            // Añadir solicitud a la cola (RequestQueue)
            rq.add(sr);
        }
    }

    //---------------------------------------------------------------------------------
    // 4) Método ON_CREATE_VIEW
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // "Inflar" el layout del fragment. El fragment es el elemento 'vista'
        vista = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = vista.getRoot();

        // Conseguir View del Recycler
        recyclerSinVer = (RecyclerView) vista.getRoot().findViewById(R.id.rv_sinVer);

        // Conseguir View del Recycler
        Button b_exportar = (Button) vista.getRoot().findViewById(R.id.exportar);

        // Añadir LISTENER al botón de 'EXPORTAR LISTA'
        b_exportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Llamar al método que genera la lista en .txt
                exportarListaSinVer();
                // Notificar éxito mediante un Toast
                Toast.makeText(getActivity(), getResources().getString(R.string.exportadoExito), Toast.LENGTH_LONG).show();
            }
        });

        //Settear layout de la lista
        recyclerSinVer.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));

        Frag_SinVer contexto = this;

        // CONSULTA A DB REMOTA --> Obtener películas que no ha visto el usuario
        // Indicar recurso del servidor
        String ruta = URL_server + "guardar.php";
        ArrayList<Pelicula> miLista = new ArrayList<Pelicula>();
        StringRequest sr = new StringRequest(
        Request.Method.POST, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Si ha habido respuesta, obtener la lista de películas en JSON
                if (response.contains("titulo")) {
                    JSONObject json = null;
                    // Separar el resultado
                    String[] pelis_aux = response.split("\t");
                    // Para cada película, convertirla en JSON
                    for (String peli : pelis_aux) {
                        try {
                            json = new JSONObject(peli);
                        }
                        catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // Extraer datos a partor de elementos JSON
                        String titulo, caratula, director, actores, genero, trama, anyo, duracion, trailer;
                        Double valoracion;

                        try {
                            // De los datos extraídos, conseguir la instancia de PELÍCULA
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
                            int PeliVista=0;
                            // Crear película y añadirla a la lista
                            Pelicula p = new Pelicula(titulo, caratula, director, actores, genero, trama,anyo, PeliVista, Float.parseFloat(String.valueOf(valoracion)), duracion, trailer);
                            miLista.add(p);

                        }
                        catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        // Actualizar lista
                        listaPelis = miLista;
                        // Instanciar adaptador y cargar los datos
                        adaptador = new Adapter_SinVer(getActivity(), listaPelis, contexto);
                        recyclerSinVer.setAdapter(adaptador);
                    }
                }
                else {
                    // Asignar un nombre por defecto, no debería ocurrir
                    Log.d("DB_REMOTA", "No hay películas");
                    listaPelis = miLista;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Si ha habido un error, indicarlo en el log de errores de la app
                Log.e("DB_REMOTA", "Error al obtener peli vista");
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Preparar parámetros para el PHP
                Map<String, String> parametros = new HashMap<String, String>();
                // RECURSO: guardar.php > getPeliculasSinVer
                parametros.put("id_recurso", "getPeliculasSinVer");
                parametros.put("email", email);
                // Enviar
                return parametros;
            }
        };
        // Añadir solicitud a la cola (RequestQueue)
        rq.add(sr);
        return root;
    }

    //---------------------------------------------------------------------------------
    // 5) Método TAKE_PICTURE_LAUNCHER: Este será el encargado de llamar al método de subir
    // la imagen al servidor cuando se SAQUE POR LA CÁMARA
    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Cuando haya recibido una respuesta y sea 'RESULT_OK'
                if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
                    Bundle bundle = result.getData().getExtras();
                    // Obtener imagen y colocarla en el ImageView como Bitmap
                    ImageView iv_fotoPerfil = getActivity().findViewById(R.id.iv_perfil);
                    Bitmap foto = (Bitmap) bundle.get("data");
                    iv_fotoPerfil.setImageBitmap(foto);

                    // Obtener imagen en base 64 a partir del Bitmap
                    String fotoen64 = getImagenBase64(foto);
                    String nombre= "profile_"+email.replace("@", "").replace(".", "")+".jpeg";

                    // Subir imagen al servidor AWS de DAS
                    subirImagenAWS(fotoen64, nombre);
                }
                else {
                    Log.d("PERFIL", "No se ha sacado ninguna foto con la cámara");
                }
            });

    //---------------------------------------------------------------------------------
    // 6) Método TAKE_FROM_GALLERY: Este será el encargado de llamar al método de subir
    // la imagen al servidor cuando se SELECCIONE POR LA GALERÍA
    private ActivityResultLauncher<Intent> takeFromGallery =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Cuando haya recibido una respuesta y sea 'RESULT_OK'
                if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
                    Intent data = result.getData();
                    Uri imagen = data.getData();
                    ImageView iv_fotoPerfil = getActivity().findViewById(R.id.iv_perfil);
                    Bitmap foto = null;
                    try {
                        // Obtener foto de la galería en formato Bitmap
                        foto = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagen);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // Settear imagen en ImageView
                    iv_fotoPerfil.setImageBitmap(foto);

                    // Obtener imagen en base 64, y generar nombre para la foto de perfil de este usuario en concreto
                    String fotoen64 = getImagenBase64(foto);
                    String nombre= "profile_"+email.replace("@", "").replace(".", "")+".jpeg";

                    // Subir imagen al servidor AWS de DAS
                    subirImagenAWS(fotoen64, nombre);
                }
                else {
                    Log.d("PERFIL", "No se ha seleccionado ninguna foto de la galería");
                }
            });

    //---------------------------------------------------------------------------------
    // 7) Método GET_IMAGEN_BASE64: Este método recibe como entrada una imagen en BITMAP
    // y devuelve como respuesta la misma imagen en BASE 64

    // FUENTE: The polyglot developer
    // * https://www.thepolyglotdeveloper.com/2015/06/from-bitmap-to-base64-and-back-with-native-android/
    private String getImagenBase64(Bitmap foto) {
        // Crear un nuevo 'ByteArrayOutputStream'
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] fotoTransformada = stream.toByteArray();
        String fotoen64 = Base64.encodeToString(fotoTransformada, Base64.DEFAULT);
        return fotoen64;
    }

    //---------------------------------------------------------------------------------
    // 8) Método SUBIR_IMAGEN_AWS: El método que contiene la petición HTTP que subirá
    // las imágenes al servidor
    private void subirImagenAWS(String fotoen64 , String nombre) {
        // Mostrar un diálogo de carga para informar al usuario
        // * FUENTE: Android Developers
        // * https://developer.android.com/reference/android/app/ProgressDialog
        final ProgressDialog cargando = ProgressDialog.show(getActivity(), "Subiendo imagen al servidor DAS", "Espere por favor" );

        // Indicar la URL del recurso en el servidor
        String ruta = URL_server + "subirImagenes.php";
        // Crear StringRequest para la petición (POST)
        StringRequest sr = new StringRequest(Request.Method.POST, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Cerrar pantalla de carga
                cargando.dismiss();
                // Informar al usuario del estado con un Toast
                Toast.makeText(getActivity().getApplicationContext(), "Imagen guardada con éxito", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Cerrar pantalla de carga
                cargando.dismiss();
                // Si hay un error con la subida, dejarlo indicado en el log de errores de la aplicación
                Log.e("CAMERA", error.toString());
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Preparar parámetros de la petición PHP
                Map<String, String> parametros = new HashMap<String, String>();
                // RECURSO: subirImagenes.php>subirFoto
                parametros.put("id_recurso", "subirFoto");
                parametros.put("imagen", fotoen64);
                parametros.put("nombre", nombre);
                // Enviar
                return parametros;
            }
        };
        // Añadir solicitud a la cola (RequestQueue)
        rq.add(sr);
    }

    //---------------------------------------------------------------------------------
    // 9) Método OPEN_CAMERA: Este método lanzará el intent a la aplicación de cámara
    public void openCamera() {
        // Intent a la aplicación de cámara
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intentCamara);
    }

    //---------------------------------------------------------------------------------
    // 10) Método OPEN_CAMERA: Este método lanzará el intent a la galería de imágenes
    private void elegirFotoDeGaleria() {
        // Intent a la aplicación de galería
        Intent intentGaleria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeFromGallery.launch(intentGaleria);
    }

    //----------------------------------------------------------------------------------------------
    // Respuesta a la petición de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 600:
                //Si se han concedido los permisos, mostrar el dialog de selección
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mostrarCuadroDialogo();
                }else{
                    //Si no, mostrar un toast informativo
                    Toast.makeText(getActivity(), "NO ha concedido permisos", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }
    //----------------------------------------------------------------------------------
    // 11) Método MOSTRAR_CUADRO_DIALOGO: Con este método se creará un simple diálogo de selección:
    // Permite elegir entre la galería o la cámara de fotos para establecer una foto de perfil.
    private void mostrarCuadroDialogo(){

        // Crear un alert dialog genérico
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        // Colocar texto en las opciones del diálogo
        String[] pictureDialogItem={getResources().getString(R.string.galeria),getResources().getString(R.string.camara) };
        // Crear un listener que reaccione a la selección del usuario
        alertDialog.setItems(pictureDialogItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selec) {
                switch(selec){
                    case 0:
                        // Si ha seleccionado la opción de 'Seleccionar foto de galería'
                        elegirFotoDeGaleria();
                        break;
                    case 1:
                        // Si ha seleccionado la opción de 'Sacar foto desde la cámara'
                        openCamera();
                        break;
                }
            }
        });
        // Mostrar diálogo
        alertDialog.show();
    }

    //---------------------------------------------------------------------------------
    // 12) Método EXPORTAR_LISTA_SIN_VER: Este método es el encargado de escribir la
    //    lista de películas sin ver por cada usuario que la solicite. Según el idioma,
    //    generará un texto diferente (traducido). Se guardará en la memoria interna
    //    del dispositivo [Desde ANDROID: data>data>proyecto>files]
    private void exportarListaSinVer() {
        // Crear variables para guardar datos (titulo, director, año, valoración e idioma)
        String t, dir, anyo, val;
        String idioma = gestorIdioma.getIdiomaActual();

        // Generación del fichero
        try{
            // Dar un nombre al fichero
            String nombreFichero = "peliculas_sin_ver.txt";
            if (email != null) {
                nombreFichero = email + "_peliculas_sin_ver.txt";
            }
            // Crear objeto 'OutputStreamWriter' para escribir en el interior del fichero
            OutputStreamWriter fichero = new OutputStreamWriter(getActivity().openFileOutput(nombreFichero, Context.MODE_PRIVATE));

            //Según el idioma, el texto será diferente
            switch (idioma) {
                // Si es ESPAÑOL
                case "es":
                    fichero.write("#---------<< LISTA DE PELICULAS PARA VER >>-------------#\n");
                    fichero.write("#-------------------------------------------------------#\n");
                    fichero.write("\n");
                    break;
                // Si es EUSKERA
                case "eu":
                    fichero.write("#---------<< ORAINDIK IKUSI GABEKO PELIKULAK >>-------------#\n");
                    fichero.write("#-----------------------------------------------------------#\n");
                    fichero.write("#            -----------------------------                  #\n");
                    break;
                // Si es INGLÉS
                case "en":
                    fichero.write("#------------------<< TO SEE LIST >>--------------------#\n");
                    fichero.write("#-------------------------------------------------------#\n");
                    fichero.write("\n");
                    break;
                default:
                    break;
            }
            int i=1;
            // Iterar lista de películas sin ver por el usuario
            for( Pelicula p: listaPelis){
                // Obtener datos por cada película
                t = p.getTitulo();
                dir = p.getDirector();
                anyo = p.getAnyo();
                val = String.valueOf(p.getValoracion());

                //Según el idioma, el texto será diferente
                switch (idioma) {
                    // Si es ESPAÑOL
                    case "es":
                        fichero.write("____\n");
                        fichero.write(i+") TITULO: "+t+"\n");
                        fichero.write("    DIRECTOR(ES): "+dir+"\n");
                        fichero.write("    AÑO DE PUBLICACIÓN: "+anyo+"\n");
                        fichero.write("    VALORACIÓN OBTENIDA: "+val+"/5 \n");
                        break;
                    // Si es EUSKERA
                    case "eu":
                        fichero.write("____\n");
                        fichero.write(i+") TITULUA: "+t+"\n");
                        fichero.write("    ZUZENDARIA(K): "+dir+"\n");
                        fichero.write("    ARGITARAPEN-URTEA: "+anyo+"\n");
                        fichero.write("    LORTUTAKO BALORAZIOA: "+val+"/5 \n");
                        break;
                    // Si es INGLES
                    case "en":
                        fichero.write("____\n");
                        fichero.write(i+") TITTLE: "+t+"\n");
                        fichero.write("    DIRECTOR(S): "+dir+"\n");
                        fichero.write("    YEAR OF RELEASE: "+anyo+"\n");
                        fichero.write("    RATING OF THE FILM: "+val+"/5 \n");
                        break;
                }
                // Escribir separador
                fichero.write("#-------------------------------------------------------#\n");
                fichero.write("#------------------- BLOCKBUSTER! ----------------------#\n");
                i++;
            }
            // Cerrar el fichero al acabar de escribir
            fichero.close();
        }
        catch(Exception e){
            // Notificar del error en el LOG
            Log.e("EXPORTAR", "Error escribiendo el fichero de texto");
        }
    }

    //---------------------------------------------------------------------------------
    // 13) Método ON_DESTROY_VIEW
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        vista = null;
    }

    //---------------------------------------------------------------------------------
    // 14) Método ON_ITEM_LONG_CLICK: Se ejecutará cada vez que el usuario mantenga pulsado
    // un item de la lista durante unos segundos. En este caso, se eliminará la película
    // si el usuario confirma la acción mediante el díalogo de alerta que se lanza
    @Override
    public void onitemLongClick(int pos) {

        // Obtener identificadores de la película --> Titulo y directores
        String t = listaPelis.get(pos).getTitulo();
        String d = listaPelis.get(pos).getDirector();
        // Llamar al diálogo (Dial_EliminarPelicula)
        Dial_EliminarPelicula d2 = new Dial_EliminarPelicula(this, pos, t, d);
        d2.show(getActivity().getSupportFragmentManager(), "EliminarVisto");

    }
    //---------------------------------------------------------------------------------
    // Método de la Interfaz - No es necesario en esta clase
    @Override
    public void onClick(int pos) {}

    //---------------------------------------------------------------------------------
    // 15) Método ELIMINAR_CONFIRMADO: Se ejecutará cuando el usuario confirme la
    // acción de eliminar la película desde el dialog. Se eliminará el registro de
    // la película de las películas vistas y se notificará al adaptador para que actualice la lista
    @Override
    public void eliminarConfirmado(int pos) {
        // Obtener elemento de la lista
        Pelicula p = listaPelis.get(pos);

        // CONSULTA A DB REMOTA: Añadirla como vista
        String ruta = URL_server + "guardar.php";
        StringRequest sr = new StringRequest(
                Request.Method.POST, ruta, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Eliminar elemento de la lista
                listaPelis.remove(pos);
                //Notificar al adapter
                adaptador.notifyItemRemoved(pos);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { Log.e("DB_REMOTA", "Error al obtener peli vista");}
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Parametros del PHP
                Map<String, String> parametros = new HashMap<String, String>();
                // RECURSO: guardar.php>insertarPeliculaVista
                parametros.put("id_recurso", "insertarPeliculaVista");
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
    // Método de la Interfaz - No es necesario en esta clase
    @Override
    public void notificarActualizado() {}

}