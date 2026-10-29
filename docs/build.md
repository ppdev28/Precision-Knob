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
- La compilación release no está firmada; añadir `signingConfig` antes de publicar.

## Tests
```bash
./gradlew :app:testDebugUnitTest   # parser AutoEq, biquads, mapeo de volumen fino
```

## Prueba de tono (solo debug)
Con el teléfono conectado por ADB, reproduce un tono y recorre ajustes de `LoudnessEnhancer` y `Equalizer` globales para comprobar de oído que el dispositivo los aplica:
```bash
adb shell am start -n com.precisevolumedial.app/.debug.ToneTestActivity
```
