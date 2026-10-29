# Specification: Arcanum Unification & Cosmic Dragon Redesign

## Problem Statement

Users managing private media and binary files on Android currently have to juggle fragmented applications: one app for encrypted vault storage, another utility (MT Manager) for dual-pane file management and APK/archive exploration, and another tool (Mori) for multi-platform media downloading. Furthermore, the existing YouTube-style media player within Arcanum suffers from broken playback synchronization (seeking controls do not update video frame positions, variable speed multipliers fail to apply, and track completion triggers no queue progression), while the mascot was previously an owl confined to an isolated onboarding flow rather than an interactive, expressive companion.

## Solution

Arcanum is unified into an integrated Android security, download, and file manipulation suite wrapped in a cohesive Cosmic Celestial theme (featuring stars, solar system orbits, and planetary aesthetics) and branded with the Neon Dragon Folder emblem. 

Key pillars of this solution include:
1. **Unified Navigation**: A 4-tab Cosmic Dock (`Vault Orbit`, `MT Explorer`, `Mori Downloader`, `Binary Scanner`), retiring extraneous habit/todo systems while making the **Cosmic Dragon** an omnipresent, touch-reactive quadruped companion with wings and shifting galaxy aurora.
2. **Repaired YouTube-Style Media Deck**: Reliable hardware-accelerated video/audio playback with true two-way seek synchronization, variable playback speed (`0.75x` – `2.0x`), automatic sibling ZIP track progression, and background leak prevention.
3. **Dual-Scope MT Explorer**: Full file system browsing across the entire device storage alongside an exclusive vault isolation mode, dual-pane side-by-side management, archive inspection, hex/text viewing, and one-tap NYX2 encryption bridging.
4. **Mori Ingestion Pipeline**: 16-platform watermark-free downloader connecting straight into NYX2 cryptographic vault storage.
5. **App Icon & Visual Overhaul**: Implementation of the custom Neon Dragon Folder icon across launcher mipmaps and deep obsidian galaxy UI styling.

## User Stories

