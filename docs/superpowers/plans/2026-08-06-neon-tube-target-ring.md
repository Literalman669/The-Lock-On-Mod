# Neon Tube Target Ring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the flat active-target outline with a vivid, layered neon tube ring that preserves target-state colors, health information, and reduced-motion behavior.

**Architecture:** Keep `RingGeometry` as the safe entity-relative geometry source. `NeonRingAppearance` provides aura/core/highlight sizes, and `TorusGeometry` maps a circular tube cross-section into radial and vertical offsets; `TargetRingRenderer` renders those offsets as fixed-size quad meshes, using additive blending only for the aura.

**Tech Stack:** Java 8, Minecraft Forge 1.12.2, fixed-function OpenGL through `GlStateManager`, `Tessellator`, `BufferBuilder`, and JUnit 4.

## Global Constraints

- Preserve the current configuration schema; this upgrade has no new user-facing config values.
- Preserve the existing 32 ring segments and current target-status color sources.
- Do not add shaders, textures, particles, or world-lighting changes.
- Keep the renderer's current exception boundary and restore all OpenGL state in `finally`.
- Reduced-motion mode must retain the neon appearance while avoiding time-based pulse changes.

---

### Task 1: Create the pure neon ring appearance model

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearance.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearanceTest.java`

**Interfaces:**

- Consumes: `RingGeometry` through `NeonRingAppearance.from(RingGeometry geometry)`.
- Produces: `getAuraHalfThickness()`, `getCoreHalfThickness()`, `getHighlightHalfThickness()`, `getAuraAlphaMultiplier()`, `getCoreAlphaMultiplier()`, and `getHighlightAlphaMultiplier()` for `TargetRingRenderer`.

- [ ] **Step 1: Write the failing test**

```java
@Test
public void derivesAThickNeonAuraCoreAndHighlight() {
    NeonRingAppearance appearance = NeonRingAppearance.from(
        RingGeometry.create(1.0D, 2.0D, 4.0D, 1.0F, false)
    );

    assertTrue(appearance.getAuraHalfThickness() > appearance.getCoreHalfThickness());
    assertTrue(appearance.getCoreHalfThickness() > appearance.getHighlightHalfThickness());
    assertTrue(appearance.getAuraAlphaMultiplier() < appearance.getCoreAlphaMultiplier());
    assertTrue(appearance.getHighlightAlphaMultiplier() >= appearance.getCoreAlphaMultiplier());
}

