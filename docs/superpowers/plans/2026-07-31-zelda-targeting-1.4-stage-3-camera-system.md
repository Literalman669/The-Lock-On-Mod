# Zelda Targeting 1.4.0 Stage 3 Camera System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy Minecraft-coupled camera controller with a testable profile-driven camera core, vanilla Forge adapter, temporary free look, collision-safe framing, close-camera behavior, and unified restoration.

**Architecture:** Plain Java core classes own profiles, framing, collision, interpolation, perspective fallback, and restoration. A generic coordinator drives those classes through a narrow runtime interface, while one vanilla adapter samples Minecraft state and applies frames through Forge render events. `TargetingManager` forwards immutable snapshots and no longer owns camera state.

**Tech Stack:** Java 8, Minecraft 1.12.2, Forge 14.23.5.2859, Gradle 4.9 wrapper, JUnit 4.13.2.

## Global Constraints

- Remain client-only and compatible with unmodified multiplayer servers.
- Preserve the public mod version and existing configuration schema at 1.3.0.
- Use vanilla first and third person only in Stage 3; optional camera-mod adapters remain deferred to Stage 5.
- Do not add ASM, a coremod, an access transformer, or new runtime dependencies.
- Do not change server reach, hit detection, movement, or authoritative combat state.
- Keep client-tick and render-tick work constant-time with no avoidable collection allocation.
- Run each behavior change through a red-green test cycle and keep the branch buildable after every commit.
- Use Left Alt (`Keyboard.KEY_LMENU`) as the default hold binding for temporary free look.
- Normal restoration must not restore acquisition-time yaw or pitch.
- Lifecycle invalidation must restore presentation state immediately and idempotently.

## Stage 3 File Structure

### Snapshot metadata

- Modify `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java` to normalize and carry the selected camera profile name.
- Modify `src/main/java/com/zeldatargeting/mod/client/session/LockOnSession.java` to capture the profile on acquisition and preserve it through switches.
- Modify `src/main/java/com/zeldatargeting/mod/client/session/LockOnSnapshot.java` to publish the immutable profile name.
- Modify `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java` to pass profile metadata into acquisition.

### Pure camera core

- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfile.java` for complete immutable profile values.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfiles.java` for Cinematic, Balanced, Snappy, and name fallback.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFraming.java` for size, distance, FOV, alpha, and interpolation formulas.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSamples.java` for exactly eight scalar ray distances.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionResult.java` for desired-safe distance, vanilla-safe distance, and translation delta.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSolver.java` for collision selection and safety margin.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraRestoreMode.java` for eased versus immediate restoration.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveDecision.java` for idempotent perspective commands.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveController.java` for capture and fallback hysteresis.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraInput.java` for an immutable frame sample.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFrame.java` for an immutable output frame.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraDirector.java` as the sole owner of interpolated frame state.

### Coordinator and vanilla runtime

- Create `src/main/java/com/zeldatargeting/mod/client/camera/CameraProfileResolver.java` for profile lookup without configuration coupling.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeState.java` for captured perspective and baseline FOV.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeAdapter.java` as the generic runtime seam.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/CameraCoordinator.java` to coordinate snapshots, free look, GUI suspension, release, and lifecycle clear.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/LegacyCameraProfileAdapter.java` to map 1.3 camera fields.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraOffsetApplier.java` for camera-space Forge-event translation.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/CloseCameraTransparency.java` for local-player render alpha.
- Create `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraAdapter.java` for Minecraft sampling, ray tracing, frame application, and Forge camera/FOV/player events.
- Modify `src/main/java/com/zeldatargeting/mod/client/KeyBindings.java` and `src/main/resources/assets/zeldatargeting/lang/en_us.lang` for free look.
- Modify `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java` to use the coordinator and capture the profile in `TargetingOptions`.
- Delete `src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java` after integration.

---

### Task 1: Publish Camera Profile Metadata in Lock Snapshots

**Files:**

- Modify: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/session/LockOnSession.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/session/LockOnSnapshot.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java`
- Modify: `src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java`
- Modify: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java`

**Interfaces:**

- Consumes: existing `TargetPriority`, `TargetingOptions`, `LockOnSession<T>`, and `LockOnSnapshot<T>`.
- Produces: `TargetingOptions.getCameraProfileName()` and `LockOnSnapshot.getCameraProfileName()`, each returning one of `cinematic`, `balanced`, or `snappy`.

- [ ] **Step 1: Write failing profile-capture tests**

Add this acquisition assertion to `LockOnSessionTest` and use the new four-argument acquisition metadata signature:

```java
assertTrue(session.beginAcquire(
    observation("pig", 4, true, TargetAnchor.HEAD),
    TargetPriority.HEALTH,
    "CINEMATIC",
    100L
));
assertEquals("cinematic", session.snapshot().getCameraProfileName());

session.completeAcquire(101L);
session.beginSwitch(
    observation("cow", 5, true, TargetAnchor.CENTER),
    TargetPriority.NEAREST,
    120L
);
assertEquals("cinematic", session.snapshot().getCameraProfileName());
```

Add this service-level assertion to `TargetingServiceTest`:

```java
TargetingOptions cinematic = new TargetingOptions(
    TargetPriority.NEAREST,
    "CINEMATIC",
    20.0D,
    750L,
    200L,
    250L,
    false
);
LockOnSnapshot<String> acquired = service.acquire(100L, cinematic);
assertEquals("cinematic", acquired.getCameraProfileName());
```

Also assert that `null`, blank, and unknown profile names normalize to `balanced` in the existing options-validation test.

- [ ] **Step 2: Run the focused tests and verify the red state**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.session.LockOnSessionTest" --tests "com.zeldatargeting.mod.client.targeting.core.TargetingServiceTest" --no-daemon
```

Expected: `compileTestJava` fails because the new constructor, acquisition signature, and profile getters do not exist.

- [ ] **Step 3: Implement normalized immutable profile metadata**

Change the `TargetingOptions` constructor to:

```java
public TargetingOptions(
        TargetPriority priority,
        String cameraProfileName,
        double maxTrackingDistance,
        long occlusionGraceMillis,
        long releaseFadeMillis,
        long cycleCooldownMillis,
        boolean quickSwitchEnabled)
```

Normalize with `Locale.ROOT` and an explicit allowlist:

```java
private static String normalizeCameraProfileName(String value) {
    if (value == null) {
        return "balanced";
    }
    String normalized = value.trim().toLowerCase(Locale.ROOT);
    if ("cinematic".equals(normalized) || "snappy".equals(normalized)) {
        return normalized;
    }
    return "balanced";
}
```

Add `private final String cameraProfileName` and `getCameraProfileName()` to both `TargetingOptions` and `LockOnSnapshot`.

Change acquisition to:

```java
public boolean beginAcquire(
        TargetObservation<T> target,
        TargetPriority priority,
        String cameraProfileName,
        long nowMillis)
