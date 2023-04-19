<?php

# Incluir la conexión con la base de datos ('conexion.php')
include 'conexion.php';

#======================================================================#
#                               GUARDAR.php                            #
########################################################################
# Este fichero PHP agrupa todas las operaciones relacionadas con la tabla
# 'guardar' de la base de datos "Xalopez437_peliculasDB". Para diferenciar 
# en cada caso la operación a realizar, se ha utilizado un * parámetro 
# especial * que determinara qué líneas se ejecutarán.
#
# Se trata de 'id_recurso' -> Según el valor enviado en la petición para 
#                             el parámetro 'id_recurso' se ejecutarán
#                             diferentes acciones (getPeliculasSinVer, 
#                             insertarPeliculaVista, etc.)


# Obtener valor clave 'id_recurso'
$RECURSO=$_POST['id_recurso'];

# Según el valor del parámetro, ejecutar una sección:
switch($RECURSO){

    # 1) Obtener si una película ya ha sido vista por el usuario
    case "getVisto":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);

            # Preparar consulta a ejecutar
            $query=$con->prepare("SELECT * from guardar WHERE email = '$email' and titulo='$titulo' and director='$director'");
            # Ejecutar consulta contra la DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            # Si ha habido resultado (Ya la ha visto)
            if ($fila = $resultado->fetch_assoc()) {
                # Devolver resultado 'code 200'
                echo '{"code":"200"}';  
            }
            else{ # Si no ha habido resultado (No la ha visto)
                # Devolver resultado 'code 400'
                echo '{"code":"404"}';
            }
        }
        break;

    # 2) Marcar una película como vista por el usuario indicado
    case "anadirVisto":
        
        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
            
            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);

           # Preparar consulta y ejecutarla
            $query = "INSERT INTO guardar (email, titulo, director) VALUES ('$email', '$titulo', '$director')";
            $respuesta = $con ->query($query);

            # Si ha ido bien
            if($respuesta === TRUE){
                 # Devolver resultado 'code 200'
                echo '{"code":"200"}';
            } 
            else{ # Si no ha ido bien
                  # Devolver resultado 'code 500'
                echo '{"code":"500"}';
            }
        }
        break;

    # 3) Obtener lista de películas que el usuario no ha visto aún
    case "getPeliculasSinVer":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);

            # Preparar consulta y ejecutarla
            $query = "SELECT * FROM peliculas WHERE titulo NOT IN (SELECT titulo FROM guardar WHERE email='$email')";
            $respuesta = $con ->query($query);

            # Para cada una de las filas obtenidas en la consulta, convertirlas a JSON 
            # y devolverlas como respuesta separadas por un tab ('\t' -> Util para separar
            # las películas en el código JAVA)
            for ($i = 1; $i <= mysqli_num_rows($respuesta); $i++) {
                $fila = $respuesta->fetch_assoc();
                echo json_encode($fila,JSON_UNESCAPED_UNICODE); 
                if ($i != mysqli_num_rows($respuesta) ){  
                    echo "\t";
                }
            }
        }
        break;

    # 4) Marcar una película como vista para un usuario en particular
    case "insertarPeliculaVista":
        
        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);

            # Preparar consulta y ejecutarla
            $query = "INSERT INTO guardar (email, titulo, director) VALUES ('$email', '$titulo', '$director')";
            $respuesta = $con ->query($query);

            # Si ha ido bien
            if($respuesta === TRUE){
                 # Devolver resultado 'code 200'
                echo '{"code":"200"}';
            }
            else{ # Si no ha ido bien
                    # Devolver resultado 'code 500'
                echo '{"code":"500"}';
            }
        }
        break;

    # 5) Marcar una película como NO vista para un usuario en particular
    case "eliminarPeliculaVista":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
            
            # Recoger parámetros de la petición
            $email=addslashes($_POST['email']);
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);

            # Preparar consulta y ejecutarla
            $query = "DELETE from guardar where email ='$email' and titulo='$titulo' and director='$director'";
            $respuesta = $con ->query($query);

            # Si ha ido bien
            if($respuesta === TRUE){
                 # Devolver resultado 'code 200'
                echo '{"code":"200"}';
            }
            else{ # Si no ha ido bien
                     # Devolver resultado 'code 500'
                echo '{"code":"500"}';
            }
        }
        break;
}

# Cerrar conexión con DB

$con->close();

?>

