package com.example.entregaindividual_2_anelopezmena.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/*************************************************************************/
/** -------------------------- ALARM HANDLER -------------------------- **/
/*************************************************************************/
// Código extraído de Youtube:
//      * Video: Custom Widget Update Interval in Android Studio
//      * Autor: Tihomir RAdeff
//      * FUENTE: https://www.youtube.com/watch?v=Vz3Fm6YZxBY

// Esta clase será la encargada de activar y desactivar la alarma que
// llamará a actualizar el Widget.

public class AlarmHandler {
    // Atributos privados de la clase
    private final Context contexto;

    //---------------------------------------------------------------------------------
    // 1) Método constructor
    public AlarmHandler(Context context){
        this.contexto = context;
    }

    //---------------------------------------------------------------------------------
    // 2) Método SET_ALARM_MANAGER: El método setAlarmManager setteará la alarma del
    // Widget para que se lance cada 10 segundos.
    public void setAlarmManager() {

        // Crear un intent que se lance a la clase 'WidgetService'
        Intent i = new Intent(contexto, WidgetService.class);

        // Crear un pending intent para que se quede a la espera
        PendingIntent sender;
        // Si la versióna cctual es igual o superior a Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Crear el remitente de la alarma
            sender = PendingIntent.getBroadcast(contexto, 2, i, PendingIntent.FLAG_IMMUTABLE);
        } else {
            // Crear el remitente de la alarma
            sender = PendingIntent.getBroadcast(contexto, 2, i,0);
        }

        // Obtener el AlarmManager
        AlarmManager am = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);

        // Obtener la hora actual y sumarle 10s
        Calendar c = Calendar.getInstance();
        long l = c.getTimeInMillis() + 10000;

        // Si AlarmManager es distinto de null
        if(am != null){
            // y si la versión de android del dispositivo es igual o superior a Marshmallow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Iniciar la alarma pasados 10 segundos
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,l,sender);
            } else {
                // Iniciar la alarma pasados 10 segundos
                am.set(AlarmManager.RTC_WAKEUP,l,sender);
            }

        }
    }

    //---------------------------------------------------------------------------------
    // 3) Método CANCEL_ALARM_MANAGER: El método setAlarmManagerse encargará de cancelar
    //    la alarma del Widget.
    public void cancelAlarmManager(){

        // Crear un intent que se lance a la clase 'WidgetService'
        Intent i = new Intent(contexto, WidgetService.class);
        // Crear un pending intent que se quede a la espera
        PendingIntent sender;

        // Comprobar que la versión de android del dispositivo es igual o superior a Marshmallow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Crear el remitente de la alarma
            sender = PendingIntent.getBroadcast(contexto, 2, i, PendingIntent.FLAG_IMMUTABLE);
        } else {
            // Crear el remitente de la alarma
            sender = PendingIntent.getBroadcast(contexto, 2, i,0);
        }

        // Crear el AlarmManager para cancelar la alarma
        AlarmManager am = (AlarmManager) contexto.getSystemService(Context.ALARM_SERVICE);

        // Si AlarmManager no es nulo
        if(am != null){
            // Cancelar la alarma
            am.cancel(sender);
        }
    }
}
