package com.example.entregaindividual_2_anelopezmena.activities;

import static com.example.entregaindividual_2_anelopezmena.activities.Act_MenuPrincipal.NOTIFICACION_ID;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.entregaindividual_2_anelopezmena.dialogs.Dial_CredencialesIncorrectas;
import java.util.HashMap;
import java.util.Map;

/*************************************************************************/
/** ------------------------- ACTIVITY_LOGIN -------------------------- **/
/*************************************************************************/
// De forma complementaria a la actividad anterior (Act_Registro), la pantalla
// de login, será la forma de permitir el acceso a los usuarios a los servicios
// de ‘BlockBuster!’. Si el usuario introduce correctamente su combinación de
// usuario y contraseña, podrá acceder a la app, mientras que si es incorrecta,
// recibirá una alerta indicando que ha habido un error al acceder a sus sesión.

public class Act_Login extends AppCompatActivity {
    // Atributos privados
    private EditText email, contrasena;
    private Button b_login;
    private Gestor_Idioma gestorIdioma;
    private String idiomaActual;
    private String e_anterior, e;
    private RequestQueue rq;

    // URL del servidor AWS de DAS (22-23)
    private static final String URL="http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/alopez437/WEB/";

    //---------------------------------------------------------------------------------
    // 1) Método ON_CREATE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Instanciar objeto RequestQueue para peticiones al servidor
        rq = Volley.newRequestQueue(this);
        // Crear el manager de notificaciones
        NotificationManagerCompat NMC = NotificationManagerCompat.from(getApplicationContext());
        // Cuando se entre a la actividad de Login, desaparecerá la notificación
        NMC.cancel(NOTIFICACION_ID);

        // -----------------------------------------------------------------------------------
        // Crear gestor de idioma
        gestorIdioma = new Gestor_Idioma(this);
        // -----------------------------------------------------------------------------------
        // Recibir intent desde la pantalla de registro
        Bundle extras = getIntent().getExtras();
        // Si el usuario se ha registrado ahora, guardar el email para copiarlo
        // en el campo de email del Act_Login
        if (extras != null){
            this.e_anterior = extras.getString("var_email");
            this.idiomaActual = extras.getString("var_idioma");
        }
        // -----------------------------------------------------------------------------------
        // Gestionar pérdida de información al girar la pantalla:
        // GUARDAR:
        //      * EMAIL
        //      * IDIOMA
        if (savedInstanceState != null){
            // Obtener idioma guardado para volver a settearlo, además del email
            this.idiomaActual = savedInstanceState.getString("var_idioma");
            this.e = savedInstanceState.getString("var_email");
        }
        // Settear idioma recuperado
        gestorIdioma.setIdioma(idiomaActual);

        // Crear la vista
        setContentView(R.layout.act_login_layout);

        // Una vez creada la vista, escribir email en el campo
        email = (EditText) findViewById(R.id.id_usuario_login);
        email.setText(e_anterior);
    }

    // -----------------------------------------------------------------------------------
    // 2) Método COMPROBAR_USUARIO: Aquí se comprobarán las credenciales del
    //    usuario que intenta loguearse. Si son correctas, se le dará paso, y si no,
    //    se le mostrará un dialogo informativo.
    public void comprobarUsuario(View v){
        // Conseguir elementos de la vista
        email = (EditText) findViewById(R.id.id_usuario_login);
        contrasena = (EditText) findViewById(R.id.id_contrasena_login);
        b_login = (Button) findViewById(R.id.id_login);

        // Conseguir texto de los elementos
        String e = email.getText().toString();
        String c = contrasena.getText().toString();

        //------------------------------------------------------------------------------------------

        if (e.length()>0 && c.length()>0) {

            //------------------- COMPROBAR CONTRASEÑA DE USUARIO EN DB -----------------------
            // Consulta a DB_REMOTA
            // Indicar recurso PHP del servidor en la URL
            String ruta = URL + "usuarios.php";
            // Crear objeto StringRequest
            StringRequest sr = new StringRequest(
            Request.Method.POST, ruta, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (response.contains("200")) {
                        // La CONTRASEÑA es CORRECTA
                        // Llamar a act_MenuPrincipal y hacer finish
                        Intent i = new Intent(getApplicationContext(), Act_MenuPrincipal.class);
                        // Pasar email e idioma a la siguiente pantalla
                        i.putExtra("var_email", e);
                        i.putExtra("var_idioma", idiomaActual);
                        // Lanzar intent y finish de la activity actual
                        startActivity(i);
                        finish();
                    } else {
                        // La CONTRASEÑA es INCORRECTA
                        // No se ha encontrado al usuario en la DB
                        // Crear dialog para informar al usuario
                        Dial_CredencialesIncorrectas d = new Dial_CredencialesIncorrectas();
                        d.show(getSupportFragmentManager(), "CredencialesIncorrectas");

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // No se encuentra el usuario
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    // Enviar parámetros para la petición por PHP
                    Map<String, String> parametros = new HashMap<String, String>();
                    // RECURSO --> usuarios.php> comprobarContraseña
                    parametros.put("id_recurso", "comprobarContraseña");
                    parametros.put("email", e);
                    parametros.put("contraseña", c);
                    // Enviar
                    return parametros;
                }
            };
            // Añadir solicitud a la cola (RequestQueue)
            rq.add(sr);
        }else{
            // Hay algún campo vacío
            Toast.makeText(this, getResources().getString(R.string.e_registroVacio), Toast.LENGTH_LONG).show();
        }
    }

    // -----------------------------------------------------------------------------------
    // 3) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy() de la
    //  aplicación, por lo que es aquí dónde se deben guardar los datos que se quieran
    //  mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil, interrupciones,
    //  etc.
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardar datos para evitar la pérdida al girar el móvil, interrupciones, etc.
        outState.putString("var_idioma", this.idiomaActual);
        outState.putString("var_email", e);
    }


}