1. As a privacy-focused user, I want Arcanum to serve as a single unified app for my media downloading, dual-pane file management, and encrypted vault needs, so that I do not need to install three separate fragmented tools.
2. As a user viewing hidden videos, I want the YouTube-style video player's scrubber bar to accurately seek forward and backward in real-time, so that I can jump to specific timestamps without the video freezing or ignoring my input.
3. As a user watching tutorial or study videos, I want to change playback speed between 0.75x, 1.0x, 1.25x, 1.5x, and 2.0x, so that I can consume media at my desired pace.
4. As a user playing video packages extracted from a `.zip` archive, I want the player to automatically queue and play the next sibling video when the current one finishes, so that I enjoy uninterrupted sequential playback.
5. As a user navigating through any screen of the app, I want an omnipresent 4-legged winged cosmic dragon companion visible in the interface, so that the app feels lively, charming, and personalized.
6. As an interactive user, I want to tap or poke the dragon mascot at any time to see it react with playful animations (such as barrel rolls, purring, or sneezing stardust), so that using the app is engaging and fun.
7. As a security-minded user, I want the cosmic dragon to fold its wings over its eyes while I type my secret PIN, so that entering my credentials feels delightfully private and guarded.
8. As a user entering an incorrect PIN, I want the dragon to snarl with cute celestial sparks and enter an alert posture, so that I receive immediate, intuitive visual feedback of an authentication failure.
9. As a user successfully unlocking Vault 1 or Vault 2, I want the dragon to celebrate with a triumphant leap and glowing starry wings, so that unlocking feels rewarding.
10. As a power user, I want the MT Explorer to let me browse the entire Android device storage (internal storage, SD card, Downloads, and DCIM), so that I can perform comprehensive file management on my phone.
11. As a security user, I want to toggle MT Explorer into an exclusive Vault Mode, so that I can inspect and organize solely the sandboxed `.nyx` encrypted containers without device clutter.
12. As a file manager user, I want a dual-pane side-by-side or split view in MT Explorer, so that I can easily drag, copy, or move files between external storage and the encrypted vault.
13. As an Android developer or enthusiast, I want to inspect `.apk`, `.zip`, and `.jar` archives inside MT Explorer to view manifests, dex classes summaries, and verify digital signatures, so that I can analyze application packages safely.
14. As a technical user, I want a built-in hex and text viewer in MT Explorer, so that I can inspect raw binary contents and scripts without exporting files to external third-party editors.
15. As a user browsing local files in MT Explorer, I want a 1-tap "Encrypt to Vault" action, so that I can securely encrypt any file using NYX2 dual-cipher directly from the file manager.
16. As a media collector, I want to paste links from TikTok, Instagram, YouTube, X, or 12 other platforms into the Mori Downloader, so that I can retrieve watermark-free video, audio, or image carousels.
17. As a mobile user, I want Mori Downloader to automatically detect links copied to my device clipboard upon opening or when shared via Android Share Sheet, so that I can initiate downloads in a single tap.
18. As a user downloading media from social platforms, I want an option to save downloaded files directly into Arcanum's encrypted NYX2 Vault, so that downloaded media never appears in public phone galleries.
19. As a user downloading media, I want the dragon mascot to animate into a "Download Hunter" state chasing a comet across the screen, so that I have whimsical visual feedback during active transfers.
20. As a user admiring the visual aesthetics, I want the app icon on my phone launcher to display the glowing Neon Dragon Folder over a starry cosmic orbit, so that the app looks distinct and high-end.
21. As a night-time user, I want the entire UI to embrace a deep Cosmic Celestial theme with nebula purple/cyan accents, subtle planetary orbit rings, and floating 3D orbital elements, so that the interface feels futuristic and visually stunning.
22. As a user who dislikes clutter, I want extraneous todo and habit tracker screens completely removed, so that the application remains focused purely on file management, encryption, media playback, and downloads.

## Implementation Decisions

### Architectural Seams & Navigation

The application navigation is streamlined from the previous experimental structure into a unified 4-dock orbital navigation model:
- `Vault Orbit`: Core NYX2 encrypted media vault, gallery views, and YouTube-style synchronized video/audio player.
- `MT Explorer`: Dual-mode, dual-pane file management suite (All-Device Storage + Exclusive Vault Mode), APK/DEX/ZIP inspection, and Hex/Text viewer.
- `Mori Downloader`: Universal media extraction engine for 16+ platforms with auto-clipboard capture and direct-to-vault cryptographic routing.
- `Binary Scanner`: Pre-encryption scanner for file integrity, MIME validation, and APK permission auditing.

The extraneous Todo and Habit tracker panes are removed entirely. The **Cosmic Dragon** is elevated from a tab-confined widget to an omnipresent ambient companion present across the header and interactive overlays of all screens.

### Omnipresent Cosmic Dragon Mascot System

- **Anatomy & Rendering**: Rendered via Compose Canvas with vector paths defining four distinct legs, dual articulate wings, head crest, and animated swishing tail.
- **Dynamic Shifting Aura**: GPU-driven gradient transitioning cyclically across Nebula Violet (`0xFF7C3AED`), Starlight Cyan (`0xFF06B6D4`), and Cosmic Magenta (`0xFFEC4899`) with twinkling star particle emitters.
- **State Machine**:
  - `IDLE_BREATHE`: Gentle sinusoidal vertical displacement and breathing scale modulation.
  - `TOUCH_REACTION`: 0ms latency touch handler triggering celebratory rolls, stardust sneezes, or wing flutter upon user tap.
  - `PIN_SHIELD`: Wings folded forward shielding eyes when entering security PIN.
  - `ALERT_FIRE`: Crest flare and celestial spark emission upon invalid PIN entry.
  - `SUCCESS_LEAP`: Upward translation with glowing wing expansion upon biometric or PIN unlock.
  - `DOWNLOAD_HUNTER`: Dynamic flight loop during active network downloads.
  - `MT_INSPECTING`: Cosmic magnifying glass overlay during binary or archive inspection.

