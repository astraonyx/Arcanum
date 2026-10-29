# Synchronized Media Playback Engine with Seek and PlaybackParams

To resolve desynchronization in the YouTube-style media deck, the player couples Compose state directly with Android MediaPlayer and VideoView controllers, binding seek events, variable playback speeds (0.75x–2.0x), automatic queue advancement, and proper background resource release.
