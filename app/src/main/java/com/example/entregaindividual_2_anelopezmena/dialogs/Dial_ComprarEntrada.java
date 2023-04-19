package com.example.entregaindividual_2_anelopezmena.dialogs;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.controlador.ReminderBroadcast;
import com.example.entregaindividual_2_anelopezmena.interfaces.InterfazRV;
import com.example.entregaindividual_2_anelopezmena.java.Pelicula;

import java.util.Calendar;


/*******************************************************************/
/** ------------------  DIAL_COMPRAR_ENTRADAS ------------------- **/
/*******************************************************************/
// Clase JAVA asociada al diálogo que se mostrará cuando el usuario
// pulse la opción de comprar entradas. En esta se incluirá una pantalla
// en la que el usuario podrá concretar los datos de la compra (asientos,
// num de entradas, fecha...). Extiende de la clase 'DialogFragment'

public class Dial_ComprarEntrada extends DialogFragment implements AdapterView.OnItemSelectedListener{

    // Atributos privados
    private InterfazRV listener;
    private int cuantas;
    private String asiento;
    private String email;
    private String director;
    private String titulo;

    //---------------------------------------------------------------------------------
    // 1.1) Método constructor
    public Dial_ComprarEntrada(InterfazRV listener, int pCantidad, String pFila, String pAsiento, String pEmail, String pTitulo) {
        // Recibe como parámetro una interfaz, que será la que actúe como listener
        // e implementa sus métodos en otra clase
        this.listener = listener;
        this.cuantas = pCantidad;
        this.asiento = pAsiento;
        this.email = pEmail;
        this.titulo = pTitulo;
    }

    //---------------------------------------------------------------------------------
    // 1.2) Método constructor
    public Dial_ComprarEntrada(){}