### Repaired YouTube-Style Media Deck

- Replace static and unlinked `VideoView` calls with a tightly synchronized playback controller.
- Connect Compose `mediaState.currentSeconds` to `VideoView.seekTo(ms)` with debounce logic to prevent frame jitter during continuous scrubbing.
- Implement Android `PlaybackParams` support on the underlying player to enforce `0.75x`, `1.0x`, `1.25x`, `1.5x`, and `2.0x` speeds.
- Register `OnCompletionListener` to trigger queue advancement to sibling files extracted from the same `.zip` archive or the current vault category.
- Enforce full lifecycle cleanup in `DisposableEffect` to release video surfaces and hardware decoders upon navigation away.

### MT Explorer Subsystem

- **Scope Toggle**: Fast switcher between `DEVICE_ALL` (traversing `/storage/emulated/0`, external storage volumes, and user media folders) and `VAULT_EXCLUSIVE` (sandboxed inside `context.filesDir/nyx_vault_storage/`).
- **Dual-Pane View**: Split view allowing source-to-destination comparisons, drag-or-tap transfers, and batch copy/move/delete.
- **Archive & APK Inspector**: In-memory inspection of `.apk` and `.zip` tables, displaying `AndroidManifest.xml` entry metadata, dex entry counts, compression ratios, and certificate digest info.
- **Hex/Text Engine**: Read-only binary hex dump with ASCII column view for rapid byte inspection and plain text preview.
- **Cryptographic Bridge**: Direct invocation of `VaultRepository.encryptAndStoreFile()` from the MT file action bar.

### Visual Identity & App Icon

- Process user-uploaded image (`media_1793281867720_96e91fb7.jpg`) into launcher assets:
  - Standard launcher icon (`ic_launcher.png`) across mipmap densities (`hdpi`, `mdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`).
  - Round launcher icon (`ic_launcher_round.png`).
  - In-app brand header emblem and splash screen hero.
- Cosmic Galaxy Theme palette:
  - Deep Space Void: `#08090D`
  - Cosmic Obsidian Card: `#11131C`
  - Nebula Violet: `#7C3AED`
  - Starlight Cyan: `#06B6D4`
  - Celestial Amber: `#F59E0B`
  - Galaxy White: `#F8FAFC`

## Testing Decisions

- **Good Test Criteria**: Tests must verify user-observable behavior through the top-level `VaultViewModel` state flow and Compose UI interactions without coupling to internal private helper implementations.
- **Modules Tested**:
  1. `VaultViewModel`: Verifies state transitions across tabs, dual vault switching, MT file manager scope switching, Mori download lifecycle, and media playback state synchronization.
  2. `CosmicDragonState`: Verifies emotion state machine triggers (touch, PIN entry, download events, unlock success).
  3. `MTManagerEngine`: Verifies file system directory listing, archive parsing, and vault bridge encryption execution.
  4. `MoriDownloaderEngine`: Verifies URL domain matching, extraction parsing, and error handling.
  5. `MediaPlaybackController`: Verifies seeking, playback speed alterations, and playlist progression.
- **Prior Art**: Builds upon `ExampleRobolectricTest.kt` and `ExampleUnitTest.kt` present in `app/src/test/java/com/example/`.

## Out of Scope

- Implementing an in-app smali decompiler or full DEX bytecode editor (inspection, hex view, and APK manifest extraction are included; interactive bytecode rewriting is deferred to future releases).
- Third-party cloud sync or remote server storage (Arcanum operates strictly as a zero-cloud local vault).
- Habit tracking, gamified streaks, and todo lists (explicitly excluded per user requirement).

## Further Notes

All cryptographic operations remain 100% compliant with the existing NYX2 dual-cipher specification (`ChaCha20-Poly1305` + `AES-256-GCM` with 77-byte header and Padmé padding).
