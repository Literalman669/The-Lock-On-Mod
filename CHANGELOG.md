# Changelog

## 1.4.1

### Added

- Switch Target on Kill (Targeting page, on by default): when the locked target dies, the lock moves to the next eligible target instead of ending.
- Cycle Aim Point key (V by default) moves the lock between the target's head, body, and feet.
- Pressing lock with nothing to target now plays a soft click and, with Level View Without Target (Camera page, on by default), eases the view back to the horizon. Pressing lock while riding explains why nothing happened.
- Only My Hits (Damage Numbers) shows numbers only for your own melee hits.
- Compatibility Debug Output now also logs where unexpected view turning comes from (`[Lock On Mouse]` lines in the log), and each swing during a lock with its aim, distance, speeds, and whether it hit (`[Lock On Aim]` lines).
- Target ring effects: a flash on your critical hits, a shockwave when the target drops to low health, and a burst where it dies. Reduced Motion keeps only the color flash.
- Entity blacklist, Disable While Riding, and Match Range to Reach, carried over from 1.3.1.
- Reticle Color for the ring's corner brackets.

### Changed

- The mod is now called Lock On. Its settings, key bindings, and config file are unchanged; delete the old zelda-targeting JAR when updating.
- The config screen has eight pages with tabs, plain-language labels, sliders with units, hover help, and options that grey out when they do not apply. Tuning and debug options moved to an Advanced page.

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
- With Shoulder Surfing Reloaded, lock-on no longer swings the player sideways or backwards when very close to a target; the shoulder aim correction now tops out at 25 degrees.
- Lock-on no longer spins the camera when the target is almost directly below or above the player.
- Shoulder Surfing aim correction now follows SSR's camera smoothly while it moves instead of trailing a tick behind.
- With Shoulder Surfing Reloaded, the player model is no longer faded by Lock On's own camera check; SSR handles it.
- A Targeting Range above Max Tracking Distance no longer locks and instantly releases distant targets.
- Match Range to Reach now measures to the target's hitbox like vanilla reach, so large mobs in melee range can be locked.
- Pressing lock during the short fade after a kill or unlock now starts a new lock instead of doing nothing.
- The lock returns to the target's center after briefly aiming at its head while its body was hidden.
- The Soft Aim Indicator now points toward the target from the player's view.
- Reset in the config screen now restores only the current page, as documented.
- An old or unreadable config no longer creates a new backup file on every launch.
- The cinematic sound theme can now be selected.
- Arrow and other projectile damage no longer shows as melee damage in the target panel.
- Lock-on and camera timing use a steady high-resolution clock, avoiding stutter from coarse system timers.
- Lock-on sounds use the same steady clock, so a system clock change can no longer mute them.
- The kill burst still plays when Switch Target on Kill moves the lock in the same moment the target dies.
- Damage estimates for the target panel and ring are computed once per game tick instead of several times per frame.
- With Shoulder Surfing Reloaded's Adaptive crosshair and a wide shoulder offset, close targets can be hit again outside Epic Fight's Battle mode. The lock now uses SSR's eye-based crosshair in every mode; SSR's shoulder ray could not reach targets nearer than its sideways offset.

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
