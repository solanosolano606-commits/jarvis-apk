# Compilar el APK de JARVIS en la nube (sin instalar Android Studio)

Esto resuelve el problema de que no puedas descargar/instalar Android
Studio: en vez de compilar en tu computador, lo hacemos en los servidores
de GitHub, gratis, y al final descargas el `.apk` ya listo. No necesitas
Git, ni línea de comandos avanzada — todo por la página web.

## Paso 1 — Crear una cuenta de GitHub (si no tienes)

1. Ve a **https://github.com**
2. Clic en **"Sign up"**, arriba a la derecha.
3. Sigue los pasos (correo, contraseña, nombre de usuario). Es gratis.

## Paso 2 — Crear un repositorio nuevo

1. Ya con tu cuenta iniciada, clic en el **"+"** arriba a la derecha → **"New repository"**.
2. En "Repository name" escribe, por ejemplo: `jarvis-apk`
3. Déjalo en **"Public"** (no importa, no tiene datos tuyos, solo el código).
4. **No marques** ninguna casilla de "Add a README" (lo vamos a subir ya armado).
5. Clic en **"Create repository"**.

## Paso 3 — Subir los archivos (arrastrar y soltar, sin Git)

1. En la página del repositorio recién creado, busca el enlace que dice
   **"uploading an existing file"** (aparece en el texto de la pantalla
   inicial del repo vacío).
2. Se abre una pantalla de subida. Ahora, desde el explorador de archivos
   de tu computador:
   - Abre la carpeta `capacitor/` de este paquete que te compartí.
   - **Selecciona TODO el contenido de adentro** de `capacitor/` (no la
     carpeta `capacitor` en sí, sino lo que está dentro: `www`,
     `android-plugin-ejemplo`, `.github`, `scripts`, `package.json`,
     `capacitor.config.json`, `AndroidManifest-completo.xml`, etc.)
   - Arrástralo todo hacia la zona de "Drag files here" de GitHub.

   ⚠️ Importante: la carpeta `.github` a veces los sistemas la ocultan por
   el punto al inicio del nombre. Si tu explorador de archivos no te deja
   verla o arrastrarla, activa "Mostrar archivos ocultos" (en Windows:
   pestaña "Vista" del explorador → marcar "Elementos ocultos").

3. Abajo, en "Commit changes", deja el mensaje que trae por defecto y haz
   clic en **"Commit changes"** (el botón verde).

## Paso 4 — Lanzar la compilación

1. Ve a la pestaña **"Actions"** arriba del repositorio.
2. Deberías ver un flujo llamado **"Compilar JARVIS APK"** en la lista de
   la izquierda. Haz clic en él.
3. Clic en el botón **"Run workflow"** (a la derecha, con una flechita) →
   confirma con el botón verde **"Run workflow"** que aparece.
4. Aparece una ejecución en la lista, con un círculo amarillo girando —
   eso significa que está compilando. **Tarda entre 5 y 10 minutos.**
5. Puedes hacer clic sobre esa ejecución para ver el progreso en vivo, paso
   por paso (instalando Node, Java, el SDK de Android, compilando...).

## Paso 5 — Descargar el .apk ya compilado

1. Cuando el círculo se pone **verde con un check ✔**, la compilación
   terminó bien.
2. Baja hasta la sección **"Artifacts"** (al final de esa misma página de
   la ejecución).
3. Haz clic en **"jarvis-apk"** — se descarga un archivo `.zip` a tu
   computador.
4. Descomprímelo: adentro está `app-debug.apk`.
5. Pásalo a tu celular (por USB, por un enlace de Google Drive/WhatsApp
   contigo mismo, como prefieras) y ábrelo para instalarlo — recuerda
   activar "Instalar apps de orígenes desconocidos" la primera vez.

## Si la compilación falla (círculo rojo ✗)

1. Haz clic en la ejecución fallida.
2. Va a mostrarte en qué paso exacto falló (con una ❌ roja al lado del
   nombre del paso).
3. Copia aquí el texto del error que aparece ahí — con eso lo resolvemos
   directo, sin adivinar.

## Para actualizaciones futuras

Cada vez que quieras una nueva versión (por ejemplo, después de que
sigamos mejorando `index.html`):
1. En GitHub, entra a la carpeta `capacitor/www/`, abre `index.html`, clic
   en el ícono de lápiz (editar), pega el código nuevo, y confirma el
   cambio ("Commit changes").
2. Eso solo, automáticamente, vuelve a lanzar la compilación (el workflow
   está configurado para activarse solo con cada cambio en `capacitor/`).
3. Repite el Paso 5 para descargar la nueva versión.

---

## Alternativa si prefieres seguir intentando instalar Android Studio

A veces el problema es puntual y se arregla así:
- Prueba con **otro navegador** (si usabas Chrome, intenta con Edge o
  Firefox, o viceversa).
- Revisa que tengas **al menos 10 GB libres** en el disco donde se
  descarga (usualmente "Descargas" en el disco C:).
- Si tienes un antivirus de terceros (no el de Windows), pruébalo
  pausándolo un momento durante la descarga — algunos bloquean archivos
  `.exe` grandes sin avisar.
- Si estás en una red de universidad, trabajo, o con VPN activa, intenta
  desde otra red (datos móviles del celular como punto de acceso, por
  ejemplo) — algunas redes bloquean descargas de instaladores.

Pero con el camino de GitHub Actions de arriba, ni siquiera necesitas
resolver eso — es la vía más segura para avanzar ya mismo.
