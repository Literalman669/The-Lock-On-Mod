# Zelda Targeting 1.4.0 Overhaul Design

- Status: Approved for implementation planning
- Date: 2026-07-31
- Target: Minecraft 1.12.2, Forge 14.23.5.2859+, Java 8

## Summary

Version 1.4.0 is a staged subsystem rewrite of Zelda Targeting. It retains the mod's standalone, client-only identity while replacing the oversized GUI, renderer, and configuration classes with isolated components. It also adds a more reliable lock-on state model, adaptive camera framing, an Adaptive Hybrid HUD, curated configuration presets, accessibility controls, and optional camera-mod adapters.

The rewrite will be delivered as one public 1.4.0 release, but development is divided into buildable stages. Every stage must compile, reobfuscate, and pass its relevant tests before the next stage begins.

Better Lock-On was reviewed as a behavioral reference for camera framing, collision handling, close-camera behavior, scalable reticles, and target-anchor concepts. Its source and assets will not be copied. Zelda Targeting's implementation, assets, naming, and architecture will remain original.

## Product Decisions

- The mod remains a standalone lock-on system. No combat framework is required.
- The mod remains client-only and can connect to an unmodified multiplayer server.
- Optional compatibility is isolated behind runtime adapters.
- Vanilla first person, vanilla third person, Better Third Person, and Shoulder Surfing Reloaded are the guaranteed 1.4.0 camera environments.
- Version 1.4.0 replaces the 1.3 configuration schema instead of migrating individual settings.
- An existing 1.3 configuration is retained as a backup before the new schema is written.
- The default visual direction is the Adaptive Hybrid HUD.
- Cinematic, Balanced, and Snappy are the curated experience presets. The player must select a preset on first run; Balanced supplies safe in-memory behavior until a selection is saved.
- The rewrite proceeds subsystem by subsystem instead of replacing the entire mod in one change.

## Goals

1. Eliminate stuck locks, stale entity references, target-cycle instability, and abrupt camera restoration.
2. Make targeting, camera, HUD, feedback, configuration, and compatibility independently understandable and testable.
3. Improve large-target framing, close-camera behavior, collision handling, and transition quality.
4. Present combat information through a scalable target ring, compact detail panel, and conditional boss panel.
5. Preserve client-only multiplayer compatibility and avoid any server-side combat advantage.
6. Provide a clean extension point for later camera or combat-mod adapters.
7. Keep per-frame camera and rendering work constant-time and free of avoidable collection allocation.

## Non-Goals

- Porting to a newer Minecraft version or loader.
- Requiring Epic Fight, Better Combat, or another combat framework.
- Changing server reach, hit detection, damage, entity tracking, or authoritative combat state.
- Copying Better Lock-On source code, assets, translations, or branding.
- Supporting every Minecraft 1.12.2 camera or combat mod in the initial 1.4.0 release.
- Preserving the exact 1.3 configuration layout or automatically mapping its values.

## Architecture

### Dependency Direction

The targeting core owns lock state and publishes a read-only snapshot. Camera, HUD, and feedback consume the snapshot but do not call into one another.

```text
Input + world observations
           |
           v
    TargetingService
           |
           v
      LockOnSession -----> LockOnSnapshot
                              |    |    |
                              v    v    v
                           Camera HUD Feedback

Config presets ----------------^----^----^
Camera adapter registry --------^         
```

Compatibility modules depend on the camera adapter contract. The core, HUD, feedback, and configuration model do not import optional-mod classes.

### Core Targeting Components

- `LockOnSession` is the only mutable owner of the active lock lifecycle.
- `LockOnSnapshot` is the read-only state published to camera, HUD, and feedback consumers.
- `LockPhase` defines `IDLE`, `ACQUIRING`, `LOCKED`, `OCCLUDED_GRACE`, `SWITCHING`, and `RELEASING`.
- `TargetingService` coordinates acquisition, validation, cycling, release, and quick-switch behavior.
- `TargetCandidate` contains the normalized measurements used to compare an entity without embedding selection policy in the entity scan.
- `TargetScorer` implements the Nearest, Health, Threat, and Angle policies with deterministic tie-breaking.
- `TargetAnchorResolver` selects head, center, or lower-body focus positions from vanilla eye position and bounding-box data.
- `TargetHistory` prevents immediate bounce-back during rapid cycling and supplies the optional recent-target presentation.

