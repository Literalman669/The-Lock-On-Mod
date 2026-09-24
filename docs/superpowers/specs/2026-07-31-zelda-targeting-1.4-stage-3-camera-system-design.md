# Zelda Targeting 1.4.0 Stage 3 Camera System Design

- Status: Approved for implementation planning
- Date: 2026-07-31
- Target: Minecraft 1.12.2, Forge 14.23.5.2859, Java 8
- Parent design: `docs/superpowers/specs/2026-07-31-zelda-targeting-1.4-overhaul-design.md`

## Summary

Stage 3 replaces the stateful Minecraft-coupled `CameraController` with a pure camera core, a thin vanilla runtime adapter, and one camera coordinator. The subsystem provides size-aware framing, frame-rate-independent rotation, profile-driven FOV and distance, eight-ray collision handling, close-camera transparency, automatic first-person fallback, temporary free look, and unified restoration.

The stage preserves the public 1.3.0 version and configuration schema. It supports vanilla first and third person only; Better Third Person and Shoulder Surfing Reloaded adapters remain deferred to Stage 5.

## Goals

1. Make camera framing, collision, free look, fallback, and restoration independently testable without Minecraft.
2. Prevent full camera spins at wrapped yaw boundaries and remove frame-rate-dependent smoothing.
3. Frame small and large targets continuously from snapshot size and distance measurements.
4. Keep third-person composition usable near walls and low ceilings.
5. Restore perspective, FOV contribution, distance, transparency, and adapter state through one idempotent path.
6. Keep the render hot path constant-time and free of avoidable collection allocation.

## Non-Goals

- Replacing the 1.3 configuration schema or configuration GUI.
- Implementing Better Third Person or Shoulder Surfing Reloaded integration.
- Modifying server-authoritative combat, reach, hit detection, or movement.
- Rewriting the HUD, feedback, damage numbers, or audio systems.
- Adding arbitrary per-entity camera rules or class-name checks.
- Restoring the player's acquisition-time yaw and pitch after lock-on ends.

## Architecture

### Pure Camera Core

The package `com.zeldatargeting.mod.client.camera.core` contains no Minecraft or Forge imports.

- `CameraProfile` is an immutable complete set of camera parameters.
- `CameraProfiles` supplies Cinematic, Balanced, and Snappy profiles and maps profile names case-insensitively.
- `CameraInput` is an immutable per-frame sample containing player rotation and position, target focus and size, target distance, perspective, elapsed time, free-look state, and collision-constrained distance.
- `CameraFrame` is an immutable output containing desired yaw, pitch, distance, FOV multiplier, player alpha, requested perspective, and whether tracking rotation should be applied.
- `CameraDirector` owns the current interpolated frame for one camera session. It never reads global state.
- `CameraCollisionSolver` selects a safe distance from exactly eight normalized ray samples.
- `PerspectiveController` is a pure session state machine that captures the original presentation state, applies fallback hysteresis, and emits normal or immediate restoration decisions.
- `CameraRestoreMode` distinguishes `EASED` normal release from `IMMEDIATE` lifecycle invalidation.

`CameraDirector` is the only mutable owner of interpolated yaw, pitch, distance, and FOV contribution. `PerspectiveController` is the only mutable owner of captured perspective and fallback state.

### Vanilla Runtime Layer

The package `com.zeldatargeting.mod.client.camera.vanilla` contains Minecraft-facing code.

- `VanillaCameraAdapter` samples interpolated player and target state, resolves the active target focus, performs eight world ray traces, and applies a `CameraFrame`.
- `VanillaCameraOffsetApplier` applies the collision-safe difference from vanilla's own camera distance during Forge's `EntityViewRenderEvent.CameraSetup` event.
- `CloseCameraTransparency` applies and restores local-player alpha during player render events.
- `LegacyCameraProfileAdapter` converts the existing 1.3 camera fields into a complete `CameraProfile` without changing the file schema.

