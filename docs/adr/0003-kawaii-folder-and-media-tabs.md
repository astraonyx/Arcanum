# 3. Kawaii Folder Companion, Dedicated Photo Tab, and Recursive Unzip Engine

Date: 2026-10-30

## Status
Accepted

## Context
User requested a shift to Mascot Model 3 ("Kawaii Folder Companion" inspired by Minji/CUDDY kawaii aesthetics), a restructured 4-tab bottom navigation including a dedicated Photo View tab without MT Manager clutter, an upgraded theater video player with gesture controls and folder-scoped queues, and automatic recursive unpacking of nested ZIP archives (ZIP inside ZIP).

## Decision
1. **Mascot Model 3 Adoption**: Adopt the animated Kawaii Folder Companion (golden squircle folder, expressive glossy anime eyes, blush cheeks, interactive blinks and leaps) as the primary system mascot across Onboarding, Vault Dashboard, Media Hub, and Security Gate.
2. **Dedicated Photo View & 5-Tab Bar**:
   - Establish a 5-tab bottom frosted squircle navigation: `[1. Brankas]`, `[2. Video Player]`, `[3. Galeri Foto]`, `[4. Mori]`, and `[5. Setelan]`.
   - Each tab strictly enforces vault boundary isolation (`vaultProfileId == activeVaultId`).
   - The **Settings Tab** is strictly zero-knowledge: opening one vault never exposes the PINs, secrets, or existence of other vaults.
3. **YouTube-Style Video Theater**:
   - Refactor the video viewer into a YouTube-style theater interface:
     - Top in-vault video search bar.
     - Double-tap on right: skip forward 5s (`+5s`) with animated YouTube ripple.
     - Double-tap on left: skip backward 5s (`-5s`) with animated YouTube ripple.
     - Hold-to-2x speed with `⚡ 2X CEPAT` badge.
     - Related/upcoming video cards strictly scoped to the active folder below the player.
4. **Auto Recursive ZIP Extractor with Entry-Count Bound**:
   - Introduce `RecursiveZipUnpacker` with streaming table scan.
   - Replaces arbitrary depth restrictions with an entry-count bound (up to 2,500 total folders/files) to safely unpack nested archives of arbitrary hierarchy without risking decompression bombs.

## Consequences
- Clean, focused media experience with photos separated from video/audio streams.
- Deep nested archives unpack seamlessly with zero manual effort.
- Mascot is friendly, expressive, and perfectly matches the pastel & kawaii design direction.
