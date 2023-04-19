package com.example.entregaindividual_2_anelopezmena.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.entregaindividual_2_anelopezmena.R;

/*******************************************************************/
/** ----------------- DIAL_PELICULA_YA_EXISTE ------------------- **/
/*******************************************************************/
// Clase JAVA asociada al diálogo que se mostrará cuando el usuario
// intenta añadir una película que ya ha sido registrada anteriormente.
// Extiende de la clase 'DialogFragment'

public class Dial_PeliculaYaExiste extends DialogFragment {

    //---------------------------------------------------------------------------------
    // 1) Método constructor (vacío)
    public Dial_PeliculaYaExiste() { }

    //---------------------------------------------------------------------------------
    // 2) Método ON_CREATE_DIALOG: Método que se ejecuta al crearse el dialog
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Utilizar la clase Builder, para construir el diálogo más adelante
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Añadir el aspecto al dialog --> (layout) dial_1_boton.xml
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View aspecto = inflater.inflate(R.layout.dial_1_boton, null);

        // Recoger views del aspecto creado
        TextView d_titulo = aspecto.findViewById(R.id.dial_1_boton_titulo);
        TextView d_mensaje = aspecto.findViewById(R.id.dial_1_boton_mensaje);
        Button d_OK = aspecto.findViewById(R.id.dial_1_boton_mensaje_boton);

        // Settear texto del dialog, en diferentes idiomas
        d_titulo.setText(R.string.d_peliYaRegistrada_t);
        d_mensaje.setText(R.string.d_peliYaRegistrada_m);
        d_OK.setText(R.string.d_peliYaRegistrada_b);

        // Cuando aparezca el dialog de la película que YA EXISTE, el usuario
        // tiene la opción de pulsar el botón de OK
        d_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Se cierra el dialog con un 'dismiss'
                dismiss();
            }
        });

        // Settear el aspecto en el builder
        builder.setView(aspecto);

        // Crear el objeto AlertDialog desde el builder y devolverlo
        return builder.create();
    }
}