Optional-mod types and reflective lookups for optional mods are forbidden in these packages.

### Coordinator and Integration

`CameraCoordinator` lives in `com.zeldatargeting.mod.client.camera`. It consumes the latest `LockOnSnapshot`, owns one director, perspective controller, and vanilla adapter, and exposes lock transition, client tick, render tick, and restoration entry points.

`TargetingManager` forwards snapshot transitions to the coordinator. It no longer owns prior perspective state and never applies camera rotation directly. The superseded `CameraController` is deleted after the coordinator is connected.

Stage 3 adds a normalized camera-profile name to `TargetingOptions` and `LockOnSnapshot`. `LockOnSession` captures that name at acquisition and preserves it through tracking and target switches. The targeting core stores only the normalized string (`cinematic`, `balanced`, or `snappy`) and does not import camera presentation classes. This completes the profile field deferred from the Stage 2 snapshot without reversing the dependency direction.

`KeyBindings` registers `cameraFreeLook` with Left Alt (`Keyboard.KEY_LMENU`) as the default. Minecraft's Controls screen remains the source of key remapping, so this does not alter the mod configuration schema.

## Data Flow and Timing

1. A targeting transition starts or ends the camera session on the client tick.
2. While a session is active, the client-tick path updates free-look state, collision samples, safe distance, fallback hysteresis, and lifecycle validity.
3. The render-tick path samples interpolated player and target positions using render partial ticks.
4. The adapter builds a `CameraInput` from the snapshot, sampled world state, resolved profile, and cached collision result.
5. `CameraDirector` computes one immutable `CameraFrame`.
6. The vanilla adapter applies rotation, dynamic FOV, perspective, and player alpha. During `CameraSetup`, the offset applier adds the collision-safe distance delta after vanilla has positioned its camera.
7. Release or lifecycle invalidation enters the same restoration path with the appropriate restore mode.

Collision ray tracing runs at client-tick cadence. Render ticks reuse the most recent safe distance and do not rescan the world.

## Camera Profiles

All three profiles are complete; callers never combine partial profiles.

| Parameter | Cinematic | Balanced | Snappy |
| --- | ---: | ---: | ---: |
| Rotation half-life | 180 ms | 100 ms | 45 ms |
| Size distance gain | 3.0 blocks | 2.0 blocks | 1.0 block |
| Maximum dynamic FOV | 12% | 6% | 0% |
| Maximum yaw adjustment | 60 degrees | 90 degrees | 120 degrees |
| Maximum pitch adjustment | 40 degrees | 60 degrees | 75 degrees |
| Release duration | 200 ms | 200 ms | 200 ms |

Every profile uses a 4.0-block base third-person distance, 1.25-block transparency start, 0.45-block transparency inner distance, 0.25 minimum player alpha, 0.65-block first-person fallback entry, and 1.25-block fallback exit.

`LegacyCameraProfileAdapter` selects a profile from the normalized name captured in the lock snapshot. Missing or unknown values resolve to Balanced. A profile changed through the legacy GUI applies to the next acquisition rather than mutating an active camera session. Existing `maxYawAdjustment`, `maxPitchAdjustment`, and `cameraFocusYOffset` remain advanced overrides. Existing `cameraSmoothness` remains an override when it differs by more than `0.001` from the selected preset's legacy default (`0.15`, `0.4`, or `0.75`). The override uses `halfLife = 35 + (1 - clamp(cameraSmoothness, 0, 1)) * 215` milliseconds. When `perModeSmoothingEnabled` is true in first person, the resulting half-life is multiplied by `5 / 3`. Existing camera enablement and automatic third person settings remain respected.

## Frame Calculation

### Rotation

Desired yaw and pitch use the selected snapshot anchor and Minecraft yaw conventions. Yaw interpolation always wraps the signed difference into `[-180, 180)`. Pitch remains clamped to `[-90, 90]` and each update respects the profile's maximum yaw and pitch adjustment.

