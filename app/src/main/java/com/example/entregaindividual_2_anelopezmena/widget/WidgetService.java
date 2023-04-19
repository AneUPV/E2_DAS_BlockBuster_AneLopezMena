package com.example.entregaindividual_2_anelopezmena.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/*************************************************************************/
/** -------------------------- WIDGET SERVICE ------------------------- **/
/*************************************************************************/
// Código extraído de Youtube:
//      * Video: Custom Widget Update Interval in Android Studio
//      * Autor: Tihomir RAdeff
//      * FUENTE: https://www.youtube.com/watch?v=Vz3Fm6YZxBY

// Se trata de la clase java encargada de la actualización del Widget.
// Gestiona la actualización del widget del reloj de la app BlockBuster!. Esta
// extiende de la clase BroadcastReceiver

public class WidgetService extends BroadcastReceiver {

    //---------------------------------------------------------------------------------
    // 1) Método ON_RECEIVE: Actualiza el Widget de la aplicación.
    @Override
    public void onReceive(Context context, Intent intent) {
        // Despertar al dispositivo
        WakeLocker.acquire(context);

        // Forzar la actualización del Widget
        Intent i = new Intent(context, WidgetBlockbuster.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WidgetBlockbuster.class));
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        context.sendBroadcast(i);

        // Volver a estado IDLE
        WakeLocker.release();
    }

}
