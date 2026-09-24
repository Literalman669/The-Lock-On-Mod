# Zelda Targeting 1.4.0 Stage 4 Presentation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the monolithic target renderer with an Adaptive Hybrid presentation system: Classic Ring, responsive compact detail panel, conditional boss panel, and deduplicated presentation feedback.

**Architecture:** Pure presentation calculations live in `client.presentation.core`; they have no Minecraft, Forge, LWJGL, or OpenGL imports. Minecraft sampling and OpenGL rendering live behind small runtime components. `TargetRenderer` remains the single Forge event subscriber but coordinates one immutable target snapshot per event instead of doing selection, layout, feedback, and rendering itself.

**Tech Stack:** Java 8, Minecraft 1.12.2, Forge 14.23.5.2859, Gradle 4.9 wrapper, JUnit 4.13.2.

## Global Constraints

- Remain client-only and compatible with unmodified multiplayer servers.
- Preserve the current 1.3.0 persisted configuration schema and current configuration controls; Stage 5 owns the replacement schema and preset selector.
- Use Classic Ring as the only Stage 4 selectable/default theme. Segmented and Tactical themes remain Stage 5 work.
- Preserve existing HUD anchor, X/Y offset, visibility toggles, damage-number behavior, and per-event sound controls.
- Do not change target selection, lock validity, camera ownership, player movement, combat damage, reach, or server-authoritative state.
- Do not add ASM, a coremod, an access transformer, or runtime dependencies.
- Keep world and overlay rendering constant-time; do not create collections per frame or query the targeting manager independently from each renderer.
- Pure presentation classes must not import `net.minecraft`, `net.minecraftforge`, `org.lwjgl`, or OpenGL types.
- Every behavioral change follows a red-green test cycle and every task ends with a focused commit.

## File Structure

- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelAnchor.java`: parsed anchor enum for existing configuration strings.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayout.java`: immutable safe-area panel bounds.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculator.java`: pure panel anchoring and screen clamping.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/BossEligibility.java`: deterministic boss-panel decision.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatus.java`: non-color presentation states.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatusResolver.java`: pure health/hits/occlusion state mapping.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationPalette.java`: built-in default and color-vision-safe colors.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/RingGeometry.java`: immutable Classic Ring bounds and health-arc geometry.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackEvent.java`: bit-mask constants for one-shot feedback edges.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackState.java`: transition and status edge deduplication.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/TargetPresentationSnapshot.java`: Minecraft-facing immutable target sample.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/TargetPresentationSnapshotFactory.java`: one-time snapshot sampler.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/PresentationFeedbackController.java`: runtime bridge for transition, damage, and status feedback.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java`: world-space Classic Ring renderer.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/render/DetailPanelRenderer.java`: measured compact overlay renderer.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/render/BossPanelRenderer.java`: safe-area boss overlay renderer.
- Create `src/main/java/com/zeldatargeting/mod/client/presentation/render/SoftAimRenderer.java` and `TargetHistoryRenderer.java`: extracted existing feature renderers.
- Modify `src/main/java/com/zeldatargeting/mod/client/render/TargetRenderer.java`: reduce to snapshot/event coordination only.
- Modify `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`: forward targeting transitions to the presentation feedback controller instead of directly playing lock/switch/lost feedback.
- Modify `src/main/java/com/zeldatargeting/mod/client/render/DamageNumbersRenderer.java`: forward critical/lethal damage edges to the feedback controller while retaining current damage-number behavior.
- Create pure JUnit tests in `src/test/java/com/zeldatargeting/mod/client/presentation/core/`.
- Create `docs/development/stage-4-presentation-smoke-test.md`: reproducible manual acceptance matrix and log baseline.

---

