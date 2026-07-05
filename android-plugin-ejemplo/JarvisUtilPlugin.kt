// JarvisUtilPlugin.kt
// ---------------------------------------------------------------------
// Plugin nativo propio para dos cosas que NO dependen de paquetes de
// terceros (usamos APIs estándar de Android directamente, así no hay
// sorpresas de nombres de paquete o versiones):
//   - Enviar un SMS real sin abrir la app de mensajes (SmsManager).
//   - Crear un evento en el calendario del teléfono (Intent.ACTION_INSERT).
//
// DÓNDE VA:
//   android/app/src/main/java/com/tuempresa/jarvis/JarvisUtilPlugin.kt
//
// CÓMO SE REGISTRA (junto con AppLauncherPlugin, en MainActivity.java):
//   registerPlugin(JarvisUtilPlugin.class);
// ---------------------------------------------------------------------

package com.tuempresa.jarvis

import android.content.Intent
import android.provider.CalendarContract
import android.telephony.SmsManager
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback

@CapacitorPlugin(
    name = "JarvisUtil",
    permissions = [
        Permission(strings = ["android.permission.SEND_SMS"], alias = "sms")
    ]
)
class JarvisUtilPlugin : Plugin() {

    // Envía un SMS real. Requiere permiso SEND_SMS (ya declarado en el manifest).
    @PluginMethod
    fun enviarSms(call: PluginCall) {
        val numero = call.getString("numero")
        val mensaje = call.getString("mensaje")
        if (numero == null || mensaje == null) {
            call.reject("Faltan 'numero' o 'mensaje'")
            return
        }
        if (getPermissionState("sms") != com.getcapacitor.PermissionState.GRANTED) {
            requestPermissionForAlias("sms", call, "onSmsPermissionResult")
            return
        }
        enviarSmsInterno(numero, mensaje, call)
    }

    @PermissionCallback
    private fun onSmsPermissionResult(call: PluginCall) {
        val numero = call.getString("numero")
        val mensaje = call.getString("mensaje")
        if (getPermissionState("sms") == com.getcapacitor.PermissionState.GRANTED && numero != null && mensaje != null) {
            enviarSmsInterno(numero, mensaje, call)
        } else {
            call.reject("Permiso de SMS no otorgado")
        }
    }

    private fun enviarSmsInterno(numero: String, mensaje: String, call: PluginCall) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(numero, null, mensaje, null, null)
            call.resolve()
        } catch (e: Exception) {
            call.reject("No se pudo enviar el SMS: " + e.message)
        }
    }

    // Abre la pantalla de "nuevo evento" del calendario ya con los datos
    // rellenados (el usuario solo confirma) — no requiere permisos extra
    // de calendario porque usa un Intent, no escritura directa.
    @PluginMethod
    fun crearEventoCalendario(call: PluginCall) {
        val titulo = call.getString("titulo") ?: "Recordatorio de Jarvis"
        val inicioMillis = call.getLong("inicioMillis", System.currentTimeMillis())

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, inicioMillis + 30 * 60 * 1000)
            .putExtra(CalendarContract.Events.TITLE, titulo)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
            call.resolve()
        } catch (e: Exception) {
            call.reject("No se pudo abrir el calendario: " + e.message)
        }
    }
}

/* ---------------------------------------------------------------------
   USO DESDE index.html (JavaScript):

   const { JarvisUtil } = Capacitor.Plugins;

   // Enviar SMS real
   await JarvisUtil.enviarSms({ numero: '3001234567', mensaje: 'Hola, te escribe Jarvis.' });

   // Crear evento de calendario
   await JarvisUtil.crearEventoCalendario({
     titulo: 'Cita con el doctor',
     inicioMillis: new Date('2026-07-10T15:00:00').getTime()
   });
   --------------------------------------------------------------------- */
