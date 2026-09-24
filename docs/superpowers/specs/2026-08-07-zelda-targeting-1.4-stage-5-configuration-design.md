# Zelda Targeting 1.4.0 Stage 5 Configuration Design

- Status: approved design, awaiting written-spec review
- Date: 2026-08-07
- Target: Minecraft 1.12.2, Forge 14.23.5.2859, Java 8
- Parent design: `docs/superpowers/specs/2026-07-31-zelda-targeting-1.4-overhaul-design.md`

## Summary

Stage 5 replaces the 1.3 configuration surface with curated 1.4 presets, a versioned schema, and a transactional configuration screen. The existing screen directly mutates static runtime fields and writes `zeldatargeting.cfg` on nearly every interaction. The replacement keeps those fields as a temporary runtime bridge, but moves editing, validation, persistence, and preview ownership into a testable settings model.

Existing 1.3 configuration is deliberately not migrated. On first 1.4 use it is safely backed up, and the player chooses a Cinematic, Balanced, or Snappy starting preset. Optional Better Third Person and Shoulder Surfing Reloaded adapters are explicitly deferred to the next Stage 5 delivery after this configuration foundation is accepted.

## Goals

1. Persist a `schemaVersion=4` configuration without corrupting the existing 1.3 file.
2. Require a fresh preset choice rather than attempting unreliable migration of legacy values.
3. Make Save, Cancel, and Reset deterministic: no disk write occurs until Save.
4. Let GUI edits preview safely at runtime, while Cancel restores the exact entry state.
5. Group player-facing controls into Presets, Targeting, Camera, HUD, Audio, Compatibility, and Accessibility pages.
6. Expose the Stage 4 presentation choices, including target-ring behavior, palette, reduced motion, HUD scale, and HUD opacity.
7. Preserve stable runtime access for existing code while setting up a clean seam for the later optional compatibility adapters.

## Non-Goals

- Adding, detecting, or activating Better Third Person or Shoulder Surfing Reloaded adapters in this delivery.
- Migrating legacy 1.3 option values to schema 4.
- Changing targeting selection, multiplayer reach, combat damage, camera ownership, or renderer behavior except when a newly configured value intentionally changes it.
- Adding new runtime dependencies, a coremod, access transformer, or server-side component.

## Settings Architecture

### Pure settings model

`TargetingSettings` is the complete schema-4 value object. It contains all persisted player choices, has explicit defaults, supports deep copying, clamps values at its boundaries, and contains no Forge, Minecraft, GUI, or filesystem imports. `TargetingPreset` supplies the curated Cinematic, Balanced, and Snappy value sets. Preset selection replaces the working model with a fresh validated copy rather than mutating a few camera fields in place.

`TargetingSettingsValidator` normalizes invalid, missing, non-finite, or out-of-range values. It reports a valid, usable settings object even when a malformed file has to fall back to defaults. Validation rules are shared by disk loading and GUI mutation so their behavior cannot drift.

The live mod remains compatible with existing `TargetingConfig` field reads during this stage. `TargetingConfig` becomes the small bridge that:

- owns the active validated `TargetingSettings` snapshot;
- applies a snapshot to the legacy static fields used by existing gameplay and renderer code;
- creates an immutable entry snapshot for the GUI; and
- delegates loading and saving to the schema store.

No GUI widget writes `Configuration` or a static field directly.

### Schema store and safe first run

`TargetingSettingsStore` is the only Forge `Configuration` owner. A configuration containing `schemaVersion=4` is loaded as schema 4. Any absent, older, malformed, or otherwise unsupported schema is a first-run condition.

On the first-run condition, the store must:

1. locate the existing `zeldatargeting.cfg` if present;
2. copy it to `zeldatargeting-1.3-backup.cfg`, adding `-2`, `-3`, and so on without overwriting an existing backup;
3. leave the original untouched if the backup cannot be created, log the failure once, and run with the Balanced settings only in memory; and
4. refrain from writing schema 4 until the player explicitly chooses a preset.

The first eligible main-menu tick opens a small preset-selection screen. Choosing a preset creates schema 4 and writes it atomically through the normal store path. Closing the selector keeps an in-memory Balanced configuration for that session and shows the selector again on the next launch. The game must never crash or block the title screen because the backup or write operation failed.

## Configuration Screen

