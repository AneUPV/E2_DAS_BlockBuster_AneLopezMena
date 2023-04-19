package com.example.entregaindividual_2_anelopezmena.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.entregaindividual_2_anelopezmena.R;
import com.example.entregaindividual_2_anelopezmena.activities.Act_Login;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/*************************************************************************/
/** ------------------------ WIDGET BLOCKBUSTER ----------------------- **/
/*************************************************************************/
// Código extraído de Youtube:
//      * Video: Custom Widget Update Interval in Android Studio
//      * Autor: Tihomir RAdeff
//      * FUENTE: https://www.youtube.com/watch?v=Vz3Fm6YZxBY

// Se trata de la clase que representa el objeto Widget de la aplicación.
// En este caso se ha implementado el Widget de reloj, que mostrará la hora
// con una estética de cine, a juego con la aplicación. Esta clase extenderá de
// la clase 'AppWidgetProvider'

public class WidgetBlockbuster extends AppWidgetProvider {
    //---------------------------------------------------------------------------------
    // 1) Método on_UPDATE: En este método se actualiza cada instancia del Widget de la
    // aplicación. Internamente, se le llama al método updateAppWidget, que recibirá la
    // hora desde un objeto Calendar y la actualizará en el layout
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Recorrer todos los Widgets de la aplicación
        for(int appWidgetId: appWidgetIds){
            // Llamar al método que actualiza el Widget
            updateAppWidget(context,appWidgetManager,appWidgetId);
        }
    }

    //---------------------------------------------------------------------------------
    // 2) Método UPDATE_APP_WIDGET: Método al que llama onUpdate. En este se consigue la
    // hora, se actualiza la vista, y se gestiona el refresco del Widget mediante las alarmas
    void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId){

        // Crear un intent deje al usuario en la pantalla de Login al pulsar el Widget
        Intent i = new Intent(context, Act_Login.class);
        // Crear un pendingIntent que se quede a la espera de que se toque el Widget
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,i,PendingIntent.FLAG_IMMUTABLE);

        // Conseguir las views del Widget
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_reloj_blockbuster);

        // Obtener hora actual desde el calendario
        Calendar calendario = Calendar.getInstance();
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm");
        String horaConFormato = formato.format(calendario.getTime());

        // Preparar el pending intent
        views.setOnClickPendingIntent(R.id.iv_fondoWidget,pendingIntent);

        // Actualizar la hora en el TextView del layout
        views.setTextViewText(R.id.tv_reloj, horaConFormato);

        // Actualizar el Widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Reiniciar el refresco del Widget con las alarmas
        AlarmHandler alarmHandler = new AlarmHandler(context);
        alarmHandler.cancelAlarmManager();
        alarmHandler.setAlarmManager();
    }

    //---------------------------------------------------------------------------------
    // 3) Método ON_DISABLED: Método que se ejecutará cuando se elimine el último de
    // todos los widgets de BlockBuster sobre la pantalla
    @Override
    public void onDisabled(Context context) {
        // Detener la actualización del Widget
        AlarmHandler alarmHandler = new AlarmHandler(context);
        alarmHandler.cancelAlarmManager();

    }
}
