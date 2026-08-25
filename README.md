<p align="center">
  <img src="[TBA]" alt="AVELISSE" width="120" height="120" />
</p>

<h1 align="center">AVELISSE for Android</h1>

<p align="center">
  <strong>Free, open-source Android keyboard for voice dictation — 100% on-device.</strong><br />
  Speak in any app. No cloud, no account, no subscription.
</p>

<p align="center">
  <a href="https://github.com/varbonida/avelisse-android/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/varbonida/avelisse-android/ci.yml?branch=main&label=CI" alt="CI" /></a>
  <a href="https://github.com/varbonida/avelisse-android/actions/workflows/release.yml"><img src="https://img.shields.io/github/actions/workflow/status/varbonida/avelisse-android/release.yml?branch=main&label=Release" alt="Release" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/varbonida/avelisse-android" alt="License: MIT" /></a>
  <a href="https://developer.android.com/about/versions/10"><img src="https://img.shields.io/badge/Android-10%2B%20(API%2029%2B)-3DDC84?logo=android&logoColor=white" alt="Android 10+" /></a>
  <a href="https://github.com/varbonida/avelisse-android/stargazers"><img src="https://img.shields.io/github/stars/varbonida/avelisse-android?style=social" alt="Stars" /></a>
</p>

<p align="center">
  <a href="[TBA]">Website</a> ·
  <a href="https://github.com/varbonida/avelisse-android/releases/latest">Download APK</a> ·
  <a href="https://github.com/getdictus/dictus-ios">iOS</a> ·
  <a href="https://github.com/getdictus/dictus-desktop">Desktop</a> ·
  <a href="[TBA]">Community</a>
</p>

---

AVELISSE is a free, open-source Android keyboard that adds voice dictation to any app. All speech recognition runs **on-device** via Whisper (whisper.cpp) and NVIDIA Parakeet (sherpa-onnx) — no server, no account, no subscription.

## Why AVELISSE?

- 🔒 **100% on-device** — your voice never leaves your phone. No cloud, no telemetry, no account.
- 🆓 **Free & open source** — MIT licensed, no subscription, fully auditable code.
- ⌨️ **System-wide IME** — works in every app as your default keyboard.
- ⚡ **Multi-engine** — Whisper (multilingual, 5 model sizes) or NVIDIA Parakeet (a fast English-only model, or a 25-language high-accuracy model for capable devices).
- 🌐 **FR + EN dictionaries** — smart word predictions while typing.

## How AVELISSE compares

| Feature | **AVELISSE** | Wispr Flow | Gboard Voice | SuperWhisper |
| --- | :---: | :---: | :---: | :---: |
| Price | **Free** | Free / $15/mo | Free | Free / $8.49/mo |
| 100% offline | ✅ | ❌ | ⚠️ | ⚠️ |
| Privacy-first | ✅ | ❌ | ⚠️ | ⚠️ |
| Open source | ✅ | ❌ | ❌ | ❌ |
| System keyboard | ✅ | ❌ | ✅ | ❌ |
| Cross-platform | ✅ ([iOS](https://github.com/getdictus/dictus-ios) · [Android](https://github.com/varbonida/avelisse-android) · [Desktop](https://github.com/getdictus/dictus-desktop)) | iOS · macOS · Win · Android | Android · Wear OS | iOS · macOS · Win |

See the full comparison on [AVELISSE's website]([TBA]).

## Install the beta

AVELISSE is currently in public beta — install by sideloading the APK from [GitHub Releases](https://github.com/varbonida/avelisse-android/releases/latest).

1. On your Android device, go to **Settings → Apps → Special app access → Install unknown apps** and allow your browser.
2. Download the latest APK from [Releases](https://github.com/varbonida/avelisse-android/releases/latest).
3. Open the `.apk` and tap **Install**.
4. Go to **Settings → System → Languages & input → On-screen keyboard → Manage on-screen keyboards**.
5. Enable **AVELISSE**.
6. Open any text field, tap the keyboard icon in the navigation bar, and select **AVELISSE**.

**Requirements:** Android 10 (API 29) or higher · ~80 MB for the smallest model (Whisper Tiny) — onboarding recommends a larger model automatically based on your device's available RAM.

## Screenshots

| Keyboard | Model Manager | Settings |
|----------|---------------|----------|
| ![Keyboard in action](screenshots/keyboard.png) | ![Model manager](screenshots/models.png) | ![Settings](screenshots/settings.png) |

## Features

- **Offline voice dictation** — Whisper + NVIDIA Parakeet, entirely on-device
- **Multi-engine STT** — Whisper (multilingual, 5 model sizes) or NVIDIA Parakeet (fast English-only, or 25-language high-accuracy)
- **Smart suggestions** — word predictions from FR+EN dictionaries while typing
- **Personal dictionary** — learns your frequently typed words
- **System keyboard** — works in any app as your default IME
- **AZERTY & QWERTY** — switchable keyboard layouts

## Roadmap

- [x] On-device Whisper + Parakeet engines
- [x] System IME with AZERTY / QWERTY layouts
- [x] FR + EN word predictions and personal dictionary
- [ ] Smart Mode Pro — on-device LLM reformulation
- [ ] Custom vocabulary (technical terms, names)
- [ ] Searchable local transcription history
- [ ] Audio-file transcription
- [ ] Sync settings across AVELISSE iOS / Android / Desktop (offline-first)

Have an idea? Open a [feature request](https://github.com/varbonida/avelisse-android/issues/new) — we prioritize the most-upvoted ones.

## Tech stack

- Kotlin + Jetpack Compose
- [whisper.cpp](https://github.com/ggerganov/whisper.cpp) (MIT) — Whisper STT
- [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) (Apache 2.0) — Parakeet STT
- Material Design 3

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md) for build setup, module overview, and PR guidelines. Good entry points:

- `good first issue` and `help wanted` in [Issues](https://github.com/varbonida/avelisse-android/issues)
- Bug reports with logs from a recent build
- Translations & locale tuning

## Privacy

AVELISSE collects no personal data and includes no analytics, telemetry, or crash reporting. All speech recognition and transcription happen entirely on-device — your voice and dictated text are never sent to a server.

The app's only network access is for downloading speech models the first time you select them (Whisper models from Hugging Face, Parakeet models from GitHub Releases) — no data is uploaded as part of this. See our [Privacy Policy]([TBA]).

## Support the project

AVELISSE is free and will stay free. If it helps you every day, consider [supporting development]([TBA]) — it directly funds new features and platform support.

## Community

- 🌐 [Website]([TBA])
- 💬 [Telegram]([TBA])
- 🐛 [Issues](https://github.com/varbonida/avelisse-android/issues)
- 📧 [Email](mailto:[TBA])

## License

MIT — see [LICENSE](LICENSE).

---

<p align="center">
  <sub>Made with ❤️ by <a href="[TBA]">AVELISSE Solutions</a> · <a href="https://github.com/varbonida">@varbonida</a></sub>
</p>
