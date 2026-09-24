# Zelda Targeting 1.4.0 Stage 6: Presentation Policy Design

**Date:** 2026-08-07  
**Status:** Approved for implementation  
**Scope:** Client-only Minecraft Forge 1.12.2

## Goal

Make Zelda Targeting's visual settings behave as one coherent presentation system. The selected palette, HUD opacity, ring glow strength, and reduced-motion preference must be applied consistently without changing the approved targeting, camera, or target-ring geometry.

## Chosen approach

Introduce a small, plain-Java `PresentationStyle` value object created from the active targeting configuration. Renderers receive this style at draw time rather than each renderer interpreting raw configuration values independently.

The style owns four cross-cutting presentation values:

- `PresentationPalette` for normal, warning, and lethal target colors;
- HUD opacity for 2D detail and boss panels;
- ring glow strength for the existing 3D target-ring aura and core brightness;
- reduced-motion state for presentation-only animation.

The existing per-feature switches remain authoritative. For example, compact HUD, boss-style panel, target history, soft aim, and target-ring enablement still decide whether their respective components are rendered.

## Behavior contract

| Setting | Affected output | Must not affect |
| --- | --- | --- |
| Color palette | target-ring health arc and marker, target panels, status colors, soft-aim and history accents | target selection and damage calculations |
| HUD opacity | 2D detail panel and boss panel composition | world geometry, ring shape, camera |
| Ring glow | existing tube ring's aura/core intensity | ring radius, health-arc sweep, target focus point |
| Reduced motion | ring pulse/transition presentation and other visual-only animation | lock-on camera motion, player input, targeting logic |

The approved classic ring remains one 3D tube with one depleting health arc. It continues to use the same health thresholds as the panel: normal above 60%, warning at 25% through 60%, and lethal below 25% or when the predicted hit is lethal.

## Architecture

`TargetingConfig` remains the source of persisted values. A `PresentationStyle` is produced from the validated active configuration and passed into runtime renderers.

```text
TargetingConfig
      |
      v
PresentationStyle.fromConfig()
      |
      +--> TargetRingRenderer (palette, glow, reduced motion)
      +--> DetailPanelRenderer (palette, HUD opacity)
      +--> BossPanelRenderer (palette, HUD opacity)
      +--> SoftAimRenderer / TargetHistoryRenderer (palette, reduced motion)
```

`RingColorPolicy` becomes palette-aware so panel and ring status colors cannot drift apart. Ring geometry remains responsible for radius, the health-arc sweep, and collision-safe placement; only its animation input is controlled by the style.

## Out of scope

- Changing the targeting state machine or entity selection rules.
- Changing camera smoothing, focus offsets, free-look, or restoration behavior.
- Changing the target ring's approved tube dimensions, health-arc layout, or color thresholds.
- Changing configuration page count, navigation, transactions, or persistence schema.
- Adding a new external compatibility dependency.

## Verification

Automated coverage will verify palette/status mapping, style configuration mapping, opacity and glow clamping, and reduced-motion propagation. The project test suite, `git diff --check`, and a clean Git status will be required before handoff.

Manual testing will verify:

1. Changing the palette changes the ring, panel, marker, and auxiliary visual accents together.
2. HUD opacity changes detail/boss panels while leaving the world ring unchanged.
3. Ring glow changes brightness without changing the tube size or health-arc sweep.
4. Reduced motion stops visual pulse/transition behavior but camera lock-on feels unchanged.
5. The ring is still a single 3D tube with a single depleting arc, and panel/ring colors switch at the same health thresholds.
