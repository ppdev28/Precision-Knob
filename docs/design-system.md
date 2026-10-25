# Sistema de diseño

Código en `ui/theme/` y `ui/components/`. Estilo oscuro por defecto, inspirado en `screenshoot_UI.png`.

## Color (oscuro)
| Token | Valor |
|---|---|
| background | `#050608` |
| surface | `#14171C` |
| surfaceVariant | `#1F242B` |
| outline | `#3C4350` |
| onSurface | `#FFFFFF` |
| onSurfaceVariant | `#BCC3CE` |
| primary (cian) | `#63D6F5` |
| primaryContainer | `#0F3945` |

Paleta de alto contraste: fondo casi negro, superficies y bordes claramente elevados, texto principal blanco. Tema claro definido en `Color.kt`. El brillo (glow) se usa solo en el arco activo y el knob.

## Espaciado y formas
- Rejilla de 8dp: `Spacing.xs 4 / s 8 / m 16 / l 24 / xl 32`; objetivo táctil mínimo 48dp.
- Radios: small 8, medium 16, large 24, pill.

## Tipografía
Material 3 con fuente por defecto. Estilos propios: `DialValueStyle` (72sp) y `DialUnitStyle` (28sp). Pendiente: DM Sans.

## Componentes
`AppTopBar`, `StreamSelector`, `RotaryDial`, `QuickControls`, `EqCurve`, `EqBandSlider`. Las filas de Settings usan `ListItem` estándar de Material 3.
