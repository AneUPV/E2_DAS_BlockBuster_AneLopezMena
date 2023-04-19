<?php

# Incluir la conexión con la base de datos ('conexion.php')
include 'conexion.php';

#======================================================================#
#                               USUARIOS.php                           #
########################################################################
# Este fichero PHP agrupa todas las operaciones relacionadas con la tabla
# 'usuarios' de la base de datos "Xalopez437_peliculasDB". Para diferenciar 
# en cada caso la operación a realizar, se ha utilizado un * parámetro 
# especial * que determinara qué líneas se ejecutarán.
#
# Se trata de 'id_recurso' -> Según el valor enviado en la petición para 
#                             el parámetro 'id_recurso' se ejecutarán
#                             diferentes acciones (comprobarUsuario, 
#                             anadirUsuario, getNombreUsuario, etc.)

# Obtener valor clave 'id_recurso'
$RECURSO=$_POST['id_recurso'];

# Según el valor del parámetro, ejecutar una sección:
switch($RECURSO){

    # 1) Comprobar si el usuario existe en la DB
    case "comprobarUsuario":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            
            # Realizar una consulta para comprobar si el usuario existe
            $query=$con->prepare("SELECT * FROM usuarios WHERE email=?");
            # Pasar parámetros a la consulta
            $query->bind_param('s',$email);
            # Ejecutar consulta contra la DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            if ($fila = $resultado->fetch_assoc()) {
                # Devolver resultado en formato JSON
                echo json_encode($fila,JSON_UNESCAPED_UNICODE);     
            }
        }
        break;

    # 2) Añadir un nuevo usuario a la DB (registrar usuario)
    case "anadirUsuario":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
            
            # Recoger parámetros de la petición
            $nombre=addslashes($_POST['nombre']);
            $email=addslashes($_POST['email']);
            $contraseña=addslashes($_POST['contraseña']);

            # Generar hash para almacenar la contraseña de forma segura. De esta manera, la contraseña
            # no se mostrará en claro en la base de datos
            $hash = password_hash($contraseña, PASSWORD_DEFAULT);

            # Insertar usuario en la DB mediante consulta INSERT
            $query = "INSERT INTO usuarios (email, nombre, contraseña) VALUES ('$email', '$nombre', '$hash')";
            echo $query;
            # Obtener respuesta
            $respuesta = $con ->query($query);

            # Si ha habido éxito
            if($respuesta === TRUE){
                # Devolver resultado 'code 200'
                echo '{"code":"200"}';
            }else{
                # Si no ha habido éxito
                # Devolver resultado 'code 500'
                echo '{"code":"500"}';
            }
        }
        break;
    
    # 3) Comprobar credenciales del usuario
    case "comprobarContraseña":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            $contraseña=addslashes($_POST['contraseña']);
            
            # Preparar consulta a ejecutar - Obtener HASH de la contraseña
            $query = $con->prepare("SELECT contraseña from usuarios where email = '$email'");
            $query->execute();

            # Obtener resultado
            $resultado = $query->get_result();
            # Obtener hash
            $hash = $resultado->fetch_assoc();

            # Verificar si la contraseña introducida coincide con el hash almacenado
            if (password_verify($contraseña, $hash["contraseña"]) ){
                # Contraseña correcta
                echo '{"code":"200"}';
            }else{
                # Contraseña incorrecta
                echo '{"code":"500"}';
            }
        }
        break;

    # 4) Obtener nombre de usuario a partir del email
    case "getNombreUsuario":
        
        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
            
            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            
            # Preparar consulta y ejecutarla
            $query = $con->prepare("SELECT nombre from usuarios where email='$email'");
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            # Si ha habido resultado
            if ($fila = $resultado->fetch_assoc()) {
                # Devolver el nombre de usuario
                echo $fila["nombre"];     
            }
            $query->close();
        }
        break;

    # 5) Actualizar el nombre de usuario. Esto es posible por el uso de preferencias, 
    # que permiten al usuario modificar su nombre de usuario
    case "actualizarUsuario":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $nombre=addslashes($_POST['nombre']);
            $email=addslashes($_POST['email']);
            
            # Ejecutar consulta de update en la base de datos para ese usuario
            $query ="UPDATE usuarios SET nombre='$nombre' WHERE email='$email'";
        
            # Obtener resultado
            $resultado = $con -> query($query);
        
            if ($resultado === TRUE){
                # Actualización exitosa
                echo '{"code":"200"}';
            }else{
                # Error actualizando los datos del usuario
                echo '{"code":"500"}';
            }
        }
        break;
        
        
}

# Cerrar conexión con DB

$con->close();

?>