# Zelda Targeting Roadmap

> Living checklist for planned improvements, in-progress tasks, and future ideas.
> Keep this file updated every session.

## How to Use This Roadmap

- Mark `[x]` when a task is complete.
- Keep tasks small and specific.
- Add new ideas under the relevant phase before implementing.
- Move completed items to the **Completed History** section if a phase gets too long.

---

## Phase 1 — UI/UX Polish (Current Focus)

### 1.1 HUD Polish (In-Game Overlay)
- [x] Redesign HUD panel styling (clear hierarchy, cleaner spacing, accent bar).
- [x] Improve health bar readability and color states.
- [x] Add explicit Hits-to-Kill line when enabled.
- [x] Improve distance/readability formatting.
- [x] Add small target status indicators (e.g., lethal / resistant / weak) where relevant.
- [x] Keep HUD placement safe for different resolutions.
- [x] Validate HUD in first-person and third-person camera views.

### 1.2 Config GUI Polish
- [x] Improve page visual layout and section clarity.
- [x] Add clear usage hints (left-click/right-click/shift fine-tune).
- [x] Add hover tooltips for key settings.
- [x] Improve toggle readability (strong ON/OFF visual feedback).
- [x] Disable incompatible controls contextually (e.g., dependent controls).
- [x] Make page navigation more intuitive and discoverable.
- [x] Validate GUI readability across common GUI scales.

### 1.3 UX Consistency Pass
- [x] Align naming between HUD labels and config labels.
- [x] Ensure `showHitsToKill` and similar toggles are reflected correctly in UI output.
- [x] Reduce visual clutter while keeping important combat info visible.

---

## Phase 2 — Targeting & Camera Reliability

### 2.1 Core Targeting
- [x] Add additional safeguards for target switching edge-cases.
- [x] Fix target cycle jank (250ms cooldown + stable bearing sort tiebreaker).
- [x] Add optional target-priority presets (distance / threat / angle).
- [x] Improve fallback behavior when target becomes invalid mid-combat.

### 2.2 Camera Behavior
- [x] Fine-tune lock-on feel presets (cinematic / balanced / snappy).
- [x] Add optional vertical offset controls for camera focus point.
- [x] Add per-mode smoothing overrides (first-person/third-person/SSR active).

### 2.3 Mod Compatibility
- [ ] Re-verify compatibility logic with Better Third Person.
- [x] Re-verify compatibility logic with Shoulder Surfing Reloaded.
- [x] Add debug flag for compatibility diagnostics (optional log mode).

---

## Phase 3 — Audio & Combat Feedback

### 3.1 Audio
- [x] Add additional curated sound profile presets.
- [x] Add per-event enable/disable quick toggles in GUI grouping.
- [x] Ensure no duplicate or stacked sound triggers in rapid retargeting.

### 3.2 Combat Feedback
- [x] Improve damage number motion presets (subtle / arcade).
- [x] Add optional critical-hit emphasis variants.
- [x] Add optional low-health warning styling in HUD.

---

## Phase 4 — Performance, QA, and Release Readiness

### 4.1 Performance
- [x] Profile render/update paths for avoidable per-frame allocations.
- [x] Cache repeated calculations where safe.
- [x] Confirm stable behavior at low FPS and high entity density (manual test).

### 4.2 QA
- [x] Build a manual test checklist for each feature area.
- [x] Add regression checklist for camera alignment and target cycling.
- [x] Verify all config values persist correctly after restart.

### 4.3 Docs / Packaging
- [x] Refresh README feature list and screenshots after UI pass.
- [x] Update changelog with grouped release notes.
- [x] Prepare release candidate checklist (manual).

---

## Backlog / Ideas Parking Lot

- [ ] Optional compact HUD mode.
- [ ] Optional lock-on soft aim indicator.
- [ ] Optional target history ring / recently targeted marker.
- [ ] Optional boss-style target panel skin.

---

## Completed History

- [x] SSR runtime detection and reflection-based offset integration.
- [x] Camera compensation limited to actual SSR shoulder mode.
- [x] Stable horizontal-bearing target cycling.
- [x] Velocity feed-forward + adaptive smoothing for moving targets.
- [x] Configuration persistence fix for reticle color.