```

Store the normalized name on acquisition, preserve it in `beginSwitch`, publish it from `snapshot()`, and reset it to `balanced` when release reaches `IDLE` or `clear` removes the session. Update `TargetingService.acquire` to pass `options.getCameraProfileName()`.

Update every existing `TargetingOptions` construction in `TargetingServiceTest` to pass `"balanced"` after the priority. Update every direct `beginAcquire` call in `LockOnSessionTest` and `TargetingServiceTest` to pass `"balanced"` before the timestamp.

- [ ] **Step 4: Run the focused and full pure tests**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.session.LockOnSessionTest" --tests "com.zeldatargeting.mod.client.targeting.core.TargetingServiceTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: both commands pass; profile names remain stable through target switches and reset only after the session becomes idle or is cleared.

- [ ] **Step 5: Commit snapshot profile metadata**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java src/main/java/com/zeldatargeting/mod/client/session/LockOnSession.java src/main/java/com/zeldatargeting/mod/client/session/LockOnSnapshot.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java
git commit -m "feat: capture camera profile in lock snapshots"
```

---

### Task 2: Define Complete Camera Profiles and Framing Math

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfile.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfiles.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFraming.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/core/CameraProfilesTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/core/CameraFramingTest.java`

**Interfaces:**

- Consumes: scalar target width, height, distance, and elapsed milliseconds.
- Produces: immutable `CameraProfile`; `CameraProfiles.fromName(String)`; `CameraFraming.desiredDistance`, `fovMultiplier`, `playerAlpha`, and `interpolationFactor`.

- [ ] **Step 1: Write failing profile and framing tests**

Create tests with these exact behaviors:

```java
@Test
public void profilesAreCompleteAndUnknownNamesUseBalanced() {
    assertEquals("cinematic", CameraProfiles.CINEMATIC.getName());
    assertEquals(180.0D, CameraProfiles.CINEMATIC.getRotationHalfLifeMillis(), EPSILON);
    assertEquals(0.12D, CameraProfiles.CINEMATIC.getMaxFovScale(), EPSILON);
    assertEquals(100.0D, CameraProfiles.BALANCED.getRotationHalfLifeMillis(), EPSILON);
    assertEquals(45.0D, CameraProfiles.SNAPPY.getRotationHalfLifeMillis(), EPSILON);
    assertEquals(0.0D, CameraProfiles.SNAPPY.getMaxFovScale(), EPSILON);
    assertSame(CameraProfiles.BALANCED, CameraProfiles.fromName(null));
    assertSame(CameraProfiles.BALANCED, CameraProfiles.fromName("unknown"));
    assertSame(CameraProfiles.CINEMATIC, CameraProfiles.fromName("CINEMATIC"));
}

@Test
public void targetSizeContinuouslyExpandsDistanceWithinProfileCap() {
    assertEquals(4.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 0.6D, 1.8D), EPSILON);
    assertEquals(5.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 2.0D, 4.9D), EPSILON);
    assertEquals(6.0D, CameraFraming.desiredDistance(CameraProfiles.BALANCED, 8.0D, 8.0D), EPSILON);
}

@Test
public void fovAndAlphaRespectExactBounds() {
    assertEquals(1.06D, CameraFraming.fovMultiplier(CameraProfiles.BALANCED, 8.0D, 8.0D, 20.0D, 20.0D), EPSILON);
    assertEquals(1.0D, CameraFraming.fovMultiplier(CameraProfiles.SNAPPY, 8.0D, 8.0D, 20.0D, 20.0D), EPSILON);
    assertEquals(1.0F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 1.25D), FLOAT_EPSILON);
    assertEquals(0.25F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 0.45D), FLOAT_EPSILON);
    assertEquals(0.625F, CameraFraming.playerAlpha(CameraProfiles.BALANCED, 0.85D), FLOAT_EPSILON);
}

@Test
public void interpolationFactorIsFrameRateIndependentAndBounded() {
    double oneHundredMillis = CameraFraming.interpolationFactor(100L, 100.0D);
    double twoFiftyMillis = CameraFraming.interpolationFactor(250L, 100.0D);
    assertEquals(0.5D, oneHundredMillis, EPSILON);
    assertTrue(twoFiftyMillis > oneHundredMillis);
    assertEquals(0.0D, CameraFraming.interpolationFactor(-1L, 100.0D), EPSILON);
    assertEquals(CameraFraming.interpolationFactor(250L, 100.0D), CameraFraming.interpolationFactor(900L, 100.0D), EPSILON);
}
```

- [ ] **Step 2: Run the tests and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraProfilesTest" --tests "com.zeldatargeting.mod.client.camera.core.CameraFramingTest" --no-daemon
```

Expected: `compileTestJava` fails because the profile and framing classes do not exist.

- [ ] **Step 3: Implement profiles and scalar framing formulas**

`CameraProfile` must expose immutable values for name, rotation half-life, size distance gain, maximum FOV scale, maximum yaw and pitch adjustments, release duration, base distance, transparency thresholds, minimum alpha, fallback thresholds and delays, focus Y offset, and first-person half-life scale. Constructor validation rejects non-finite numbers, non-positive half-life, inverted transparency/fallback thresholds, and alpha outside `[0, 1]`. Add `withLegacyOverrides(double halfLifeMillis, float maxYaw, float maxPitch, double focusYOffset)` to return a fully validated copy while retaining every other profile value.

Define the three profiles exactly as the design table, with shared values:

```java
private static CameraProfile profile(
        String name,
        double halfLife,
        double sizeGain,
        double maxFov,
        float maxYaw,
        float maxPitch) {
    return new CameraProfile(
        name, halfLife, sizeGain, maxFov, maxYaw, maxPitch, 200L,
        4.0D, 1.25D, 0.45D, 0.25F,
        0.65D, 1.25D, 100L, 200L,
        0.0D, 1.0D
    );
}
```

Implement formulas exactly:

```java
double size = Math.max(sanitize(width), sanitize(height));
double sizeFactor = clamp((size - 1.8D) / 6.2D, 0.0D, 1.0D);
double desiredDistance = profile.getBaseDistance()
    + sizeFactor * profile.getSizeDistanceGain();

double distanceFactor = clamp(targetDistance / maxTrackingDistance, 0.0D, 1.0D);
double fovMultiplier = 1.0D
    + profile.getMaxFovScale() * sizeFactor * distanceFactor;

double factor = 1.0D - Math.pow(
    0.5D,
    clamp(elapsedMillis, 0L, 250L) / halfLifeMillis
);
```

Alpha is `1.0` at or above the start distance, minimum alpha at or below the inner distance, and linear between them.

- [ ] **Step 4: Run focused tests and the complete suite**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraProfilesTest" --tests "com.zeldatargeting.mod.client.camera.core.CameraFramingTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: profile selection, validation, continuous distance, FOV caps, alpha endpoints, and time-based interpolation all pass.

- [ ] **Step 5: Commit profiles and framing**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfile.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraProfiles.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFraming.java src/test/java/com/zeldatargeting/mod/client/camera/core/CameraProfilesTest.java src/test/java/com/zeldatargeting/mod/client/camera/core/CameraFramingTest.java
git commit -m "feat: add camera profiles and framing math"
```

---

### Task 3: Add Fixed-Sample Collision Solving

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSamples.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionResult.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSolver.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSolverTest.java`

