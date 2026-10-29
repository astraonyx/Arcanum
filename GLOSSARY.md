# Arcanum Domain Model

Arcanum is an advanced Android security ecosystem combining a dual-cipher cryptographic vault, multi-platform media downloader, and dual-pane file/APK inspection suite with an interactive cosmic dragon companion.

## Language

### Core & Brand Identity

**Arcanum**:
The unified Android application uniting the cryptographic vault, MT file management, and Mori media downloader under a cosmic celestial design system.
_Avoid_: Trantor Vault, NyxApp, separate apps.

**Cosmic Dragon**:
The 4-legged winged celestial dragon mascot featuring shifting galaxy gradients and dynamic emotional states that guides and reacts to user actions throughout the app.
_Avoid_: Nyx Duo-Owl, owl mascot, bird.

**Neon Dragon Folder**:
The official application icon and brand insignia depicting a glowing celestial dragon spiraling around a smiling rounded file folder across a planetary orbit.
_Avoid_: generic lock icon, plain folder icon.

**Cosmic Celestial Theme**:
The visual language of Arcanum featuring deep space void backgrounds, planetary orbits, solar system accents, starfield particles, and iridescent glassmorphic bento cards.
_Avoid_: plain dark theme, flat monochrome theme.

### Architecture & Subsystems

**Dual-Vault**:
Two cryptographic storage containers (Primary Vault and Stealth Vault) isolated in internal app sandbox storage, accessible via independent PIN sets.
_Avoid_: single vault, shared storage.

**NYX2 Dual-Cipher**:
The hybrid cryptographic protocol encrypting binary streams using sequential ChaCha20-Poly1305 and AES-256-GCM with HKDF-SHA256 key derivation.
_Avoid_: single AES, plaintext storage.

**MT Engine**:
The dual-panel file management and reverse-engineering subsystem providing side-by-side directory navigation, APK/DEX/ARSC/ZIP inspection, and binary/hex viewing.
_Avoid_: MT Manager clone, separate APK.

**Mori Engine**:
The native and script-driven media extraction subsystem capable of scraping and downloading watermark-free media from 16+ social platforms into the vault.
_Avoid_: third-party downloader, external web view.

**Dragon Emotion State**:
The real-time behavioral animation mode of the Cosmic Dragon (e.g. Idle Breathing, Star Sneeze, Shielding Eyes, Cosmic Flame, Stellar Cheer) triggered by user interactions.
_Avoid_: static mascot, avatar sticker.