### Client Presentation Components

- `CameraDirector` converts a snapshot and camera profile into a desired camera frame.
- `CameraCollisionSolver` shortens the desired offset using eight-ray collision sampling.
- `PerspectiveController` remembers and restores the player's prior perspective and FOV.
- `HudCoordinator` decides which HUD layers are visible for the current snapshot.
- `TargetRingRenderer`, `TargetDetailPanel`, and `BossTargetPanel` render independent Adaptive Hybrid layers.
- `DamageNumberRenderer` owns damage-number lifetime and motion only.
- `FeedbackDirector` converts session transitions into sound and emphasis events exactly once.
- `ReticleThemeRegistry` supplies the original Classic Ring, Segmented, and Tactical themes.

### Configuration Components

- `ClientConfigModel` is the validated in-memory configuration.
- `ConfigPreset` supplies complete Cinematic, Balanced, and Snappy configurations.
- `ConfigRepository` loads, validates, snapshots, saves, and backs up configuration files.
- `ConfigSession` provides transactional Save, Cancel, Reset, and live-preview behavior.
- Separate GUI page components own Presets, Targeting, Camera, HUD, Audio, Compatibility, and Accessibility controls.

### Compatibility Components

- `CameraAdapter` defines availability, activation, desired-offset adjustment, lock-start, lock-end, and failure-reporting behavior.
- `CameraAdapterRegistry` chooses the adapter matching the camera implementation active at runtime.
- `VanillaCameraAdapter` is always available.
- Better Third Person and Shoulder Surfing Reloaded adapters load only after their mod IDs are detected.
- Optional-mod types are isolated inside their adapter packages and are never referenced by core class signatures.

## Lock-On State and Data Flow

### Snapshot Contents

Every published snapshot contains:

- lock phase and transition timing;
- target entity ID and an ephemeral current-world entity reference;
- target name, current health, maximum health, and alive state;
- target distance, screen bearing, visibility, and occlusion duration;
- selected target anchor and resolved world-space focus position;
- target size measurements from its bounding box;
- predicted damage, prediction confidence, and hits-to-kill when enabled;
- active target-priority policy and camera profile;
- reason for the most recent transition or release.

The ephemeral entity reference is discarded immediately when the world changes. No snapshot survives a disconnect, dimension transition, player death, or invalid world reference.

### Acquisition

1. A lock input requests acquisition when the session is idle.
2. The detector collects living entities within the acquisition range.
3. Type filters, alive state, spectator state, detection angle, and optional line of sight remove invalid candidates. Eligibility is based on `EntityLivingBase`, so enabled players and modded living entities are not accidentally excluded by an `EntityLiving`-only check.
4. `TargetCandidate` measurements are calculated once.
5. The selected scoring policy ranks candidates. Entity ID is the final tie-breaker, producing deterministic results.
6. The winner becomes the active target and the session publishes an `ACQUIRING` transition followed by `LOCKED`.

The Balanced baseline uses the existing 16-block acquisition range, 20-block tracking distance, 60-degree detection angle, and required line of sight. The configuration GUI continues to expose validated range and angle controls.

The scoring policies are defined as follows:

- Nearest sorts by squared distance, then view angle, then entity ID.
- Health sorts by current health ascending, then squared distance, then entity ID.
- Angle sorts by three-dimensional angle from the player's look vector, then squared distance, then entity ID.
- Threat uses only generic client-visible data: hostile marker, whether an `EntityLiving` currently targets the player, attack-damage attribute, current health, and distance. It does not inspect class-name strings or maintain hard-coded mob lists. Equal threat scores use view angle, then entity ID.
- Line of sight tests the selected anchor candidates in Head, Center, Lower Body order and accepts the entity when any enabled anchor is visible.