**Interfaces:**

- Consumes: desired distance and exactly eight absolute ray hit distances.
- Produces: `CameraCollisionSolver.solve(double, CameraCollisionSamples)` returning immutable desired-safe distance, estimated vanilla-safe distance, and Forge camera translation delta.

- [ ] **Step 1: Write failing collision tests**

```java
@Test
public void shortestOfEightSamplesConstrainsDesiredAndVanillaDistance() {
    CameraCollisionSamples samples = new CameraCollisionSamples(
        7.0D, 6.0D, 5.0D, 3.0D, 7.0D, 7.0D, 7.0D, 7.0D
    );
    CameraCollisionResult result = CameraCollisionSolver.solve(6.0D, samples);
    assertEquals(2.9D, result.getSafeDistance(), EPSILON);
    assertEquals(3.0D, result.getVanillaDistance(), EPSILON);
    assertEquals(-0.1D, result.getTranslationDelta(), EPSILON);
}

@Test
public void unobstructedLargeTargetExtendsBeyondVanillaFourBlocks() {
    CameraCollisionSamples samples = CameraCollisionSamples.uniform(7.0D);
    CameraCollisionResult result = CameraCollisionSolver.solve(7.0D, samples);
    assertEquals(6.9D, result.getSafeDistance(), EPSILON);
    assertEquals(4.0D, result.getVanillaDistance(), EPSILON);
    assertEquals(2.9D, result.getTranslationDelta(), EPSILON);
}

@Test
public void invalidSampleIsBlockedAndNeverExtendsTheCamera() {
    CameraCollisionSamples samples = new CameraCollisionSamples(
        7.0D, Double.NaN, 7.0D, 7.0D, 7.0D, 7.0D, 7.0D, 7.0D
    );
    CameraCollisionResult result = CameraCollisionSolver.solve(7.0D, samples);
    assertEquals(0.0D, result.getSafeDistance(), EPSILON);
    assertEquals(0.0D, result.getVanillaDistance(), EPSILON);
    assertEquals(0.0D, result.getTranslationDelta(), EPSILON);
}
```

Also test negative desired distance clamps to zero and `uniform` stores eight independent scalar slots rather than exposing an array.

- [ ] **Step 2: Run the collision test and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraCollisionSolverTest" --no-daemon
```

Expected: `compileTestJava` fails because the collision types do not exist.

- [ ] **Step 3: Implement fixed scalar samples and solver**

`CameraCollisionSamples` has eight final `double` fields, a public eight-argument constructor, `uniform(double)`, and `minimumValidDistance()`. It must not retain or return an array or collection.

Implement the solver:

```java
double desired = sanitizeNonNegative(desiredDistance);
double shortest = samples.minimumValidDistance();
double safe = clamp(shortest - 0.1D, 0.0D, desired);
double vanilla = clamp(shortest, 0.0D, 4.0D);
return new CameraCollisionResult(safe, vanilla, safe - vanilla);
```

Any non-finite or negative sample makes `minimumValidDistance()` return `0.0D`.

- [ ] **Step 4: Run collision and full tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraCollisionSolverTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: exact safety-margin, extension, obstruction, invalid-sample, and zero-distance cases pass.

- [ ] **Step 5: Commit collision solving**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSamples.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionResult.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSolver.java src/test/java/com/zeldatargeting/mod/client/camera/core/CameraCollisionSolverTest.java
git commit -m "feat: add collision-safe camera distance solver"
```

---

### Task 4: Add Perspective Capture and Fallback Hysteresis

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraRestoreMode.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveDecision.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveController.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/core/PerspectiveControllerTest.java`

**Interfaces:**

- Consumes: current perspective, baseline FOV, automatic-third-person flag, safe distance, profile thresholds, and monotonic milliseconds.
- Produces: `PerspectiveDecision` values from `begin`, `update`, and `restore`; idempotent `clear` and `isActive` state.

- [ ] **Step 1: Write failing capture, hysteresis, and restoration tests**

```java
@Test
public void beginCapturesOnceAndRequestsRearThirdPersonWhenEnabled() {
    PerspectiveController controller = new PerspectiveController();
    PerspectiveDecision first = controller.begin(0, 70.0F, true, 0L);
    PerspectiveDecision repeated = controller.begin(1, 90.0F, true, 10L);

    assertTrue(first.shouldApply());
    assertEquals(1, first.getPerspective());
    assertEquals(70.0F, first.getBaselineFov(), EPSILON);
    assertFalse(repeated.shouldApply());
    assertEquals(0, controller.getCapturedPerspective());
}

@Test
public void fallbackUsesEntryAndExitDelaysWithHysteresis() {
    PerspectiveController controller = new PerspectiveController();
    controller.begin(0, 70.0F, true, 0L);

    controller.update(0.50D, CameraProfiles.BALANCED, 0L);
    assertFalse(controller.update(0.50D, CameraProfiles.BALANCED, 99L).shouldApply());
    PerspectiveDecision enter = controller.update(0.50D, CameraProfiles.BALANCED, 100L);
    assertTrue(enter.shouldApply());
    assertEquals(0, enter.getPerspective());
    assertTrue(enter.isFallbackActive());

    controller.update(1.30D, CameraProfiles.BALANCED, 200L);
    assertFalse(controller.update(1.30D, CameraProfiles.BALANCED, 399L).shouldApply());
    PerspectiveDecision exit = controller.update(1.30D, CameraProfiles.BALANCED, 400L);
    assertTrue(exit.shouldApply());
    assertEquals(1, exit.getPerspective());
    assertFalse(exit.isFallbackActive());
}

@Test
public void fallbackDoesNotChangeAnIntentionalThirdPersonSession() {
    PerspectiveController controller = new PerspectiveController();
    controller.begin(1, 70.0F, true, 0L);
    assertFalse(controller.update(0.10D, CameraProfiles.BALANCED, 500L).shouldApply());
    assertEquals(1, controller.getRequestedPerspective());
}

@Test
public void restoreReturnsCapturedStateExactlyOnce() {
    PerspectiveController controller = new PerspectiveController();
    controller.begin(0, 72.0F, true, 0L);
    PerspectiveDecision restore = controller.restore();
    PerspectiveDecision repeated = controller.restore();

    assertTrue(restore.shouldApply());
    assertEquals(0, restore.getPerspective());
    assertEquals(72.0F, restore.getBaselineFov(), EPSILON);
    assertFalse(repeated.shouldApply());
    assertFalse(controller.isActive());
}
```

Add a timer-reset test: drop below the entry threshold for 50 ms, recover, drop again, and verify fallback waits a fresh 100 ms. Add an automatic-third-person-disabled test whose `begin` and `restore` decisions both return `shouldApply=false` while capture state still clears exactly once.

- [ ] **Step 2: Run the focused test and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.PerspectiveControllerTest" --no-daemon
```

Expected: `compileTestJava` fails because the perspective types do not exist.

- [ ] **Step 3: Implement one-time capture and hysteresis**

Create:

```java
public enum CameraRestoreMode {
    EASED,
    IMMEDIATE
}
```

