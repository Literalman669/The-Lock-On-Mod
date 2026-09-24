# Zelda Targeting 1.4.0 Stage 4 Presentation Design

- Status: approved design, awaiting written-spec review
- Date: 2026-08-05
- Target: Minecraft 1.12.2, Forge 14.23.5.2859, Java 8
- Parent design: `docs/superpowers/specs/2026-07-31-zelda-targeting-1.4-overhaul-design.md`

## Summary

Stage 4 rebuilds lock-on presentation as an Adaptive Hybrid HUD. It preserves the familiar top-right target panel while separating target-ring, detail-panel, boss-panel, and transition-feedback responsibilities. The existing configuration schema remains active; the Stage 5 schema replacement will expose new presentation preferences such as palette and Reduced Motion.

## Goals

1. Render one consistent target presentation from a single per-frame snapshot.
2. Replace the oversized `TargetRenderer` with focused world-ring, compact-panel, boss-panel, and feedback components.
3. Keep the detail panel readable at every supported GUI scale and at every existing HUD anchor.
4. Add a Classic Ring default that communicates health and lock stability through geometry, opacity, and motion as well as color.
5. Show a boss presentation only when a living target is a vanilla boss or has at least 100 maximum health; never suppress the world-space ring.
6. Drive lock, switch, lost, critical, lethal, and low-health feedback from explicit transitions so no event repeats every frame.
7. Keep overlay and world render hot paths constant-time and free of avoidable collection allocation.

## Non-Goals

- Replacing the persisted configuration schema or configuration GUI.
- Changing target selection, camera behavior, multiplayer reach, combat damage, or server state.
- Adding third-party camera integrations; those remain Stage 5 work.
- Requiring new runtime dependencies, ASM, a coremod, or an access transformer.
- Making palette or Reduced Motion player-selectable before the Stage 5 preset/configuration flow exists.

## Architecture

### Presentation snapshot

`TargetPresentationSnapshot` is the immutable render input. It contains the target reference and identity, display name, interpolated world position, bounding dimensions, health/max-health/ratio when available, distance, predicted damage, hits-to-kill, vulnerability text, lock phase, transition identifier, target priority, and the existing visibility flags needed by presentation.

`TargetPresentationSnapshotFactory` is the only Minecraft-facing sampler. It reads the current target once for a render event and returns no snapshot when the manager, player, target, or world is unavailable. No renderer queries the targeting manager or damage calculator independently.

### Render components

- `TargetRingRenderer` renders world-space ring geometry from a snapshot during `RenderWorldLastEvent`. Classic Ring is the default. Its health arc follows the health ratio; bracket and marker shapes express lock/low-health/lethal state even under a color-vision palette.
- `DetailPanelRenderer` renders the compact 2D panel from a measured line list during `RenderGameOverlayEvent.Post`. It honors the existing HUD anchor and X/Y offset values. Its layout has a bounded width, shared padding, and only reserves a line when its corresponding existing toggle is enabled.
- `BossPanelRenderer` renders a larger overlay when `isBossEligible` is true and the existing boss-style-panel toggle is enabled. It avoids the vanilla boss-bar safe area and leaves the target ring active.
- `SoftAimRenderer` and target-history rendering are moved behind clear presentation seams without changing their current public behavior.
- `PresentationFeedbackController` converts state changes into one-shot presentation feedback. It owns only prior transition/status values, not targeting or camera state.

`TargetRenderer` remains the single Forge event subscriber and becomes a small coordinator that creates one snapshot, invokes the relevant components, and restores GL state at each component boundary.

### Pure presentation core

The `client.presentation.core` package contains no Minecraft, Forge, LWJGL, or OpenGL imports:

- `PanelAnchor` and `PanelLayout` calculate panel bounds from screen size, anchor, offsets, scale, and enabled line measurements.
- `BossEligibility` identifies boss presentation candidates from an explicit boss flag and max-health threshold.
- `RingGeometry` calculates normalized ring/bracket dimensions and health-arc bounds from target dimensions, distance, and health ratio.
- `PresentationStatus` maps health, hits-to-kill, and lock phase to shape/opacity/motion signals.
- `PresentationFeedbackState` deduplicates transition-driven feedback and reports which feedback edges are newly entered.

These values are independent of render APIs and are covered by JUnit tests.

## Rendering Behavior

### Classic Ring