### Task 1: Build Safe Compact-Panel Layout and Boss Eligibility

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelAnchor.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayout.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculator.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/BossEligibility.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculatorTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/BossEligibilityTest.java`

**Interfaces:**
- Consumes: existing `TargetingConfig.hudAnchor`, `hudOffsetX`, `hudOffsetY`, and `bossStylePanel` values at the runtime boundary only.
- Produces: `PanelAnchor.fromConfig(String)`, `PanelLayoutCalculator.calculate(int, int, int, int, PanelAnchor, int, int, int)`, and `BossEligibility.isEligible(boolean, float, float)` for overlay renderers.

- [ ] **Step 1: Write the failing layout and eligibility tests**

```java
@Test
public void topRightLayoutHonorsOffsetButStaysInsideTheSafeArea() {
    PanelLayout layout = PanelLayoutCalculator.calculate(
        320, 240, 130, 68, PanelAnchor.TOP_RIGHT, 500, -500, 8
    );

    assertEquals(182, layout.getX());
    assertEquals(8, layout.getY());
    assertEquals(130, layout.getWidth());
    assertEquals(68, layout.getHeight());
}

@Test
public void bossEligibilityRequiresBossFlagOrConfiguredHealthThreshold() {
    assertTrue(BossEligibility.isEligible(true, 20.0F, 100.0F));
    assertTrue(BossEligibility.isEligible(false, 100.0F, 100.0F));
    assertFalse(BossEligibility.isEligible(false, 99.9F, 100.0F));
    assertFalse(BossEligibility.isEligible(false, Float.NaN, 100.0F));
}
```

- [ ] **Step 2: Run the focused tests to verify red**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PanelLayoutCalculatorTest" --tests "com.zeldatargeting.mod.client.presentation.core.BossEligibilityTest" --no-daemon
```

Expected: `compileTestJava` fails because the presentation-core classes do not exist.

- [ ] **Step 3: Implement the pure layout types**

```java
public enum PanelAnchor {
    TOP_RIGHT, TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER;

    public static PanelAnchor fromConfig(String value) {
        if ("top-left".equals(value)) return TOP_LEFT;
        if ("bottom-left".equals(value)) return BOTTOM_LEFT;
        if ("bottom-right".equals(value)) return BOTTOM_RIGHT;
        if ("center".equals(value)) return CENTER;
        return TOP_RIGHT;
    }
}

public static PanelLayout calculate(
        int screenWidth, int screenHeight, int panelWidth, int panelHeight,
        PanelAnchor anchor, int offsetX, int offsetY, int margin) {
    int safeMargin = Math.max(0, margin);
    int width = Math.max(0, Math.min(Math.max(0, screenWidth - safeMargin * 2), panelWidth));
    int height = Math.max(0, Math.min(Math.max(0, screenHeight - safeMargin * 2), panelHeight));
    int maxX = Math.max(safeMargin, screenWidth - safeMargin - width);
    int maxY = Math.max(safeMargin, screenHeight - safeMargin - height);
    int baseX = anchor == PanelAnchor.TOP_RIGHT || anchor == PanelAnchor.BOTTOM_RIGHT ? maxX
        : anchor == PanelAnchor.CENTER ? (screenWidth - width) / 2 : safeMargin;
    int baseY = anchor == PanelAnchor.BOTTOM_LEFT || anchor == PanelAnchor.BOTTOM_RIGHT ? maxY
        : anchor == PanelAnchor.CENTER ? (screenHeight - height) / 2 : safeMargin;
    return new PanelLayout(clamp(baseX + offsetX, safeMargin, maxX),
        clamp(baseY + offsetY, safeMargin, maxY), width, height);
}

private static int clamp(int value, int minimum, int maximum) {
    return Math.max(minimum, Math.min(value, maximum));
}

public static boolean isEligible(boolean vanillaBoss, float maxHealth, float threshold) {
    return vanillaBoss || (Float.isFinite(maxHealth) && Float.isFinite(threshold)
        && maxHealth >= Math.max(1.0F, threshold));
}
```

`PanelLayout` exposes `getX()`, `getY()`, `getWidth()`, and `getHeight()` only. `PanelLayoutCalculator` must treat a missing anchor as `TOP_RIGHT`, keep the entire panel inside the margin, and never return negative dimensions.

- [ ] **Step 4: Run the focused tests to verify green**

Run the command from Step 2.

Expected: both test classes pass with no failures or errors.

