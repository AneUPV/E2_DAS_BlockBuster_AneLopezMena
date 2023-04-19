<?php

# Incluir la conexión con la base de datos ('conexion.php')
include 'conexion.php';

#======================================================================#
#                       ENVIAR NOTIFICACION.php                        #
########################################################################
# Este fichero php atiende las peticiones realizadas desde la clase 
# 'Act_AñadirPelicula', de manera que, cada vez que se añade una obra en 
# la DB, se obtienen todos los Token diferentes de los dispositivos, y se
# les notifica sobre el cambio en la cartelera de forma inmediata.


# Indicar API KEY para funcionalidades de FCM
$FCM_APIKEY="AAAA_ey0UfU:APA91bGDfF7EnHanib4i9s6kK4B6xZUiHf90plgnOZ6XG44T6fkuVlmzd_bJ73scd8D2-3qemOaGCPre-dhNObiRTEmHERNgzykfIGPK2hqna4Za00FygttL-y7V-Bq31eX9dNgI_OGW";

# Si la petición se realiza por método POST, proceder
if ($_SERVER['REQUEST_METHOD'] == "POST"){

    # Obtener los datos desde la petición
    $tituloNoti=$_POST['tituloN'];
    $mensajeNoti=$_POST['mensajeN'];
    $t_idioma=$_POST['idioma'];


    # Recoger en un array los tokens de todos los usuarios
    $listaTokens = array();
    $query= $con->prepare("SELECT DISTINCT DEVICEID from usuarios");
    $query->execute();

    # Obtener resultado
    $resultado = $query->get_result();

    # Para todos los token obtenidos en la consulta
    for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
        $fila = $resultado->fetch_assoc();
        # Insertarlos en el array
        array_push($listaTokens, $fila['DEVICEID']);
        
    }

       
    # Conseguir los datos de la película nueva para pasárselos a 
    # la activity Info_Peliculas mediante el Intent al clicar la 
    # notificación

    # Devolver resultado en formato JSON
    $t_titulo = $_POST['tituloP'];
    $t_caratula = $_POST['caratulaP'];
    $t_director = $_POST['directorP'];
    $t_actores = $_POST['actoresP'];
    $t_genero = $_POST['generoP'];
    $t_trama = $_POST['tramaP'];
    $t_anyo = $_POST['anyoP'];
    $t_duracion = $_POST['duracionP'];
    $t_valoracion = $_POST['valoracionP'];
    $t_trailer = $_POST['trailerP'];

    # Preparar cabeceras para la petición
    $cabecera = array("Authorization: key=$FCM_APIKEY","Content-Type: application/json");

    #$query= $con->prepare("SELECT DISTINCT DEVICEID from usuarios");

    # Integrar los datos de la petición en el mensaje de la notificación, de manera
    # que se puedan escribir en la pantalla de información de la película si se
    # abre la notificación.
    $msg = array(
        'registration_ids'=> $listaTokens,
        'data' => array(
            "var_caratula" => $t_caratula,
            "var_titulo" => $t_titulo,
            "var_director" => $t_director,
            "var_actores" => $t_actores,
            "var_genero" => $t_genero,
            "var_duracion" => $t_duracion,
            "var_trama" => $t_trama,
            "var_anyo" => $t_anyo,
            "var_trailer" => $t_trailer,
            "var_valoracion" => $t_valoracion,
            "var_idioma" => $t_idioma
        ),
        'notification' => array(
            'title' => $tituloNoti,
            'body' => $mensajeNoti,
            'image' => $t_caratula,
            'click_action' => "PELICULANUEVA"
        )
    );

   
    # Convertir mensaje a JSON
    $msgJSON = json_encode($msg);

    # Indicar URL de FCM
    $url = 'https://fcm.googleapis.com/fcm/send';


    #  ------ Abrir conexión CURL  ------ 
    # Inicializar el handler de curl
    $ch = curl_init(); 
    # Indicar el destino de la petición, el servicio FCM de google
    curl_setopt( $ch, CURLOPT_URL, $url );
    # Indicar que la conexión es de tipo POST
    curl_setopt( $ch, CURLOPT_POST, true );
    # Agregar las cabeceras
    curl_setopt( $ch, CURLOPT_HTTPHEADER, $cabecera);
    # Indicar que se desea recibir la respuesta a la conexión en forma de string
    curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
    # Agregar los datos de la petición en formato JSON
    curl_setopt( $ch, CURLOPT_POSTFIELDS, $msgJSON );

    # Ejecutar la llamada
    $resultado= curl_exec( $ch );

    # Si ha ocurrido un error, devolverlo
    if (curl_errno($ch)) {
        print curl_error($ch);
    }
    echo $resultado;

    # Cerrar el handler de curl
    curl_close( $ch );

}

?>