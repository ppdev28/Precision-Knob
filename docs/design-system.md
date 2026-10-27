# Sistema de diseño

Código en `ui/theme/` y `ui/components/`. Estilo oscuro por defecto, inspirado en `screenshoot_UI.png`.

## Color
Definidos en `ui/theme/Color.kt` y asignados en `Theme.kt`. Los acentos salen de un solo tono (~168°, el verde azulado de `secondaryContainer`); no hay azules. Los fondos y superficies del tema oscuro se mantienen casi negros; solo los bordes y el texto secundario llevan un matiz de ese tono.

| Token (oscuro) | Valor | Uso |
|---|---|---|
| background | `#050608` | fondo de pantalla |
| surfaceContainer (`BottomBarDark`) | `#0D1013` | barra de navegación inferior |
| surface | `#14171C` | tarjetas, centro del dial, listas |
| surfaceVariant | `#1F242B` | pistas de interruptores |
| outline | `#3C4846` | bordes, arco inactivo |
| onSurface / onSurfaceVariant | `#FFFFFF` / `#BCC8C5` | texto |
| primary / secondary | `#5FE0C2` | arco del dial, botones, logo, interruptores |
| primaryContainer / secondaryContainer | `#17493F` | botones del dial, destino seleccionado de la barra inferior |

Tema claro en `Color.kt` con el mismo tono (primary `#00695C`). El brillo (glow) se usa solo en el arco activo y el knob. Los interruptores usan `flatSwitchColors()`.

## Espaciado y formas
- Rejilla de 8dp: `Spacing.xs 4 / s 8 / m 16 / l 24 / xl 32`; objetivo táctil mínimo 48dp.
- Radios: small 8, medium 16, large 24, pill.

## Tipografía
Material 3 con fuente por defecto. Estilos propios: `DialValueStyle` (72sp) y `DialUnitStyle` (28sp). Pendiente: DM Sans.

## Componentes
`AppTopBar`, `StreamSelector`, `RotaryDial`, `QuickControls`, `EqCurve`, `EqBandSlider`. Las filas de Settings usan `ListItem` estándar de Material 3.
