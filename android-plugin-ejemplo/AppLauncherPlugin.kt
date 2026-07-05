// AppLauncherPlugin.kt
// ---------------------------------------------------------------------
// Plugin nativo de ejemplo para que JARVIS pueda "abrir cualquier app
// instalada diciendo su nombre". Esto NO es posible desde JavaScript/web
// puro (Android no le entrega esa lista a una página) — por eso necesita
// este código nativo, que se ejecuta ya dentro del proyecto Android real.
//
// DÓNDE VA:
//   android/app/src/main/java/com/tuempresa/jarvis/AppLauncherPlugin.kt
//   (crea la carpeta si no existe, usando el mismo paquete de tu app)
//
// CÓMO SE REGISTRA:
//   En android/app/src/main/java/com/tuempresa/jarvis/MainActivity.java,
//   agrega antes de super.onCreate():
//       registerPlugin(AppLauncherPlugin.class);
// ---------------------------------------------------------------------

package com.tuempresa.jarvis

import android.content.Intent
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject

@CapacitorPlugin(name = "AppLauncher")
class AppLauncherPlugin : Plugin() {

    // Devuelve la lista de apps instaladas con nombre visible y "package name".
    // Úsalo desde JS para hacer coincidir lo que dijo la persona por voz
    // ("abre whatsapp") contra el nombre real de la app instalada.
    @PluginMethod
    fun listarApps(call: PluginCall) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(0)
        val resultado = JSArray()
        for (app in apps) {
            // Solo apps con ícono de lanzador (evita listar servicios internos del sistema)
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                val obj = JSObject()
                obj.put("nombre", pm.getApplicationLabel(app).toString())
                obj.put("paquete", app.packageName)
                resultado.put(obj)
            }
        }
        val ret = JSObject()
        ret.put("apps", resultado)
        call.resolve(ret)
    }

    // Abre una app dado su "package name" exacto (obtenido de listarApps).
    @PluginMethod
    fun abrir(call: PluginCall) {
        val paquete = call.getString("paquete")
        if (paquete == null) {
            call.reject("Falta el parámetro 'paquete'")
            return
        }
        val intent: Intent? = context.packageManager.getLaunchIntentForPackage(paquete)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            call.resolve()
        } else {
            call.reject("No se pudo abrir esa app")
        }
    }
}

/* ---------------------------------------------------------------------
   USO DESDE index.html (JavaScript), una vez registrado el plugin:

   const { AppLauncher } = Capacitor.Plugins;

   async function abrirAppNativa(nombreHablado){
     const { apps } = await AppLauncher.listarApps();
     // Búsqueda simple por coincidencia de nombre (se puede afinar después)
     const encontrada = apps.find(a =>
       a.nombre.toLowerCase().includes(nombreHablado.toLowerCase())
     );
     if(encontrada){
       await AppLauncher.abrir({ paquete: encontrada.paquete });
       speak('Abriendo ' + encontrada.nombre + '.');
     } else {
       speak('No encontré una app instalada llamada ' + nombreHablado + '.');
     }
   }
   --------------------------------------------------------------------- */
