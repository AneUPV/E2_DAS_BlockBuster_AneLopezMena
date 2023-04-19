package com.example.entregaindividual_2_anelopezmena.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.controlador.Gestor_Idioma;
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_UsuarioYaRegistrado;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*************************************************************************/
/** ----------------------- ACTIVITY_REGISTRO ------------------------- **/
/*************************************************************************/
// Será la primera pantalla que el usuario conozca de la aplicación. Se trata
// de una sencilla pantalla de registro al uso, con campos dónde introducir los
// datos personales (Nombre de usuario) y las credenciales de acceso a la app
// (correo electrónico y contraseña). Mediante esta actividad, un nuevo usuario
// que pruebe la aplicación podrá crearse una cuenta en la que iniciar sesión y
// consultar la información de las películas.

public class Act_Registro extends AppCompatActivity {

    // Atributos privados
    private EditText usuario, contrasena, email, repContrasena;
    private Button b_login, b_registro;
    private Gestor_Idioma gestorIdioma;
    private String idiomaActual;

    private String TAG= "MyFirebaseMsgService";
    private RequestQueue rq;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1) Método ON_CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // El idioma por defecto será el Español
        idiomaActual = "es";
        // Obtener gestor de idioma, para configuración de idioma
        gestorIdioma = new Gestor_Idioma(this);

        // Instanciar objeto RequestQueue para peticiones al server AWS
        rq = Volley.newRequestQueue(this);

       // -----------------------------------------------------------------------------------
       // Atrapar el intent lanzado al cerrar sesión
       Bundle extras = getIntent().getExtras();
       // Si se han enviado datos desde el menú principal, se recogen
        if ( extras != null){
            // Atrapar la señal de exit de cerrar sesión
            // para provocar el finish que cierre la aplicacion
            boolean salir = extras.getBoolean("EXIT");
            if (salir) {
                finish();
            }
        }

    // -----------------------------------------------------------------------------------
        // Comprobar las preferencias del usuario sobre el idioma
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // Si el usuario ha indicado el idioma en el menú de las preferencias
        if (sp.contains("miidioma")){
            this.idiomaActual = sp.getString("miidioma", "es");
        }
        // Settear idioma en el Gestor de idiomas
        gestorIdioma.setIdioma(idiomaActual);

    // -----------------------------------------------------------------------------------
        // Gestionar pérdida de información al girar la pantalla:
        // GUARDAR:
        //      * IDIOMA
        if (savedInstanceState != null){
            // Obtener idioma guardado y volver a settearlo
            this.idiomaActual = savedInstanceState.getString("var_idioma");
            gestorIdioma.setIdioma(idiomaActual);
        }
        // Una vez indicado el idioma, crear la vista
        setContentView(R.layout.act_registro_layout);

        // Obtener elementos de la vista
        usuario = (EditText) findViewById(R.id.id_usuario);
        email = (EditText) findViewById(R.id.id_email);
        contrasena = (EditText) findViewById(R.id.id_contrasena);
        repContrasena = (EditText) findViewById(R.id.id_recontrasena);
        b_login = (Button) findViewById(R.id.id_botonLogin);
        b_registro = (Button) findViewById(R.id.id_botonRegistro);
    }

    //---------------------------------------------------------------------------------
    // 2) Método LOGIN: Este método prepara el Intent de la actividad Registro
    //    hacia la actividad Login   ( Registro ---> Login )
    public void login(View v){
        // Crear intent hacia Login
        Intent i = new Intent(getApplicationContext(), Act_Login.class);
        // Comprobar las preferencias del usuario sobre el idioma
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // Si el usuario ha indicado el idioma en el menú de las preferencias
        if (sp.contains("miidioma")){
            this.idiomaActual = sp.getString("miidioma", "es");
        }
        // Enviar idioma actual en el intent
        i.putExtra("var_idioma", idiomaActual);
        startActivity(i);
    }

    //---------------------------------------------------------------------------------
    // 3) Método REGISTRO: Este método será el encargado de validar los datos introducidos
    //    por el usuario. Si son correctos, se le notificará de que se ha registrado correc-
    //    tamente. De no ser así, será notificado del problema con Toasts o Dialogs.
    public void registro(View v) {
        // Coger los datos de los campos
        String u = usuario.getText().toString();
        String e = email.getText().toString();
        String c = contrasena.getText().toString();
        String repc = repContrasena.getText().toString();
        //-----------------------------------------------
        // Pintar contraseñas de negro
        contrasena.setTextColor(getResources().getColor(R.color.black));
        repContrasena.setTextColor(getResources().getColor(R.color.black));

        /*········· VALIDACIÓN DE CAMPOS DEL REGISTRO ······*/
        // Si hay algún campo incompleto
        if (u.length() == 0 || e.length() == 0 || c.length() == 0 || repc.length() == 0) {
            Toast.makeText(this, getResources().getString(R.string.e_registroVacio), Toast.LENGTH_LONG).show();
        }
        else {
            // Si todos los campos están completos
            if (validarMail(e)) { // Si el email es CORRECTO
                if (c.equals(repc)) { //Si contraseña y repetida son IGUALES

                    // CONSULTA A DB REMOTA
                    // Indicar ruta del recurso PHP
                    String ruta = URL + "usuarios.php";
                    StringRequest stringRequest = new StringRequest(
                    Request.Method.POST, ruta,
                    new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                            if (!response.isEmpty()){
                                // Si el usuario YA está registrado
                                // Mostrar Dialogo para avisar al usuario (Dial_UsuarioYaRegistrado)
                                Dial_UsuarioYaRegistrado d = new Dial_UsuarioYaRegistrado();
                                d.show(getSupportFragmentManager(), "registroFallido");
                            }
                            else{
                                // EL USUARIO NO EXISTE, SE PUEDE CREAR

                                /*········· AÑADIR USUARIO A DB REMOTA ·········*/
                                StringRequest sr = new StringRequest(
                                Request.Method.POST, ruta, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (response.contains("200")) {
                                        // Notificar al usuario del éxito y llevarlo al login
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.bienvenido)
                                        + u + " !", Toast.LENGTH_LONG).show();

                                        Intent i = new Intent(getApplicationContext(), Act_Login.class);
                                        i.putExtra("var_email", e);

                                        // Comprobar las preferencias del usuario sobre el idioma
                                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                        // Si el usuario ha indicado el idioma en el menú de las preferencias
                                        if (sp.contains("miidioma")) {
                                            idiomaActual = sp.getString("miidioma", "es");
                                        }

                                        i.putExtra("var_idioma", idiomaActual);
                                        startActivity(i);

                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.errorr), Toast.LENGTH_LONG).show();
                                    }
                                }}, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        // Si ha habido un error, añadirlo al Log de errores
                                        Log.e("DB_REMOTA", "Error al registrar usuario");
                                    }
                                }){
                                    @Nullable
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        // Preparar parámetros para petición PHP
                                        Map<String, String> parametros = new HashMap<String, String>();
                                        // Recurso -> usuarios.php>anadirUsuario
                                        parametros.put("id_recurso", "anadirUsuario");
                                        Log.d("NOMBRE", "El nombre es: "+u);
                                        parametros.put("nombre", u);
                                        parametros.put("email", e);
                                        parametros.put("contraseña", c);
                                        // Enviar
                                        return parametros;
                                    }
                                };
                                // Añadir solicitud a la cola (RequestQueue)
                                rq.add(sr);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Si ha habido un error, añadirlo al Log de errores
                            Log.e("DB_REMOTA", "Error al comprobar si existe el usuario");
                        }
                    })
                    {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            // Preparar parámetros para petición PHP
                            Map<String, String> parametros = new HashMap<String, String>();
                            // Recurso -> usuarios.php>comprobarUsuario
                            parametros.put("id_recurso", "comprobarUsuario");
                            parametros.put("email", e);
                            //Enviar
                            return parametros;
                        }
                    };
                    // Añadir solicitud a la cola (RequestQueue)
                    rq.add(stringRequest);
                }
                else {
                    // Si contraseña y repetida NO SON IGUALES
                    Toast.makeText(this, getResources().getString(R.string.e_contraseñasRepesNoCoinciden), Toast.LENGTH_LONG).show();
                    contrasena.setTextColor(getResources().getColor(R.color.rojoNeon));
                    repContrasena.setTextColor(getResources().getColor(R.color.rojoNeon));
                }
            }
            else{ // Si el email es INCORRECTO
                // Notificar al usuario por medio de un Toast
                Toast.makeText(this, getResources().getString(R.string.e_emailIncorrecto), Toast.LENGTH_LONG).show();
            }
        }
    }

    //---------------------------------------------------------------------------------
    // 4) Método VALIDAR_MAIL: Este método comprobará que la cadena introducida en el
    //    campo del email es válido y cumple con el formato de los emails x@y.zz
    public boolean validarMail(String email){
        /*
        // Código de validación de Mail
        // FUENTE: https://www.baeldung.com/java-email-validation-regex
        */
        // Patrón para validar el email
        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        // Verificar que se corresponde con el patron regex
        Matcher mather = pattern.matcher(email);
        // Si coincide, devolver true
        if (mather.find() == true) {
            return true;
        } else {
            // Si NO coincide, devolver false
            return false;
        }
    }

    //---------------------------------------------------------------------------------
    // 5) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy() de la
    //    aplicación, por lo que es aquí dónde se deben guardar los datos que se quieran
    //    mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil, interrupciones, etc.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar datos en el objeto outState para evitar la
        // pérdida al girar el móvil, interrupciones, etc.
        outState.putString("var_idioma", this.idiomaActual);
    }

}