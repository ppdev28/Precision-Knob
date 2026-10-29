# Sistema de diseño

Código en `ui/theme/` y `ui/components/`. Estilo oscuro por defecto, inspirado en `screenshoots/screenshoot_UI.png`.

## Color
Definidos en `ui/theme/Color.kt` y asignados en `Theme.kt`. Los acentos salen de un solo tono: verde salvia (*sage*, ~135°, poco saturado); no hay azules ni menta. Los fondos y superficies del tema oscuro se mantienen casi negros con un matiz verdoso muy leve; los bordes y el texto secundario también.

| Token (oscuro) | Valor | Uso |
|---|---|---|
| background | `#070908` | fondo de pantalla |
| surfaceContainer (`BottomBarDark`) | `#0E1210` | barra de navegación inferior |
| surface | `#151A17` | tarjetas, centro del dial, listas |
| surfaceVariant | `#212825` | pistas de interruptores |
| outline | `#414B45` | bordes, arco inactivo |
| onSurface / onSurfaceVariant | `#FFFFFF` / `#BAC5BD` | texto |
| primary / secondary | `#A9C7A6` | arco del dial, botones, logo, interruptores |
| primaryContainer / secondaryContainer | `#30443A` | botones del dial, destino seleccionado de la barra inferior |

Tema claro en `Color.kt` con el mismo tono (primary `#3F6B4A`). El brillo (glow) se usa solo en el arco activo y el knob. Los interruptores usan `appSwitchColors()`. El icono de la app usa fondo `#A9C7A6` y trazo `#0E1A10`.

## Popup de volumen (override)
`override/VolumePopup`: píldora vertical de 64×264dp dibujada con `Canvas` (overlay de accesibilidad, no tocable). Tres estilos en `PopupStyle`: *Sage accent* (panel oscuro y relleno salvia), *Dark* (relleno gris claro) y *Light* (panel claro y relleno salvia oscuro); usan los colores de `Color.kt`.

## Espaciado y formas
- Rejilla de 8dp: `Spacing.xs 4 / s 8 / m 16 / l 24 / xl 32`; objetivo táctil mínimo 48dp.
- Radios: small 8, medium 16, large 24, pill.

## Tipografía
Material 3 con fuente por defecto. Estilos propios: `DialValueStyle` (72sp) y `DialUnitStyle` (28sp). Pendiente: DM Sans.

## Componentes
`AppTopBar`, `StreamSelector`, `RotaryDial`, `QuickControls`, `EqCurve`, `EqBandSlider`. Las filas de Settings usan `ListItem` estándar de Material 3.