- [ ] **Step 5: Commit the pure layout unit**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelAnchor.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayout.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculator.java src/main/java/com/zeldatargeting/mod/client/presentation/core/BossEligibility.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculatorTest.java src/test/java/com/zeldatargeting/mod/client/presentation/core/BossEligibilityTest.java
git commit -m "feat: add presentation panel layout core"
```

### Task 2: Define Classic-Ring Geometry and Non-Color Status

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatus.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatusResolver.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationPalette.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/RingGeometry.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatusResolverTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationPaletteTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/RingGeometryTest.java`

**Interfaces:**
- Consumes: target width, height, distance, health ratio, hits-to-kill, and occlusion-grace booleans as scalar values.
- Produces: normalized `PresentationStatus` and a `RingGeometry` with half-width, half-height, health-arc degrees, marker scale, and alpha.

- [ ] **Step 1: Write failing status and ring-geometry tests**

```java
@Test
public void lethalAndLowHealthUseDistinctNonColorStatuses() {
    assertEquals(PresentationStatus.LETHAL,
        PresentationStatusResolver.resolve(0.50F, 1, false));
    assertEquals(PresentationStatus.LOW_HEALTH,
        PresentationStatusResolver.resolve(0.20F, 4, false));
    assertEquals(PresentationStatus.OCCLUDED,
        PresentationStatusResolver.resolve(0.90F, 4, true));
}

@Test
public void namedPalettesHaveDefaultFallbackAndFiniteArgbColors() {
    assertSame(PresentationPalette.DEFAULT, PresentationPalette.fromName(null));
    assertSame(PresentationPalette.DEUTERANOPIA, PresentationPalette.fromName("deuteranopia"));
    assertNotEquals(0, PresentationPalette.TRITANOPIA.getWarningColor());
}

@Test
public void ringGeometryClampsSizeAndHealthArc() {
    RingGeometry geometry = RingGeometry.create(0.6D, 12.0D, 2.0D, 0.25F, false);

    assertEquals(0.60D, geometry.getHalfWidth(), 0.000001D);
    assertEquals(2.00D, geometry.getHalfHeight(), 0.000001D);
    assertEquals(90.0F, geometry.getHealthArcDegrees(), 0.0001F);
    assertTrue(geometry.getAlpha() > 0.0F);
}
```

- [ ] **Step 2: Run the focused tests to verify red**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PresentationStatusResolverTest" --tests "com.zeldatargeting.mod.client.presentation.core.PresentationPaletteTest" --tests "com.zeldatargeting.mod.client.presentation.core.RingGeometryTest" --no-daemon
```

Expected: `compileTestJava` fails because the status and geometry APIs do not exist.

- [ ] **Step 3: Implement normalized geometry and status mapping**

```java
public enum PresentationStatus {
    NORMAL, WARNING, LOW_HEALTH, LETHAL, OCCLUDED
}

public enum PresentationPalette {
    DEFAULT(0xFF4CD964, 0xFFFFA340, 0xFFFF4B4B),
    DEUTERANOPIA(0xFF56B4E9, 0xFFE69F00, 0xFFCC79A7),
    PROTANOPIA(0xFF56B4E9, 0xFFF0E442, 0xFFCC79A7),
    TRITANOPIA(0xFF009E73, 0xFFD55E00, 0xFFCC79A7);

    private final int healthColor;
    private final int warningColor;
    private final int lethalColor;

    PresentationPalette(int healthColor, int warningColor, int lethalColor) {
        this.healthColor = healthColor;
        this.warningColor = warningColor;
        this.lethalColor = lethalColor;
    }

    public int getHealthColor() { return healthColor; }
    public int getWarningColor() { return warningColor; }
    public int getLethalColor() { return lethalColor; }

    public static PresentationPalette fromName(String value) {
        for (PresentationPalette palette : values()) {
            if (palette.name().equalsIgnoreCase(value)) return palette;
        }
        return DEFAULT;
    }
}

public static PresentationStatus resolve(float healthRatio, int hitsToKill, boolean occluded) {
    if (occluded) return PresentationStatus.OCCLUDED;
    if (hitsToKill == 1) return PresentationStatus.LETHAL;
    if (!Float.isFinite(healthRatio)) return PresentationStatus.NORMAL;
    if (healthRatio <= 0.25F) return PresentationStatus.LOW_HEALTH;
    if (hitsToKill > 1 && hitsToKill <= 3) return PresentationStatus.WARNING;
    return PresentationStatus.NORMAL;
}