`PerspectiveDecision` contains `shouldApply`, `perspective`, `baselineFov`, and `fallbackActive`, plus a `noChange(int perspective, float baselineFov, boolean fallbackActive)` factory.

`PerspectiveController.begin` captures only when inactive. It requests perspective `1` only when `autoThirdPerson` is true and the captured perspective is `0`; otherwise it preserves the current perspective. Store a `changedPerspective` flag only for that `0` to `1` request. Fallback is eligible only when that begin call changed perspective from `0` to `1`.

Use separate `belowThresholdSince` and `aboveThresholdSince` timestamps initialized to `Long.MIN_VALUE`. Entry occurs at `profile.getFallbackEnterDelayMillis()` and exit occurs at `profile.getFallbackExitDelayMillis()`. Reset the opposite timer on every update and reset both when the distance returns to the hysteresis band.

`restore()` returns the captured perspective and FOV once with `shouldApply=true` only when `changedPerspective` is set, then clears all state. When the coordinator never changed perspective, it returns a no-change decision and still clears capture state. `clear()` discards state without issuing a command and is safe to repeat.

- [ ] **Step 4: Run perspective and full tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.PerspectiveControllerTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: capture, intentional-perspective preservation, exact entry/exit timing, timer reset, and idempotent restoration pass.

- [ ] **Step 5: Commit perspective control**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/core/CameraRestoreMode.java src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveDecision.java src/main/java/com/zeldatargeting/mod/client/camera/core/PerspectiveController.java src/test/java/com/zeldatargeting/mod/client/camera/core/PerspectiveControllerTest.java
git commit -m "feat: add perspective fallback and restoration state"
```

---

### Task 5: Build the Frame-Rate-Independent Camera Director

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraInput.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFrame.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/core/CameraDirector.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/core/CameraDirectorTest.java`

**Interfaces:**

- Consumes: immutable `CameraInput`, `CameraProfile`, existing pure `CameraMath`, and `CameraCollisionResult`.
- Produces: `begin`, `update`, `beginRestore(CameraRestoreMode, long)`, `updateRestore`, `reset`, `isActive`, and `isRestoring` on `CameraDirector`; immutable `CameraFrame` values for the runtime adapter.

- [ ] **Step 1: Write failing interpolation, free-look, and release tests**

Use a test helper that constructs `CameraInput` with player yaw/pitch, target deltas, dimensions, target distance, collision result, elapsed milliseconds, free-look flag, and requested perspective.

```java
@Test
public void twoFiftyMillisecondStepsEqualOneHundredMillisecondStep() {
    CameraDirector oneStep = new CameraDirector();
    oneStep.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);
    CameraFrame one = oneStep.update(
        input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 100L, false),
        CameraProfiles.BALANCED
    );

    CameraDirector twoSteps = new CameraDirector();
    twoSteps.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);
    twoSteps.update(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 50L, false), CameraProfiles.BALANCED);
    CameraFrame two = twoSteps.update(
        input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 50L, false),
        CameraProfiles.BALANCED
    );

    assertEquals(one.getYaw(), two.getYaw(), FLOAT_EPSILON);
    assertEquals(-45.0F, one.getYaw(), FLOAT_EPSILON);
}

@Test
public void freeLookStopsRotationAndReleaseReseedsFromActualPlayerYaw() {
    CameraDirector director = new CameraDirector();
    director.begin(input(0.0F, 0.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);

    CameraFrame held = director.update(
        input(30.0F, 5.0F, 1.0D, 0.0D, 0.0D, 50L, true),
        CameraProfiles.BALANCED
    );
    assertFalse(held.shouldApplyRotation());

    CameraFrame returned = director.update(
        input(30.0F, 5.0F, 1.0D, 0.0D, 0.0D, 100L, false),
        CameraProfiles.BALANCED
    );
    assertTrue(returned.shouldApplyRotation());
    assertEquals(-30.0F, returned.getYaw(), FLOAT_EPSILON);
}

@Test
public void normalRestoreEasesPresentationWithoutApplyingOldRotation() {
    CameraDirector director = new CameraDirector();
    director.begin(input(10.0F, 2.0F, 1.0D, 0.0D, 0.0D, 0L, false), CameraProfiles.BALANCED);
    director.update(input(10.0F, 2.0F, 1.0D, 0.0D, 0.0D, 100L, false), CameraProfiles.BALANCED);
    director.beginRestore(CameraRestoreMode.EASED, 1000L);

    CameraFrame midpoint = director.updateRestore(1100L, CameraProfiles.BALANCED, 1);
    CameraFrame complete = director.updateRestore(1200L, CameraProfiles.BALANCED, 1);
    assertFalse(midpoint.shouldApplyRotation());
    assertFalse(midpoint.isRestoreComplete());
    assertEquals(1.0D, complete.getFovMultiplier(), DOUBLE_EPSILON);
    assertEquals(0.0D, complete.getTranslationDelta(), DOUBLE_EPSILON);
    assertEquals(1.0F, complete.getPlayerAlpha(), FLOAT_EPSILON);
    assertTrue(complete.isRestoreComplete());
}
```

Also test wrapped yaw across `+/-180`, pitch clamping, maximum yaw/pitch adjustment, Snappy zero FOV, non-finite target deltas retaining the last valid frame, and immediate baseline restoration.

- [ ] **Step 2: Run the director test and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraDirectorTest" --no-daemon
```

Expected: `compileTestJava` fails because the frame types and director do not exist.

- [ ] **Step 3: Implement immutable frame input/output and director state**

`CameraInput` constructor fields, in order:

```java
public CameraInput(
        float playerYaw,
        float playerPitch,
        double targetDeltaX,
        double targetDeltaY,
        double targetDeltaZ,
        double targetWidth,
        double targetHeight,
        double targetDistance,
        CameraCollisionResult collision,
        long elapsedMillis,
        boolean freeLook,
        int requestedPerspective)
```

`CameraFrame` constructor fields, in order:

```java
public CameraFrame(
        float yaw,
        float pitch,
        double cameraDistance,
        double translationDelta,
        double fovMultiplier,
        float playerAlpha,
        boolean applyRotation,
        int requestedPerspective,
        boolean restoreComplete)
