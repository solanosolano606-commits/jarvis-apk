// JarvisListenService.kt
// ---------------------------------------------------------------------
// Punto de partida REAL (no un simulacro) para que JARVIS pueda seguir
// "vivo" en segundo plano con una notificación fija, y relanzar la
// escucha periódicamente. Léelo con estas expectativas claras:
//
//  - Esto NO es detección de palabra clave tipo "Hey Siri"/"Ok Google" con
//    la pantalla apagada y consumo casi nulo de batería — eso usa un chip
//    dedicado (DSP) al que apps normales no tienen acceso.
//  - Lo que SÍ logra: mantener un servicio en primer plano (con su
//    notificación visible, como Spotify o una llamada) que puede reactivar
//    el reconocimiento de voz nativo cada cierto tiempo, incluso con la
//    app minimizada. Con la pantalla completamente apagada, Android igual
//    puede limitar el micrófono según el fabricante — pruébalo en tu
//    equipo específico.
//  - Consume batería mientras está activo. Está pensado para activarlo
//    cuando quieras "modo escucha" un rato, no para dejarlo 24/7 por
//    defecto.
//
// DÓNDE VA:
//   android/app/src/main/java/com/tuempresa/jarvis/JarvisListenService.kt
//
// QUÉ FALTA PARA QUE QUEDE 100% FUNCIONAL:
//   Conectar aquí tu motor de voz nativo elegido (por ejemplo, el mismo que
//   use el plugin @capacitor-community/speech-recognition, o Vosk si migras
//   a reconocimiento offline). Este esqueleto deja el "andamiaje" del
//   servicio y la notificación persistente ya resueltos.
// ---------------------------------------------------------------------

package com.tuempresa.jarvis

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class JarvisListenService : Service() {

    private val CANAL_ID = "jarvis_escucha"
    private val NOTIF_ID = 1001

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()
        startForeground(NOTIF_ID, construirNotificacion())
        // TODO: aquí inicias tu motor de reconocimiento de voz nativo
        // y lo relanzas cada vez que termine un ciclo (onResult/onEnd).
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: Android intenta volver a crear el servicio si el
        // sistema lo mata por falta de memoria.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        // TODO: detener el motor de voz nativo aquí.
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID, "JARVIS escuchando",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun construirNotificacion(): Notification {
        return NotificationCompat.Builder(this, CANAL_ID)
            .setContentTitle("JARVIS")
            .setContentText("Escuchando en segundo plano")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }
}

/* ---------------------------------------------------------------------
   CÓMO ACTIVARLO/DESACTIVARLO DESDE index.html (con un pequeño plugin
   puente similar a AppLauncherPlugin.kt, con métodos iniciar()/detener()
   que llamen startForegroundService(...) / stopService(...) sobre este
   servicio). Puedo escribirte ese plugin puente cuando decidas avanzar
   con esta pieza específica.
   --------------------------------------------------------------------- */
