# Alt Manager

Mod para Fabric (Minecraft 1.21.11) que añade un botón **"Alt Manager"** en el menú
principal. Al pulsarlo se abre una pantalla donde
puedes:

- Añadir perfiles (con un nombre elegido por ti).
- Ver la lista de perfiles guardados.
- Seleccionar un perfil (se marca con «»» delante del nombre).
- Eliminar perfiles.

Todo se guarda automáticamente en:
`.minecraft/config/altmanager/profiles.json` y `.minecraft/config/altmanager/state.json`

Estos datos persisten aunque cierres y vuelvas a abrir Minecraft.

> Nota de alcance: este mod únicamente guarda una **lista de nombres/perfiles locales**.
> No inicia sesión con ninguna cuenta, no maneja tokens de sesión ni credenciales de
> Mojang/Microsoft. Es una utilidad de organización, no un sistema de login.

## Requisitos

- JDK 21 (Temurin/Adoptium recomendado)
- Gradle (se usa el wrapper incluido, no necesitas instalarlo aparte)
- Conexión a internet la primera vez que compiles (Gradle/Loom descargan Minecraft,
  las Mojang Mappings y Fabric API)

## Estructura del proyecto

```
AltManager/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── LICENSE
├── src/main/java/com/altmanager/
│   ├── AltManagerClient.java        (entrypoint del cliente)
│   ├── data/
│   │   ├── Profile.java             (modelo de datos de un perfil)
│   │   └── ProfileManager.java      (carga/guarda perfiles en JSON)
│   ├── gui/
│   │   └── AltManagerScreen.java    (interfaz gráfica del mod)
│   └── mixin/
│       └── TitleScreenMixin.java    (inyecta el botón en el menú principal)
└── src/main/resources/
    ├── fabric.mod.json
    └── altmanager.mixins.json
```

## Cómo compilar (opción A: GitHub Actions, recomendado)

Este proyecto incluye `.github/workflows/build.yml`, que compila el mod automáticamente
en la nube cada vez que subís los archivos a un repositorio de GitHub. No necesitás
instalar nada en tu PC. Pasos:

1. Creá un repositorio nuevo y vacío en GitHub.
2. Subí **todo el contenido** de esta carpeta (todo lo que está dentro de `AltManager/`,
   incluyendo la carpeta oculta `.github`) usando "Add file" → "Upload files".
3. Andá a la pestaña "Actions" del repositorio. Va a empezar a correr solo.
4. Esperá a que se ponga verde ✅ (puede tardar varios minutos la primera vez).
5. Entrá al proceso terminado → sección "Artifacts" → descargá `altmanager-jar`.
6. Adentro de ese zip está `altmanager-1.0.0.jar`, listo para la carpeta `mods`.

> Importante: al subir los archivos por la web de GitHub, asegurate de que la carpeta
> `.github/workflows/build.yml` quede incluida. Si tu navegador no sube carpetas ocultas,
> creála a mano en GitHub ("Add file" → "Create new file", nombre: `.github/workflows/build.yml`)
> y pegá el contenido de ese archivo.

## Cómo compilar (opción B: en tu PC)

1. Instala JDK 21 si no lo tienes.
2. Genera el wrapper de Gradle (solo la primera vez, si no existe la carpeta `gradle/wrapper`):
   ```bash
   gradle wrapper --gradle-version 9.0
   ```
   Si ya tienes `gradlew`/`gradlew.bat` en el proyecto, omite este paso.
   (Necesitas tener Gradle instalado una vez para generar el wrapper. Fabric Loom
   1.14.10 —usado en este proyecto— requiere Gradle 9.0 o superior.)
3. Desde la carpeta raíz del proyecto (`AltManager/`), ejecuta:

   **Linux/macOS:**
   ```bash
   ./gradlew build
   ```

   **Windows:**
   ```bat
   gradlew.bat build
   ```

4. El `.jar` final quedará en:
   ```
   build/libs/altmanager-1.0.0.jar
   ```
   (Ese es el jar remapeado, listo para usar. Ignora el que termina en `-sources.jar`).

5. Copia `altmanager-1.0.0.jar` a la carpeta `mods` de tu instalación de Minecraft
   con **Fabric Loader** instalado (versión 0.16.10 o superior) y **Fabric API**
   0.141.4+1.21.11 también instalado en `mods`.

## Notas

- La primera compilación puede tardar varios minutos porque Loom descarga y
  remapea Minecraft con Mojang Mappings.
- Si tu IDE (IntelliJ IDEA recomendado) lo pide, ejecuta `./gradlew genSources`
  para tener el código fuente de Minecraft navegable mientras editas.
- Para probar en modo desarrollo sin generar el jar: `./gradlew runClient`.
