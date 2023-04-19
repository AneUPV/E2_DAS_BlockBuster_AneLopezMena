package com.example.entregaindividual_2_anelopezmena.java;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.entregaindividual_2_anelopezmena.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/********************************************************************/
/** --------------- MY FIREBASE MESSAGING SERVICE ---------------- **/
/********************************************************************/
// Esta clase gestionará el servicio de mensajería FCM. Será la clase
// encargada de crear el canal y de enviar las notificaciones cada vez
// que un usuario añada una nueva película, de manera que se notificará
// a todos los usuarios registrados en la app que tengan la app instalada
// Esta es una clase que hereda de FirebaseMessagingService, es un servicio


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Atributos privados
    private static final String TAG = "MyFirebaseMsgService";
    private static String CHANNEL_ID = "FCM";

    //---------------------------------------------------------------------------------
    // 1) Método ON_MESSAGE_RECEIVED: Este método se ejecutará cuando detecte que
    // hay un mensaje listo para enviar por FCM (cuando se añada una nueva película).
    // Este llamará al método que crea propiamente el canal y notifica a los usuarios
    // (método 'showNotification')
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Si hay contenido
        if (remoteMessage.getNotification() != null) {
            // Obtener título y mensaje
            String titulo = remoteMessage.getNotification().getTitle();
            String texto = remoteMessage.getNotification().getBody();

            // La notificación se mostrará también en la barra de estado
            showNotification(titulo, texto);
        }
    }

    //---------------------------------------------------------------------------------
    // 2) Método SHOW_NOTIFICATION: Este será el encargado crear el canal de notificaciones
    // "FCM" y de enviar las notificaciones a todos los usuarios de la app 'BlockBuster!'
    private void showNotification(String title, String text) {

        // Crear un Notification Manager
        NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // -----------****#### CREAR UN CANAL PARA LAS NOTIFICACIONES ####****-------------- //
        // Si la versión de ANDROID es mayor que OREO, se creará un canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Indicar nombre del canal --> "FCM"
            CharSequence nombre = "FCM";
            // Pasar el ID, nombre y la prioridad en el momento de crearlo
            NotificationChannel NC = new NotificationChannel(CHANNEL_ID, nombre, NotificationManager.IMPORTANCE_DEFAULT);
            // Crear canal
            NM.createNotificationChannel(NC);
            // Habilitar las luces
            NC.enableLights(true);
            // Dar una descripción al canal
            NC.setDescription("Notificación global de FCM");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        // Settear icono a la notificación
        builder.setSmallIcon(R.drawable.claqueta);
        // Settear título de la notificación
        builder.setContentTitle(getResources().getString(R.string.tituloN));
        // Settear texto de la notificación
        builder.setContentText((getResources().getString(R.string.mensajeN)));
        // Poner luces de notificación
        builder.setLights(Color.BLUE, 1000, 1000);

        // Crear un 'NotificationManagerCompat'
        NotificationManagerCompat NMC = NotificationManagerCompat.from(getApplicationContext());
        // Comprobar si se dispone de permisos de notificación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Enviar notificaciones a todos los dispositivos
        NMC.notify(300, builder.build());

    }
}

