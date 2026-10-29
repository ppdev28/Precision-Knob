# Funcionalidad

Objetivo de la app: **control fino del volumen (de 1% en 1%)** y **ecualización de auriculares con perfiles de AutoEq** para que suenen planos.

## Volume (pantalla principal)
- Barra superior con el **interruptor de procesamiento de audio**. Navegación inferior con tres destinos: Volume, EQ Correction y Settings. La app solo controla el volumen **multimedia** (sin selector de streams).
- **Dial rotatorio** (`RotaryDial`): arco de 270°, arrastre o toque, ticks, brillo.
- **Pasos del dial** (Settings → Dial step: 1, 2, 5 o 10 %, por defecto 5 %): el knob "encaja" en múltiplos del paso, como un potenciómetro de pasos, con una marca por paso. Up/Down siguen moviendo 1 %.
- **Háptica**: un clic por paso (más fuerte cada 10 % y en los extremos), directo al vibrador con `VibrationEffect` (permiso `VIBRATE`). Se desactiva en Settings → Haptic feedback, y tampoco suena con el procesamiento de audio apagado. Estados 0%, 1–99%, 100%, arrastre y deshabilitado.
- **Control de 1% en Media**: ver [architecture.md](architecture.md#volumen-fino). El dial, Up y Down se mueven de 1 en 1%. El sublabel muestra el paso del sistema y la atenuación extra, p. ej. `System step 8 of 15 · -1.5 dB`.
- Interruptor de la barra superior (también en Settings): apagado libera los efectos de audio, detiene el servicio en segundo plano y vuelve a pasos nativos sin EQ.
- Si el dispositivo no permite el efecto de audio, Media vuelve a pasos nativos y se avisa en pantalla.
- Mute/Unmute recuerda el porcentaje anterior.

## EQ Correction (perfiles de auriculares)
- Interruptor maestro On/Off.
- **Buscar auriculares**: descarga el índice del repositorio público [AutoEq](https://github.com/jaakkopasanen/AutoEq) (~8.800 modelos), búsqueda por modelo o fuente, y descarga del `ParametricEQ.txt`.
- **Importar archivo**: cualquier `ParametricEQ.txt` (formato AutoEq / EqualizerAPO) con el selector del sistema.
- Perfiles guardados en el dispositivo; se selecciona o borra desde "My profiles". Se recuerda el perfil activo.
- Sin ecualizador gráfico: la pantalla solo corrige la curva propia de los auriculares con el perfil elegido.
- Curva de respuesta calculada con los filtros reales del perfil (PK, LS/LSC, HS/HSC) y preamp.
- El perfil se aproxima a las 5 bandas del ecualizador del sistema; no reproduce picos estrechos.

## Volume Button Override (Settings → Controls) — beta
Los botones físicos de volumen mueven el volumen multimedia con la resolución fina (1 % por defecto) **en cualquier app**, y se muestra un popup propio en lugar del del sistema.
- Pantalla `OverrideScreen`: interruptor general, aviso si falta activar el servicio de accesibilidad (botón que abre los ajustes del sistema), interruptores por situación (**dispositivo desbloqueado**, **bloqueado**, **pantalla apagada**), estilo del popup (Sage accent / Dark / Light) y *Tweaks*: paso por pulsación (1, 2 o 5 %) y lado del popup (derecha / izquierda).
- Requiere activar **Precise Volume Dial** en Ajustes → Accesibilidad. El servicio solo pide filtrar teclas; no lee contenido de pantalla.
- Durante llamadas o con el teléfono sonando (`AudioManager.mode != MODE_NORMAL`) no interviene: las teclas siguen controlando ese volumen.
- El **interruptor de la pantalla Volume** (procesamiento de audio) también apaga el override: con él apagado las teclas vuelven al sistema, y `OverrideScreen` lo avisa. El ajuste propio del override se conserva.
- Sin procesamiento de audio (o si el dispositivo no admite el efecto), cada pulsación es un paso nativo del sistema.
- El popup es interactivo: toca el botón de arriba para silenciar/restaurar y arrastra la pista para fijar el volumen; el dial de la app se actualiza al mismo tiempo.
- El botón **"…"** de abajo despliega los tres volúmenes (**Media, Ring y Alarm**) para ajustar el timbre y la alarma sin desactivar la app. Ring y Alarm usan los pasos nativos del sistema (la atenuación fina solo existe en Media); las teclas siguen moviendo Media. Con el modo No molestar activo, algunos móviles no dejan cambiar Ring: queda como estaba.
- Con la pantalla apagada se cambia el volumen sin popup. Que el sistema entregue las teclas al servicio con la pantalla apagada o bloqueada depende del fabricante.

## Settings
Mismo estilo que `OverrideScreen`: filas con icono a la izquierda sobre el fondo de la app, cabeceras de sección en color primario y separadores (componentes `ClickRow` / `SwitchRow` compartidos).
- Controles: acceso a Volume Button Override.
- Audio: procesamiento de audio (interruptor general), mostrar porcentaje, háptica.
- Apariencia: oscuro, claro, seguir sistema.
- Info: versión, privacidad (sin datos; red solo para descargar perfiles), permisos y límites.