@Test
public void clampsEveryBandAndAlphaForInvalidGeometry() {
    NeonRingAppearance appearance = NeonRingAppearance.from(
        RingGeometry.create(Double.NaN, Double.NaN, Double.NaN, Float.NaN, true)
    );

    assertTrue(Double.isFinite(appearance.getAuraHalfThickness()));
    assertTrue(Double.isFinite(appearance.getCoreHalfThickness()));
    assertTrue(Double.isFinite(appearance.getHighlightHalfThickness()));
    assertTrue(appearance.getHighlightHalfThickness() > 0.0D);
    assertTrue(appearance.getAuraAlphaMultiplier() >= 0.0F);
    assertTrue(appearance.getAuraAlphaMultiplier() <= 1.0F);
    assertTrue(appearance.getCoreAlphaMultiplier() >= 0.0F);
    assertTrue(appearance.getCoreAlphaMultiplier() <= 1.0F);
    assertTrue(appearance.getHighlightAlphaMultiplier() >= 0.0F);
    assertTrue(appearance.getHighlightAlphaMultiplier() <= 1.0F);
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.NeonRingAppearanceTest" --no-daemon`

Expected: compilation fails because `NeonRingAppearance` does not exist.

- [ ] **Step 3: Write the minimal implementation**

```java
public final class NeonRingAppearance {
    public static NeonRingAppearance from(RingGeometry geometry) {
        double radius = geometry == null ? 0.5D : geometry.getHalfWidth();
        double core = clamp(radius * 0.09D, 0.045D, 0.16D);
        return new NeonRingAppearance(
            clamp(core * 2.8D, 0.10D, 0.34D),
            core,
            clamp(core * 0.35D, 0.018D, 0.06D),
            0.42F,
            0.92F,
            1.0F
        );
    }
}
```

Implement a private constructor, the six listed getters, and `clamp(double, double, double)` so every public value is finite, positive for thicknesses, and in `[0, 1]` for alpha multipliers.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.NeonRingAppearanceTest" --no-daemon`

Expected: `NeonRingAppearanceTest` passes with zero failures.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearance.java src/test/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearanceTest.java
git commit -m "feat: add neon ring appearance model"
```

### Task 2: Render the true three-dimensional neon tube in the world

**Files:**

- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/TorusGeometry.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearanceTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/TorusGeometryTest.java`

**Interfaces:**

- Consumes: `NeonRingAppearance.from(geometry)`, `NeonRingAppearance.scale(double factor)`, and `TorusGeometry.create(double majorRadius, double tubeRadius)`.
- Produces: volumetric aura, core, highlight, and health-arc tube meshes while retaining `drawCornerBrackets` and `drawStatusMarker`.

- [ ] **Step 1: Write a failing geometry test for the tube cross-section**

```java
@Test
public void createsARealTubeCrossSectionAroundTheRing() {
    TorusGeometry geometry = TorusGeometry.create(1.5D, 0.18D);

    assertEquals(1.68D, geometry.getRadialDistance(0.0D), 0.000001D);
    assertEquals(0.18D, geometry.getVerticalOffset(Math.PI / 2.0D), 0.000001D);
    assertEquals(-0.18D, geometry.getVerticalOffset(-Math.PI / 2.0D), 0.000001D);
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.NeonRingAppearanceTest" --no-daemon`

Expected: compilation fails because `TorusGeometry` does not exist.

- [ ] **Step 3: Implement the renderer integration**

```java
NeonRingAppearance appearance = NeonRingAppearance.from(geometry);
TorusGeometry tube = TorusGeometry.create(halfWidth, appearance.getCoreHalfThickness());
GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
drawTorus(x, y, z, halfWidth, appearance.getAuraHalfThickness(), ringColor,
    alpha * appearance.getAuraAlphaMultiplier(), 0.10F, 0.0D, 360.0D);

GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
drawTorus(x, y, z, tube.getMajorRadius(), tube.getTubeRadius(), ringColor,
    alpha * appearance.getCoreAlphaMultiplier(), 0.58F, 0.0D, 360.0D);
```

Add `drawTorus(double x, double y, double z, double radius, double tubeRadius, int ringColor, float alpha, float highlightStrength, double startDegrees, double spanDegrees)`. It must render `max(1, round(SEGMENTS * spanDegrees / 360)) * 8` quad faces using `DefaultVertexFormats.POSITION_COLOR`. Each cross-section vertex uses `TorusGeometry.getRadialDistance` and `getVerticalOffset`; its color is darkened underneath and mixed toward white on top. Use the same helper for the raised health arc.

Add `NeonRingAppearance scale(double factor)` before the renderer integration. It returns a new appearance with each half-thickness multiplied by a finite factor clamped to `[0.1D, 1.0D]` and preserves the three alpha multipliers. The raised health arc uses `appearance.scale(0.35D)`.

Add a private `mixColor(int source, int target, float targetWeight)` that linearly interpolates each RGB channel, returns an opaque ARGB value, and clamps `targetWeight` to `[0, 1]`. Retain the existing alpha handling in `color`.

Leave the pulse expression intact for normal mode; construct `RingGeometry` with the actual reduced-motion setting if one exists at render time, otherwise retain the current false argument until the later configuration migration. Do not add a new option in this task.

- [ ] **Step 4: Run the focused and full automated verification**

Run: `./gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.NeonRingAppearanceTest" --no-daemon`

Expected: all `NeonRingAppearanceTest` cases pass.

Run: `./gradlew.bat test build reobfJar --rerun-tasks --no-daemon`

Expected: `BUILD SUCCESSFUL` and a reobfuscated JAR under `build/libs`.

- [ ] **Step 5: Perform visual verification in the dev client**

Run: `Start-Process -FilePath ".\\gradlew.bat" -ArgumentList "runClient" -WorkingDirectory (Get-Location) -WindowStyle Hidden`

Verify in-game:

1. Lock a healthy target: aura, core, and inner highlight visibly read as a neon tube.
2. Damage a target until it is low health and then lethal: ring uses warning and lethal colors without losing the health arc.
3. Toggle reduced-motion behavior when it becomes available: the neon bands remain but pulse is static.
4. Check close and distant targets: ring stays smooth, centered, and does not obscure the target.
5. Unlock and relock repeatedly: no broken OpenGL state, disappearing HUD, or console exceptions.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java src/main/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearance.java src/test/java/com/zeldatargeting/mod/client/presentation/core/NeonRingAppearanceTest.java
git commit -m "feat: render neon tube target ring"
```

## Corrective implementation amendment

The visual review showed that three physical tubes in the same plane looked cramped. The finished renderer instead uses a close low-opacity glow shell, one vertex-lit core tube, and a narrow health-arc tube elevated above the core. `NeonRingAppearance` now exposes `getHealthArcHalfThickness()` and `getHealthArcVerticalOffset()` rather than a raised white highlight or a scaled nested health ring. Lethal state uses a diamond marker, avoiding any additional concentric circle.

## Final implementation amendment

The elevated health arc was removed after visual testing because a full-health target still displayed a competing second ring. `RingColorPolicy` now drives both the active tube and target marker: healthy is `PresentationPalette.DEFAULT.getHealthColor()`, warning/low health is `getWarningColor()`, lethal is `getLethalColor()`, and occluded is muted gray. `NeonRingAppearance` now contains only compact glow-shell and primary-tube dimensions.

## Final implementation clarification

The remaining primary tube receives `RingGeometry.getHealthArcDegrees()` as its span, starting at the top of the target. It therefore drains with health rather than remaining a full circle. `PresentationStatusResolver` establishes a warning state at 75% health, preserves one-hit lethal as the red override, and remains the sole input to `RingColorPolicy`. Both panel renderers use that same policy for their accent and health-bar fills, keeping the ring, marker, and panels synchronized.
