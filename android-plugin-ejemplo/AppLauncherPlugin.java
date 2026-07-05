// AppLauncherPlugin.java
// ---------------------------------------------------------------------
// Igual que la versión anterior, pero en Java (para no depender de que
// el proyecto Android tenga configurado el compilador de Kotlin).
// ---------------------------------------------------------------------

package com.tuempresa.jarvis;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "AppLauncher")
public class AppLauncherPlugin extends Plugin {

    @PluginMethod
    public void listarApps(PluginCall call) {
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        JSArray resultado = new JSArray();

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                JSObject obj = new JSObject();
                obj.put("nombre", pm.getApplicationLabel(app).toString());
                obj.put("paquete", app.packageName);
                resultado.put(obj);
            }
        }

        JSObject ret = new JSObject();
        ret.put("apps", resultado);
        call.resolve(ret);
    }

    @PluginMethod
    public void abrir(PluginCall call) {
        String paquete = call.getString("paquete");
        if (paquete == null) {
            call.reject("Falta el parámetro 'paquete'");
            return;
        }
        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(paquete);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } else {
            call.reject("No se pudo abrir esa app");
        }
    }
}
