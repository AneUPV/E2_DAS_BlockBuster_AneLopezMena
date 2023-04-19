<?php

# Incluir la conexión con la base de datos ('conexion.php')
include 'conexion.php';

#======================================================================#
#                             PELICULAS.php                            #
########################################################################
# Este fichero PHP agrupa todas las operaciones relacionadas con la tabla
# 'peliculas' de la base de datos "Xalopez437_peliculasDB". Para diferenciar 
# en cada caso la operación a realizar, se ha utilizado un * parámetro 
# especial * que determinara qué líneas se ejecutarán.
#
# Se trata de 'id_recurso' -> Según el valor enviado en la petición para 
#                             el parámetro 'id_recurso' se ejecutarán
#                             diferentes acciones (eliminarPelicula, 
#                             insertarPelicula, etc.)

# Obtener valor clave 'id_recurso'
$RECURSO=$_POST['id_recurso'];

# Según el valor del parámetro, ejecutar una sección:
switch($RECURSO){

    # 1) Insertar los datos de una nueva película en la base de datos
    case "insertarPelicula":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $titulo=addslashes($_POST['titulo']);
            $caratula=addslashes($_POST['caratula']);
            $director=addslashes($_POST['director']);
            $actores=addslashes($_POST['actores']);
            $genero=addslashes($_POST['genero']);
            $trama=addslashes($_POST['trama']);
            $anyo=addslashes($_POST['anyo']);
            $valoracion=addslashes($_POST['valoracion']);
            $duracion=addslashes($_POST['duracion']);
            $trailer=addslashes($_POST['trailer']);

            # Preparar consulta
            $query = "INSERT INTO peliculas (titulo, caratula, director, actores, genero, trama, anyo, valoracion, duracion, trailer) VALUES ('$titulo', '$caratula', '$director', '$actores', '$genero', '$trama', '$anyo', '$valoracion', '$duracion', '$trailer')";
            # y ejecutarla
            $respuesta = $con ->query($query);

            # Si ha ido bien, devolver registro nuevo
            if($respuesta === TRUE){

                # Preparar consulta y ejecutar
                $query = $con->prepare("SELECT * from peliculas where titulo = '$titulo' and director='$director'");
                $query->execute();

                # Obtener resultado
                $resultado = $query->get_result();
                echo '{"code":"200"}';
                
                if ($fila = $resultado->fetch_assoc()) {
                    # Devolver resultado en formato JSON
                    echo json_encode($fila,JSON_UNESCAPED_UNICODE);   
                }

            }else{
                echo '{"code":"500"}';
            }
        }
        break;
   
    # 2) Obtener todas las películas registradas en la DB  
    case "getPeliculas":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
                        
            # Obtener todas las películas
            $query=$con->prepare("SELECT * FROM peliculas");
            # Ejecutar consulta contra la DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            # Para cada una de las filas obtenidas en la consulta, convertirlas a JSON 
            # y devolverlas como respuesta separadas por un tab ('\t' -> Util para separar
            # las películas en el código JAVA)
            for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
                $fila = $resultado->fetch_assoc();
                echo json_encode($fila,JSON_UNESCAPED_UNICODE); 
                if ($i != mysqli_num_rows($resultado) ){  
                    echo "\t";
                }
            }
        }
        break;
    # 3) Eliminar una película de la DB 
    case "eliminarPelicula":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
            
            # Recoger parámetros de la petición
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);
           
            # Preparar consulta y ejecutarla
            $query = "DELETE from peliculas where titulo ='$titulo' and director='$director'";
            $respuesta = $con ->query($query);

            # Si devuelve TRUE (OK)
            if($respuesta === TRUE){
                # Devolver resultado 'code 200'
                echo '{"code":"200"}';
            }else{ # Si devuelve FALSE (error)
                # Devolver resultado 'code 500'
                echo '{"code":"500"}';
            }
        }
        break;

    # 4) Actualizar datos de una película de la DB 
    case "actualizarPelicula":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
         
            # Recoger parámetros de la petición
            $titulo=addslashes($_POST['titulo']);
            $caratula=addslashes($_POST['caratula']);
            $director=addslashes($_POST['director']);
            $actores=addslashes($_POST['actores']);
            $genero=addslashes($_POST['genero']);
            $trama=addslashes($_POST['trama']);
            $anyo=addslashes($_POST['anyo']);
            $valoracion=addslashes($_POST['valoracion']);
            $duracion=addslashes($_POST['duracion']);
            $trailer=addslashes($_POST['trailer']);
     
            # Preparar consulta 
            $query = "UPDATE peliculas SET caratula='$caratula', actores='$actores', genero='$genero', trama='$trama', anyo='$anyo', valoracion='$valoracion', duracion='$duracion', trailer='$trailer' WHERE titulo='$titulo' AND director='$director'";
            # Ejecutarla y obtener resultado
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

    # 5) Conseguir todos los datos de una película a partir del título y el director 
    case "conseguirPelicula":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $titulo=addslashes($_POST['titulo']);
            $director=addslashes($_POST['director']);

            # Preparar consulta y ejecutarla
            $query = $con->prepare("SELECT * from peliculas where titulo = '$titulo' and director='$director'");
            $query->execute();

            # Obtener resultado
            $resultado = $query->get_result();

            # Si ha habido resultado
            if ($fila = $resultado->fetch_assoc()) {
                # Devolver el resultado en formato JSON
                echo json_encode($fila,JSON_UNESCAPED_UNICODE);   
            }
        }
        break;

    # 6) Conseguir todos los datos de las películas del año indicado
    case "getPeliculasNuevas":
        
        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){

            # Recoger parámetros de la petición
            $anyo=addslashes($_POST['anyo']);

            # Preparar consulta y ejecutarla
            $query = $con->prepare("Select * from peliculas where anyo='$anyo'");
            $query->execute();

            # Obtener resultado
            $resultado = $query->get_result();

            # Para cada una de las filas obtenidas en la consulta, convertirlas a JSON 
            # y devolverlas como respuesta separadas por un tab ('\t' -> Util para separar
            # las películas en el código JAVA)
            for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
                $fila = $resultado->fetch_assoc();
                echo json_encode($fila,JSON_UNESCAPED_UNICODE); 
                if ($i != mysqli_num_rows($resultado) ){  
                    echo "\t";
                }
            }

        }
        break;

    # 7) Conseguir todas las URLs de las carátulas, para cada película 
    # registrada en la DB
    case "getCaratulas":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
                        
            # Preparar consulta
            $query=$con->prepare("SELECT caratula FROM peliculas");
            # Ejecutar consulta contra DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            # Para cada una de las filas obtenidas en la consulta, separarlas 
            # por un tab ('\t' -> Util para separar las carátulas en el código JAVA)
            for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
                $fila = $resultado->fetch_assoc();
                echo $fila['caratula']; 
                if ($i != mysqli_num_rows($resultado) ){  
                    echo "\t";
                }
            }

        }
        break;

    # 7) Conseguir todos los títulos de las películas guardadas en la DB
    case "getNombrePeliculas":

        # Si el método HTTP es POST, proceder
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
                        
            # Realizar consulta
            $query=$con->prepare("SELECT * FROM peliculas");
            # Ejecutar consulta contra DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            # Para cada una de las filas obtenidas en la consulta, separarlas 
            # por un tab ('\t' -> Util para separar los títulos en el código JAVA)
            for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
                $fila = $resultado->fetch_assoc();
                echo $fila['titulo']; 
                if ($i != mysqli_num_rows($resultado) ){  
                    echo "\n";
                }
            }
        }
        break;

    /*# 8) Conseguir todos los títulos de las películas guardadas en la DB
    case "getCaratulasNuevas":
        if ($_SERVER['REQUEST_METHOD'] == "POST"){
                        
            # Realizar una consulta para comprobar si el usuario existe
            $query=$con->prepare("SELECT caratula FROM peliculas WHERE anyo >=2020");
            # Ejecutar consulta contra la DB
            $query->execute();
            # Obtener resultado
            $resultado = $query->get_result();

            for ($i = 1; $i <= mysqli_num_rows($resultado); $i++) {
                $fila = $resultado->fetch_assoc();
                echo $fila['caratula']; 
                if ($i != mysqli_num_rows($resultado) ){  
                    echo "\n";
                }
            }

        }
        break;
*/
 
}

# Cerrar conexión con DB

$con->close();

?>