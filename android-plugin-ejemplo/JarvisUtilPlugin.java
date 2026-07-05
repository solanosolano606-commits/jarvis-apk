// JarvisUtilPlugin.java
// ---------------------------------------------------------------------
// Igual que la versión anterior, pero en Java (para no depender de que
// el proyecto Android tenga configurado el compilador de Kotlin).
// ---------------------------------------------------------------------

package com.tuempresa.jarvis;

import android.content.Intent;
import android.provider.CalendarContract;
import android.telephony.SmsManager;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(
    name = "JarvisUtil",
    permissions = {
        @Permission(strings = { "android.permission.SEND_SMS" }, alias = "sms")
    }
)
public class JarvisUtilPlugin extends Plugin {

    @PluginMethod
    public void enviarSms(PluginCall call) {
        String numero = call.getString("numero");
        String mensaje = call.getString("mensaje");
        if (numero == null || mensaje == null) {
            call.reject("Faltan 'numero' o 'mensaje'");
            return;
        }
        if (getPermissionState("sms") != PermissionState.GRANTED) {
            requestPermissionForAlias("sms", call, "onSmsPermissionResult");
            return;
        }
        enviarSmsInterno(numero, mensaje, call);
    }

    @PermissionCallback
    private void onSmsPermissionResult(PluginCall call) {
        String numero = call.getString("numero");
        String mensaje = call.getString("mensaje");
        if (getPermissionState("sms") == PermissionState.GRANTED && numero != null && mensaje != null) {
            enviarSmsInterno(numero, mensaje, call);
        } else {
            call.reject("Permiso de SMS no otorgado");
        }
    }

    private void enviarSmsInterno(String numero, String mensaje, PluginCall call) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numero, null, mensaje, null, null);
            call.resolve();
        } catch (Exception e) {
            call.reject("No se pudo enviar el SMS: " + e.getMessage());
        }
    }

    @PluginMethod
    public void crearEventoCalendario(PluginCall call) {
        String titulo = call.getString("titulo", "Recordatorio de Jarvis");
        Long inicioMillis = call.getLong("inicioMillis", System.currentTimeMillis());

        Intent intent = new Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, inicioMillis + 30 * 60 * 1000)
            .putExtra(CalendarContract.Events.TITLE, titulo)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) {
            call.reject("No se pudo abrir el calendario: " + e.getMessage());
        }
    }
}
