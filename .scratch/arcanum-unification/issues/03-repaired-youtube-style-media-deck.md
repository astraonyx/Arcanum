# 03: Repaired YouTube-Style Media Deck

**What to build:**
Repair the YouTube-style video and media deck in `VaultMediaPlayerScreen.kt` by replacing static playback with two-way synchronized `seekTo` handling, Android `PlaybackParams` speed multiplier support (`0.75x` to `2.0x`), sequential auto-play progression for same-ZIP/vault packages via `OnCompletionListener`, and clean `DisposableEffect` decoder release.

**Blocked by:** 01-neon-dragon-icon-and-cosmic-theme-tokens

**Status:** completed

- [x] Connect slider scrub and 10s rewind/forward buttons to `VideoView.seekTo(ms)` with debounce
- [x] Implement true playback speed configuration (`PlaybackParams`) on underlying player
- [x] Add `setOnCompletionListener` to auto-play next video in current ZIP/vault queue
- [x] Ensure clean hardware release in `DisposableEffect` to avoid black screen and decoder leaks
- [x] Verify below-video ZIP playlist selection immediately switches active video
