# Arcanum Design System (DESIGN.md)
*Apple Minimalist Pastel & Astrodracon Cosmic Design Language*

## 1. Design Philosophy
Arcanum blends **Apple Human Interface Guidelines (HIG) Minimalism** with **Astrodracon Cosmic Space Aesthetics**.
- **Clarity & Deference**: Content is king. Vault files, videos, audios, and photos are elevated with generous negative space, soft continuous squircle radiuses (24dp-28dp), and frosted glass blur.
- **Strict Isolation**: Zero data leaks between Vault profiles (`vaultProfileId`). Files and playlists belonging to one vault never cross boundaries into another.
- **Delightful Companion**: Astrodracon (Guardian of the Cosmos) guides the user with smooth 60fps micro-animations, expressive gaze shifts, and instant haptic/visual responsiveness.

---

## 2. Color System & Design Tokens

### Backgrounds & Surfaces (Apple Dark Glassmorphism)
- **`CosmicVoid`**: `#050814` — Deep infinite space background for main scaffolding.
- **`CosmicSurface`**: `#0E1428` — Elevated surface container for cards and modals.
- **`CosmicGlass`**: `rgba(22, 30, 58, 0.72)` — Translucent frosted card container with 1dp border `rgba(255, 255, 255, 0.08)`.
- **`CosmicPillDock`**: `rgba(15, 23, 42, 0.88)` — Floating docked bottom navigation bar with 50% border squircle.

### Soft Pastel Accent Palette (Inspired by Reference Visuals)
- **`PastelSky`**: `#7DD3FC` — Primary interactive accent, video playback active indicators.
- **`PastelLavender`**: `#C4B5FD` — Audio stream and music player badge accent.
- **`PastelPeach`**: `#FED7AA` — Playlist folders and archive badges.
- **`PastelMint`**: `#86EFAC` — Cryptographic security verified, AES-256 status.
- **`PastelRose`**: `#FCA5A5` — Encryption locks, destructive warnings, PIN auth.
- **`AstrodraconSapphire`**: `#3B82F6` — Astrodracon cosmic dragon signature aura and horns.

---

## 3. Typography Hierarchy
- **Display**: System Rounded / SF Pro Display Bold, 26sp, tracking -0.5sp.
- **Headline**: System Rounded Medium, 20sp, tracking -0.2sp.
- **Title Large**: System Bold, 17sp, line height 22sp.
- **Body Regular**: System Regular, 14sp, line height 20sp, color `rgba(255, 255, 255, 0.85)`.
- **Label / Tag**: System SemiBold, 12sp, uppercase tracking 0.8sp.
- **Mono / Cryptographic**: JetBrains Mono Bold, 12sp-14sp, letter spacing 1.5sp.

---

## 4. Component Standards

### Squircle Cards
- Border radius: `24.dp` to `28.dp`.
- Padding: `16.dp` horizontal, `14.dp` vertical.
- Border: `1.dp` solid `rgba(255, 255, 255, 0.08)` or subtle pastel highlight.

### Media Player & Scoped Playlist
- **Aspect Ratio**: 16:9 docked video stage with 20dp rounded viewport corners.
- **Hold-to-2x Gesture**: Press & hold on video accelerates speed to `2.0x` and displays `2X CEPAT` floating pill HUD.
- **Playback Modes**: Loop 1 (`Icons.Default.RepeatOne`), Auto-Advance in Playlist (`Icons.Default.SkipNext`), Repeat All (`Icons.Default.Repeat`).
- **Scoped Queue**: The Up Next queue only renders media inside the active playlist/folder.

### Floating Pill Dock
- Height: `64.dp`, horizontal margins: `20.dp`, bottom margin: `16.dp`.
- 4 Streamlined Tabs:
  1. **Brankas** (`Icons.Default.Lock` / `Shield`)
  2. **Mori Grabber** (`Icons.Default.Download` / `CloudDownload`)
  3. **Media Player & Galeri** (`Icons.Default.PlayCircle` / `PhotoLibrary`)
  4. **Pengaturan & Enkripsi** (`Icons.Default.Settings` / `Key`)

