# Neon Tube Target Ring Design

## Purpose

Upgrade the current flat target outline into a high-visibility neon tube while preserving the existing target-status colors, health information, and reduced-motion behavior.

## Scope

This is a rendering-only refinement for the active target ring. It does not change targeting, camera behavior, world lighting, shader requirements, target-history markers, or the existing configuration schema.

## Chosen approach

Render the horizontal ring as a real three-dimensional torus mesh rather than a flat annulus or a platform-dependent OpenGL line:

1. **Aura** — a broad, low-opacity additive torus surrounding the core.
2. **Tube core** — a saturated torus with a circular cross-section, giving the ring physical vertical depth.
3. **Highlight** — a narrow raised torus along the upper surface of the core, with vertex shading that makes the tube read as glossy and rounded.

The health arc becomes a smaller raised tube on the core's upper surface rather than a separate inner disk. Corner brackets and the status marker remain crisp line-based elements so the display keeps a useful focal point instead of becoming a solid halo.

## Architecture

`RingGeometry` remains responsible for safe entity-relative dimensions, health arc length, alpha, and motion scaling. The pure `NeonRingAppearance` model derives finite, clamped aura/core/highlight widths and alpha multipliers. `TorusGeometry` maps a tube cross-section into radial and vertical offsets. `TargetRingRenderer` combines them into fixed-size quad meshes with per-vertex color shading.

The renderer retains its current exception boundary and restores every changed OpenGL state in `finally`. It uses additive blending only for the aura, then conventional alpha blending for the core and highlight. This keeps the effect intense without bleaching nearby UI or leaking render state into Minecraft.

## Motion and accessibility

The normal ring keeps its existing subtle pulse. Reduced-motion mode uses the same static neon appearance, but no pulsing scale or time-dependent brightness changes. Status colors remain unchanged: configured/default for normal, warning for low health, red for lethal, and muted gray for occluded targets.

## Performance and failure behavior

The ring uses the existing 32 major segments and eight cross-section segments. Its four tube meshes have a fixed upper bound on vertices per target, with no textures, shaders, particles, or per-frame object allocations. Invalid dimensions, distances, health ratios, and derived widths are clamped in the pure models before reaching OpenGL.

If rendering throws, the existing once-only warning remains the containment boundary and the next frame is still eligible to render.

## Verification

Automated tests will first specify that the appearance model:

- derives a visibly wider aura than core and a narrower highlight than core;
- produces finite, positive geometry for invalid inputs;
- maps the tube cross-section to distinct radial and vertical positions, proving it is volumetric rather than planar;
- preserves the static reduced-motion result while allowing normal pulse behavior;
- keeps every alpha multiplier within `[0, 1]`.

After the tests turn green, the full Gradle test/build/reobfuscation command will run. The dev client will then be launched for visual confirmation with normal, low-health, lethal, occluded, close-range, and reduced-motion targets.

## Corrective revision: separated tube layers

The initial layered-torus implementation made the glow shell, white highlight, and health arc all physical tubes in nearly the same plane. That caused them to read as a compressed stack of rings instead of a single neon tube.

The corrected implementation has one primary, vertex-lit tube. Its additive glow shell is at most 1.5 times the core width and uses low opacity, so it reads as light rather than another band. The health arc is a narrow tube with a measured vertical gap above the core. The core's per-vertex lighting replaces the former white highlight torus, and the lethal status is a diamond marker rather than an extra inner circle.

## Final revision: one-ring health state

Visual testing showed that even a separated health arc adds unnecessary visual competition when a target starts at full health. The finished design therefore renders only the primary neon tube. It is green for a healthy target, changes to orange for warning or low health, and changes to red for lethal state. The overhead target marker uses the same policy; occluded targets remain muted gray.

## Final behavior clarification: shared health span and color state

The one remaining tube retains `RingGeometry`'s health span: it is a complete green circle at full health and contracts clockwise as the target is damaged. Every visible target-health surface now shares the same `PresentationStatus`: the tube, target marker, panel accent, detail health bar, and boss health bar are green above 75% health, orange at or below 75% (or when a target needs only two or three hits), and red only when the target is a one-hit lethal target. This prevents the UI from presenting contradictory colors.
