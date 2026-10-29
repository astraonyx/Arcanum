# 05: MT Dual-Pane Explorer & Vault Bridge

**What to build:**
Deliver MT Explorer featuring a dual-pane file manager layout, scope switcher between `DEVICE_ALL` (full phone filesystem access) and `VAULT_EXCLUSIVE` (sandboxed `.nyx` vault storage), archive & APK inspection (view APK manifest, dex class counts, zip entry tree), built-in Hex/Text viewer, and a 1-tap "Encrypt to Vault" cryptographic bridge.

**Blocked by:** 01-neon-dragon-icon-and-cosmic-theme-tokens, 02-omnipresent-cosmic-dragon-mascot

**Status:** ready-for-agent

- [ ] Implement dual-pane directory explorer supporting external storage and vault directories
- [ ] Add scope switcher toggle (`DEVICE_ALL` vs `VAULT_EXCLUSIVE`)
- [ ] Build archive/APK inspector extracting manifest info and entry hierarchy
- [ ] Build raw Hex/Text viewer modal for instant binary byte inspection
- [ ] Wire 1-tap NYX2 encryption bridge from MT Explorer toolbar directly into VaultRepository