---

## 5. Astrodracon Mascot Guidelines
- Master Vector Source: `D:\Users\Astraonyx\Arcanum\mascot_orbit_duo.svg` and `mascot_kawaii_folder.svg`.
- Vector Paradigm: Zero raster PNG dependency. 100% scalable vector geometry with semantic groups for animation hooks.
- Semantic Group Hierarchy:
  - `#layer-ambient-shadow`: Dynamic floor ellipse scale & blur filter.
  - `#layer-orbit-back` & `#layer-orbit-front`: 3D tilted orbital rings with neon cyan-indigo gradient.
  - `#layer-folder`: Kawaii squircle container, anime eyes (`#folder-eye-left`, `#folder-eye-right`), cheeks (`#folder-blush-*`), mouth (`#folder-mouth`).
  - `#layer-dragon-front`: Sinuous sweeping dragon body, crystal wings (`#dragon-wing-front`), crest plume (`#dragon-crest`), and starlight eye (`#dragon-eye-iris`).
  - `#layer-stardust`: Twinkling 4-point stars (`#star-1`, `#star-2`, `#star-3`) and floating cosmic dust.

---

## 6. GSAP Timeline Animation Choreography (gsap-timeline Standard)
All mascot micro-interactions are driven by sequenced GSAP Timelines with position parameters, labels, and defaults:

### 1. Master Idle Breathing Timeline (`tlIdle`)
- **Defaults**: `{ ease: "sine.inOut" }`, continuous loop (`repeat: -1`).
- **Dragon Float**: `y: -14`, duration 2.2s, yoyo.
- **Wing Flap**: `rotation: 6`, `scale: 1.04`, duration 1.5s, `transformOrigin: "bottom left"`, relative offset `"<0.2"`.
- **Crest Sway**: `rotation: -4`, `scaleY: 1.05`, duration 1.8s, `transformOrigin: "bottom center"`.
- **Kawaii Counter-Bob**: `y: -6`, `scale: 1.015`, duration 2.0s, relative offset `"<0.3"`.
- **Eye Blink Label**: `tl.addLabel("blink", 1.8)` -> `scaleY: 0.1`, duration 0.12s, yoyo, repeat 1.
- **Stardust Twinkle**: Staggered diamond star rotations (`rotation: 45`, `scale: 1.35`).

### 2. Celebrate / Leap Timeline (`tlCheer`)
- **Squash & Prep**: `scaleX: 1.15`, `scaleY: 0.85`, `y: 10`, duration 0.15s, ease `"power2.in"`.
- **Leap Peak**: `y: -40`, `scaleX: 0.9`, `scaleY: 1.15`, duration 0.35s, ease `"back.out(2)"`.
- **Dragon Wings Expand**: `rotation: 18`, `scale: 1.15`, aligned with leap `"<"`.
- **Elastic Landing**: `y: 0`, `scale: 1.0`, duration 0.5s, ease `"elastic.out(1, 0.4)"`.

### 3. Vault Security Guard Timeline (`tlGuard`)
- **Defensive Wrap**: Dragon lowers horns `rotation: -8`, wraps tightly around folder `y: 15`, `scale: 1.05`.
- **Laser Glint**: Dragon eye iris shifts to `#00F0FF`, `scale: 1.4`.
- **Forcefield Pulse**: Orbit ring stroke width thickens to `18px`, glow intensifies.

### 4. Stealth Snooze Timeline (`tlSleep`)
- **Eyelids Close**: `#folder-eye-*` and `#dragon-eye-*` `scaleY: 0.05`, duration 0.4s.
- **Gentle Settle**: Mascot rests downwards `y: 8`, shadow dims `opacity: 0.15`.