```

Add `CameraFrame.baseline(int requestedPerspective)` returning distance `4.0D`, translation `0.0D`, FOV multiplier `1.0D`, alpha `1.0F`, `applyRotation=false`, and `restoreComplete=true`.

`CameraDirector.begin` seeds current yaw and pitch from `CameraInput`, sets distance to the collision-safe distance, sets FOV and alpha to baseline values, and records the profile. In `update`, calculate the desired rotation with `CameraMath.lookAt`; clamp wrapped desired-yaw minus actual player-yaw to the profile yaw cap; clamp desired-pitch minus actual player-pitch to the profile pitch cap; then interpolate current yaw/pitch toward those bounded targets. Call `CameraFraming.fovMultiplier(profile, targetWidth, targetHeight, targetDistance, 20.0D)` for the fixed Stage 3 tracking baseline, use the collision-safe distance for alpha, and propagate the collision translation delta.

While free look is held, update distance/FOV/alpha but return `applyRotation=false`. On the first non-free-look frame, reseed current yaw and pitch from the input player's actual rotation before interpolation.

`beginRestore(CameraRestoreMode.EASED, nowMillis)` captures the current distance, translation delta, FOV multiplier, and alpha. `updateRestore` linearly interpolates those values to `4.0`, `0.0`, `1.0`, and `1.0` over the profile's release duration and always returns `applyRotation=false`. `beginRestore(CameraRestoreMode.IMMEDIATE, nowMillis)` returns a complete baseline frame and clears interpolated state without using stored yaw or pitch. Reject a non-finite candidate frame and return the last valid frame.

- [ ] **Step 4: Run director and full tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.core.CameraDirectorTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: cadence equivalence, wrapped yaw, bounds, free look, reseeded return, eased release, immediate release, and invalid-frame retention pass.

- [ ] **Step 5: Commit the camera director**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/core/CameraInput.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraFrame.java src/main/java/com/zeldatargeting/mod/client/camera/core/CameraDirector.java src/test/java/com/zeldatargeting/mod/client/camera/core/CameraDirectorTest.java
git commit -m "feat: add frame-rate-independent camera director"
```

---

### Task 6: Coordinate Snapshots Through a Runtime Seam

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/CameraProfileResolver.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeState.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeAdapter.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/CameraCoordinator.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/CameraCoordinatorTest.java`

**Interfaces:**

- Consumes: `LockOnSnapshot<T>`, pure camera core classes, free-look state, presentation-suspension state, lifecycle-clear flag, automatic-third-person state, render partial ticks, and monotonic milliseconds.
- Produces: a generic coordinator testable with a fake runtime; no Minecraft imports in the coordinator or its interfaces.

- [ ] **Step 1: Write failing coordinator tests with a fake runtime**

Define a nested fake implementing the exact runtime interface below. Record capture count, collision count, applied frames, perspective decisions, immediate restores, and cleared frames.

```java
@Test
public void acquisitionCapturesOnceAndSwitchDoesNotRecapture() {
    FakeRuntime runtime = new FakeRuntime();
    CameraCoordinator<String> coordinator = coordinator(runtime);

    coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 100L);
    coordinator.onRenderTick(0.5F, 100L);
    coordinator.onClientTick(tracking("cow", 2L), false, false, false, true, 120L);
    coordinator.onRenderTick(0.5F, 120L);

    assertEquals(1, runtime.captureCount);
    assertEquals(2, runtime.collisionCount);
    assertEquals(2, runtime.appliedFrames.size());
}

@Test
public void normalReleaseEasesThenRestoresPerspectiveOnce() {
    FakeRuntime runtime = new FakeRuntime();
    CameraCoordinator<String> coordinator = coordinator(runtime);
    coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 0L);
    coordinator.onRenderTick(0.0F, 0L);

    coordinator.onClientTick(releasing(2L), false, false, false, true, 100L);
    coordinator.onRenderTick(0.0F, 200L);
    assertEquals(0, runtime.clearedFrameCount);
    coordinator.onRenderTick(0.0F, 300L);

    assertEquals(1, runtime.clearedFrameCount);
    assertEquals(1, runtime.restorePerspectiveCount);
    assertEquals(0, runtime.immediateRestoreCount);
}

@Test
public void lifecycleClearRestoresImmediatelyAndIsIdempotent() {
    FakeRuntime runtime = new FakeRuntime();
    CameraCoordinator<String> coordinator = coordinator(runtime);
    coordinator.onClientTick(tracking("pig", 1L), false, false, false, true, 0L);
    coordinator.onClientTick(idle(2L), false, false, true, true, 10L);
    coordinator.onClientTick(idle(2L), false, false, true, true, 20L);

    assertEquals(1, runtime.immediateRestoreCount);
    assertEquals(1, runtime.clearedFrameCount);
}

@Test
public void guiSuspendsAndClosingGuiStartsFreshWithoutReacquiring() {
    FakeRuntime runtime = new FakeRuntime();
    CameraCoordinator<String> coordinator = coordinator(runtime);
    LockOnSnapshot<String> locked = tracking("pig", 1L);
    coordinator.onClientTick(locked, false, false, false, true, 0L);
    coordinator.onClientTick(locked, false, true, false, true, 10L);
    coordinator.onClientTick(locked, false, false, false, true, 20L);
    assertEquals(2, runtime.captureCount);
    assertEquals(1, runtime.immediateRestoreCount);
}
```

Also test free-look propagation and that rendering while idle performs no sampling or application.

- [ ] **Step 2: Run the coordinator test and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.CameraCoordinatorTest" --no-daemon
```

Expected: `compileTestJava` fails because the coordinator and runtime interfaces do not exist.

- [ ] **Step 3: Define the runtime interfaces exactly**

```java
public interface CameraProfileResolver {
    CameraProfile resolve(String profileName, int perspective);
}

public final class CameraRuntimeState {
    private final int perspective;
    private final float baselineFov;

    public CameraRuntimeState(int perspective, float baselineFov) {
        this.perspective = perspective;
        this.baselineFov = baselineFov;
    }

    public int getPerspective() {
        return perspective;
    }

    public float getBaselineFov() {
        return baselineFov;
    }
}

public interface CameraRuntimeAdapter<T> {
    CameraRuntimeState captureState();
    CameraCollisionResult sampleCollision(LockOnSnapshot<T> snapshot, CameraProfile profile);
    CameraInput sampleFrame(
        LockOnSnapshot<T> snapshot,
        CameraProfile profile,
        CameraCollisionResult collision,
        long elapsedMillis,
        boolean freeLook,
        int requestedPerspective,
        float partialTicks
    );
    void applyPerspective(PerspectiveDecision decision);
    void applyFrame(CameraFrame frame);
    void restoreImmediate(PerspectiveDecision decision);
    void clearFrame();
    void resetWorldSession();
}
```

- [ ] **Step 4: Implement coordinator transitions**

Constructor:

```java
public CameraCoordinator(
        CameraDirector director,
        PerspectiveController perspectiveController,
        CameraRuntimeAdapter<T> runtime,
        CameraProfileResolver profileResolver)
```

Public entry points:

```java
public void onClientTick(
    LockOnSnapshot<T> snapshot,
    boolean freeLook,
    boolean presentationSuspended,
    boolean lifecycleClear,
    boolean autoThirdPerson,
    long nowMillis
)

public void onRenderTick(float partialTicks, long nowMillis)
```

Behavior:

- Start a session only for a tracking snapshot while presentation is not suspended.
- Capture runtime state and profile once; switches update the snapshot but do not recapture.
- Sample collision once per client tick.
- Track the previous render timestamp inside the coordinator and pass only the non-negative render delta to `sampleFrame`; reset that timestamp on session start, suspension, and clear so a new session begins with zero elapsed time. `CameraFraming` owns the 250 ms clamp.
- Begin director restoration with `CameraRestoreMode.EASED` when a previously active coordinator receives `RELEASING`. Continue that restoration even after the targeting snapshot advances to `IDLE`.
- Complete normal restoration only after `CameraFrame.isRestoreComplete()`, then apply the one perspective restore decision and clear the runtime frame.
- For lifecycle clear or presentation suspension, call `director.beginRestore(CameraRestoreMode.IMMEDIATE, nowMillis)`, restore perspective only if the coordinator changed it, clear the director, and ignore repeated clear calls. A lifecycle clear additionally invokes `runtime.resetWorldSession()` after restoration so warn-once and disabled-adapter state can reset only at the world boundary.
- After suspension ends, an unchanged tracking snapshot starts a fresh camera session without modifying the target session. Pass `autoThirdPerson` only to the first `PerspectiveController.begin` call for that camera session.
- Forward free-look state only to frame samples; do not mutate the lock snapshot.

