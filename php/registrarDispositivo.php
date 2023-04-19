<?php

# Incluir la conexión con la base de datos ('conexion.php')
# Incluir cabeceras
include_once 'headers.php';
include 'conexion.php';

#======================================================================#
#                     REGISTRAR DISPOSITIVO.php                        #
########################################################################
# Este fichero PHP se encargará de registrar un token para cada usuario
# en la base de datos. Este token es necesario para enviar notificaciones
# globales mediante la mensajería FCM

# Recoger parámetros de la petición
$token=$_POST['DEVICEID'];
$email=$_POST['email'];

# Si el token es válido
if ($token != NULL){
   # Y si el email es válido
    if ($email != null){

        # Se inserta ese token para ese usuario en la tabla Usuarios
        $stmt = $con->prepare("UPDATE usuarios SET DEVICEID='$token' WHERE email='$email'");
        $res = $stmt->execute();
        $stmt->close();
        # Devolver un código 200
        echo '{"code":"200"}';
    }
    $con->close();

}else{
    # Devolver un código 500 si alguno de los parámetros recibidos
    # no era válido (era null)
    echo '{"code":"500"}';
}


?>