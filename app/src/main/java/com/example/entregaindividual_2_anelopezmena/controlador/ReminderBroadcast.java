package com.example.entregaindividual_2_anelopezmena.controlador;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.entregaindividual_2_anelopezmena.R;

public class ReminderBroadcast extends BroadcastReceiver {
/************************************************************************/
/** -------------------- CLASE REMINDER_BROADCAST -------------------- **/
/************************************************************************/
// Se trata de la clase encargada de gestionar las notificaciones para la
// compra de entradas. Aquí se creará el canal y se notificará a
// los dispositivos CUANDO RECIBA EL INTENT LANZADO POR EL ALARM MANAGER.
// Extiende de la clase 'BroadcastReceiver'.

    // -----------------------------------------------------------------------------------
    // 1) Método ON_RECEIVE: Aquí se implementa el método en BroadcastReceiver.
    //    Aquí se creará el canal de notificaciones "CompraEntradas", y se se tienen los
    //    permisos necesarios de notificación, se enviará el aviso a los usuarios
    @Override
    public void onReceive(Context context, Intent intent) {
        // Para que se lance una notificación, primero debe crearse el canal. En este caso, se llamará
        // "CompraEntradas"
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CompraEntradas")
                .setSmallIcon(R.drawable.tickets)
                .setContentTitle("Compra de entradas BLOCKBUSTER")
                .setContentText("¡La compra de entradas se ha tramitado con éxito!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Crear NotificationManagerCompat
        NotificationManagerCompat NMC = NotificationManagerCompat.from(context);

        // Si no se cuenta con los permisos
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // no se hará nada
            return;
        }
        // En cambio, si se han concedido los permisos, enviar notificación
        NMC.notify(100, builder.build());
    }
}