public static RingGeometry create(
        double width, double height, double distance, float healthRatio, boolean reducedMotion) {
    double safeWidth = Double.isFinite(width) ? Math.max(0.5D, Math.min(3.0D, width)) : 0.5D;
    double safeHeight = Double.isFinite(height) ? Math.max(0.5D, Math.min(2.0D, height / 2.0D)) : 0.5D;
    double safeDistance = Double.isFinite(distance) ? Math.max(0.0D, distance) : 0.0D;
    float ratio = Float.isFinite(healthRatio) ? Math.max(0.0F, Math.min(1.0F, healthRatio)) : 1.0F;
    float alpha = (float) Math.max(0.35D, Math.min(1.0D, 1.0D - safeDistance / 160.0D));
    float motionScale = reducedMotion ? 1.0F : 1.04F;
    return new RingGeometry(safeWidth, safeHeight,
        ratio * 360.0F, alpha, motionScale);
}
```

Use four bracket corners for every status. `LETHAL` adds an inner marker, `LOW_HEALTH` changes marker geometry, and `OCCLUDED` reduces alpha. `RingGeometry` must be immutable and reject no ordinary runtime input; invalid scalar inputs normalize to safe values.

- [ ] **Step 4: Run the focused tests to verify green**

Run the command from Step 2.

Expected: both classes pass with no failures or errors.

- [ ] **Step 5: Commit ring/status core**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatus.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatusResolver.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationPalette.java src/main/java/com/zeldatargeting/mod/client/presentation/core/RingGeometry.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationStatusResolverTest.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationPaletteTest.java src/test/java/com/zeldatargeting/mod/client/presentation/core/RingGeometryTest.java
git commit -m "feat: add classic ring presentation core"
```