- [ ] **Step 5: Run coordinator and full tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.CameraCoordinatorTest" --no-daemon
.\gradlew.bat test --no-daemon
```

Expected: capture, switch, free look, release, immediate clear, GUI suspension, restart, and idle behavior pass without Minecraft on the test classpath.

- [ ] **Step 6: Commit the generic coordinator**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/CameraProfileResolver.java src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeState.java src/main/java/com/zeldatargeting/mod/client/camera/CameraRuntimeAdapter.java src/main/java/com/zeldatargeting/mod/client/camera/CameraCoordinator.java src/test/java/com/zeldatargeting/mod/client/camera/CameraCoordinatorTest.java
git commit -m "feat: coordinate camera sessions through runtime seam"
```

---

### Task 7: Implement the Legacy Profile Bridge and Vanilla Forge Adapter

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/LegacyCameraProfileAdapter.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraOffsetApplier.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/CloseCameraTransparency.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraAdapter.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/vanilla/LegacyCameraProfileAdapterTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraOffsetApplierTest.java`

**Interfaces:**

- Consumes: the Stage 2 snapshot, existing 1.3 camera configuration fields, Minecraft player/world/entity state, and Forge `CameraSetup`, `FOVModifier`, and `RenderPlayerEvent` events.
- Produces: a complete profile, exactly eight collision samples per client tick, immutable render inputs, event-scoped camera translation/FOV/alpha, and warn-once runtime containment.

- [ ] **Step 1: Write failing profile-bridge and translation-sign tests**

In `LegacyCameraProfileAdapterTest`, snapshot and restore every mutated static config field in `@Before` and `@After`. Add these behaviors:

```java
@Test
public void selectedPresetKeepsItsHalfLifeAtTheMatchingLegacyDefault() {
    TargetingConfig.cameraSmoothness = 0.4F;
    TargetingConfig.maxYawAdjustment = 80.0F;
    TargetingConfig.maxPitchAdjustment = 55.0F;
    TargetingConfig.cameraFocusYOffset = 0.1F;
    TargetingConfig.perModeSmoothingEnabled = false;

    CameraProfile profile = new LegacyCameraProfileAdapter().resolve("balanced", 1);

    assertEquals(100.0D, profile.getRotationHalfLifeMillis(), EPSILON);
    assertEquals(80.0F, profile.getMaxYawAdjustment(), FLOAT_EPSILON);
    assertEquals(55.0F, profile.getMaxPitchAdjustment(), FLOAT_EPSILON);
    assertEquals(0.1D, profile.getFocusYOffset(), EPSILON);
}

@Test
public void customSmoothnessAndFirstPersonModeUseTheDocumentedConversion() {
    TargetingConfig.cameraSmoothness = 0.5F;
    TargetingConfig.perModeSmoothingEnabled = true;

    CameraProfile thirdPerson = new LegacyCameraProfileAdapter().resolve("balanced", 1);
    CameraProfile firstPerson = new LegacyCameraProfileAdapter().resolve("balanced", 0);

    assertEquals(142.5D, thirdPerson.getRotationHalfLifeMillis(), EPSILON);
    assertEquals(237.5D, firstPerson.getRotationHalfLifeMillis(), EPSILON);
}

@Test
public void unknownPresetUsesBalanced() {
    TargetingConfig.cameraSmoothness = 0.4F;
    CameraProfile profile = new LegacyCameraProfileAdapter().resolve("missing", 1);
    assertEquals("balanced", profile.getName());
    assertEquals(100.0D, profile.getRotationHalfLifeMillis(), EPSILON);
}
```

In `VanillaCameraOffsetApplierTest`, test the pure package-private sign helper without opening an OpenGL context:

```java
@Test
public void translationUsesCameraSpaceDirectionForEachPerspective() {
    assertEquals(-2.9D, VanillaCameraOffsetApplier.translationFor(1, 2.9D), EPSILON);
    assertEquals(2.9D, VanillaCameraOffsetApplier.translationFor(2, 2.9D), EPSILON);
    assertEquals(0.0D, VanillaCameraOffsetApplier.translationFor(0, 2.9D), EPSILON);
    assertEquals(0.0D, VanillaCameraOffsetApplier.translationFor(1, Double.NaN), EPSILON);
}
```

- [ ] **Step 2: Run the focused tests and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.vanilla.LegacyCameraProfileAdapterTest" --tests "com.zeldatargeting.mod.client.camera.vanilla.VanillaCameraOffsetApplierTest" --no-daemon
```

Expected: `compileTestJava` fails because the vanilla bridge classes do not exist.

- [ ] **Step 3: Implement legacy profile resolution**

`LegacyCameraProfileAdapter implements CameraProfileResolver`. Resolve the named preset first. The matching legacy smoothness defaults are Cinematic `0.15F`, Balanced `0.4F`, and Snappy `0.75F`. If `abs(TargetingConfig.cameraSmoothness - presetDefault) > 0.001F`, calculate:

```java
double halfLife = 35.0D
    + (1.0D - CameraMath.clamp(TargetingConfig.cameraSmoothness, 0.0F, 1.0F))
    * 215.0D;
```

Otherwise retain the preset half-life. Multiply the result by `5.0D / 3.0D` only when `perModeSmoothingEnabled` is true and perspective is `0`. Return `base.withLegacyOverrides(halfLife, maxYawAdjustment, maxPitchAdjustment, cameraFocusYOffset)`. Sanitize all values through `CameraProfile` validation and fall back to the selected preset value if a legacy field is non-finite.

- [ ] **Step 4: Implement the event-scoped distance and transparency helpers**

`VanillaCameraOffsetApplier.translationFor` returns `-delta` for rear third person (`1`), `+delta` for front third person (`2`), and zero for first person or a non-finite delta. `apply(CameraFrame, int)` calls `GlStateManager.translate(0.0D, 0.0D, translation)` only for a finite non-zero translation.

`CloseCameraTransparency` stores the active frame alpha as a scalar. On `RenderPlayerEvent.Pre` for `Minecraft.getMinecraft().player` only, alpha below `1.0F` enables blending, uses `SRC_ALPHA, ONE_MINUS_SRC_ALPHA`, sets color to `(1, 1, 1, alpha)`, and records that state was changed. The matching `Post` always restores `color(1, 1, 1, 1)` and disables the blend state it enabled. `reset()` restores scalar state to `1.0F` and is idempotent.

- [ ] **Step 5: Implement Minecraft sampling and contained Forge event application**

