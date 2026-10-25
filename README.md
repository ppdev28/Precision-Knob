# FlatDial

Native Android (Kotlin + Jetpack Compose + Material 3) volume controller built around a rotary dial, with 1% media volume steps and AutoEq headphone profiles.
UI reference: `screenshoot_UI.png`.

## Screens
- **Volume** – `RotaryDial` (media only), mute/down/up, processing on/off switch, status line.
- **EQ Correction** – headphone correction profiles from AutoEq (search/download or import `ParametricEQ.txt`), response curve (no graphic EQ: the screen only corrects the headphones' own curve).
- **Settings** – default stream, percentage/haptics toggles, theme, app info.

## Layout
```
app/src/main/java/com/flatdial/app/
  audio/   AudioStreamType, VolumeController, VolumeMapper, AudioFxEngine, eq/ (AutoEq parser, biquads, EqController)
  data/    SettingsRepository (DataStore)
  ui/
    theme/       colors, type, 8dp spacing
    components/  RotaryDial, StreamSelector, QuickControls, AppTopBar, EQ widgets
    volume/ equalizer/ settings/   screen + ViewModel each
    FlatDialApp.kt   nav host + bottom bar
```

## Build
Open in Android Studio and sync, or run `./gradlew :app:assembleDebug` (JDK 17, wrapper included). Details in [docs/](docs/README.md). minSdk 28, compileSdk 36.

## Notes
- Volume is exposed by Android in discrete steps; the dial shows the percentage rounded to the nearest step.
- Fine volume (`LoudnessEnhancer`) and EQ (`Equalizer`, 5 bands) use global audio effects; not every device/ROM honours them (the app detects this and falls back). Verified on a Realme RMX3370.

## TODO
See [docs/roadmap.md](docs/roadmap.md).