### Transaction model

Opening the screen captures an immutable `entrySnapshot` from active settings and creates a separate `workingCopy`. Widgets bind only to the working copy. A preview application may update active runtime values while the screen is open, but it never writes the file.

- **Save** validates the working copy, applies it as active, then persists schema 4. If writing fails, the active validated settings remain usable and the user receives a contained in-game/log diagnostic.
- **Cancel** restores the exact entry snapshot to active runtime state and returns to the parent screen without writing.
- **Reset** replaces only the working copy with the currently selected preset. It remains reversible until Save.

The GUI must retain its existing resolution-safe page navigation and content viewport behavior. Long pages scroll inside the viewport; footer navigation stays visible at every supported GUI scale.

### Pages

1. **Presets** — Cinematic, Balanced, and Snappy descriptions, selected preset, and reset behavior.
2. **Targeting** — range, tracking distance, detection angle, line of sight, target priority, and entity filters.
3. **Camera** — enablement, profile/feel, smoothing, yaw/pitch constraints, focus offset, automatic third person, and future adapter preference.
4. **HUD** — panel/ring visibility, detail lines, anchor, offsets, scale, opacity, boss presentation, ring theme, and target-history controls.
5. **Audio** — master enablement, theme, variation, event enables, volume, and pitch.
6. **Compatibility** — vanilla fallback selection, preferred camera owner placeholder, diagnostics, and clear status text. This page must work safely before third-party adapters exist.
7. **Accessibility** — palette, Reduced Motion, non-color status emphasis, and other presentation-safety choices.

Every page is described by focused layout and value-binding components rather than another monolithic GUI class. Tooltips explain unfamiliar terms and display clamped ranges where relevant.

### Preview behavior

HUD, ring, palette, scale, opacity, and motion changes preview while the GUI is open. Camera and targeting changes may also update the live runtime so their effect is understandable, but Cancel must restore the entry snapshot before leaving. Preview never changes multiplayer protocol, server state, stored key bindings, or files.

## Presets and Defaults

Balanced is the default safe profile and temporary first-run state. Cinematic favors smoother, more composed camera behavior. Snappy favors direct response. Presets set a coherent group of camera, presentation, and accessibility-safe defaults; they do not silently toggle target filters, audio personalizations, or diagnostics outside their stated scope.

Explicit changes after applying a preset are preserved as custom values. The selected preset remains the reset baseline until the user selects another preset.

## Failure Handling and Compatibility Boundary

Missing, unknown, or invalid enum values fall back to the schema-4 default. Numeric values are clamped before runtime use. A failed backup, read, validation, or write is contained and rate-limited in the log. Targeting, camera, HUD rendering, and lock state continue using a valid in-memory configuration.

The Compatibility page exposes only a safe Vanilla/default behavior in this delivery. Its data model has an adapter preference and diagnostic state so the upcoming Better Third Person and Shoulder Surfing Reloaded modules can plug in without altering the schema or GUI structure. Until those modules land, unavailable choices resolve to Vanilla with clear status text.

## Testing

JUnit coverage must include:

- default and curated preset values;
- copy isolation and Cancel restoration;
- Reset changing only the working copy;
- valid settings application to the legacy runtime bridge;
- validation/clamping for invalid numbers and unknown enum values;
- first-run determination for absent, legacy, malformed, and schema-4 files;
- backup destination naming without overwrite;
- backup/write failure fallback that retains usable Balanced settings; and
- page navigation/viewport safety plus footer access at constrained dimensions.

Manual verification must cover a fresh config, a real legacy config backup, each preset selection, close/reopen of the selector, Save, Cancel, Reset, every configuration page, narrow/high-GUI-scale layouts, ring/panel preview, palette, Reduced Motion, and a normal lock-on session after save/cancel.

## Completion Criteria

This configuration delivery is complete only when:

- only schema 4 is treated as persisted 1.4 settings;
- old files are backed up without overwrite before replacement and safely fall back if backup fails;
- all GUI edits are transactional and no setting is written before Save;
- Cancel fully restores runtime values from entry;
- presets, pages, preview, validation, and accessibility preferences are functional;
- unavailable compatibility remains visibly and behaviorally Vanilla-safe; and
- JUnit, Forge build, reobfuscation, and the manual configuration matrix pass.