Interpolation is frame-rate independent:

```text
factor = 1 - pow(0.5, elapsedMilliseconds / halfLifeMilliseconds)
next = current + wrappedDifference * factor
```

Elapsed time is clamped to `[0, 250]` milliseconds. Non-finite inputs do not replace the last valid frame.

### Size-Aware Distance and FOV

The normalized target-size factor is:

```text
size = max(targetWidth, targetHeight)
sizeFactor = clamp((size - 1.8) / 6.2, 0, 1)
desiredDistance = 4.0 + sizeFactor * profile.sizeDistanceGain
```

Dynamic FOV contribution is the profile maximum multiplied by `sizeFactor` and by the normalized target distance within the 20-block Stage 3 tracking baseline. The result is clamped from zero to the profile maximum. The adapter applies it as a multiplier over the current baseline FOV without permanently overwriting the user's FOV setting.

The resolved snapshot anchor remains the primary focus. `cameraFocusYOffset` applies a vertical offset relative to target height after anchor resolution.

## Collision and Close-Camera Behavior

The vanilla adapter casts eight rays from `+/-0.1`-block offsets around the player-to-camera segment. Every ray extends to `max(4.0, desiredDistance)` blocks so the same samples describe both vanilla's four-block camera and the desired profile distance. Each ray reports the absolute unobstructed hit distance, or the sample distance when unobstructed.

`CameraCollisionSolver` chooses the shortest hit distance. The desired safe distance is `clamp(shortest - 0.1, 0, desiredDistance)`. The estimated vanilla safe distance is `clamp(shortest, 0, 4.0)`. The resulting camera-setup delta is `desiredSafeDistance - vanillaSafeDistance`.

`VanillaCameraOffsetApplier` runs after vanilla's own eight-ray camera placement. For rear third person it translates camera-space Z by the negative delta; for front third person it uses the opposite sign. It performs no translation in first person. The custom solver therefore never extends the camera beyond its own collision-safe result, while avoiding bytecode transformation or a coremod.

Missing, negative, or non-finite samples are treated as fully blocked. The solver accepts exactly eight samples so missing rays cannot silently produce an unsafe distance.

Player alpha is piecewise linear:

- safe distance at or above 1.25 blocks: alpha `1.0`;
- safe distance at or below 0.45 blocks: alpha `0.25`;
- between the thresholds: linear interpolation from `0.25` to `1.0`.

Automatic first-person fallback enters after safe distance remains below 0.65 blocks for 100 milliseconds. It exits only after safe distance remains above 1.25 blocks for 200 milliseconds. Hysteresis timers reset when their respective condition stops being true.

Fallback is active only when automatic third person is enabled and the camera session changed the player's perspective. A player who intentionally remains in first person is never forced into third person by fallback recovery.

## Temporary Free Look

Holding Left Alt sets free look for the active camera session. During free look:

- the lock target and HUD remain unchanged;
- target acquisition, validation, and cycling continue normally;
- the director does not apply tracking yaw or pitch;
- collision distance, FOV, perspective safety, and transparency continue updating;
- no lock or switch feedback is replayed.

On release, the director seeds its current rotation from the player's actual free-look rotation and eases toward the current target using the active profile. This prevents snapping back to the rotation from before free look.

## Restoration

At camera-session start, `PerspectiveController` captures the player's perspective, baseline FOV, and whether the coordinator changed the perspective. Camera distance is an event-scoped contribution whose baseline is always a zero translation delta. Capture occurs once even if acquisition and switching transitions repeat.

Normal manual release, target death, target removal, range loss, and occlusion expiry use `EASED` restoration. FOV contribution, distance, and transparency return to baseline over 200 milliseconds. The previous perspective is restored after the eased presentation values reach baseline. The player's current yaw and pitch remain unchanged.

