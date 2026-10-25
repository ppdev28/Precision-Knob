# Roadmap

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
- ⬜ Ring/Alarm: valorar si merece la pena algo más que pasos nativos

## Fase 2 – Perfiles de auriculares 🚧
- ✅ Parser `ParametricEQ.txt` (PK, LS/LSC, HS/HSC) y respuesta por biquads
- ✅ Búsqueda en el índice de AutoEq, descarga e importación de archivo, perfiles guardados
- ✅ Curva de respuesta y ajuste manual de 5 bandas
- ✅ Ajuste del perfil a las 5 bandas del `Equalizer` global (aproximación)
- ⬜ EQ más fiel: `DynamicsProcessing` por sesión de audio (detectar sesiones, `DUMP` por ADB) como hace Precise Volume
- ⬜ Validar en el teléfono que el EQ suena como el perfil (comparar con/sin; revisar que no haya clipping con el preamp)
- ⬜ Perfil automático por dispositivo de salida (auriculares Bluetooth/cable conectados)
- ⬜ Otros formatos: `GraphicEQ.txt`, `FixedBandEQ.txt`
- ⬜ Favoritos, filtro por tipo (in-ear/over-ear) y por fuente de medición
- ⬜ A/B rápido (activar/desactivar el EQ con un toque)

## Fase 3 – Calidad
- ⬜ Tests de UI del dial y de la pantalla EQ; previews de Compose
- ⬜ Aviso/acceso a No molestar para Ring
- ⬜ Fuente DM Sans y pulido visual frente a `screenshoot_UI.png`
- ⬜ Manejo de errores de red y modo sin conexión más claro

## Fase 4 – Experiencia y publicación
- ⬜ Quick Settings tile y widget con volumen fino
- ⬜ Material You opcional, accesibilidad, tablets
- ⬜ Firma de release, CI, localización ES/EN, política de privacidad

## Ideas futuras
- Perfiles de volumen por contexto y programación horaria
- Límite de volumen para auriculares
- Compartir perfiles propios
