# Compilar y ejecutar

## Requisitos
- JDK 17 (sirve el JBR de Android Studio).
- Android SDK con plataforma 36 y build-tools (compileSdk 36, targetSdk 35, minSdk 28).
- Gradle 8.14.3 mediante el wrapper incluido (`./gradlew`).
- AGP 8.13.0, Kotlin 2.0.21, Compose BOM 2024.12.01 (ver `gradle/libs.versions.toml`).

## Configuración local
`local.properties` está en `.gitignore`. Cada máquina debe crearlo:

```
sdk.dir=/ruta/al/Android/sdk
```

Android Studio lo genera solo al abrir el proyecto.

## Línea de comandos
```bash
export JAVA_HOME="/ruta/al/jdk17"      # p. ej. <Android Studio>/Contents/jbr/Contents/Home
./gradlew :app:assembleDebug           # APK en app/build/outputs/apk/debug/
./gradlew :app:installDebug            # instala en dispositivo/emulador conectado
./gradlew :app:assembleRelease         # R8 y recorte de recursos activados
```

## Android Studio
Abrir la carpeta raíz, esperar el sync y ejecutar el módulo `app`.

## Problemas frecuentes
- *SDK location not found*: falta `local.properties` o `ANDROID_HOME`.
- *Failed to find Platform SDK with path: platforms;android-36*: instalar la plataforma desde SDK Manager.
- *Unsupported class file major version*: Gradle usa un JDK distinto de 17+; revisar `JAVA_HOME`.
- Probar en dispositivo real: los emuladores tienen pasos de volumen y política de ring distintos.
- Sin `keystore.properties` la compilación release sale sin firmar (ver "Publicar una versión").

## Publicar una versión
1. Subir `versionCode` y `versionName` en `app/build.gradle.kts`.
2. Firma: `keystore.properties` en la raíz (en `.gitignore`, nunca se sube) con `storeFile`, `storePassword`, `keyAlias` y `keyPassword`. El keystore de release (`precise-knob-release.jks`, alias `precise-knob`) vive **fuera del repo** (`~/.keystores/`). Hay que guardar una copia de seguridad: sin esa clave no se pueden publicar actualizaciones de una app ya instalada.
3. `./gradlew :app:testDebugUnitTest :app:assembleRelease` → `app/build/outputs/apk/release/app-release.apk` (R8 y recorte de recursos activos). Comprobar la firma con `apksigner verify --print-certs`.
4. Renombrar a `PreciseKnob-<versión>.apk`, crear el tag `v<versión>` y subir la APK a una GitHub Release usando `docs/releases/v<versión>.md` como notas.

Una APK de release no se puede instalar sobre una de debug (firma distinta): desinstalar primero.

## Tests
```bash
./gradlew :app:testDebugUnitTest   # parser AutoEq, biquads, mapeo de volumen fino
```

## Prueba de tono (solo debug)
Con el teléfono conectado por ADB, reproduce un tono y recorre ajustes de `LoudnessEnhancer` y `Equalizer` globales para comprobar de oído que el dispositivo los aplica:
```bash
adb shell am start -n com.precisevolumedial.app/.debug.ToneTestActivity
```