### Maintenance and Release

- The active entity is validated each client tick without rescanning every candidate.
- Temporary line-of-sight loss enters `OCCLUDED_GRACE` for 750 milliseconds by default.
- The ring visibly fades during grace instead of disappearing immediately.
- Visibility recovery returns to `LOCKED` without replaying the acquisition sound.
- Grace expiration releases the target with reason `OCCLUDED`.
- Death, removal, invalid dimension, disconnect, or missing world releases immediately.
- Tracking-distance release uses the configured 20-block baseline and never extends attack reach.
- When quick switch is enabled, target death attempts one deterministic replacement scan before releasing.
- Release publishes one transition, one target-lost feedback event, and a 200-millisecond presentation fade.
- A normal `RELEASING` snapshot retains detached presentation values for that fade but never retains the entity reference. Lifecycle invalidation skips the fade and clears the complete snapshot immediately.

### Cycling and Anchors

- Q and E continue to cycle in stable screen-bearing order.
- A 250-millisecond cooldown prevents repeated input jitter.
- Manual cycling always selects the immediately adjacent candidate in deterministic screen-bearing order; history never changes that order.
- Quick switch and automatic reacquisition avoid the most recently dropped target while another valid candidate exists.
- Generic anchor cycling offers Head, Center, and Lower Body without requiring skeletal or server data.
- A target-specific optional adapter may expose richer anchors later, but 1.4.0 does not require one.
- The chosen anchor name appears briefly above the target ring and persists in the session.

### Temporary Free Look

Holding the configurable free-look modifier suspends camera tracking while retaining the target and HUD. Releasing the modifier eases the camera back to the current target. Free look never changes the selected target or replays lock feedback.

## Camera Design

### Camera Frame

`CameraDirector` calculates a desired target focus, yaw, pitch, camera offset, and FOV from:

- active target anchor;
- player-to-target distance;
- target width and height;
- current perspective;
- selected profile;
- active camera adapter;
- collision-constrained camera distance.

Target size and distance influence framing continuously, avoiding special-case jumps between normal and large entities. All rotation uses wrapped-angle interpolation so crossing +/-180 degrees cannot cause a full camera spin.

### Presets

| Preset | Tracking | Framing | Dynamic FOV | Intended feel |
| --- | --- | --- | --- | --- |
| Cinematic | soft response | strong offset | up to 12% | dramatic large-target composition |
| Balanced | moderate response | moderate offset | up to 6% | default general combat |
| Snappy | fast response | light offset | disabled by default | precise low-motion combat |

Advanced controls may override profile values. Selecting a preset replaces the associated advanced values so the resulting configuration remains predictable.

### Collision and Close Camera

- Eight rays are cast from small offsets around the player-to-camera segment.
- The shortest unobstructed ratio constrains the camera offset.
- The camera never moves beyond the collision-constrained distance.
- Player-model transparency begins when the camera enters a configurable near distance and reaches the configured minimum alpha at the inner distance.
- Optional automatic first-person fallback activates only when collision makes third-person composition unusable.
- The previous perspective is recorded before fallback and restored only when safe camera distance returns.

### Restoration

Unlock, death, world change, compatibility fallback, and configuration-screen entry all use one restoration path. It restores the previous perspective, dynamic FOV contribution, camera offset, and adapter state. Normal unlock eases out; lifecycle invalidation restores immediately.

## Adaptive Hybrid HUD

### World-Space Target Ring

- The ring scales from the target bounding box with configurable minimum and maximum sizes.
- Its health arc reflects current and maximum health.
- Lock stability is communicated through opacity and motion rather than color alone.
- Occlusion grace fades and softens the ring.
- Anchor changes show a short text label.
- Missing theme resources fall back to Classic Ring.

### Compact Detail Panel

The panel can independently display:

- target name;
- exact health and maximum health;
- distance;
- learned or estimated damage;
- hits-to-kill;
- vulnerability or resistance status.

Each line has its own toggle. The panel honors the existing anchor and X/Y offset concept but uses shared layout calculations rather than renderer-specific positioning branches.

