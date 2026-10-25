# Sistema de diseño

Código en `ui/theme/` y `ui/components/`. Estilo oscuro por defecto, inspirado en `screenshoot_UI.png`.

## Color (oscuro)
| Token | Valor |
|---|---|
| background | `#101114` |
| surface | `#17191D` |
| surfaceVariant | `#1E2126` |
| outline | `#2A2D33` |
| onSurface | `#F2F3F5` |
| onSurfaceVariant | `#9AA0A8` |
| primary (cian) | `#5ECBEA` |
| primaryContainer | `#16313A` |

Tema claro definido en `Color.kt`. El brillo (glow) se usa solo en el arco activo y el knob.

## Espaciado y formas
- Rejilla de 8dp: `Spacing.xs 4 / s 8 / m 16 / l 24 / xl 32`; objetivo táctil mínimo 48dp.
- Radios: small 8, medium 16, large 24, pill.

## Tipografía
Material 3 con fuente por defecto. Estilos propios: `DialValueStyle` (72sp) y `DialUnitStyle` (28sp). Pendiente: DM Sans.

## Componentes
`SonicTopBar`, `StreamSelector`, `RotaryDial`, `QuickControls`, `EqCurve`, `EqBandSlider`. Las filas de Settings usan `ListItem` estándar de Material 3.
