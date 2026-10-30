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

**Multi-Vault Dynamic Registry**:
An extensible vault registry supporting unlimited independent cryptographic vaults (Vault 1, Vault 2, Vault 3, etc.), each isolated with its own 3-layer PIN and storage sandbox.
_Avoid_: fixed dual-vault, hardcoded two-vault limit.

**Stealth Dashboard**:
The neutral, unlabelled dashboard presentation that conceals all vault identifiers (numbers, profiles, and badges) to prevent observation of which vault is unlocked.
_Avoid_: vault indicator, profile badge in header.

**Cosmic Master Recovery Key**:
A cryptographic 12-word mnemonic seed generated at setup, required to authorize any PIN reset without trivial bypass or biometric shortcuts.
_Avoid_: simple security question, biometric bypass, instant pin reveal.

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

**Astrodracon (Guardian of the Cosmos)**:
The official brand guardian dragon characterized by deep cosmic midnight body, sapphire-indigo crystal wings, celestial horns, planetary orbits, and modular expression states defined in the master asset sheet.
_Avoid_: generic cartoon dragon, bird, owl.

**Folder-Scoped Playlist**:
A dynamic playback queue strictly confined to the currently selected folder or ZIP archive package, preventing files in other folders from leaking into "Up Next" or auto-advancing.
_Avoid_: global monolithic playlist, unpartitioned media queue.

**Hold-to-2x Speed**:
A touch-and-hold video gesture that dynamically increases playback speed to 2.0x while pressed, reverting immediately to 1.0x upon release with an on-screen HUD badge.
_Avoid_: fixed menu-only speed toggles.

**Apple Minimalist Glassmorphism**:
The elevated aesthetic featuring 24dp-28dp squircle cards, floating frosted pill docks, soft pastel badges, and restraint typography inspired by Apple HIG and clean pastel fintech decks.
_Avoid_: harsh borders, cluttered multi-colored boxes.