### Conditional Boss Panel

- The panel appears for vanilla bosses or targets at or above the default 100 maximum-health threshold.
- The threshold is configurable.
- Boss presentation does not suppress the world-space ring.
- Layout avoids existing vanilla boss overlays and the configured HUD safe area.

### Reticle Themes

1. Classic Ring is the clean Zelda-inspired default.
2. Segmented emphasizes health and status through separated arc segments.
3. Tactical uses an angular marker for a precision-combat presentation.

All theme textures and geometry will be newly created for Zelda Targeting.

### Damage and Audio Feedback

- Damage numbers retain Default, Subtle, and Arcade motion styles.
- Critical, lethal, lock, switch, target-lost, and low-health feedback are driven by explicit session or damage transitions.
- A transition identifier prevents duplicate sound or visual events.
- Existing per-event enable, volume, and pitch controls remain available in the new schema.
- Sound cooldowns prevent event stacking during rapid switches.

### Accessibility

- Built-in palettes include Default, Deuteranopia, Protanopia, and Tritanopia.
- Reduced Motion disables target-ring pulse, large critical-number scaling, and dynamic FOV while retaining essential transitions.
- HUD scale and opacity are global controls.
- Health, warning, and lock states use shape, opacity, or text in addition to color.
- Every sound event can be disabled independently.

## Replacement Configuration Experience

### First Run

1. When 1.4.0 finds a configuration without `schemaVersion = 4`, it attempts a non-overwriting backup named `zeldatargeting-1.3-backup.cfg`, adding a numeric suffix if needed.
2. If backup fails, the original file is not overwritten. The mod uses in-memory Balanced defaults, logs one actionable error, and retries next launch.
3. After a successful backup, the 1.3 file is ignored and the preset selector is shown at the first eligible main-menu client tick.
4. Cinematic, Balanced, or Snappy must be selected to write the new schema.
5. Closing the selector uses in-memory Balanced defaults for that session and shows the selector again next launch.

### Configuration GUI

- Pages are Presets, Targeting, Camera, HUD, Audio, Compatibility, and Accessibility.
- Page layout, control creation, tooltips, and value binding are separate components.
- A live preview demonstrates target ring, panel, boss panel, palette, scale, and motion changes without requiring a world.
- Save validates and writes the working model.
- Cancel restores the immutable entry snapshot.
- Reset replaces the working model with the selected preset and does not write until Save.
- Invalid numeric and enum values clamp or fall back before becoming active.

## Optional Compatibility

### Adapter Selection

- Vanilla is the unconditional fallback.
- Better Third Person and Shoulder Surfing adapters report whether their camera behavior is currently active, not merely whether the mod is installed.
- When multiple supported camera mods are installed, the adapter reporting active camera ownership is selected.
- Compatibility preference defaults to Auto. Auto uses the active owner and falls back to vanilla when ownership is ambiguous.
- An explicit Better Third Person or Shoulder Surfing preference tries that adapter only when it is installed and active, then falls back to vanilla.

### Failure Handling

- Optional-mod linkage and runtime failures are contained inside the adapter boundary.
- A failing adapter is disabled for the current session.
- One warning records the adapter, exception type, and fallback action.
- Camera state is restored before vanilla fallback becomes active.
- Repeated per-tick logging is prohibited.
- Compatibility diagnostics are opt-in and rate-limited.

### Future Adapters

Future combat integrations may provide target anchors or camera adjustments, but cannot replace session ownership, target validation, or client-only safety rules.

## Performance Rules

- Idle acquisition scans run only in response to acquisition input.
- Cycle input performs one candidate scan subject to the 250-millisecond cooldown.
- A locked target is validated directly each client tick.
- `TargetingService` owns and clears reusable candidate and sort buffers. Allocation required by a Minecraft API is confined to acquisition and cycle scans and never occurs in camera or HUD render paths.
- Camera and HUD render paths perform constant-time snapshot reads.
- No per-frame stream pipeline or avoidable collection allocation is permitted in camera or HUD paths.
- Expensive compatibility detection occurs at initialization or adapter-state changes, not every tick.
- Debug output is sampled and disabled by default.

