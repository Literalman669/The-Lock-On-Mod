# Changelog

## 1.4.0

### Added

- Preset-first transactional configuration with seven focused pages.
- Adaptive Hybrid HUD components, target-history support, and accessibility options.
- A tubular glowing target ring with a remaining-health arc.
- Optional Shoulder Surfing Reloaded 2.9.x integration that preserves its shoulder camera during lock-on.
- Optional Epic Fight 2.2.8 compatibility, including simultaneous use with Shoulder Surfing Reloaded.

### Changed

- Reworked the target and camera flow around clearer client-side state.
- Updated the target HUD, ring, and panel to share the same health-state colors.
- Consolidated camera behavior behind a testable camera core and a thin Minecraft adapter.
- Removed Better Third Person support to keep camera, mouse, and movement ownership deterministic.
- Changed the default lock, cycle, and free-look keys to R, Z/X, and Left Alt.

### Fixed

- Mouse-bound targeting controls respond on the mouse press instead of waiting for a keyboard event.
- Development-client keybinding labels load their translations.
- The version shown by Forge matches the 1.4.0 build and mod metadata.
- Target aim point now favors a practical hit location instead of aiming above short mobs.
- Target-history capture is more consistent across target changes.
- Configuration navigation, control layout, and button rendering work consistently across the seven pages.
- Shoulder Surfing lock-on aligns aim to SSR's active attack ray while preserving its shoulder side and offset.
- Epic Fight Battle mode faces melee attacks toward the locked target while SSR keeps the shoulder camera; SSR's Adaptive crosshair changes to dynamic only for that lock.
- SSR's Adaptive crosshair no longer switches mode for ordinary SSR-only lock-on.
- Removed the second WASD rotation that could reverse or mix movement around close targets.
- Unlocked camera input no longer participates in a reconstructed compatibility yaw.
