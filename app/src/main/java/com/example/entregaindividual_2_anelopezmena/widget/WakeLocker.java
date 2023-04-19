package com.example.entregaindividual_2_anelopezmena.widget;

import android.content.Context;
import android.os.PowerManager;

/*************************************************************************/
/** --------------------------- WAKE LOCKER --------------------------- **/
/*************************************************************************/
// Código extraído de Youtube:
//      * Video: Custom Widget Update Interval in Android Studio
//      * Autor: Tihomir RAdeff
//      * FUENTE: https://www.youtube.com/watch?v=Vz3Fm6YZxBY

// Será la clase encargada del 'bloqueo' del Widget. Extiende de la clase WakeLocker

public abstract class WakeLocker {

    // Atributos privados
    private static PowerManager.WakeLock wakeLock;

    //---------------------------------------------------------------------------------
    // 1) Método ACQUIRE: Mediante este método se retira el bloqueo, es decir,
    // "despierta" el dispositivo
    public static void acquire(Context context){
        // Comprobar que el WakeLock existe
        if(wakeLock != null){
            // Si existe, llama al método release
            wakeLock.release();
        }

        // Crear un powerManager y un wakeLock
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WIDGET: Método acquire");

        // Volver a llamar a este método, con timeout de 2 segundos
        wakeLock.acquire(2000);
    }

    //---------------------------------------------------------------------------------
    // 2) Método RELEASE: Este método libera la variable que contiene el bloqueador
    public static void release(){
        // Comprobar si es nulo
        if(wakeLock != null){ // Si no es nulo --> llamada recursiva
            wakeLock.release();
        }
        // Poner a null el bloqueador
        wakeLock = null;
    }
}