### Task 3: Deduplicate Transition-Driven Presentation Feedback

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackEvent.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackState.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackStateTest.java`

**Interfaces:**
- Consumes: lock transition ID, `LockPhase`, `PresentationStatus`, and explicit critical/lethal damage-edge flags.
- Produces: an integer bit mask of `LOCK`, `SWITCH`, `LOST`, `LOW_HEALTH`, `CRITICAL`, and `LETHAL` event flags; zero means no new feedback edge.

- [ ] **Step 1: Write the failing feedback-state tests**

```java
@Test
public void stableFramesDoNotReplayFeedbackButNewTransitionsDo() {
    PresentationFeedbackState state = new PresentationFeedbackState();

    assertEquals(PresentationFeedbackEvent.LOCK,
        state.advance(4L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
    assertEquals(PresentationFeedbackEvent.NONE,
        state.advance(4L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
    assertEquals(PresentationFeedbackEvent.SWITCH,
        state.advance(5L, LockPhase.SWITCHING, PresentationStatus.NORMAL));
}

@Test
public void lowHealthAndDamageEventsOnlyFireOnTheirEnteringEdge() {
    PresentationFeedbackState state = new PresentationFeedbackState();

    assertEquals(PresentationFeedbackEvent.LOW_HEALTH,
        state.advance(6L, LockPhase.LOCKED, PresentationStatus.LOW_HEALTH));
    assertEquals(PresentationFeedbackEvent.NONE,
        state.advance(6L, LockPhase.LOCKED, PresentationStatus.LOW_HEALTH));
    assertEquals(PresentationFeedbackEvent.CRITICAL | PresentationFeedbackEvent.LETHAL,
        state.damage(true, true));
}
```

- [ ] **Step 2: Run the focused test to verify red**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PresentationFeedbackStateTest" --no-daemon
```

Expected: `compileTestJava` fails because the feedback types do not exist.

- [ ] **Step 3: Implement explicit feedback edges**

```java
public final class PresentationFeedbackEvent {
    public static final int NONE = 0;
    public static final int LOCK = 1;
    public static final int SWITCH = 1 << 1;
    public static final int LOST = 1 << 2;
    public static final int LOW_HEALTH = 1 << 3;
    public static final int CRITICAL = 1 << 4;
    public static final int LETHAL = 1 << 5;
}

public int advance(long transitionId, LockPhase phase, PresentationStatus status) {
    int events = PresentationFeedbackEvent.NONE;
    if (transitionId != lastTransitionId) {
        if (phase == LockPhase.ACQUIRING) events |= PresentationFeedbackEvent.LOCK;
        if (phase == LockPhase.SWITCHING) events |= PresentationFeedbackEvent.SWITCH;
        if (phase == LockPhase.RELEASING) events |= PresentationFeedbackEvent.LOST;
        lastTransitionId = transitionId;
    }
    if (status == PresentationStatus.LOW_HEALTH && lastStatus != PresentationStatus.LOW_HEALTH) {
        events |= PresentationFeedbackEvent.LOW_HEALTH;
    }
    lastStatus = status;
    return events;
}

public int damage(boolean critical, boolean lethal) {
    return (critical ? PresentationFeedbackEvent.CRITICAL : 0)
        | (lethal ? PresentationFeedbackEvent.LETHAL : 0);
}

public void clear() {
    lastTransitionId = Long.MIN_VALUE;
    lastStatus = PresentationStatus.NORMAL;
}
```

`clear()` resets retained transition and status state. A release phase returns `LOST` only for a new transition. No method allocates a collection.

- [ ] **Step 4: Run the focused test to verify green**

Run the command from Step 2.

Expected: the test class passes with no failures or errors.

- [ ] **Step 5: Commit presentation feedback core**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackEvent.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackState.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackStateTest.java
git commit -m "feat: add deduplicated presentation feedback state"
```

### Task 4: Create Snapshot-Based Ring and Panel Renderers

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/TargetPresentationSnapshot.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/TargetPresentationSnapshotFactory.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/render/DetailPanelRenderer.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/render/BossPanelRenderer.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/render/SoftAimRenderer.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetHistoryRenderer.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/render/TargetRenderer.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculatorTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/RingGeometryTest.java`

**Interfaces:**
- Consumes: `TargetingManager.getLockSnapshot()`, existing `TargetingConfig` presentation fields, `DamageCalculator`, `ScaledResolution`, and the pure types from Tasks 1 and 2.
- Produces: one `TargetPresentationSnapshot` per render event and independent renderer calls that receive only that snapshot plus event-specific render context.

- [ ] **Step 1: Extend the existing pure tests with renderer-boundary cases**

```java
@Test
public void bottomLeftLayoutUsesTheSameSafeMarginAtHighGuiScale() {
    PanelLayout layout = PanelLayoutCalculator.calculate(
        320, 240, 160, 80, PanelAnchor.BOTTOM_LEFT, -400, 400, 8
    );

    assertEquals(8, layout.getX());
    assertEquals(152, layout.getY());
}

@Test
public void invalidRingInputsStayFiniteForTheOpenGlBoundary() {
    RingGeometry geometry = RingGeometry.create(Double.NaN, -1.0D, Double.NaN, Float.NaN, true);

    assertTrue(Double.isFinite(geometry.getHalfWidth()));
    assertTrue(Double.isFinite(geometry.getHalfHeight()));
    assertTrue(Float.isFinite(geometry.getAlpha()));
}
```

- [ ] **Step 2: Run the focused tests to verify red**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PanelLayoutCalculatorTest" --tests "com.zeldatargeting.mod.client.presentation.core.RingGeometryTest" --no-daemon
```

Expected: at least one assertion fails until the pure calculations meet the additional safe-boundary contract.

- [ ] **Step 3: Implement snapshot and focused renderers, then reduce TargetRenderer to coordination**

```java
public final class TargetPresentationSnapshot {
    public Entity getTarget();
    public String getName();
    public double getInterpolatedX();
    public double getInterpolatedY();
    public double getInterpolatedZ();
    public float getWidth();
    public float getHeight();
    public float getHealthRatio();
    public float getMaxHealth();
    public double getDistance();
    public float getPredictedDamage();
    public int getHitsToKill();
    public String getVulnerabilityText();
    public boolean isVanillaBoss();
    public long getTransitionId();
    public LockPhase getLockPhase();
    public PresentationStatus getStatus();
}

public TargetPresentationSnapshot create(TargetingManager manager, float partialTicks) {
    LockOnSnapshot<EntityLivingBase> lock = manager.getLockSnapshot();
    if (lock == null || !lock.isPresentationVisible() || mc.player == null || mc.world == null) return null;
    EntityLivingBase target = lock.getTarget() == null ? null : lock.getTarget().getReference();
    if (target == null || !target.isEntityAlive()) return null;
    return buildSnapshot(lock, target, partialTicks);
}
```

`TargetRingRenderer.render(snapshot, partialTicks)` owns world-space GL geometry, uses `PresentationPalette.DEFAULT`, and always restores matrix, depth, texture, blend, and color state. `DetailPanelRenderer.render(snapshot, resolution)` builds the enabled existing lines, calculates `PanelLayout`, and clamps it to the scaled safe area. `BossPanelRenderer.render(snapshot, resolution)` first checks `bossStylePanel` and `BossEligibility`; it uses a top-safe layout and never replaces the ring. Each component catches a `RuntimeException`, writes one component-scoped warning through `ZeldaTargetingMod.getLogger()`, and skips only its own frame. Move the existing soft-aim and target-history methods into their named renderers without changing their configuration keys or behavior.

Add this manager accessor without exposing the mutable `TargetingService`:

```java
public LockOnSnapshot<EntityLivingBase> getLockSnapshot() {
    return targetingService.snapshot();
}
```

In `TargetRenderer`, remove direct calls to `DamageCalculator` and all panel/ring drawing helpers. Each Forge event obtains one snapshot and forwards it to the required components. Do not retain a per-frame `List<String>` or `List<Integer>`.

- [ ] **Step 4: Run the focused tests and compile the Forge-facing integration**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PanelLayoutCalculatorTest" --tests "com.zeldatargeting.mod.client.presentation.core.RingGeometryTest" --no-daemon
.\gradlew.bat compileJava --no-daemon
```

Expected: focused tests pass and `compileJava` succeeds.

- [ ] **Step 5: Commit renderer separation**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/presentation src/main/java/com/zeldatargeting/mod/client/render/TargetRenderer.java src/main/java/com/zeldatargeting/mod/client/TargetingManager.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PanelLayoutCalculatorTest.java src/test/java/com/zeldatargeting/mod/client/presentation/core/RingGeometryTest.java
git commit -m "refactor: split adaptive target presentation renderers"
```

### Task 5: Connect One-Shot Feedback and Verify Stage 4 In Game

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/presentation/PresentationFeedbackController.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/render/DamageNumbersRenderer.java`
- Create: `docs/development/stage-4-presentation-smoke-test.md`
- Test: `src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackStateTest.java`

**Interfaces:**
- Consumes: `LockOnSnapshot<EntityLivingBase>` transitions in `TargetingManager`, `LivingHurtEvent` damage outcomes, `TargetingSounds`, and `PresentationFeedbackState` from Task 3.
- Produces: one lock/switch/lost sound edge per lock transition and visual feedback bits for critical, lethal, and low-health events.

- [ ] **Step 1: Extend the feedback test with release reset behavior**

```java
@Test
public void clearAllowsTheNextLockSessionToEmitItsInitialLockEvent() {
    PresentationFeedbackState state = new PresentationFeedbackState();

    assertEquals(PresentationFeedbackEvent.LOCK,
        state.advance(9L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
    state.clear();
    assertEquals(PresentationFeedbackEvent.LOCK,
        state.advance(9L, LockPhase.ACQUIRING, PresentationStatus.NORMAL));
}
```

- [ ] **Step 2: Run the focused test to verify red**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.presentation.core.PresentationFeedbackStateTest" --no-daemon
```

Expected: the new test fails until `clear()` resets all retained transition and status state.

- [ ] **Step 3: Add the runtime feedback bridge and replace direct feedback calls**

```java
public final class PresentationFeedbackController {
    private int pendingVisualEvents;

    public void onTransition(LockOnSnapshot<EntityLivingBase> snapshot) {
        EntityLivingBase target = snapshot.getTarget() == null ? null : snapshot.getTarget().getReference();
        int events = state.advance(
            snapshot.getTransitionId(), snapshot.getPhase(), PresentationStatus.NORMAL
        );
        if ((events & PresentationFeedbackEvent.LOCK) != 0 && target != null) TargetingSounds.playTargetLockSound(target);
        if ((events & PresentationFeedbackEvent.SWITCH) != 0) TargetingSounds.playTargetSwitchSound();
        if ((events & PresentationFeedbackEvent.LOST) != 0) TargetingSounds.playTargetLostSound();
    }

    public void onPresentationStatus(long transitionId, PresentationStatus status) {
        pendingVisualEvents |= state.advance(transitionId, LockPhase.LOCKED, status)
            & PresentationFeedbackEvent.LOW_HEALTH;
    }

    public void onDamage(boolean critical, boolean lethal) {
        pendingVisualEvents |= state.damage(critical, lethal);
    }

    public int consumeVisualEvents() {
        int events = pendingVisualEvents;
        pendingVisualEvents = PresentationFeedbackEvent.NONE;
        return events;
    }

    public void clear() {
        state.clear();
        pendingVisualEvents = PresentationFeedbackEvent.NONE;
    }
}
```

`TargetingManager` owns one `PresentationFeedbackController`, delegates lock/switch/lost feedback to it after target validation, and exposes `getPresentationFeedbackController()` to the render coordinator. `TargetRenderer` forwards each snapshot's transition ID and status to `onPresentationStatus`. `DamageNumbersRenderer.onLivingHurt` invokes `onDamage(isCritical, isLethal)` once after it accepts an event for display. World/lifecycle clear paths call `PresentationFeedbackController.clear()`. Existing sound enable, volume, and pitch values remain enforced by `TargetingSounds`.

Create the smoke-test document with these required checks:

1. Normal target: Classic Ring health arc, brackets, compact panel, and existing line toggles.
2. GUI Scale Auto and the highest usable scale at all five existing HUD anchors with extreme saved offsets; no clipping or off-screen panel.
3. A boss or 100+ health target with boss panel off/on; ring stays active and panel avoids vanilla boss-bar space.
4. Rapid cycling, target death, and reacquisition; exactly one lock/switch/lost sound per transition with no repeated feedback on a stationary target.
5. Low-health and lethal target states; marker shape remains distinct without relying on color.
6. Soft aim and target-history options preserve current behavior.

- [ ] **Step 4: Run automated verification and the manual smoke run**

Run:

```powershell
.\gradlew.bat clean test build reobfJar --no-daemon
rg -n "net\.minecraft|net\.minecraftforge|org\.lwjgl|org\.lwjgl\.opengl" src/main/java/com/zeldatargeting/mod/client/presentation/core
git diff --check
```

Expected: the Gradle command succeeds; the forbidden-import scan returns no matches; `git diff --check` is silent.

Before launching, record a log baseline:

```powershell
$logPath = 'run/logs/latest.log'
$baselineLines = if (Test-Path $logPath) { (Get-Content $logPath).Count } else { 0 }
"baselineLines=$baselineLines"
```

After the user completes all six smoke checks, inspect only appended lines:

```powershell
$baselineLines = 0
$appended = Get-Content 'run/logs/latest.log' | Select-Object -Skip $baselineLines
$appended | Select-String -Pattern 'zeldatargeting|Exception|ERROR|FATAL|OpenGL|GL_INVALID|camera'
```

Replace `0` with the recorded baseline. New presentation exceptions, GL-state errors, repeated feedback messages, or camera regressions fail the smoke run.

- [ ] **Step 5: Record results and commit Stage 4 acceptance**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/presentation/PresentationFeedbackController.java src/main/java/com/zeldatargeting/mod/client/TargetingManager.java src/main/java/com/zeldatargeting/mod/client/render/DamageNumbersRenderer.java src/test/java/com/zeldatargeting/mod/client/presentation/core/PresentationFeedbackStateTest.java docs/development/stage-4-presentation-smoke-test.md
git commit -m "feat: complete adaptive presentation feedback"
```

Update the smoke-test document with the artifact SHA-256, full test count, six manual results, and appended-log findings before the commit.
