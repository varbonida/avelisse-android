# AVELISSE Android — Design Reference

Mockups for the Android app are in `avelisse-android-design.pen`.
This file is readable via the Pencil MCP tools (`batch_get`, `get_screenshot`, `snapshot_layout`).

## Screens

| Screen | Frame ID | Description |
|--------|----------|-------------|
| Home Tab | `wrqi0` | 3-bar logo, active model card, last transcription card, "New dictation" button, bottom nav |
| Models Tab | `kSWvw` | Downloaded/Available sections, model cards with accuracy/speed gauges, engine badges |
| Settings Tab | `CqKvx` | Transcription/Keyboard/About sections, toggles, links |
| Recording Screen | `0yEaN` | Full-screen overlay: 30-bar waveform, timer, red stop button |
| Waveform Spec | `VUDbq` | Technical documentation: colors, 3 visual states with mini-waveforms, dimensions, Android specs |

## Reusable Components

| Component | ID | Description |
|-----------|----|-------------|
| BottomNavBar | `d7cJl` | Pill tab bar with 3 tabs (Home, Models, Settings) |
| GlassCard | `eKShU` | Glass card: surface #161C2C, border #FFFFFF15, corner 16px, shadow |

## Design System

### Colors
- Background: `#0A1628`
- Surface/Card: `#161C2C`
- Accent: `#3D7EFF`
- Accent highlight: `#6BA3FF`
- Accent dark: `#2563EB`
- Recording: `#EF4444`
- Success: `#22C55E`
- Text primary: `#FAFAF9`
- Text secondary: `#6B6B70`
- Border subtle: `#2A2A3E`
- Border glass: `#FFFFFF15`

### Typography
- Font: Inter (all sizes)
- Weights: 400 (body), 500 (labels), 600 (titles), 700 (headers)

### Glass Effect (Liquid Glass iOS -> Android adaptation)
- Surface `#161C2C` + 1px border `#FFFFFF15` + shadow(0, 4, 16, #00000040)
- Press animation: scale 0.96 + opacity 0.85, spring(dampingRatio=0.6)
- Corner radius: 16-20px cards, 31px pill nav bar

### Waveform
- 30 bars, 2px spacing, dynamic width
- Rendered via Canvas/CustomView (not 30 separate Views)
- Center (bars 12-18): gradient #6BA3FF -> #2563EB
- Edges (bars 1-11, 19-30): white opacity 15% -> 90%
- States: idle (flat 2px), recording (live energy), transcribing (sinusoidal)

## Custom Keyboard (IME)

Mockups in `avelisse-keyboard-design.pen` (separate file, light theme).

| Screen | Frame ID | Description |
|--------|----------|-------------|
| Keyboard Idle | `RTyxV` | Full AZERTY keyboard, blue pill mic button, gear settings |
| Keyboard Recording | `WwpNL` | Dynamic 30-bar waveform, X/check buttons, "00:03" timer, "Listening..." |
| Keyboard Transcribing | `XfNKh` | Sinusoidal waveform, "Transcribing..." label, no action buttons |

### Keyboard Colors (light theme)
- Background: `#D1D3D9` | Keys: `#FFFFFF` | Special keys: `#AEB3BE`
- Waveform center: gradient `#6BA3FF` -> `#2563EB`
- Waveform edges: `#B0B5C0` opacity gradient
- Mic button: `#3D7EFF` | Check: `#22C55E` | Cancel: `#6B6B70`

## Usage

To view an app screen:
```
get_screenshot(filePath: "design/avelisse-android-design.pen", nodeId: "wrqi0")
```

To view a keyboard screen:
```
get_screenshot(filePath: "design/avelisse-keyboard-design.pen", nodeId: "RTyxV")
```

To read a component's structure:
```
batch_get(filePath: "design/avelisse-android-design.pen", nodeIds: ["d7cJl"], readDepth: 3)
```
