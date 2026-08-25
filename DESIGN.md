---
name: AVELISSE Android
description: Offline voice-dictation keyboard for Android — Mediterranean Coastal palette (sea teal, terracotta, sun amber, whitewash) over a warm, flat, borderless-shadow UI.
colors:
  primary: "#007C92"
  primary-container: "#D9EBEF"
  secondary: "#D85C27"
  secondary-container: "#F9E7DF"
  accent: "#FFB74D"
  background: "#F7F7F7"
  surface: "#FFFFFF"
  surface-variant: "#F3E5AB"
  text-primary: "#071A1D"
  text-secondary: "#6E8A8F"
  border: "#D8E2E3"
  error: "#C0392B"
  success: "#6B8E4E"
  recording: "#C0392B"
typography:
  display:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "32sp"
    fontWeight: 700
    letterSpacing: "-0.5sp"
  title-large:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "28sp"
    fontWeight: 600
    letterSpacing: "-0.5sp"
  title-medium:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "17sp"
    fontWeight: 600
  body-large:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
  body-medium:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "15sp"
    fontWeight: 400
    lineHeight: "22.5sp"
  body-small:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "13sp"
    fontWeight: 400
  label-large:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "14sp"
    fontWeight: 500
  label-medium:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
  label-small:
    fontFamily: "DM Sans, sans-serif"
    fontSize: "12sp"
    fontWeight: 400
rounded:
  xs: "3dp"
  sm: "8dp"
  md: "12dp"
  lg: "14dp"
  xl: "16dp"
  xxl: "20dp"
  pill: "31dp"
spacing:
  xs: "2dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "20dp"
  xxl: "32dp"
components:
  card:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.xl}"
    padding: "20dp"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    height: "40dp"
  toggle-track-on:
    backgroundColor: "{colors.primary}"
    rounded: "{rounded.pill}"
    width: "51dp"
    height: "31dp"
  nav-bottom:
    backgroundColor: "{colors.surface}"
    rounded: "{rounded.pill}"
    height: "80dp"
---

# Design System: AVELISSE Android

## Overview

**Creative North Star: "The Whitewashed Terrace"**

AVELISSE is a system keyboard, not a destination app — most of its screens exist to get the user in and out fast (grant a permission, pick a model, glance at settings) or to stay quietly out of the way during the one moment that matters: recording. The implemented layout/component system reflects that split, and now adopts a new color identity: **Mediterranean Coastal** — a sea-teal primary (`#007C92`), a sun-baked terracotta secondary (`#D85C27`), a warm sun-amber accent (`#FFB74D`), and whitewashed-wall neutrals (`#F7F7F7` background, `#FFFFFF` surface, `#F3E5AB` pale sand for emphasis surfaces). This replaces the previous ivory/tan "Home" palette as the documented, current design system — see **Colors** below. Notably, the new Primary and Secondary hues land almost exactly on the app's real adaptive launcher icon colors (`#007C92` teal, `#D85C27` terracotta), which the icon has used all along — adopting this palette in-app would bring the UI into alignment with an asset that already existed, rather than introducing something foreign.

The typographic voice is unchanged: a single geometric sans (DM Sans) at every weight, with a signature move of tight negative tracking (-0.5sp) on the largest display/title sizes only. Depth is still expressed almost entirely through 1dp borders and flat color rather than elevation or shadow (with one confirmed exception, `SettingsCard` — see Elevation & Depth); the one native-feeling flourish is the recurring spring-physics press-scale (0.96, damping 0.6) on tappable cards, and a hand-tuned, physically-modeled waveform blob that remains the app's signature component.

**Implementation status:** this documents the *target* color system. The actual `AvelisseColors.kt` and the `ime` module's own theme still implement the previous palette in code as of this writing — this pass is documentation-only (see the task that produced it); a future implementation pass should update the app's source to match the hex values documented here.

**Key Characteristics:**
- Flat, borderless-shadow surfaces; depth is a 1dp border, never a shadow (`SettingsCard` is the one exception).
- One accent pair carries the system — sea teal `#007C92` (Primary) for primary actions/active state, terracotta `#D85C27` (Secondary) for a warm secondary accent, with sun amber `#FFB74D` (Accent) reserved for sparing highlight use.
- A single font (DM Sans) carries the entire system; hierarchy comes from size/weight, not family switching.
- The waveform (bars and the organic "blob") is the brand's one recurring signature motif — reused, not reinvented, across Home, Recording, Test Recording, onboarding, and the keyboard.
- The `ime` module currently implements its own separate, code-level theme, out of scope for this documentation pass — see Known Inconsistencies.