Classic Ring uses lightweight geometry, not a required texture. It surrounds the resolved target bounds with four corner brackets, a health arc, and a small directional marker. Its size is clamped from the target dimensions and distance so small and large entities remain readable without an excessive screen footprint.

- Full health uses a complete arc; reduced health shortens the arc.
- Low health changes the marker shape and opacity pulse; it does not rely on red alone.
- Lethal status adds a distinct inner marker and a short one-shot emphasis from the feedback controller.
- Occlusion grace softens opacity; target loss fades through the existing lock release rather than popping away.
- Reduced Motion holds ring scale and opacity steady while retaining health, shape, and target identity information.

Stage 4 implements Classic Ring only. Segmented and Tactical themes, along with player-facing theme selection, are deferred to Stage 5 so this stage does not add a temporary configuration surface.

### Compact detail panel

The default panel remains top-right so existing users retain their visual reference. The panel measures its content before choosing bounds and draws only enabled lines:

- target name;
- health bar and exact health;
- distance;
- predicted damage;
- hits-to-kill; and
- vulnerability or resistance text.

Existing line toggles, HUD anchor, and X/Y offsets remain the active configuration inputs. The layout calculation clamps the panel to the scaled screen safe area, so high GUI scales and offsets cannot push text or bars outside the viewport. The panel uses a neutral dark surface, a health-colored bar, and a status icon/label in addition to color.

### Conditional boss panel

A boss panel is eligible when the target is a vanilla boss or has maximum health of at least 100. The existing `bossStylePanel` option still gates its display. The panel occupies the top safe area below vanilla boss overlays; it never replaces the world ring. Ordinary targets always retain the compact panel behavior.

### Feedback

The feedback controller consumes a snapshot transition identifier plus normalized target status. A new edge can emit one feedback event; a stable frame cannot. It covers lock, target switch, target lost, critical, lethal, and low-health entries. Existing sound enables, volumes, pitches, and damage-number behavior remain the runtime source until Stage 5 replaces configuration.

## Accessibility and Fallbacks

- Every health, warning, and lock state has a shape, opacity, or text distinction in addition to a color distinction.
- The core accepts palette and Reduced Motion inputs for unit-tested behavior, but Stage 4 supplies Default/normal-motion values. Stage 5 configuration becomes their first player-facing source.
- Missing geometry/theme resources, unknown theme names, non-finite layout values, missing health data, or an unavailable target fall back to Classic/default presentation or no rendering without throwing.
- Each renderer restores texture, depth, blend, lighting, color, and matrix state before returning. A failed presentation component is contained, logged once, and does not disable targeting or camera behavior.

## Data Flow

1. Forge overlay/world render events reach the small `TargetRenderer` coordinator.
2. The coordinator asks the factory for one immutable presentation snapshot.
3. The world event invokes `TargetRingRenderer`, soft aim, and target history using that snapshot.
4. The overlay event computes `PanelLayout`, then invokes the compact and eligible boss panel renderers.
5. The feedback controller compares the snapshot with its prior transition/status state and emits only newly entered feedback edges.
6. Lock release, world loss, a missing target, or an invalid snapshot clears presentation feedback state without touching targeting or camera session ownership.

## Testing

JUnit coverage must include:

- panel bounds for each anchor, offsets, narrow screens, tall screens, and high GUI scales;
- boss eligibility for bosses, high-health ordinary entities, thresholds, and missing health;
- ring size/arc clamping for small, tall, wide, distant, and invalid inputs;
- status shape/opacity output for normal, low-health, lethal, and occlusion states;
- duplicate feedback suppression for stable frames and correct events for new transition IDs/status edges;
- safe fallback for absent or invalid snapshot fields.

Manual verification must cover normal targets, a vanilla boss or high-health entity, rapid cycling, target death, low health, GUI scales, and every existing HUD anchor. The full Forge build and reobfuscation remain required before acceptance.

## Completion Criteria

Stage 4 is complete only when:

- the target ring, compact panel, boss panel, and feedback controller have separate focused responsibilities;
- the existing top-right panel remains recognizable while all anchors and GUI scales stay inside the safe area;
- target-ring state communicates through non-color signals;
- boss eligibility is deterministic and leaves the ring visible;
- feedback does not repeat on stable render frames;
- absent data and renderer failures fall back safely;
- pure presentation tests, the Forge build, and reobfuscation pass; and
- the manual presentation matrix completes without new GL-state, camera, or targeting regressions.
