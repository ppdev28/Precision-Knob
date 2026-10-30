# Precise Volume Dial

Native Android (Kotlin + Jetpack Compose + Material 3) volume controller built around a rotary dial, with 1% media volume steps and AutoEq headphone profiles.
UI reference: `screenshoots/screenshoot_UI.png`.

## Screens
- **Volume** – `RotaryDial` (media only), mute/down/up, processing on/off switch, status line.
- **EQ Correction** – headphone correction profiles from AutoEq (search/download or import `ParametricEQ.txt`), response curve, plus a separate anti-EQ card whose curve shows the external device effect and the correction applied (no graphic EQ: the screen only corrects the headphones' own curve).
- **Volume Button Override** (Settings → Controls) – hardware volume keys move the media volume in fine steps in any app, with its own popup (accessibility service, beta).
- **Settings** – default stream, percentage/haptics toggles, theme, app info.

## Layout
```
app/src/main/java/com/precisevolumedial/app/
  audio/   AudioStreamType, VolumeController, VolumeMapper, AudioFxEngine, eq/ (AutoEq parser, biquads, EqController)
  data/    SettingsRepository (DataStore)
  override/ VolumeKeyService (accessibility), OverrideRules, VolumePopup
  ui/
    theme/       colors (sage), type, 8dp spacing
    components/  RotaryDial, StreamSelector, QuickControls, AppTopBar, EQ widgets
    volume/ equalizer/ settings/   screen + ViewModel each
    PreciseVolumeDialApp.kt   nav host + bottom bar
```

## Build
Open in Android Studio and sync, or run `./gradlew :app:assembleDebug` (JDK 17, wrapper included). Details in [docs/](docs/README.md). minSdk 28, compileSdk 36.

## Notes
- Volume is exposed by Android in discrete steps; the dial shows the percentage rounded to the nearest step.
- Fine volume (`LoudnessEnhancer`) and EQ (`Equalizer`, 5 bands) use global audio effects; not every device/ROM honours them (the app detects this and falls back). Verified on a Realme RMX3370.

## TODO
See [docs/roadmap.md](docs/roadmap.md).