## Screens

### Home
**File:** `HomeScreen.kt`. **Layout:** single scrollable `Column`, 16dp horizontal padding, full-bleed `Background` (`#F7F7F7`). **Hierarchy:** animated waveform logo at top, "AVELISSE" wordmark (28sp Bold, `Primary` teal) with the `home_tagline` string beneath it (13sp, `Text Secondary`), then an active-model card and a last-transcription card, each a `card` (16dp corner, `Surface` bg, 1dp `Border`, 20dp internal padding), followed by a "New dictation" CTA. **Navigation:** bottom nav bar (80dp pill, `Surface` white bg, `Primary` active tab) pinned at the bottom, shared by Home/Models/Settings. **States:** no loading/error state variants found for this screen itself. **Animation:** the logo animation runs continuously and unconditionally while composed (not state-triggered). **Relationships:** the wordmark/logo pairing here is visually louder (teal, bold) than the same pairing on the Onboarding Welcome screen (see Known Inconsistencies).

### Recording
**File:** `RecordingScreen.kt`, drawing shared composables (label/timer, waveform, transcribing indicator) from `HomeScreen.kt`. **Layout:** full-bleed `Background`, 32dp outer horizontal padding, waveform rendered full-bleed/edge-to-edge rather than inset. **Hierarchy:** close (X) icon top corner, large timer (48sp Medium) and "Recording..."/"Transcribing..." label above the waveform, which dominates the screen. **Colors:** the waveform blob's three traveling sine layers use `Primary`/`Secondary`/`Accent` (teal/terracotta/sun amber). **States:** idle → recording (mic-reactive blob) → transcribing (swaps to the 30-bar transcribing indicator) → result (a `card` showing the transcribed text). **Animation:** the waveform blob's amplitude follows live mic energy with fast-rise (×0.3)/slow-decay (×0.85) easing, floored at 0.1 so it never goes fully flat. **Relationships:** intentionally shares its label/timer/waveform/transcribing composables with Test Recording (onboarding) so the two never visually drift — see Do's and Don'ts.

### Transcribing state
Not a standalone screen — a state within Recording (and, separately, within the IME's own transcribing screen, see Keyboard/IME below). **App-side component:** the transcribing indicator, built on the shared 30-bar waveform rather than the blob, since there's no live audio to visualize during processing — instead a driver produces a synthetic traveling sine wave. **Colors:** inner ~40% of bars solid `Primary` teal, outer ~60% fades toward `Text Secondary` with distance-based opacity — both tokenized, on the app side. **Label:** "Transcribing..." text. **Relationship:** the *shape and driver* are identical to the keyboard's transcribing bars; only the color mapping differs (the new Mediterranean palette here vs. the IME's separate dark theme), which is the same split documented under Known Inconsistencies.

### Models
**File:** `ModelsScreen.kt` (list/grid host) + `ModelCard.kt` (per-model card). **Layout:** scrollable list, 16dp horizontal padding, `Background` (`#F7F7F7`). Screen title uses the same treatment as Settings' title (22sp Bold, `Text Primary`). Content grouped into "Downloaded"/"Available" sections. **Components:** `ModelCard` — 16dp corner; active = `Primary`-tinted (18% alpha) background + 2dp `Primary` border; inactive = `Surface` + 1dp `Border`; a provider badge ("WK" for Whisper, "NV" for Parakeet/NVIDIA) on a 20%-alpha badge-color pill; two 5-segment precision/speed bars. **States:** not-downloaded (download button, 40dp height/12dp corner, `Primary` → a darkened shade of `Primary` gradient), downloading (progress %), downloaded/selectable, active/selected, and an English-only warning banner on the Parakeet CTC-110M card — currently a hardcoded amber close in hue to the new `Accent` token (see Known Inconsistencies, which now recommends reusing `Accent` directly rather than adding a separate Warning token). **Animation:** press-scale spring (1→0.96, damping 0.6) on tap; swipe-to-delete reveals an 80dp-wide `Error`/`Recording`-red delete panel via a bouncier spring, snapping open/closed at the drag's halfway point.

