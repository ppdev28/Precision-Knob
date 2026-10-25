# Funcionalidad

Objetivo de la app: **control fino del volumen (de 1% en 1%)** y **ecualización de auriculares con perfiles de AutoEq** para que suenen planos.

## Volume (pantalla principal)
- Barra superior, selector de stream (Media / Ring / Alarm) y mensaje de estado.
- **Dial rotatorio** (`RotaryDial`): arco de 270°, arrastre o toque, ticks, brillo, háptica opcional. Estados 0%, 1–99%, 100%, arrastre y deshabilitado.
- **Control de 1% en Media**: ver [architecture.md](architecture.md#volumen-fino). El dial, Up y Down se mueven de 1 en 1%. El sublabel muestra el paso del sistema y la atenuación extra, p. ej. `System step 8 of 15 · -1.5 dB`.
- Ring y Alarm usan los pasos nativos de Android (la atenuación por software afectaría a toda la mezcla).
- Si el dispositivo no permite el efecto de audio, Media vuelve a pasos nativos y se avisa en pantalla.
- Mute/Unmute recuerda el porcentaje anterior.

## Equalizer (perfiles de auriculares)
- Interruptor maestro On/Off.
- **Buscar auriculares**: descarga el índice del repositorio público [AutoEq](https://github.com/jaakkopasanen/AutoEq) (~8.800 modelos), búsqueda por modelo o fuente, y descarga del `ParametricEQ.txt`.
- **Importar archivo**: cualquier `ParametricEQ.txt` (formato AutoEq / EqualizerAPO) con el selector del sistema.
- Perfiles guardados en el dispositivo; se selecciona o borra desde "My profiles". Se recuerda el perfil activo.
- Curva de respuesta calculada con los filtros reales del perfil (PK, LS/LSC, HS/HSC) y preamp.
- Ajuste manual de 5 bandas (±12 dB) encima del perfil.
- El perfil se aproxima a las 5 bandas del ecualizador del sistema; no reproduce picos estrechos.

## Settings
- Audio: stream por defecto, mostrar porcentaje, háptica, restaurar último stream.
- Apariencia: oscuro, claro, seguir sistema.
- Info: versión, privacidad (sin datos; red solo para descargar perfiles), permisos y límites.