## Testing Strategy

### Automated Tests

Pure Java tests cover:

- target scoring for all four policies;
- deterministic tie-breaking;
- stable clockwise and counterclockwise cycling;
- history behavior and cooldown timing;
- every valid lock-phase transition;
- occlusion grace recovery and expiration;
- immediate lifecycle invalidation;
- anchor selection and fallback;
- angle wrapping and interpolation;
- profile framing and FOV bounds;
- collision-distance selection from simulated ray results;
- perspective and FOV restoration;
- preset completeness and validation;
- Save, Cancel, and Reset transactions;
- old-configuration backup naming and failure behavior;
- adapter selection, failure containment, and vanilla fallback.

Every development stage runs `clean build`, tests, and Forge reobfuscation.

### Manual Playtest Matrix

Camera installations:

- vanilla only;
- Better Third Person only;
- Shoulder Surfing Reloaded only;
- both optional camera mods installed.

Scenarios:

- first person, vanilla third person, optional-camera mode, and temporary free look;
- open space, low ceiling, narrow corridor, wall collision, and close-camera transparency;
- player death and respawn, target death, disconnect, reconnect, and dimension change;
- small, tall, wide, flying, fast, invisible, crowded, and boss targets;
- rapid target cycling and quick switch after death;
- single-player and connection to an unmodified multiplayer server;
- common GUI scales and aspect ratios;
- low frame rate and high entity density;
- first-run preset selection, configuration backup, Save, Cancel, and Reset.

## Development Stages

### Stage 1: Safety Net and Boundaries

- Add characterization tests around current selection, cycling, configuration, and camera math.
- Add lifecycle regression tests for world unload, death, and disconnect.
- Introduce package boundaries and diagnostics without changing player-visible behavior.
- Record baseline build and profiling results.

### Stage 2: Core Targeting State

- Introduce candidates, scoring policies, session state machine, immutable snapshots, anchors, and history.
- Route current input through `TargetingService`.
- Remove superseded state ownership from `TargetingManager`, `TargetTracker`, and related classes.

### Stage 3: Camera System

- Introduce camera profiles, director, collision solver, free look, close-camera behavior, and unified restoration.
- Use vanilla adapter only until the camera core is verified.

### Stage 4: Presentation and Feedback

- Build original reticle themes and Adaptive Hybrid HUD components.
- Split damage-number and audio feedback into transition-driven components.
- Add accessibility palettes and Reduced Motion behavior.

### Stage 5: Configuration and Compatibility

- Replace the configuration schema and GUI.
- Add backup and first-run preset selection.
- Implement and test Better Third Person and Shoulder Surfing adapters.

### Stage 6: Release QA

- Complete the automated and manual matrices.
- Perform the allocation and compatibility logging pass.
- Update README, roadmap, changelog, metadata, and version to 1.4.0.
- Produce and inspect the reobfuscated release JAR.

## Release Acceptance Criteria

Version 1.4.0 is ready only when:

- no known crash, stuck-lock, stale-target, or camera-restoration path remains;
- all automated tests pass from a clean checkout;
- the Forge build and reobfuscation tasks succeed;
- vanilla, Better Third Person, Shoulder Surfing, and combined-mod camera installations complete the manual matrix;
- the client connects to and plays on an unmodified multiplayer server;
- world unload, death, respawn, disconnect, and dimension transition clear or restore state correctly;
- old configuration is retained before the new schema is written;
- camera and HUD hot paths meet the allocation rules;
- missing theme resources and failing optional adapters fall back safely;
- documentation and release metadata describe the new configuration requirement and optional integrations.

## Approved Design Record

The user approved:

- the staged subsystem rewrite approach;
- the single-snapshot component architecture;
- targeting and camera behavior;
- the Adaptive Hybrid HUD;
- the replacement configuration and accessibility direction;
- compatibility isolation, testing, performance safeguards, and delivery stages.
