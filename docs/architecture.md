# Arquitectura

App Android nativa: Kotlin, Jetpack Compose, Material 3. Paquete `com.precisevolumedial.app`. Una sola Activity y navegación con Navigation Compose.

## Capas

```
ui (Compose + ViewModel)  ->  audio / data  ->  APIs de Android
```

- **`audio/`**
  - `AudioStreamType`: Media (`STREAM_MUSIC`), Ring y Alarm. El dial y las pantallas solo usan Media; Ring y Alarm los usa el popup del override (pasos nativos, sin volumen fino).
  - `VolumeController`: envuelve `AudioManager`. Lee/escribe volumen, hace mute/unmute y emite cambios del sistema con un `ContentObserver` sobre `Settings.System`.
  - `AudioFxEngine`: dos efectos sobre la mezcla global (sesión 0): `LoudnessEnhancer` con ganancia negativa (atenuación del volumen fino + preamp del perfil) y `Equalizer` del sistema (5 bandas, ±15 dB). Son los que se comprobaron en el teléfono real; `DynamicsProcessing` global lo rechaza (`invalid parameter operation`).
  - `AudioFxEngine.stop()` libera los efectos; lo dispara el interruptor `processingEnabled` (AppContainer).
  - `AudioFxService`: servicio en primer plano (notificación mínima) que mantiene vivo el proceso mientras hay EQ o atenuación activos.
  - `VolumeMapper`: conversión pura porcentaje <-> (paso del sistema, atenuación dB).
  - `eq/`: `ParametricFilter` (biquads RBJ), `ParametricEqParser`, `EqProfile`, `EqFit` (ajuste a las bandas del `Equalizer`), `EqController` (estado del EQ a nivel de app) y `EqRange`.
- **`override/`** (volume button override)
  - `VolumeKeyService`: `AccessibilityService` con `canRequestFilterKeyEvents`. `onKeyEvent` captura `KEYCODE_VOLUME_UP/DOWN`; si `shouldOverride(...)` lo permite, consume la tecla (devuelve `true`, así el sistema no muestra su popup) y llama a `VolumeController.step` con el paso configurado. Lee los ajustes desde `SettingsRepository`.
  - `OverrideRules`: función pura `shouldOverride(settings, DeviceState, inCall)` (con test unitario; devuelve `false` si `processingEnabled` está apagado). `DeviceState` = desbloqueado / bloqueado (`KeyguardManager`) / pantalla apagada (`PowerManager.isInteractive`).
  - `VolumePopup`: ventana `TYPE_ACCESSIBILITY_OVERLAY` (sin permiso extra, `NOT_TOUCHABLE`) con una vista propia dibujada en `Canvas`; se oculta a los 1,8 s. Es táctil: el botón del altavoz silencia/restaura (`toggleMute`) y la pista se arrastra (`setPercent`).
  - Sincronía con el dial: `VolumeController.changes()` mezcla el `ContentObserver` del sistema con una señal propia que se emite al final de cada `setLevel` (con la atenuación y el paso ya aplicados; emitir antes hacía que el dial leyera un estado a medias y se desviara un paso del sistema, ~6 %), porque la atenuación fina no cambia ningún ajuste del sistema y el dial no se enteraba.
  - `ExternalEq` (en `audio/eq/`): anti-EQ. Convierte las ganancias del equipo externo (5 bandas) en filtros `Peak` de signo contrario (Q 0,7) y un preamp negativo si el equipo recorta (la inversa sube y habría clipping). `EqState` los mantiene separados del perfil: `filters`/`preampDb` son solo los del perfil (la curva de la pantalla) y `fxEnabled`/`fxFilters`/`fxPreampDb` combinan cada corrección solo mientras su interruptor está activo; `EqController` entrega esos últimos a `AudioFxEngine`, que no cambia.
- **`data/`**: `SettingsRepository` (DataStore), `ProfileRepository` (perfiles guardados en JSON) y `AutoEqClient` (única parte que usa red).
- **`ui/`**: una pantalla + ViewModel por destino (`volume`, `equalizer`, `settings`), componentes reutilizables en `components/`, tema en `theme/`.
- **Inyección**: `AppContainer` manual (en `PreciseVolumeDialApplication`) y `AppViewModelProvider` como factory. Migrar a Hilt/Koin si crece.

## Flujo del volumen

1. El usuario gira el dial -> `RotaryDial` emite un porcentaje entero.
2. `VolumeViewModel.setPercent` -> `VolumeController.setPercent` elige el paso del sistema y, en Media, la atenuación fina (ver más abajo).
3. Se relee el estado real del `AudioManager` y se publica en `StateFlow<VolumeUiState>`.
4. Cambios externos (teclas físicas, otras apps) llegan por `changes()` y refrescan el estado.

En streams nativos el porcentaje mostrado es el del paso real del sistema, no el que pidió el dedo.

## Volumen fino
Android solo ofrece ~15 pasos de varios dB. Para 1% de resolución en Media:
1. `getStreamVolumeDb` da los dB reales de cada paso para el dispositivo de salida activo (altavoz, cable, Bluetooth, USB).
2. Para un porcentaje se interpola el dB objetivo entre pasos, se elige el paso del sistema más bajo que sea igual o más alto y se compensa la diferencia con ganancia negativa (`trimDb`) en el `LoudnessEnhancer`.
3. Si el volumen cambia por teclas físicas u otra app, la atenuación se descarta y el porcentaje se recalcula desde el paso real.

Limitación: la atenuación afecta a toda la mezcla (también a tono y alarma), por eso solo se usa con el stream Media.

## Ecualizador
Los perfiles AutoEq son cadenas de ~10 filtros paramétricos, pero el `Equalizer` del sistema solo tiene 5 bandas (en el RMX3370: 30–120, 120–460, 460–1800, 1800–7000 y 7000–20000 Hz). `EqFit` promedia la respuesta del perfil dentro de cada banda y la limita a ±15 dB; el preamp del perfil se suma a la atenuación del `LoudnessEnhancer`. Es una aproximación: no reproduce picos estrechos.

## Decisiones y límites

- **Volumen discreto**: Android expone pasos (p. ej. 15 en media). El dial muestra el porcentaje redondeado y "System step X of N".
- **Efectos globales**: los efectos sobre la sesión 0 están deprecados y no todos los fabricantes/ROM los respetan. La app lo detecta (`trimAvailable` / `eqAvailable`), avisa en la UI y vuelve a pasos nativos. Validado en un Realme RMX3370 (Android 13) con `ToneTestActivity` (solo debug): `LoudnessEnhancer` y `Equalizer` globales se oyen; `DynamicsProcessing` global falla.
- **Estado de atenuación solo de Media**: `trimDb`, `lastSetLevel` y `lastSetAt` de `VolumeController` pertenecen a Media. Leer o cambiar Ring/Alarm (el popup lee los tres) no debe tocarlos; si lo hace, la atenuación fina se pierde y el dial salta un paso del sistema (~6 %).
- **Override de teclas**: un servicio de accesibilidad es la única vía sin root para ver las teclas de volumen en todas las apps. Vive en el mismo proceso que la app, por lo que comparte `AppContainer` (y mantiene el proceso vivo). `adb shell input keyevent` no pasa por el filtro de accesibilidad: hay que probarlo con las teclas físicas.
- **Tema**: oscuro por defecto; claro y "seguir sistema" configurables.
- **Accesibilidad**: dial y sliders exponen `progressBarRangeInfo` y `setProgress`; objetivos táctiles >= 48dp.
