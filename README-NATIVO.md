# JARVIS nativo — Capacitor, permisos completos y generación del APK

## Antes de empezar: qué es realista y qué no

- ✅ **Realista y ya incluido aquí:** cámara, galería, linterna, contactos,
  llamadas, SMS, alarmas, calendario, almacenamiento, ubicación, abrir
  cualquier app instalada, notificaciones — todo con permisos reales de
  Android, para uso personal (sideload, no Play Store).
- ⚠️ **Con matices:** "escucha en segundo plano" — sí puedes mantener un
  servicio vivo con notificación fija (ver `JarvisListenService.kt`), pero
  no es la detección de palabra clave de bajísimo consumo que usan Siri o
  Google Assistant (esa usa un chip dedicado al que apps normales no
  acceden). Y leer SMS/llamadas de forma amplia en Android moderno exige
  que tu app sea la app de SMS/teléfono por defecto — viable para tu propio
  uso, no para publicar en Play Store.
- ❌ **No incluido porque no es alcanzable por software de terceros:**
  integración como asistente del sistema al nivel de Gemini/Siri
  (`VoiceInteractionService` con rol de asistente por defecto). Se puede
  explorar como fase futura si quieres, pero es un proyecto grande aparte.

---

## 1. Instalar requisitos (una sola vez en tu PC)

- **Node.js** 18+ → https://nodejs.org
- **Android Studio** (trae el SDK) → https://developer.android.com/studio
- **JDK 17** (Android Studio ya lo incluye normalmente)

## 2. Preparar el proyecto

Descomprime este paquete. Abre una terminal dentro de `capacitor/`:

```bash
npm install
npx cap init "Jarvis" "com.tuempresa.jarvis" --web-dir=www
```

(Si ya existe `capacitor.config.json`, el comando `init` puede pedirte
confirmar sobrescribir — dile que no y usa el que ya viene en este paquete.)

## 3. Agregar la plataforma Android

```bash
npx cap add android
```

Esto crea la carpeta `android/` con el proyecto nativo real.

## 4. Configurar los permisos

Abre `android/app/src/main/AndroidManifest.xml` y reemplázalo por el
contenido de **`AndroidManifest-completo.xml`** (incluido en este paquete),
o copia manualmente el bloque de `<uses-permission>` si ya modificaste algo
más en ese archivo.

## 4.1 Resumen: qué función de `index.html` usa qué plugin

| Función en el código | Plugin nativo (en el APK) | Respaldo (navegador/PWA) |
|---|---|---|
| `toggleMic()` | `@capacitor-community/speech-recognition` | Web Speech API |
| `abrirCamara()` | `@capacitor/camera` | `getUserMedia` (vista previa web) |
| `elegirContacto()` | `@capacitor-community/contacts` | Contact Picker API (solo Chrome) |
| `openApp()` | `AppLauncherPlugin.kt` (propio) | Esquemas `tel:`/`mailto:`/etc. |
| `enviarMensaje()` | `JarvisUtilPlugin.kt` (propio, SMS real) | Abre app de mensajes (`sms:`) |
| `agendarEvento()` | `JarvisUtilPlugin.kt` (propio, calendario) | Se guarda como alarma local |
| `addAlarm()` / `deleteAlarm()` | `@capacitor/local-notifications` | Sondeo local (`checkAlarms`, cada 15s) |
| `pickFolder()` | `@capacitor/filesystem` (carpeta Documentos) | File System Access API |
| `toggleLinterna()` | *(sin plugin — ya funciona igual en el WebView del APK)* | `MediaStreamTrack` con `torch` |
| `reconocerObjeto()` | *(sin plugin — TensorFlow.js corre igual en el APK)* | TensorFlow.js + MobileNet |

Todas estas funciones detectan solas si están en el APK o en el navegador
(con `esNativo()`/`pluginNativo()` al inicio del script) — no hay que
elegir manualmente ningún modo.

## 4.2 Permisos en tiempo de ejecución

Declarar el permiso en el manifest (paso 4) no es suficiente para los
permisos "peligrosos" (cámara, micrófono, contactos, SMS, ubicación,
almacenamiento): Android además pide que el usuario los acepte en pantalla
la primera vez. Cada plugin oficial ya incluye ese diálogo automáticamente
(por ejemplo, `Camera.getPhoto()` lo pide solo). No necesitas código
adicional para eso — solo asegúrate de aceptar los diálogos la primera vez
que uses cada función durante las pruebas.

## 5. Registrar los plugins nativos propios

El `www/index.html` de este paquete **ya está conectado** a todos los
plugins (cámara, contactos, voz, notificaciones, archivos, abrir apps,
SMS, calendario) — detecta solo si corre dentro del APK o en un navegador,
y usa el plugin real o el respaldo web según corresponda. Lo único que
falta es agregar el código nativo Kotlin al proyecto Android y registrarlo:

1. Copia estos dos archivos a
   `android/app/src/main/java/com/tuempresa/jarvis/`:
   - `android-plugin-ejemplo/AppLauncherPlugin.kt` (abrir cualquier app)
   - `android-plugin-ejemplo/JarvisUtilPlugin.kt` (SMS real y calendario)
