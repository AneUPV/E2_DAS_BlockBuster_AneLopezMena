package com.example.entregaindividual_2_anelopezmena.interfaces;


/*******************************************************************/
/** ------------------------ INTERFAZ_DIAL----------------------- **/
/*******************************************************************/
// Esta interfaz contiene las cabeceras de los métodos que implementarán otras
// clases y que ejecutarán los listeners.

public interface Interfaz_Dial {
    void cerrarSesion();               // Implementado en 'Act_MenuPrincipal'
    void cambiarCaratula(String url);  // Implementado en 'Act_ModificarPelicula'
                                       // Implementado en 'Act_AñadirPelicula'
}