### Settings
**File:** `SettingsScreen.kt`. **Layout:** scrollable `Column`, `Background`, title at 22sp Bold `Text Primary` (16dp start/top padding). Sections: TRANSCRIPTION, CLAVIER, APPARENCE, A PROPOS, each a `SectionHeader` + `SettingsCard` group. **Components:** `SettingsCard` — `Surface` bg, 20dp corner, **3dp shadow (not a border — the one exception to the system's border-only card convention, see Known Inconsistencies)**; rows use a toggle (on-track color `Primary`) for switches, a `Surface Variant` pale-sand chip behind picker values, and a picker sheet (`Surface` container, `Primary`-selected radio button) for pickers. **Links:** GitHub/Legal rows, both pointing at real URLs. **Relationships:** establishes the `Background`/`Surface`/`Text Primary`/`Text Secondary` tokens that Licences, Debug Logs, and both Sound screens all reuse directly rather than defining their own.

### Licences
**File:** `LicencesScreen.kt`. **Layout:** scrollable `Column`, `Background`, top bar with back arrow + "Licences"/"Licenses" title (20sp SemiBold, `Text Primary`). **Components:** a license block per dependency — name (18sp SemiBold, `Text Primary`), author (14sp, `Text Secondary`), a clickable URL (14sp, `Primary`), then the full license text (11sp monospace, `Text Secondary`) inside an 8dp-corner `Surface` card; 16dp horizontal padding per block, 24dp vertical spacing between blocks. **Content sections:** auto-generated Maven/OSS entries followed by manual entries for whisper.cpp, sherpa-onnx, NVIDIA Parakeet, and the original "Dictus" project this app is forked from (MIT attribution — legally required, not decorative, and entirely unaffected by this color update). **Relationships:** the only screen where color tokens sit directly beside legally load-bearing text — colors change here exactly as everywhere else; the license/attribution text itself is never touched by a palette update.

### Onboarding (7 steps, one shared scaffold)
**Shared scaffold:** a full-screen `Box` on `Background`, 32dp horizontal padding, content vertically centered between two `weight(1f)` spacers, CTA button + progress dots pinned 60dp from the bottom. All 7 steps (Welcome, Mic Permission, Keyboard Setup, Mode Selection, Model Download, Test Recording, Success) render inside this one scaffold, so layout/spacing is identical across all of them by construction. **Welcome:** the animated logo + "AVELISSE" wordmark at 42sp ExtraLight, `Text Primary` (near-black) with -0.5sp tracking — notably different weight/color from the same wordmark on Home (see Known Inconsistencies) — plus the welcome tagline at 17sp. **Mode Selection:** a Letters/Numbers card picker, "ABC" pre-selected by default. **Model Download:** the same gradient download button as Models, plus a progress percentage and an "Extracting model files…" state. **Progress dots:** 7 dots, 8dp circles, 8dp gaps; active = `Primary` for steps 1-5, `Secondary` for the final step; inactive = `Border`. **CTA:** the shared CTA button throughout, using a `Secondary`-based gradient only on the final step instead of the default `Primary`-based gradient.

### Test Recording (onboarding step 6)
**File:** the onboarding Test Recording screen, built from the *exact same* shared composables as the main Recording screen (label/timer, waveform, transcribing indicator) rather than a local reimplementation — this was a deliberate unification (per project history) after onboarding's own drifted copy caused visual mismatches. **Layout/colors/animation:** identical to Recording (see above), composed inside the onboarding scaffold instead of as a standalone screen. **Result:** shows a `card` with the transcribed test phrase before letting the user continue. **Relationship:** this is the system's clearest example of the "Do" rule — any visual change to Recording's label, timer, or waveform is guaranteed to apply here too, since both call the same functions.

### Keyboard/IME
**Module:** `ime/` — a keyboard screen (idle typing), a separate transcribing screen (processing state), an emoji picker screen. **Theme:** wrapped in the library's own dark-by-default reactive Material theme — **this is the one part of the app not on the fixed Mediterranean Coastal palette, and out of scope for this documentation pass** (see Known Inconsistencies). **Layout:** standard QWERTY/AZERTY key grid, 48dp key height; a mic pill (56×40dp, 20dp corner) triggers recording. **Colors:** still the previous dark theme's key/accent colors, unchanged by this update. **Recording/Transcribing in-keyboard:** reuses the same waveform bar engine as the app side, but rendered in the IME's own separate palette — same motif, different color world. **States:** idle, shifted, caps-locked, recording, transcribing, emoji-picker overlay.

## Colors

**Mediterranean Coastal Serenity.** The reference palette (5 anchor colors: sea teal, sun amber, pale sand, whitewash, terracotta) reads as warm, sun-bleached, and coastal — the opposite of a cold, saturated "tech" palette. This is now the current, active AVELISSE design system; the previous ivory/tan "Home" palette is deprecated (see note below).

### Primary
- **Sea Teal** (`#007C92`, token `primary`): the system's primary action color — CTA buttons, active nav tab, active model card border/badge, selected states, slider thumb/track, link text, the toggle's on-state. Almost exactly matches the app's real launcher-icon teal (`#007C92`) already in use.
- **Pale Sea Tint** (`#D9EBEF`, token `primary-container`, derived: ~15% Primary over white): a soft container fill behind primary-colored content — e.g. an active chip's background, a highlighted row. New role; the previous system had no direct equivalent.

### Secondary
- **Terracotta** (`#D85C27`, token `secondary`): a warm secondary accent — a decorative wave layer in the animated logo, the final onboarding step's CTA gradient and progress dot. Nearly identical to the app's real launcher-icon terracotta (`#D85C27`).
- **Pale Terracotta Tint** (`#F9E7DF`, token `secondary-container`, derived: ~15% Secondary over white): soft container fill behind secondary-accented content.

### Tertiary (Accent)
- **Sun Amber** (`#FFB74D`, token `accent`): a sparing highlight color — the waveform's third animated layer, a badge or warning-banner fill (see Known Inconsistencies), a single decorative touch. Not a primary interactive color; reserve it for moments that should feel like a warm highlight, not an action.

### Neutral
- **Whitewash** (`#F7F7F7`, token `background`): app screen background — matches the app's real launcher-icon background already in use.
- **White Surface** (`#FFFFFF`, token `surface`): card/elevated-content surface sitting on `background`.
- **Pale Sand** (`#F3E5AB`, token `surface-variant`): a warmer emphasis surface distinct from the neutral card — a selected picker row, a highlighted chip background.
- **Deep Ink Teal** (`#071A1D`, token `text-primary`, derived: ~15% Primary tinted into near-black): primary body/heading text on `background`/`surface` — reads as near-black with a subtle brand-hue cast rather than flat neutral gray.
- **Slate Teal** (`#6E8A8F`, token `text-secondary`, derived from the same family as `text-primary`): secondary/muted/caption text, disabled labels.
- **Mist Border** (`#D8E2E3`, token `border`, derived from the same family): hairline borders, dividers, card outlines.

### Semantic
- **Clay Red** (`#C0392B`, token `error`): not present in the reference image (no red in it) — chosen as a warm, terracotta-compatible red that still reads unambiguously as an error/destructive state. Used for error text, delete actions, the Material `error` role.
- **Olive Grove** (`#6B8E4E`, token `success`): not present in the reference image — chosen as a Mediterranean-appropriate olive green (an iconic regional visual cue) for success/confirmation states, replacing the previous system's generic green.
- **Recording** (`#C0392B`, token `recording`): intentionally shares the `error` token — mic/stop iconography and destructive actions, consistent with how the previous system also unified Recording and Destructive into one color.
- **Transcription/Loading:** no new token — the waveform/loading animation uses `primary` as its dominant color, with `accent` as a secondary highlight layer (see Screens → Recording, Animation).

### Previous palette — deprecated
The prior "Home"/"Models" ivory-tan-teal-terracotta system (`#F5F0E6`, `#EFE3CC`, `#0F7A8C`, `#D9662E`, etc.) and the separate dark-navy Material theme (`#0A1628`, `#3D7EFF`, etc.) are no longer the documented design system. They remain implemented in the app's actual source code as of this writing (this update is documentation-only), but should not be treated as current or extended further — new work should reference the Mediterranean Coastal tokens above.

### Named Rules
**The One-Palette Rule.** Every screen that reads a color from this system uses the Mediterranean Coastal tokens above — there is no longer a sanctioned second "brand" palette for app-module screens. (The `ime` module's own separate theme is a known, out-of-scope exception — see Known Inconsistencies.)

## Typography

**Display/Body/Label Font:** DM Sans (bundled TTFs: `dm_sans_extralight.ttf`, `dm_sans_regular.ttf`, `dm_sans_medium.ttf`, `dm_sans_semibold.ttf`, `dm_sans_bold.ttf` — no distinct display or mono face).

**Character:** One geometric sans carries every role in the system; the only typographic "move" is tighter negative tracking on the two largest sizes, giving headlines a slightly denser, more considered feel than the otherwise-default-spaced body text.

### Hierarchy (Material3 `Typography` slots, as defined in `AvelisseTypography.kt`)
- **displayLarge** (Bold 700, 32sp, -0.5sp tracking): not seen wired to a screen in this pass — reserved slot.
- **titleLarge** (SemiBold 600, 28sp, -0.5sp tracking): screen-level titles (e.g. Settings' "AVELISSE" wordmark size class).
- **titleMedium** (SemiBold 600, 17sp, default tracking): sub-screen top-bar titles (Licences, Debug Logs, Sound Settings, Sound Picker all use 17-20sp SemiBold directly rather than this slot by name — see Known Inconsistencies).
- **bodyLarge** (Regular 400, 16sp): primary row/label text across Settings-style rows.
- **bodyMedium** (Regular 400, 15sp, 22.5sp line height = 1.5×): default reading text.
- **bodySmall** (Regular 400, 13sp): secondary/muted text, card descriptions.
- **labelLarge** (Medium 500, 14sp): button/toggle-row labels.
- **labelMedium** (Medium 500, 13sp): chip/badge text.
- **labelSmall** (Regular 400, 12sp): captions, nav-bar tab labels, model-size text.

Screen code frequently sets `fontSize`/`fontWeight` directly on `Text()` (e.g. 20.sp/SemiBold for a top-bar title, 48.sp/Medium for the Recording timer, 42.sp/ExtraLight for the onboarding wordmark) rather than referencing an `AvelisseTypography` slot by name — the type scale above is a defined system, but most screens hand-pick literal sizes that are *close to* but not always identical to it.

### Named Rules
**The One-Weight-Family Rule.** Every weight in use (ExtraLight through Bold) belongs to DM Sans. No other font family appears anywhere in app-owned code.

## Layout

No enforced grid or breakpoint system — this is a single-density-target phone UI (minSdk 29 / Android 10+) using `fillMaxWidth()`/`fillMaxSize()` Compose containers with `weight()`-based flexible spacers rather than fixed breakpoints. See **Responsive Behavior** below for what little adaptive behavior does exist.

Recurring padding/spacing values observed across screen-level composables (`app/`, `ime/`, excluding tests): **16dp** (screen/section horizontal padding — most common, 19 occurrences), **32dp** (onboarding's outer horizontal padding, and inter-block vertical spacing — 10 occurrences), **8dp** (row/icon-button padding — 9 occurrences), **20dp** (card internal padding), **12dp** (`SettingsCard` row spacing), **2dp** (waveform bar gap).

Onboarding uses a dedicated shared scaffold: full-screen Box on `background`, 32dp horizontal padding, content vertically centered between two `weight(1f)` spacers, CTA button + progress dots pinned 60dp from the bottom. All 7 onboarding steps (Welcome, Mic Permission, Keyboard Setup, Mode Selection, Model Download, Test Recording, Success) render inside this one scaffold.

## Elevation & Depth

Mostly flat — the standard `card` component and `ModelCard` use no `Card` elevation and no `shadow()` modifier; depth there comes entirely from (1) a 1dp `border`, and (2) flat color-block contrast between a screen's `background` and its `surface`/`surface-variant` tone (e.g. `#F7F7F7` background vs. `#FFFFFF` or `#F3E5AB` card). `ModelCard`'s active state additionally uses a thicker (2dp vs 1dp) `primary`-colored border instead of elevation to signal selection.

**Confirmed exception:** Settings' own `SettingsCard` (`SettingsScreen.kt:353-362`) uses `.shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), clip = true)` — a real, deliberate shadow, not a border. This is the one card style in the system that breaks the border-only convention (see Known Inconsistencies). This is a structural (not color) inconsistency, unaffected by this palette update.

### Named Rules
**The Border-Not-Shadow Rule (with one exception).** The standard `card` and `ModelCard` signal depth with a 1dp border, never a shadow. `SettingsCard` is the sole exception, using a 3dp shadow instead — match whichever sibling component you're extending, don't assume the border rule is universal.

## Shapes

Rounded rectangles throughout, no sharp corners anywhere in app-owned UI. The corner-radius scale in actual use, from most to least common: **16dp** (dominant — cards, model cards, download buttons, delete-swipe reveal, dominates by a wide margin at 18 occurrences), **8dp** (badges, chip backgrounds, small progress-bar clipping), **3dp**/**12dp**/**14dp** (progress bar tracks, settings cards, picker sheets), **20dp** (a couple of larger container corners), **31dp** (one deliberate outlier — the pill-shaped bottom nav bar, `RoundedCornerShape(31.dp)`, half its own 80dp height for a true stadium/pill silhouette), **4dp**/**10dp** (small one-off badge corners). Toggle knobs and the nav bar's active-tab indicator use `CircleShape`/full pill radii rather than a numeric value.

## Components

### Buttons
- **Shape:** 12dp corner radius (model download button), or 14dp corner radius via the shared onboarding CTA button (`RoundedCornerShape(14.dp)` — a rounded rectangle, not a stadium/pill shape).
- **Primary (model download):** horizontal gradient from `primary` to a darkened shade of `primary`, white 14sp SemiBold label, 40dp fixed height, no border.
- **Onboarding CTA:** full-width, 56dp height, 14dp corner, 17sp SemiBold white label; enabled state fills a gradient (`primary` → darkened `primary` for most steps, `secondary` → darkened `secondary` for step 6); disabled state swaps to solid `surface` + 1dp `border` and mutes the label/icon to `text-secondary`; press animates scale 1→0.96 and alpha 1→0.85 via a shared spring (damping 0.6, medium stiffness).
- **Toggle:** 51×31dp pill track (16dp corner radius) with a 27dp circular white knob offset by `animateDpAsState`; track color is `primary` when checked (the component's own code default is currently a green, but every real call site overrides it — see Known Inconsistencies), or a muted neutral when off.

### Cards / Containers
- **Standard card:** 16dp corner radius, `surface` background, 1dp `border`, 20dp internal padding. This is the one card language for fixed-palette screens (Home, Onboarding, Recording result cards, etc.).
- **ModelCard:** 16dp corner radius; active state = `primary` background tint (18% alpha) + 2dp `primary` border; inactive = `surface` background + 1dp `border`. Has a spring-physics press-scale (0.96, damping 0.6, medium stiffness) and a swipe-to-reveal `error`-red (80dp-wide) delete action for downloaded models.

### Navigation
- **Bottom nav bar:** 80dp-tall, 31dp-radius pill, `surface` (white) background, 8dp outer margin. Active tab: `primary` icon/label on a 15%-alpha `primary` pill (16dp radius) behind it; inactive: `text-secondary` gray, no pill. Labels are 12sp Bold.

### Signature Component — Waveform
The waveform is the system's one true piece of brand identity, appearing in two forms that share a driver but differ in visual language:
- **Bar waveform** (30 discrete pill-shaped bars, driver-smoothed): used for the keyboard's idle/recording/transcribing states and (in "processing" sine-wave mode) the app's transcribing indicator. Inner 40% of bars solid `primary`/`accent`; outer 60% fades with distance-based opacity toward `text-secondary` (app side) or the IME's own separate palette (keyboard side).
- **Waveform blob** (single filled, mirrored, organically-textured shape via 3 traveling sine waves + 5 fixed Gaussian peaks + an edge envelope, all ported 1:1 from `designs/waveform_canvas.html`): the standalone Recording/Test Recording screens' mic-reactive visualization, using `primary`/`secondary`/`accent` for its three wave layers. Volume is eased with the same fast-rise (0.3)/slow-decay (0.85) constants as the bar version.

## Do's and Don'ts

### Do:
- **Do** reuse the Mediterranean Coastal tokens (`primary`, `secondary`, `accent`, `background`, `surface`, `surface-variant`, `text-primary`, `text-secondary`, `border`, `error`, `success`) for anything on a fixed-palette screen — there is no legitimate reason to write a new `Color(0x...)` literal on those screens.
- **Do** keep Recording and Test Recording visually identical — they intentionally share their label/timer, waveform, and transcribing-indicator composables so the two never drift.
- **Do** use a 1dp border for new card-like surfaces, matching the standard card/`ModelCard` — never introduce a shadow (`SettingsCard` is a legacy exception, not a pattern to repeat).
- **Do** default new corner radii to 16dp (cards) unless the element is clearly a pill/badge (8dp) or a full stadium shape (radius = half the element's height, as the 80dp/31dp nav bar demonstrates).
- **Do** use semantic token names (`primary`, `surface-variant`, `text-secondary`) rather than names tied to a specific screen — this system no longer has screen-named palettes like the previous "Home"/"Models" split.

### Don't:
- **Don't** recreate the previous "Home"/"Models" ivory-tan palette or its dark-navy counterpart — both are deprecated; a screen citing `#F5F0E6`, `#0F7A8C`, `#D9662E`, `#0A1628`, or `#3D7EFF` as if current is out of date.
- **Don't** introduce a second font family or a display/mono face — this system has deliberately shipped one typeface (DM Sans) at every weight since its inception.
- **Don't** add elevation/shadow to a card to indicate "active" or "selected" — this system always expresses that with a thicker/colored border instead (see `ModelCard`'s 2dp accent border).
- **Don't** hardcode a new bare hex color for a status/semantic use case — check whether `primary`, `secondary`, `accent`, `error`, or `success` already covers it (e.g. the existing hardcoded warning amber in `ModelCard.kt` is close enough to `accent` that it should just reuse that token) before adding anything new.
- **Don't** create a new screen-specific color file or object — every screen should draw from the one global token set documented here.

## Animation

- **Logo** (Home screen, onboarding Welcome, app startup loading state): a Canvas-based animation, running unconditionally the entire time it's composed (no trigger — always animating). Three overlapping sine-wave "sound wave" strokes at different amplitudes/speeds/directions — now mapped to `primary` / `secondary` / `accent` — plus orbiting particles in the same three colors, computed from a fixed radius and y-amplitude, scaled to the rendered icon size.
- **Recording waveform** (Recording screen, Test Recording): triggered by/visible only while `isRecording` is true. Volume input arrives from live mic energy and is eased toward with a fast-rise (×0.3 toward target when rising) / slow-decay (×0.85 toward target when falling) filter, floored at 0.1 so near-silence still shows a calm minimal shape. Shape itself continuously animates via `time` advancing at real elapsed-seconds × 0.9 speed regardless of volume.
- **Keyboard/transcribing waveform** (keyboard idle/recording, app's transcribing indicator, IME's transcribing screen): idle/recording mode smooths 30 discrete energy values via the shared driver (same 0.3 rise / 0.85 decay constants as the blob) each display frame. "Processing" mode (during transcription) instead drives a synthetic traveling sine wave, advancing its phase by real elapsed-time / 2.0 each frame.
- **Card press** (`ModelCard`): triggered on touch-down/up; scales 1.0 → 0.96 via `animateFloatAsState` with a spring (damping ratio 0.6, medium stiffness).
- **Model-card swipe-to-delete** (`ModelCard`): triggered by a horizontal drag gesture on a downloaded, deletable card; offset animates via a bouncier spring, snapping open/closed at the halfway point of the 80dp delete-button width on drag release.
- **Toggle knob:** triggered on checked-state change; knob position animates via `animateDpAsState` (default spring, no custom parameters set).

## Responsive Behavior

No tablet/foldable-specific layout branches, multi-pane layouts, or `WindowSizeClass` usage were found — the app targets a single phone form factor. Adaptive behavior that does exist: the IME's own reactive theme still follows the system dark/light setting (unaffected by this Mediterranean Coastal update, which targets the fixed-palette app screens only). Onboarding's two-spacer (`weight(1f)` above and below content) layout adapts to available vertical space by design rather than hardcoded margins. RTL is declared supported at the manifest level (`android:supportsRtl="true"`) but no explicit `LayoutDirection`-aware sizing/mirroring beyond default Compose RTL behavior was found in the reviewed screens.

## Branding

- **Wordmark:** "AVELISSE" — always rendered as a `Text` composable (no logotype image), typically in `primary` teal (Home screen, 28sp Bold) or `text-primary` (onboarding Welcome, 42sp ExtraLight with -0.5sp tracking) — two different weights/colors for the same wordmark depending on screen (see Known Inconsistencies).
- **Tagline:** shown at 13sp under the Home wordmark and again (17sp) on the onboarding Welcome screen ("Voice dictation, 100% offline" per the English string resource).
- **Logo:** the animated wave+particle mark is the *only* logo used in-app — no static logo image asset is referenced by any screen composable.
- **App icon:** the adaptive launcher icon's real colors (`#F7F7F7` background, `#007C92` teal foreground, `#D85C27` terracotta foreground) are, essentially, the Mediterranean Coastal palette already — `background`, `primary`, and `secondary` above were chosen to land almost exactly on these existing values. Adopting this palette in-app converges the UI with the icon rather than creating new drift.
- **Brand colors:** Sea Teal (`#007C92`, `primary`) and Terracotta (`#D85C27`, `secondary`) are the two colors that read as "AVELISSE brand" going forward, across marketing-adjacent surfaces (icon, logo, wordmark) and in-app.

## Design Guidance for Future Impeccable Work

- **Use the Mediterranean Coastal palette as the source of truth.** Every color decision should trace back to `primary`, `secondary`, `accent`, `background`, `surface`, `surface-variant`, `text-primary`, `text-secondary`, `border`, `error`, or `success` as defined above — not to the deprecated ivory/tan/navy values still sitting in `AvelisseColors.kt` until a future implementation pass updates them.
- **Reuse global color tokens.** Don't write a new `Color(0x...)` literal when an existing token already covers the role — this is exactly how the previous system accumulated hardcoded outliers (see Known Inconsistencies).
- **Do not introduce arbitrary colors.** If a new role is needed, derive it from the existing five anchor hues (a tint, a shade, an alpha variant) rather than picking an unrelated new hue — this keeps the "small, coherent palette" the reference image implies.
- **Do not recreate the old Dictus palette.** The ivory `#F5F0E6`/teal `#0F7A8C`/terracotta `#D9662E` system and the dark-navy `#0A1628`/`#3D7EFF` system are both deprecated. Neither should reappear in new work, including as a "muted" or "legacy" variant.
- **Avoid screen-specific color files when a global token already exists.** The previous system's "Home palette" / "Models palette" split by screen is exactly the pattern to avoid — one token set now serves every fixed-palette screen.
- **Maintain sufficient contrast and accessibility.** `text-primary` (`#071A1D`) and `text-secondary` (`#6E8A8F`) were both derived to sit comfortably on `background`/`surface`; if a new text color is ever needed, derive it the same way (a tint/shade of `primary` or a neutral) and verify contrast against whichever surface it sits on rather than assuming.
- **Use semantic color names rather than names tied to a specific screen.** `primary`/`surface-variant`/`text-secondary`, not `home-accent`/`models-surface`/`settings-chip` — the new token names are already screen-agnostic; keep any future additions that way too.
- **Preserve DM Sans as the only typeface** and the existing type scale — this update touches color only.
- **Keep Recording and Test Recording visually consistent** and **prefer the existing waveform components** over a new custom animation — both are unaffected by, and should simply inherit, the new palette.

## Known Inconsistencies

- **Screen/component:** IME module (keyboard, transcribing screen, emoji picker) vs. the app module's Recording/Test Recording/Home/Models/Settings screens.
  **Current behavior:** The IME still renders via its own separate, reactive dark theme, while the app screens now target the fixed Mediterranean Coastal palette documented here.
  **Why it appears inconsistent:** A user recording via the standalone app screen will see a warm coastal experience once this palette is implemented; the same conceptual state inside the keyboard overlay renders in an entirely different, unrelated color world.
  **Suggested future direction:** A deliberate, scoped decision on whether the keyboard should adopt the Mediterranean Coastal palette too, or whether a dark keyboard is an intentional, separate design decision — not something to silently converge as a side effect of other work.

- **Screen/component:** Hardcoded colors bypassing the token system (the bar waveform's outer-bar color, the bottom nav bar's background).
  **Current behavior:** Both currently bypass the app's color-token object with local hardcoded literals rather than referencing a named constant.
  **Why it appears inconsistent:** Both values happen to look correct today, but they bypass the token system that every other color on these same screens goes through, so a future palette adjustment could silently miss them.
  **Suggested future direction:** When `AvelisseColors.kt` is updated to the Mediterranean Coastal values, promote both to named constants (`surface` for the nav bar background, `text-secondary` for the outer bars) rather than carrying the hardcode forward.

- **Screen/component:** `ModelCard`'s English-only warning banner.
  **Current behavior:** Uses a hardcoded amber with no equivalent named token.
  **Why it appears inconsistent:** Every other semantic state has a named token; this one hardcoded color exists outside the system.
  **Suggested future direction:** The new `accent` token (`#FFB74D`, Sun Amber) is close in hue to the existing hardcoded warning color — reuse `accent` here directly rather than adding a separate Warning token.

- **Screen/component:** The toggle component's default on-track color vs. its actual call sites.
  **Current behavior:** The component's own code default is currently a green, but every located call site on a fixed-palette screen explicitly overrides it to the brand accent.
  **Why it appears inconsistent:** The component's own default doesn't match how it's actually used anywhere in the current UI.
  **Suggested future direction:** Make `primary` the component's real default, given green currently has no real caller — documented here as `toggle-track-on: {colors.primary}` in the frontmatter.

- **Screen/component:** "AVELISSE" wordmark rendering — Home screen vs. onboarding Welcome screen.
  **Current behavior:** Home renders it in `primary` teal at 28sp Bold; onboarding Welcome renders the same word in `text-primary` (near-black) at 42sp ExtraLight, both with -0.5sp tracking.
  **Why it appears inconsistent:** Two different weights and two different colors for the identical brand word, on two screens that otherwise share the same palette and the same animated logo above it.
  **Suggested future direction:** Confirm whether this is an intentional "loud teal on Home, quiet ink on Welcome" distinction or drift, before any future onboarding/Home polish pass touches either.

- **Screen/component:** `SettingsCard` (Settings screen) vs. the standard card/`ModelCard` (every other card in the system).
  **Current behavior:** `SettingsCard` uses a 3dp shadow with no border, while every other card-like surface in the system uses a 1dp border and no shadow. This is a structural inconsistency, unrelated to which palette is active.
  **Why it appears inconsistent:** Settings sits on the same token set as Licences, Debug Logs, and the Sound screens, all of which use bordered, shadowless cards — Settings' own grouping card is the only one that breaks that pattern.
  **Suggested future direction:** Decide whether `SettingsCard` should switch to the border convention for consistency, or whether the shadow is intentional and the other components should stay as-is.