`VanillaCameraAdapter implements CameraRuntimeAdapter<EntityLivingBase>` and owns only:

- `Minecraft`, `VanillaCameraOffsetApplier`, and `CloseCameraTransparency` references;
- the latest immutable `CameraFrame` for event handlers;
- captured runtime state used for best-effort failure recovery;
- one camera-translation-disabled flag, one application-disabled flag, and one warn-once flag per failure class.

Implement these methods:

- `captureState()` returns current `thirdPersonView` and `gameSettings.fovSetting` and retains that baseline for contained recovery.
- `sampleCollision(...)` calculates `desiredDistance` from snapshot width/height, extends every ray to `max(4.0D, desiredDistance)`, mirrors vanilla 1.12.2's camera direction, and casts the exact eight `+/-0.1D` offset rays. An unobstructed ray reports the full sample distance; a hit reports its absolute distance from the unshifted eye center. Feed the eight scalar values to `CameraCollisionSolver`.
- `sampleFrame(...)` interpolates player and target positions with `partialTicks`, resolves the snapshot's `TargetAnchor` via `TargetAnchorResolver`, then applies `profile.getFocusYOffset() * targetHeight`. Build `CameraInput` using snapshot width, height, and distance plus the cached collision result.
- `applyPerspective(...)` mutates `thirdPersonView` only when `decision.shouldApply()`.
- `applyFrame(...)` updates `prevRotationYaw/Pitch` and current yaw/pitch only when `frame.shouldApplyRotation()`, then stores the frame for Forge event handlers.
- `restoreImmediate(...)` applies the decision when requested, replaces the event frame with a baseline frame, and resets transparency.
- `clearFrame()` removes the event frame and resets transparency.
- `resetWorldSession()` clears captured runtime state and re-enables contained paths and warn-once logging.

Register handlers for `EntityViewRenderEvent.CameraSetup`, `EntityViewRenderEvent.FOVModifier`, and `RenderPlayerEvent.Pre/Post`. The camera handler applies the current frame's translation after vanilla camera placement. The FOV handler calls `event.setFOV((float) (event.getFOV() * frame.getFovMultiplier()))` without writing `gameSettings.fovSetting`. Player handlers delegate to the transparency helper.

Wrap camera translation separately: on its first failure, log one warning and disable only custom translation until `resetWorldSession`. Wrap other event/frame application: warn once, restore captured perspective plus baseline frame/alpha where possible, and disable custom application until `resetWorldSession`. Never log per tick or per render frame.

- [ ] **Step 6: Run adapter tests, compile Minecraft-facing code, and build**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.vanilla.LegacyCameraProfileAdapterTest" --tests "com.zeldatargeting.mod.client.camera.vanilla.VanillaCameraOffsetApplierTest" --no-daemon
.\gradlew.bat compileJava compileTestJava --no-daemon
.\gradlew.bat test build --no-daemon
```

Expected: legacy conversion and translation tests pass; Forge event signatures compile against the mapped 1.12.2 API; the full build succeeds.

- [ ] **Step 7: Commit the vanilla adapter**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/camera/vanilla src/test/java/com/zeldatargeting/mod/client/camera/vanilla
git commit -m "feat: add vanilla camera runtime adapter"
```

---

### Task 8: Integrate the Coordinator and Remove the Legacy Controller

**Files:**

- Modify: `src/main/java/com/zeldatargeting/mod/client/KeyBindings.java`
- Modify: `src/main/resources/assets/zeldatargeting/lang/en_us.lang`
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/camera/CameraCoordinatorTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java`

**Interfaces:**

- Consumes: the existing manager's client/input events, `KeyBindings`, `TargetingConfig`, current targeting snapshots, and the Stage 3 coordinator/adapter.
- Produces: one registered vanilla event adapter, one Left Alt hold binding, client-tick collision/state updates, render-tick camera frames, and no legacy camera ownership in `TargetingManager`.

- [ ] **Step 1: Extend integration tests before wiring production code**

Add a coordinator test proving automatic-third-person is captured from the session-start argument and cannot change during the session:

```java
@Test
public void autoThirdPersonChangesApplyOnlyToTheNextCameraSession() {
    FakeRuntime runtime = new FakeRuntime();
    CameraCoordinator<String> coordinator = coordinator(runtime);
    LockOnSnapshot<String> locked = tracking("pig", 1L);

    coordinator.onClientTick(locked, false, false, false, false, 0L);
    assertEquals(0, runtime.appliedPerspectiveCount);

    coordinator.onClientTick(locked, false, false, false, true, 10L);
    assertEquals(0, runtime.appliedPerspectiveCount);

    coordinator.onClientTick(locked, false, true, false, true, 20L);
    coordinator.onClientTick(locked, false, false, false, true, 30L);
    assertEquals(1, runtime.appliedPerspectiveCount);
}
```

Also add a test that `presentationSuspended=true` prevents render sampling until suspension ends.

- [ ] **Step 2: Run the focused integration tests and verify the red state**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.CameraCoordinatorTest" --no-daemon
```

Expected: the new session-start behavior fails until coordinator capture semantics are finalized.

- [ ] **Step 3: Register the free-look hold binding**

Add `public static KeyBinding cameraFreeLook` and initialize it with:

```java
cameraFreeLook = new KeyBinding(
    "key.zeldatargeting.camera_free_look",
    Keyboard.KEY_LMENU,
    CATEGORY
);
```

Register it exactly once. Add these English entries:

```properties
key.zeldatargeting.camera_free_look=Camera Free Look
key.categories.zeldatargeting=Zelda Targeting
```

Do not consume `isPressed()` for free look; the manager reads `isKeyDown()` each client tick so the binding is a hold action and remains rebindable.

- [ ] **Step 4: Replace manager camera state with the coordinator**

Replace `CameraController` and `previousPerspective` with:

```java
private final VanillaCameraAdapter cameraAdapter;
private final CameraCoordinator<EntityLivingBase> cameraCoordinator;
```

Construct them with a new `CameraDirector`, `PerspectiveController`, and `LegacyCameraProfileAdapter`. In `TargetingManager.init()`, register the manager and `cameraAdapter` exactly once on `MinecraftForge.EVENT_BUS`.

Change the manager's lifecycle guard to run whenever `before.isPresentationVisible()` so disconnect, world loss, dimension change, or player death during the `RELEASING` phase still selects immediate restoration. On lifecycle invalidation, pass the cleared snapshot to the coordinator once with `lifecycleClear=true` before returning.

At the end of every normal client tick, call:

```java
cameraCoordinator.onClientTick(
    after,
    KeyBindings.cameraFreeLook.isKeyDown(),
    Minecraft.getMinecraft().currentScreen != null
        || !TargetingConfig.isCameraLockOnEnabled(),
    false,
    TargetingConfig.autoThirdPerson,
    nowMillis
);
```

Add a `TickEvent.RenderTickEvent` handler that calls `cameraCoordinator.onRenderTick(event.renderTickTime, System.currentTimeMillis())` only in `TickEvent.Phase.START`.

