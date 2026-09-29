# Changelog

## Unreleased

### Added

- Target ring effects: a flash on your critical hits, a shockwave when the target drops to low health, and a burst where it dies. Reduced Motion keeps only the color flash.
- Entity blacklist, Disable While Riding, and Match Range to Reach, carried over from 1.3.1.
- Reticle Color for the ring's corner brackets.

### Fixed

- These settings now take effect: Enable Target Ring, Ring Thickness, Show Ring Health Arc, HUD Scale, Damage Prediction Scale, Target Check Interval (updateFrequency), and Filter Check Interval (validationInterval).
- Critical hits and kills on the locked target are detected even when damage numbers are turned off.
- Damage estimates now follow vanilla 1.12.2 math for attack charge, enchantment bonus, and critical hits.
- Damage and hits-to-kill readouts now appear when locked onto players.
- The real-hit damage cache is now thread-safe between the integrated server and the HUD.
- Armor stands and invisible entities can no longer be locked onto, and a lock releases when its target turns invisible.
- Damage numbers now appear on multiplayer servers, not just in singleplayer.
- Damage numbers mark only real critical hits from your own attacks instead of random hits.
- Damage number animation now runs at the same speed at any frame rate, and pauses with the game.
- Reduced Motion now also stops critical damage numbers from pulsing and flashing.
- Target history rings no longer linger for mobs that unloaded or were left behind in another world.
- Color codes in the config screen no longer risk showing a stray "Â" on Windows builds.

## 1.4.0

### Added

- Preset-first transactional configuration with seven focused pages.
- Adaptive Hybrid HUD components, target-history support, and accessibility options.
- A tubular glowing target ring with a remaining-health arc.
- An Adventure Crest HUD with a compact regular-target plaque and a separate boss banner.
- Optional Shoulder Surfing Reloaded 2.9.x integration that preserves its shoulder camera during lock-on.
- Optional Epic Fight 2.2.8 compatibility, including simultaneous use with Shoulder Surfing Reloaded.

### Changed

- Reworked the target and camera flow around clearer client-side state.
- Updated the target HUD, ring, and panel to share the same health-state colors.
- Enabled the boss-style panel by default for new settings and moved it to the top-center safe area.
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
- The locked vanilla boss's built-in health bar no longer duplicates the Adventure Crest boss banner; it returns on unlock.
- Shoulder Surfing lock-on aligns aim to SSR's active attack ray while preserving its shoulder side and offset.
- Epic Fight Battle mode faces melee attacks toward the locked target while SSR keeps the shoulder camera; SSR's Adaptive crosshair changes to dynamic only for that lock.
- SSR's Adaptive crosshair no longer switches mode for ordinary SSR-only lock-on.
- Removed the second WASD rotation that could reverse or mix movement around close targets.
- Unlocked camera input no longer participates in a reconstructed compatibility yaw.
