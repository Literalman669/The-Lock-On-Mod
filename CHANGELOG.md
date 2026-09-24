# Changelog

## 1.4.0

### Added

- Preset-first transactional configuration with seven focused pages.
- Adaptive Hybrid HUD components, target-history support, and accessibility options.
- A tubular glowing target ring with a remaining-health arc.
- Optional Shoulder Surfing Reloaded 2.9.x integration with reversible runtime lock centering.

### Changed

- Reworked the target and camera flow around clearer client-side state.
- Updated the target HUD, ring, and panel to share the same health-state colors.
- Consolidated camera behavior behind a testable camera core and a thin Minecraft adapter.
- Removed Better Third Person support to keep camera, mouse, and movement ownership deterministic.
- Changed the default lock, cycle, and free-look keys to R, Z/X, and Left Alt.

### Fixed

- Target aim point now favors a practical hit location instead of aiming above short mobs.
- Target-history capture is more consistent across target changes.
- Configuration navigation, control layout, and button rendering work consistently across the seven pages.
- Shoulder Surfing lock-on temporarily centers the runtime camera and restores the exact shoulder offset on unlock.
- Removed forced SSR adaptive-crosshair activation, which could displace the crosshair and return null hit results.
- Removed the second WASD rotation that could reverse or mix movement around close targets.
- Unlocked camera input no longer participates in a reconstructed compatibility yaw.
