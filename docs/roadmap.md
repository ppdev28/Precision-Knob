# Roadmap

La app se llama **Precise Volume Dial** (antes SonicDial y FlatDial); paquete `com.precisevolumedial.app`.

Estado: ✅ hecho · 🚧 en curso · ⬜ pendiente

Objetivos: **(1)** volumen de 1% en 1% sobre los pasos nativos y **(2)** perfiles de EQ de AutoEq para auriculares planos.

## Fase 0 – Esqueleto ✅
- ✅ Proyecto Gradle con wrapper, compila en debug
- ✅ Tema, navegación, pantallas, `RotaryDial`, control de volumen, ajustes

## Fase 1 – Volumen fino (1%) 🚧
- ✅ Tabla de dB por paso (`getStreamVolumeDb`) y mapeo porcentaje <-> (paso, atenuación)
- ✅ Atenuación con `LoudnessEnhancer` global + servicio en primer plano (`DynamicsProcessing` global descartado: lo rechaza el RMX3370)
- ✅ Detección de cambios externos (teclas físicas) y fallback a pasos nativos
- ✅ Validado en el teléfono real (RMX3370): prueba de tono y pasos de 1 % en la app
- ⬜ Validar salida por cable y Bluetooth, y con la app en segundo plano / pantalla apagada
- ⬜ Pantalla de estado/diagnóstico (qué efecto se creó y con qué error)
- ⬜ Calibrar la curva: comprobar que los dB reportados coinciden con lo que se oye; considerar curva perceptual
- ⬜ Alternativa si el efecto global no funciona en el dispositivo (p. ej. reproductor propio o sesiones por app)
- ✅ App orientada solo a Media (se quitó el selector Media/Ring/Alarm)
- ✅ Interruptor para desactivar el procesamiento y el servicio en segundo plano

## Fase 2 – Perfiles de auriculares 🚧
- ✅ Parser `ParametricEQ.txt` (PK, LS/LSC, HS/HSC) y respuesta por biquads
- ✅ Búsqueda en el índice de AutoEq, descarga e importación de archivo, perfiles guardados
- ✅ Curva de respuesta (se quitó el ecualizador gráfico manual; la pantalla pasó a llamarse "EQ Correction")
- ✅ Ajuste del perfil a las 5 bandas del `Equalizer` global (aproximación)
- ⬜ EQ más fiel: `DynamicsProcessing` por sesión de audio (detectar sesiones, `DUMP` por ADB) como hace Precise Volume
- ⬜ Validar en el teléfono que el EQ suena como el perfil (comparar con/sin; revisar que no haya clipping con el preamp)
- ⬜ Perfil automático por dispositivo de salida (auriculares Bluetooth/cable conectados)
- ✅ Anti-EQ manual para equipos externos (5 bandas, presets)
- ⬜ Anti-EQ medido con el micrófono (barrido + respuesta inversa), más preciso pero más complejo
- ⬜ Otros formatos: `GraphicEQ.txt`, `FixedBandEQ.txt`
- ⬜ Favoritos, filtro por tipo (in-ear/over-ear) y por fuente de medición
- ⬜ A/B rápido (activar/desactivar el EQ con un toque)

## Fase 3 – Calidad
- ✅ Dial por pasos configurable y háptica propia (antes usaba la háptica de Compose, que no vibraba en el teléfono del usuario)
- ⬜ Tests de UI del dial y de la pantalla EQ; previews de Compose
- ✅ Paleta salvia (se quitó la menta)
- ⬜ Fuente DM Sans y pulido visual frente a `screenshoots/screenshoot_UI.png`
- ⬜ Manejo de errores de red y modo sin conexión más claro

## Fase 3b – Override de botones de volumen 🚧
- ✅ Servicio de accesibilidad que captura las teclas, paso configurable, reglas desbloqueado / bloqueado / pantalla apagada
- ✅ Popup propio (3 estilos, lado derecho/izquierdo) y pantalla de ajustes `OverrideScreen`
- ✅ Validado con las teclas físicas en el Redmi Note 8 Pro
- ✅ El interruptor de la pantalla Volume desactiva también el override
- ⬜ Validar en el RMX3370 y con el teléfono bloqueado / pantalla apagada
- ✅ Popup interactivo (botón de silencio y arrastre) y sincronizado con el dial
- ✅ Botón "..." del popup con Ring y Alarm, como en `screenshoots/feature-override.png`
- ⬜ Aceleración al mantener pulsada la tecla; ocultar el popup en pantalla completa/juegos si molesta

## Fase 4 – Experiencia y publicación
- ⬜ Quick Settings tile y widget con volumen fino
- ⬜ Material You opcional, accesibilidad, tablets
- ⬜ Firma de release, CI, localización ES/EN, política de privacidad

## Ideas futuras
- Perfiles de volumen por contexto y programación horaria
- Límite de volumen para auriculares
- Compartir perfiles propios