2. Abre `android/app/src/main/java/com/tuempresa/jarvis/MainActivity.java`
   y agrega, dentro de `onCreate()`, **antes** de `super.onCreate(...)`:
   ```java
   registerPlugin(AppLauncherPlugin.class);
   registerPlugin(JarvisUtilPlugin.class);
   ```
   (Si `MainActivity` está en Kotlin en tu proyecto, la sintaxis es la
   misma línea `registerPlugin(...)` dentro de `onCreate()`.)

## 6. (Opcional, avanzado) Servicio de escucha en segundo plano

Copia `android-plugin-ejemplo/JarvisListenService.kt` a la misma carpeta de
paquete, y descomenta el bloque `<service>` en el manifest. Este archivo
trae comentado exactamente qué falta conectar (tu motor de voz). Si quieres,
en otra sesión seguimos y te armo también el plugin puente para
iniciarlo/detenerlo desde `index.html`.

## 7. Reconocimiento de voz nativo

```bash
npm install @capacitor-community/speech-recognition
npx cap sync android
```
`www/index.html` ya detecta este plugin automáticamente en `toggleMic()`
(usa `es-CO` como idioma) — con solo instalarlo y sincronizar, empieza a
usarse dentro del APK sin tocar más código.

## 8. Sincronizar y compilar

```bash
npx cap sync android
npx cap open android
```

En Android Studio: espera a que sincronice Gradle (barra de progreso
abajo), y luego:

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

El archivo queda en:
`android/app/build/outputs/apk/debug/app-debug.apk`

## 9. Actualizar sin perder memoria ni configuraciones

Todo lo que JARVIS guarda (`localStorage`: notas, alarmas, Forja,
conversación) vive dentro del almacenamiento privado de la app en el
dispositivo — **no se borra al actualizar**, solo se borra si:
- Desinstalas la app manualmente, o
- Cambias el `appId` (`com.tuempresa.jarvis`) o la firma (keystore) entre
  builds — eso Android lo trata como una app distinta.

Para actualizaciones normales: mantén el mismo `appId` y el mismo keystore,
sube el `versionCode` en `android/app/build.gradle`, y simplemente instala
el nuevo `.apk` encima del anterior (o `adb install -r`).

### Firmar con una clave fija (para poder actualizar siempre igual)

```bash
keytool -genkey -v -keystore jarvis-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias jarvis
```

Guarda ese archivo `.jks` en un lugar seguro — lo necesitas en cada build
futuro. En Android Studio: **Build → Generate Signed Bundle/APK**, elige
APK, selecciona ese keystore, y listo.

## 10. `build.gradle` — qué revisar

Capacitor genera `android/app/build.gradle` automáticamente al correr
`npx cap add android`. Los puntos que sí conviene revisar/ajustar ahí:

```gradle
android {
    defaultConfig {
        applicationId "com.tuempresa.jarvis"
        minSdkVersion 23
        targetSdkVersion 34   // Sube esto si Android Studio te lo sugiere
        versionCode 1         // Súbelo en cada actualización (2, 3, 4...)
        versionName "1.0"     // Igual, para que se vea en Información de la app
    }
}
```

No necesitas escribir este archivo desde cero: solo edita esos valores
después de que Capacitor lo genere.

---

Si algo falla en un paso concreto (error de Gradle, plugin que no
sincroniza, permiso que Android sigue sin mostrar), copia aquí el mensaje
exacto y lo resolvemos.

---

## Arquitectura de subagentes (nueva)

El motor de comandos ya no es una sola lista plana: está organizado en
subagentes especializados (`SUBAGENTES` dentro de `index.html`):

🧠 Núcleo · ⚙️ Sistema y Dispositivo · 📞 Comunicaciones · 🗒️ Productividad ·
🎵 Multimedia · 🌐 Información y Búsqueda · 💪 Forja · 🛡️ Seguridad y
Privacidad · 🔄 Mantenimiento · 🏠 Hogar (reservado, vacío por ahora) ·
🔍 Visión y Reconocimiento

Cada vez que Jarvis atiende una orden, muestra debajo del orbe cuál
subagente la resolvió (ej. "📞 Agente: Comunicaciones"). Para agregar un
comando, entra al subagente correspondiente en el código y agrega un
objeto `{ nombre, ejemplos, offline, test, run }` a su lista `comandos`.

### Subagente de Visión (reconocimiento de objetos)

Usa TensorFlow.js + el modelo MobileNet, que corre **dentro del
dispositivo** (tu foto no se envía a ningún servidor mío). Solo necesita
internet la primera vez, para descargar el modelo (unos pocos MB); en usos
posteriores dentro de la misma sesión ya queda cargado en memoria.

Actívalo diciendo "qué es esto" o "reconoce este objeto" con la cámara
abierta o una foto ya tomada. Después puedes decir "busca información
sobre esto" y usará automáticamente lo último que reconoció.