World loss, disconnect, dimension change, player death, missing player, missing target world, and configuration-screen entry use `IMMEDIATE` restoration. Perspective, FOV contribution, distance, transparency, and adapter state restore in the same call, then both state owners clear their sessions.

While any GUI is open, camera presentation remains suspended after immediate restoration even if the lock snapshot remains active. Closing the GUI starts a fresh camera session from the still-active snapshot and captures the then-current baseline settings. It does not reacquire the target or replay lock feedback.

Restoration is idempotent. Repeating either restore call after the session is clear performs no additional mutation.

## Failure Handling

- A missing player, target, or world invokes immediate restoration.
- A non-finite desired frame retains the last valid frame and logs no per-frame noise.
- Invalid collision samples reduce the safe distance instead of extending it.
- If camera-setup translation throws, the adapter logs one warning, disables custom distance translation for the current world session, and continues with rotation, FOV, perspective, and Minecraft's native four-block collision behavior.
- Applying a camera frame is contained at the adapter boundary. Any other runtime application failure logs once, immediately restores what can be restored, and disables further custom camera application until the next world session.
- Player transparency always resets to `1.0` on restoration and render-post cleanup.

## Performance Rules

- No candidate scans or entity-list construction occur in the camera subsystem.
- Collision uses a fixed eight-sample container; it does not allocate a collection per tick.
- `CameraDirector` performs scalar math only and allocates no collection per frame.
- Forge camera, FOV, and player-render event handlers are registered once with the coordinator.
- Compatibility logging is transition-driven or warn-once, never per tick or per frame.

## Automated Verification

Pure JUnit tests cover:

- profile completeness, case-insensitive selection, and Balanced fallback;
- frame-rate-independent interpolation at multiple update cadences;
- shortest-path yaw wrapping and pitch bounds;
- target-size distance scaling and profile caps;
- dynamic FOV bounds and Snappy's zero contribution;
- exact eight-ray minimum selection, safety margin, and invalid samples;
- transparency threshold endpoints and interpolation;
- first-person fallback entry and exit hysteresis;
- free-look hold behavior and reseeded return;
- one-time capture and idempotent normal and immediate restoration;
- normal release leaving yaw and pitch unchanged;
- lifecycle restoration clearing all retained camera-session state;
- legacy profile selection and advanced-value overrides.

Minecraft-facing verification includes `compileJava`, `compileTestJava`, the full test suite, `reobfJar`, packaged-version inspection, and a focused vanilla client smoke test.

## Manual Acceptance Matrix

The focused Stage 3 smoke test covers:

1. Cinematic, Balanced, and Snappy tracking against the same target.
2. Vanilla first person and vanilla third person.
3. Left Alt free look and smooth return without feedback replay.
4. Small, tall, wide, moving, and nearby targets.
5. Open space, a wall, a low ceiling, a narrow corridor, transparency range, and automatic first-person fallback with recovery.
6. Manual unlock, target death, sustained occlusion release, player death, and world exit while locked.
7. World re-entry with no stale perspective, FOV, distance, transparency, or target state.

The tester reports each numbered result. The implementation task records the log line or timestamp baseline before the run and audits all appended lines for camera errors, exceptions, normal world unloading, and client shutdown.

## Stage 3 Completion Criteria

Stage 3 is complete only when:

- the pure camera core contains no Minecraft or Forge imports;
- one director owns interpolated frame state and one perspective controller owns capture and fallback state;
- `TargetingManager` no longer stores perspective or calls the legacy controller;
- the legacy `CameraController` is deleted;
- all profile, collision, free-look, fallback, transparency, and restoration tests pass;
- normal release never snaps back to acquisition-time yaw or pitch;
- all lifecycle invalidation paths immediately restore presentation state;
- the vanilla manual acceptance matrix passes without new camera exceptions;
- the clean Forge build and reobfuscation succeed;
- packaged metadata remains version 1.3.0; and
- the Stage 3 branch is clean.
