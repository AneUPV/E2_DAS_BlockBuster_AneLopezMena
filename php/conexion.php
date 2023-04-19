<?php

#======================================================================#
#                             CONEXION.php                             #
########################################################################
# Consiste en el fichero que establecerá la conexión con la base de datos 
# remota. Es necesario indicar las credenciales de acceso al servicio de
# phpMyAdmin para poder establecer la conexión con la DB MysQL alojada en
# el servidor.

#     * Usuario : Xalopez437  * Clave : inHUYjcoM
#     * ec2-54-93-62-124.eu-central-1.compute.amazonaws.com

$DB_SERVER="localhost"; #la dirección del servidor
$DB_USER="Xalopez437";  #el usuario para esa base de datos
$DB_PASS="inHUYjcoM";   #la clave para ese usuario


# Nombre de la base de datos a la que hay que conectarse
$DB_DATABASE="Xalopez437_peliculasDB"; 

# Se establece la conexión:
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);

# Comprobamos conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexion: ' . mysqli_connect_error();
    exit();
}else{
    //echo 'Conexión OK!!! ';
}
?>