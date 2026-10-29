# PreciseVolumeDial – project instructions

Native Android app (Kotlin, Jetpack Compose, Material 3). Overview in `README.md`, details in `docs/`.

## Build and test
```bash
export JAVA_HOME="<Android Studio>/Contents/jbr/Contents/Home"   # JDK 17
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

## Devices and docs
- **Redmi Note 8 Pro**: test device, `adb` is free to use (install, logcat, screencap). Check `adb devices -l` first.
- **Realme RMX3370 (rx330)**: the user's own phone, **never** connect via `adb` or any other route; the user tests there.
- After finishing anything substantial, update `README.md` and `docs/` (features, architecture, roadmap, design-system) in the same change.
- `adb shell input keyevent` does not go through the accessibility key filter: test the volume button override with the physical keys.

## Commits: Conventional Commits (always)
Every commit follows [Conventional Commits 1.0](https://www.conventionalcommits.org/en/v1.0.0/). Commit only when asked.

```
<type>(<optional scope>): <subject>

<optional body: what and why, wrapped at ~72 columns>

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
```

- **Types:** `feat`, `fix`, `docs`, `style` (formatting, colors/visual polish with no behavior change), `refactor`, `perf`, `test`, `build` (Gradle, dependencies), `ci`, `chore`, `revert`.
- **Scopes** (optional, pick the area touched): `dial`, `volume`, `eq`, `settings`, `theme`, `audio`, `data`, `nav`, `icon`, `docs`, `build`.
- **Subject:** English, imperative mood, lowercase after the colon, no trailing period, at most 72 characters. Example: `fix(dial): vibrate on every detent, not only every 10%`.
- **Breaking changes:** `!` after the type/scope (e.g. `refactor(data)!: ...`) and a `BREAKING CHANGE:` footer.
- **One logical change per commit.** Split unrelated work (for example a feature and a docs-only change) into separate commits when the files allow it; otherwise use the dominant type and list the rest in the body.
- Keep the trailer line above on every commit. Never rewrite published history to "fix" old messages unless asked.

History note: the first two commits (`Add SonicDial app…`, `Rename to FlatDial…`) predate this rule and are not in Conventional Commits format.