    //---------------------------------------------------------------------------------
    // 2) Método ON_CREATE_DIALOG: Método que se ejecuta al crearse el dialog, y que
    // contiene las acciones asociadas los botones de la pantalla de compra
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null){
            this.titulo = savedInstanceState.getString("var_titulo");
            this.cuantas = savedInstanceState.getInt("var_cantidad");
        }

        // Utilizar la clase Builder, para construir el diálogo más adelante
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Añadir el aspecto al dialog --> (layout) dial_2_boton.xml
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View aspecto = inflater.inflate(R.layout.dial_compra_entradas_2_boton, null);

        // Recoger views del aspecto creado
        TextView d_titulo = aspecto.findViewById(R.id.dial_1_boton_titulo);
        TextView d_mensaje = aspecto.findViewById(R.id.dial_1_boton_mensaje);
        TextView d_fechaHora = aspecto.findViewById(R.id.fechaHora);
        Button d_OK = aspecto.findViewById(R.id.aceptar);
        Button d_CANCEL = aspecto.findViewById(R.id.cancelar);

        // Settear cantidad de entradas para persistencia de datos
        TextView tv_cantidad = aspecto.findViewById(R.id.ce_numEntradas);
        tv_cantidad.setText(String.valueOf(this.cuantas));


        // Indicar la fecha y hora de las entradas para la sesión de cine
        int dia = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int mes = Calendar.getInstance().get(Calendar.MONTH);
        int año = Calendar.getInstance().get(Calendar.YEAR);
        int hora = Calendar.getInstance().get(Calendar.HOUR);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        d_fechaHora.setText(getResources().getString(R.string.cuando)+"   "+dia+"/"+mes+"/"+año+" - "+hora+":"+min);

        //Spinners
        // 1- FILA
        Spinner sp_fila = aspecto.findViewById(R.id.ce_fila);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.filas, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_fila.setAdapter(adapter);
        sp_fila.setOnItemSelectedListener(this);

        //2- ASIENTO
        Spinner sp_asiento = aspecto.findViewById(R.id.ce_asiento);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity(), R.array.asientos, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_asiento.setAdapter(adapter2);
        sp_asiento.setOnItemSelectedListener(this);

        // BOTONES DE CANTIDAD
        Button b_suma = aspecto.findViewById(R.id.suma);
        Button b_resta = aspecto.findViewById(R.id.resta);

        // Listener del botón SUMAR ENTRADA
        b_suma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView cantidad = aspecto.findViewById(R.id.ce_numEntradas);
                cuantas = Integer.parseInt(cantidad.getText().toString());
                // Si aún no se ha superado el máximo, incrementar contador
                if (cuantas < 9){
                   cuantas++;
                   cantidad.setText(String.valueOf(cuantas));
                    // Si se compra más de 1 entrada, mostrar mensaje informativo
                    if (cuantas > 1) {
                        TextView aviso = aspecto.findViewById(R.id.alerta);
                        aviso.setText(R.string.alerta);
                    }
                }
            }
        });
        // Listener del botón RESTAR ENTRADA
        b_resta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView cantidad = aspecto.findViewById(R.id.ce_numEntradas);
                cuantas = Integer.parseInt(cantidad.getText().toString());
                // Si aún no se ha pasado el minimo, decrementar contador
                if (cuantas > 1){
                   cuantas--;
                   cantidad.setText(String.valueOf(cuantas));
                   // Si solo se compra una, eliminar mensaje informativo
                    if ( cuantas ==1){
                        TextView aviso = aspecto.findViewById(R.id.alerta);
                        aviso.setText("");
                    }
                }
            }
        });

        // Settear texto del dialog, en diferentes idiomas
        d_titulo.setText(R.string.compra);
        d_mensaje.setText(this.titulo+" - CINES BLOCKBUSTER");
        d_OK.setText(R.string.b_aceptar);
        d_CANCEL.setText(R.string.b_cancelar);

        // ** Cuando aparezca el dialog de COMPRAR ENTRADAS, el usuario
        // ** tiene 2 opciones:
        //      * 1) ACEPTAR -> Se ejecutan las acciones asociadas a la compra
        d_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LO QUE HARÁ AL PULSAR "SÍ"
                Intent intent = new Intent(getActivity(), ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

                // En la siguiente línea se obtiene un AlarmManager, que gestionará
                // los intents cada periodo de tiempo como se le indique
                AlarmManager AM = (AlarmManager) view.getContext().getSystemService(ALARM_SERVICE);
                long ahora = System.currentTimeMillis();
                long despues = 1000 * 10; // 10 segundos

                // El AlarmManager 'despertará' el dispositivo para lanzar el pending intent en el tiempo indicado (10s)
                AM.set(AlarmManager.RTC_WAKEUP,ahora+despues, pendingIntent);

                Toast.makeText(getActivity(), getResources().getString(R.string.reserva), Toast.LENGTH_SHORT).show();

                // Cerrar diálogo
                dismiss();
            }
        });
        //      * 2) CANCELAR -> Se cancela la acción y se cierra el dialog con un 'dismiss'
        d_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // LO QUE HARÁ AL PULSAR "NO"
                // Cerrar diálogo
                dismiss();
            }
        });

        // Settear el aspecto en el builder
        builder.setView(aspecto);

        // Crear el objeto AlertDialog desde el builder y devolverlo
        return builder.create();
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_SAVE_INSTANCE_STATE: Este método se ejecuta antes del destroy()
    //    por lo que es aquí dónde se deben guardar los datos que se quieran
    //    mantener. Por ejemplo,para evitar la pérdida de datos al girar el móvil,
    //    interrupciones, etc. En este caso, se mantendrán el título y la cantidad de entradas,
    //    ya que los Spinner guardan la persistencia para la selección
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("var_titulo", titulo);
        outState.putInt("var_cantidad", cuantas);
    }

    //---------------------------------------------------------------------------------
    // Métodos de la interfaz, que no se utilizan en este caso:
    // +) onItemSelected
    // +) onNothingSelected
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {}
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }
}