Change `currentOptions()` to pass `TargetingConfig.lockOnPreset` immediately after priority. Keep lock/switch/release sounds and debug messages in transition feedback, but remove all camera target, perspective, and rotation mutations from `beginTrackingFeedback`, `switchTrackingFeedback`, and `endTrackingFeedback`.

- [ ] **Step 5: Delete the old controller and prove ownership is gone**

Delete `CameraController.java`. Run:

```powershell
rg -n "CameraController|previousPerspective|updateCamera|resetCamera" src/main/java
```

Expected: no matches. Then check the pure core boundary:

```powershell
rg -n "net\.minecraft|net\.minecraftforge|org\.lwjgl" src/main/java/com/zeldatargeting/mod/client/camera/core
```

Expected: no matches.

- [ ] **Step 6: Run the complete integration verification**

```powershell
.\gradlew.bat clean test build --no-daemon
.\gradlew.bat reobfJar --no-daemon
```

Expected: all pure and integration tests pass; `compileJava` validates the Forge event wiring; `build` and `reobfJar` produce the client-only mod jar.

- [ ] **Step 7: Commit coordinator integration**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/KeyBindings.java src/main/resources/assets/zeldatargeting/lang/en_us.lang src/main/java/com/zeldatargeting/mod/client/TargetingManager.java src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java src/test/java
git commit -m "refactor: replace legacy camera controller"
```

---

### Task 9: Verify Stage 3 in Forge and Record the Smoke Test

**Files:**

- Create: `docs/development/stage-3-camera-smoke-test.md`
- Inspect: `build/test-results/test/TEST-*.xml`
- Inspect: `build/libs/*.jar`
- Inspect: `run/logs/latest.log`
- Inspect: `build.gradle`
- Inspect: `src/main/resources/mcmod.info`

**Interfaces:**

- Consumes: the clean automated build, packaged jar metadata, a pre-launch log baseline, and the user's numbered in-game results.
- Produces: a reproducible Stage 3 acceptance record with automated counts, manual results, appended-log audit, known limitations, and exact tested commit.

- [ ] **Step 1: Run a clean verification and count test results**

```powershell
.\gradlew.bat clean test build --no-daemon
.\gradlew.bat reobfJar --no-daemon
$testFiles = Get-ChildItem 'build/test-results/test' -Filter 'TEST-*.xml'
$testCases = 0
$testFailures = 0
$testErrors = 0
foreach ($testFile in $testFiles) {
    [xml]$result = Get-Content -Raw $testFile.FullName
    $testCases += [int]$result.testsuite.tests
    $testFailures += [int]$result.testsuite.failures
    $testErrors += [int]$result.testsuite.errors
}
"tests=$testCases failures=$testFailures errors=$testErrors"
```

Expected: clean `build` and `reobfJar` succeed with zero test failures and errors. Record the actual counts rather than predicting them.

- [ ] **Step 2: Inspect the packaged artifact and boundaries**

```powershell
$artifact = Get-ChildItem 'build/libs' -Filter '*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$artifact.FullName
& jar tf $artifact.FullName
rg -n "version|mcversion" build.gradle src/main/resources/mcmod.info
rg -n "net\.minecraft|net\.minecraftforge|org\.lwjgl" src/main/java/com/zeldatargeting/mod/client/camera/core
rg -n "CameraController|previousPerspective" src/main/java
git status --short
```

Expected: the newest jar contains `mcmod.info` and Stage 3 classes, metadata remains internally consistent at `1.3.0`/Minecraft `1.12.2`, both boundary searches return no matches, and only the not-yet-written acceptance record is untracked.

- [ ] **Step 3: Write the tester-facing checklist and capture the log baseline**

Create `docs/development/stage-3-camera-smoke-test.md` with the tested commit hash, artifact path, automated test counts, and this numbered checklist:

1. For the same medium target in open space, acquire with Cinematic, Balanced, and Snappy; confirm each feels distinct, Snappy adds no visible FOV change, and unlock never returns the view to the acquisition angle.
2. Repeat acquisition and unlock in vanilla first person, rear third person, and front third person; with automatic third person both off and on, confirm original perspective returns exactly once.
3. Hold Left Alt while locked, look freely in several directions, release Left Alt, and confirm smooth target return with no replayed lock or switch sound.
4. Lock small, tall, wide, moving, and very near targets; confirm focus follows the resolved anchor and large shapes frame farther away without abrupt distance jumps.
5. Back toward a wall, stand under a low ceiling, and enter a narrow corridor; confirm no wall clipping or camera extension through blocks. Move through the transparency range and confirm the player fades and fully restores.
6. With automatic third person enabled, force the safe distance below fallback entry long enough to enter first person, then clear the obstruction long enough to return to rear third person without flicker.
7. Verify manual unlock, target death, sustained occlusion, player death, world exit while locked, and world re-entry. Confirm no stale target, perspective, FOV, distance, player alpha, or free-look state.
8. Open the configuration GUI while locked, confirm presentation restores immediately, close it, and confirm the same lock resumes without reacquisition feedback.

Before launch, record the current line count and last-write time:

```powershell
$logPath = 'run/logs/latest.log'
$baselineLines = if (Test-Path $logPath) { (Get-Content $logPath).Count } else { 0 }
$baselineWrite = if (Test-Path $logPath) { (Get-Item $logPath).LastWriteTime } else { $null }
"baselineLines=$baselineLines baselineWrite=$baselineWrite"
```

Ask the user to report `1 PASS` through `8 PASS`, with one concise observation for any failure. Do not mark Stage 3 complete before all eight results are available.

- [ ] **Step 4: Audit only the appended game log after the user run**

Using the recorded numeric line count:

```powershell
$baselineLines = 0
$appended = Get-Content 'run/logs/latest.log' | Select-Object -Skip $baselineLines
$appended | Select-String -Pattern 'Exception|ERROR|FATAL|camera|OpenGL|disconnect|Unloading|Stopping'
```

Replace `0` with the recorded baseline before running the command. Review every match in context. Normal disconnect, world unloading, and client shutdown messages are acceptable; new camera warnings, stack traces, OpenGL-state errors, or repeated per-frame logging are failures requiring diagnosis and a new smoke run.

- [ ] **Step 5: Record actual results and run the final completion gate**

Update the smoke-test document with the user's eight results, the appended-log findings, artifact name, automated counts, and `git rev-parse HEAD`. Then run:

```powershell
.\gradlew.bat clean test build reobfJar --no-daemon
rg -n "net\.minecraft|net\.minecraftforge|org\.lwjgl" src/main/java/com/zeldatargeting/mod/client/camera/core
rg -n "CameraController|previousPerspective" src/main/java
git diff --check
git status --short
```

Expected: the build succeeds; both `rg` checks produce no matches; `git diff --check` is silent; only the completed smoke-test record is pending.

- [ ] **Step 6: Commit the verified Stage 3 acceptance record**

```powershell
git add docs/development/stage-3-camera-smoke-test.md
git commit -m "docs: record stage three camera verification"
git status --short
```

Expected: the branch is clean. Use `superpowers:verification-before-completion` before claiming success, then `superpowers:finishing-a-development-branch` to present the local merge option requested for this overhaul.
