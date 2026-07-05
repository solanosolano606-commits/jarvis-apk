#!/bin/bash
# aplicar-nativo.sh
# ---------------------------------------------------------------------
# Se ejecuta DESPUÉS de "npx cap add android". Copia los plugins nativos
# propios, reemplaza el AndroidManifest.xml por el que ya trae todos los
# permisos, y reescribe MainActivity.java para registrar los plugins.
# Lo usan tanto el flujo local (tu PC) como el flujo en la nube (GitHub
# Actions) — es el mismo paso, automatizado.
# ---------------------------------------------------------------------
set -e

APP_ID_PATH="com/tuempresa/jarvis"
PKG_DIR="android/app/src/main/java/$APP_ID_PATH"

echo "→ Creando carpeta de paquete: $PKG_DIR"
mkdir -p "$PKG_DIR"

echo "→ Copiando plugins nativos (AppLauncher, JarvisUtil)"
cp android-plugin-ejemplo/AppLauncherPlugin.kt "$PKG_DIR/"
cp android-plugin-ejemplo/JarvisUtilPlugin.kt "$PKG_DIR/"

echo "→ Reemplazando AndroidManifest.xml con la versión completa de permisos"
cp AndroidManifest-completo.xml android/app/src/main/AndroidManifest.xml

echo "→ Reescribiendo MainActivity.java para registrar los plugins"
cat > "$PKG_DIR/MainActivity.java" << 'JAVA'
package com.tuempresa.jarvis;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    registerPlugin(AppLauncherPlugin.class);
    registerPlugin(JarvisUtilPlugin.class);
    super.onCreate(savedInstanceState);
  }
}
JAVA

echo "✔ Listo: plugins y permisos aplicados al proyecto Android."
