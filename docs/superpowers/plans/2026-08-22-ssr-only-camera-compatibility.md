# SSR-Only Camera Compatibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove Better Third Person support and make Shoulder Surfing Reloaded 2.9.x provide stable shoulder-preserving lock-on aim, crosshair alignment, mouse ownership, and movement on Minecraft 1.12.2.

**Architecture:** A pure Java solver compensates the lock direction for SSR's lateral ray origin. A reflection-only bridge samples SSR's camera offset and active crosshair mode without changing either. The Minecraft adapter applies one compensated player rotation for SSR's static ray, delegates camera translation to SSR when active, and never remaps WASD or reconstructs mouse deltas.

**Tech Stack:** Java 8, Minecraft 1.12.2, Forge 14.23.5.2859, ForgeGradle 3, JUnit 4.13.2, optional Shoulder Surfing Reloaded 2.9.6 reflection surface.

**Spec:** `docs/superpowers/specs/2026-08-22-ssr-only-camera-compatibility-design.md`

## Runtime Validation Amendment

The first convergence experiment failed because it registered an adaptive-item callback that forced SSR's dynamic crosshair during every lock. A temporary implementation centered SSR's runtime offset instead. The current implementation restores `ShoulderAimSolver` for SSR's static ray, leaves the shoulder position intact, and uses the eye ray when SSR itself selects a dynamic crosshair. No adaptive callback is registered. The task list below records the original workflow and contains superseded steps.

## Global Constraints

- Zelda Targeting remains client-only and must connect to an unmodified server.
- Shoulder Surfing Reloaded is optional; no SSR class may appear in a core public signature.
- Better Third Person runtime branches and user-facing configuration are removed.
- SSR retains its configured shoulder side and X, Y, and Z offsets while locked.
- Zelda Targeting does not alter reach, server combat rules, Minecraft mouse sensitivity, or unlocked movement input.
- New behavior is test-first: every production change follows a witnessed failing regression test.
- Existing user changes in the working tree are preserved unless this plan explicitly replaces the experimental compatibility code.
- Vanilla first and third person remain the unconditional fallback.

## File Structure

### New focused units

- `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraState.java` — immutable, Minecraft-independent snapshot of SSR activity and offsets.
- `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolver.java` — fixed-iteration, allocation-free shoulder-ray convergence math.
- `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderSurfingBridge.java` — cached optional reflection, runtime state capture, and adaptive-crosshair callback registration.
- `src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolverTest.java` — geometric regression coverage.
- `src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraStateTest.java` — invalid-state and fallback behavior.

### Reworked and deleted units

- Rework `CameraRotationPolicy.java`, `VanillaCameraAdapter.java`, `ZeldaTargetingMod.java`, and `TargetingManager.java`.
- Delete `CameraMovementBasis.java`, `MovementBasisRemapper.java`, and their tests.
- Remove BTP and manual SSR-offset fields across config, presets, store, validator, GUI, README, and changelog.

---

### Task 1: Pure Shoulder-Ray Aim Convergence

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraState.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolver.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraStateTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolverTest.java`

**Interfaces:**
- Produces: `ShoulderCameraState.inactive()`, `ShoulderCameraState.active(double, double, double, double)`, state getters, and `canCompensate()`.
- Produces: `ShoulderAimSolver.compensate(double, double, double, ShoulderCameraState)` returning `ShoulderAimSolver.AimVector`.
- `AimVector` exposes `getX()`, `getY()`, `getZ()`, and `isCompensated()`.

- [ ] **Step 1: Write state-validation tests**

```java
@Test
public void invalidOffsetsCannotCompensate() {
    ShoulderCameraState state = ShoulderCameraState.active(
        Double.NaN, 0.0D, 3.0D, 3.2D
    );
    assertFalse(state.canCompensate());
}

@Test
public void finiteActiveStateCanCompensate() {
    ShoulderCameraState state = ShoulderCameraState.active(
        -0.875D, 0.0D, 3.0D, 3.125D
    );
    assertTrue(state.canCompensate());
}
```

- [ ] **Step 2: Run the state tests and witness RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.ShoulderCameraStateTest"
```

Expected: compilation failure because `ShoulderCameraState` does not exist.

- [ ] **Step 3: Implement the immutable state**

```java
public boolean canCompensate() {
    return active
        && finite(offsetX) && finite(offsetY) && finite(offsetZ)
        && finite(cameraDistance) && cameraDistance > 0.0D
        && offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ > 1.0E-12D;
}
```

Add a private constructor, inactive/active factories, five getters, and a local finite check. Do not import Minecraft or SSR types.

- [ ] **Step 4: Run the state tests and witness GREEN**

Run the command from Step 2. Expected: PASS.

- [ ] **Step 5: Write solver regression tests**

```java
@Test
public void zeroOffsetReturnsVanillaTargetVector() {
    AimVector result = ShoulderAimSolver.compensate(
        0.0D, 0.0D, 6.0D, ShoulderCameraState.inactive()
    );
    assertEquals(0.0D, result.getX(), EPSILON);
    assertEquals(0.0D, result.getY(), EPSILON);
    assertEquals(6.0D, result.getZ(), EPSILON);
    assertFalse(result.isCompensated());
}

@Test
public void leftShoulderRayConvergesOnCloseTarget() {
    ShoulderCameraState state = ShoulderCameraState.active(
        -0.875D, 0.0D, 3.0D, 3.125D
    );
    AimVector result = ShoulderAimSolver.compensate(
        0.0D, -0.25D, 3.0D, state
    );
    assertTrue(result.isCompensated());
    assertRayHitsTarget(result, state, 0.0D, -0.25D, 3.0D);
}

@Test
public void rightShoulderRayConvergesAcrossYawWrap() {
    ShoulderCameraState state = ShoulderCameraState.active(
        0.875D, 0.2D, 3.0D, 3.15D
    );
    AimVector result = ShoulderAimSolver.compensate(
        0.05D, 0.4D, -5.0D, state
    );
    assertTrue(result.isCompensated());
    assertRayHitsTarget(result, state, 0.05D, 0.4D, -5.0D);
}
```

The test-only `assertRayHitsTarget` independently reconstructs SSR's world offset from literal vector math and asserts miss distance below `0.001D`. Add these exact cases:

```java
@Test
public void shoulderRayConvergesOnDistantElevatedTarget() {
    ShoulderCameraState state = ShoulderCameraState.active(-0.875D, 0.4D, 3.0D, 3.15D);
    AimVector result = ShoulderAimSolver.compensate(12.0D, 4.0D, 24.0D, state);
    assertRayHitsTarget(result, state, 12.0D, 4.0D, 24.0D);
}

@Test
public void shoulderRayConvergesOnNegativeXTarget() {
    ShoulderCameraState state = ShoulderCameraState.active(0.875D, -0.2D, 3.0D, 3.15D);
    AimVector result = ShoulderAimSolver.compensate(-4.0D, -0.5D, 2.0D, state);
    assertRayHitsTarget(result, state, -4.0D, -0.5D, 2.0D);
}

@Test
public void nonFiniteTargetReturnsSafeZeroVector() {
    AimVector result = ShoulderAimSolver.compensate(
        Double.NaN, 0.0D, 4.0D,
        ShoulderCameraState.active(-0.875D, 0.0D, 3.0D, 3.125D)
    );
    assertEquals(0.0D, result.getX(), EPSILON);
    assertEquals(0.0D, result.getY(), EPSILON);
    assertEquals(0.0D, result.getZ(), EPSILON);
    assertFalse(result.isCompensated());
}
```

- [ ] **Step 6: Run the solver tests and witness RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.ShoulderAimSolverTest"
```

Expected: compilation failure because `ShoulderAimSolver` does not exist.

- [ ] **Step 7: Implement the fixed-point solver**

Use four fixed iterations:

```java
public static AimVector compensate(
        double targetX, double targetY, double targetZ,
        ShoulderCameraState state) {
    if (!finiteVector(targetX, targetY, targetZ)) {
        return new AimVector(0.0D, 0.0D, 0.0D, false);
    }
    if (state == null || !state.canCompensate()) {
        return new AimVector(targetX, targetY, targetZ, false);
    }

    double aimX = targetX;
    double aimY = targetY;
    double aimZ = targetZ;
    for (int i = 0; i < 4; i++) {
        Rotation rotation = lookAt(aimX, aimY, aimZ);
        Vector view = viewVector(rotation.yaw, rotation.pitch);
        Vector camera = rotateAndScaleLocalOffset(state, rotation);
        Vector lateral = camera.subtract(view.scale(camera.dot(view)));
        aimX = targetX - lateral.x;
        aimY = targetY - lateral.y;
        aimZ = targetZ - lateral.z;
    }
    return finiteVector(aimX, aimY, aimZ)
        ? new AimVector(aimX, aimY, aimZ, true)
        : new AimVector(targetX, targetY, targetZ, false);
}
```

Match `CameraMath.lookAt` and SSR's local `(offsetX, offsetY, -offsetZ)`, `rotatePitch(-pitch)`, `rotateYaw(-yaw)` conventions. Keep `Vector` and `Rotation` private nested value types and avoid `Vec3d`.

- [ ] **Step 8: Run Task 1 and camera-core tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.*" --tests "com.zeldatargeting.mod.client.camera.core.*"
```

Expected: PASS.

- [ ] **Step 9: Commit Task 1**

```powershell
git add -- src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraState.java src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolver.java src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderCameraStateTest.java src/test/java/com/zeldatargeting/mod/client/camera/compat/ShoulderAimSolverTest.java
git commit -m "feat: add shoulder-aware aim convergence"
```

---

### Task 2: Optional SSR Bridge and Binary Camera Host

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderSurfingBridge.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/camera/compat/CameraRotationPolicy.java`
- Modify: `src/test/java/com/zeldatargeting/mod/client/camera/compat/CameraRotationPolicyTest.java`
- Modify: `src/main/java/com/zeldatargeting/mod/ZeldaTargetingMod.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`

**Interfaces:**
- Consumes: `ShoulderCameraState`.
- Produces: `ShoulderSurfingBridge.detect(boolean, Logger)`, `unavailable()`, `isLoaded()`, `isActive()`, `captureState()`, and `registerAdaptiveCrosshair(BooleanSupplier)`.
- Produces: `ZeldaTargetingMod.getShoulderSurfingBridge()` and `TargetingManager.isTrackingTarget()`.

- [ ] **Step 1: Replace policy tests with binary ownership**

```java
@Test
public void activeShoulderSurfingOwnsOffset() {
    Decision decision = CameraRotationPolicy.resolve(true);
    assertEquals(CameraHost.SHOULDER_SURFING, decision.getCameraHost());
    assertEquals(RotationOwner.PLAYER, decision.getRotationOwner());
    assertEquals(OffsetOwner.CAMERA_HOST, decision.getOffsetOwner());
}

@Test
public void inactiveShoulderSurfingUsesVanilla() {
    Decision decision = CameraRotationPolicy.resolve(false);
    assertEquals(CameraHost.VANILLA, decision.getCameraHost());
    assertEquals(RotationOwner.PLAYER, decision.getRotationOwner());
    assertEquals(OffsetOwner.ZELDA, decision.getOffsetOwner());
}
```

Delete BTP, camera-event, compatibility-mode, and movement-owner expectations.

- [ ] **Step 2: Run policy tests and witness RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.CameraRotationPolicyTest"
```

Expected: compilation failure because `resolve(boolean)` and the simplified enum surface do not exist.

- [ ] **Step 3: Implement binary policy and witness GREEN**

```java
public static Decision resolve(boolean shoulderSurfingActive) {
    return shoulderSurfingActive
        ? new Decision(CameraHost.SHOULDER_SURFING, RotationOwner.PLAYER, OffsetOwner.CAMERA_HOST)
        : new Decision(CameraHost.VANILLA, RotationOwner.PLAYER, OffsetOwner.ZELDA);
}
```

Run Step 2 again. Expected: PASS.

- [ ] **Step 4: Implement cached optional reflection**

Resolve these 2.9.x surfaces only when the mod ID is installed:

```text
ShoulderInstance.getInstance
ShoulderInstance.doShoulderSurfing
ShoulderInstance.getOffsetX / getOffsetY / getOffsetZ
ShoulderRenderer.getInstance
ShoulderRenderer.getCameraDistance
ShoulderSurfingRegistrar.getInstance
ShoulderSurfingRegistrar.registerAdaptiveItemCallback
IAdaptiveItemCallback.isHoldingAdaptiveItem
```

`captureState()` returns inactive unless SSR is active. If renderer distance is invalid, use `sqrt(x*x + y*y + z*z)`. Any invocation failure logs once and returns inactive.

Register one retained Java `Proxy` for `IAdaptiveItemCallback`. Its `isHoldingAdaptiveItem` method returns `lockActive.getAsBoolean()`. Handle identity `equals`, `hashCode`, and `toString`; return primitive defaults for unrelated methods.

- [ ] **Step 5: Wire mod lifecycle without BTP detection**

Replace BTP and inline SSR reflection fields with:

```java
private static ShoulderSurfingBridge shoulderSurfingBridge =
    ShoulderSurfingBridge.unavailable();
```

Pre-init:

```java
shoulderSurfingBridge = ShoulderSurfingBridge.detect(
    Loader.isModLoaded("shouldersurfing"), logger
);
```

Post-init, after proxy initialization:

```java
shoulderSurfingBridge.registerAdaptiveCrosshair(
    TargetingManager::isTrackingTarget
);
```

Make `TargetingManager.isTrackingTarget()` null-safe and report only SSR diagnostics.

- [ ] **Step 6: Verify optionality**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.*" --tests "com.zeldatargeting.mod.client.session.*"
```

Expected: PASS without any SSR jar on the test or runtime classpath.

- [ ] **Step 7: Commit Task 2**

```powershell
git add -- src/main/java/com/zeldatargeting/mod/client/camera/compat/ShoulderSurfingBridge.java src/main/java/com/zeldatargeting/mod/client/camera/compat/CameraRotationPolicy.java src/test/java/com/zeldatargeting/mod/client/camera/compat/CameraRotationPolicyTest.java src/main/java/com/zeldatargeting/mod/ZeldaTargetingMod.java src/main/java/com/zeldatargeting/mod/client/TargetingManager.java
git commit -m "feat: isolate optional shoulder surfing integration"
```

---

### Task 3: Runtime Camera, Mouse, Crosshair, and Movement Ownership

**Files:**
- Modify: `src/main/java/com/zeldatargeting/mod/client/camera/vanilla/VanillaCameraAdapter.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/camera/compat/CameraMovementBasis.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/camera/compat/MovementBasisRemapper.java`
- Delete: their two test files.

**Interfaces:**
- Consumes: `ShoulderAimSolver`, `ShoulderCameraState`, `ShoulderSurfingBridge`.
- Preserves: `CameraRuntimeAdapter<EntityLivingBase>`.
- Preserves: the existing presentation-only restore frames whose `shouldApplyRotation()` value is already false.

- [ ] **Step 1: Add the movement-ownership policy regression before adapter edits**

```java
@Test
public void activeShoulderSurfingDoesNotRequestInputRemapping() {
    Decision decision = CameraRotationPolicy.resolve(true);
    assertFalse(decision.shouldRemapMovement());
}
```

- [ ] **Step 2: Run and witness RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.compat.CameraRotationPolicyTest.activeShoulderSurfingDoesNotRequestInputRemapping"
```

Expected: compilation failure because `shouldRemapMovement()` does not exist.

- [ ] **Step 3: Make the binary policy explicitly reject movement remapping**

Add this behavior to `Decision`:

```java
public boolean shouldRemapMovement() {
    return false;
}
```

Run Step 2 again. Expected: PASS. This method documents the runtime contract and catches reintroduction of a second movement basis.

- [ ] **Step 4: Integrate compensated target deltas**

In `sampleFrame`:

```java
ShoulderCameraState shoulder = bridge.captureState();
AimVector aim = TargetingConfig.ssrCompensationEnabled
    ? ShoulderAimSolver.compensate(deltaX, deltaY, deltaZ, shoulder)
    : ShoulderAimSolver.compensate(
        deltaX, deltaY, deltaZ, ShoulderCameraState.inactive()
    );
```

Pass `aim` into `CameraInput`. Sample current player yaw/pitch directly. Apply each rotation-bearing frame to the player exactly once. In camera setup, skip Zelda translation when SSR owns offset.

- [ ] **Step 5: Delete the double-rotation path**

Delete `CameraMovementBasis`, `MovementBasisRemapper`, their tests, the `InputUpdateEvent` subscriber, camera-event rotation, reconstructed yaw/pitch sampling, and BTP inputs.

```powershell
rg -n "CameraMovementBasis|MovementBasisRemapper|InputUpdateEvent|CAMERA_EVENT" src/main/java src/test/java
```

Expected: no matches.

- [ ] **Step 6: Run focused camera tests**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.camera.*" --tests "com.zeldatargeting.mod.client.camera.core.*" --tests "com.zeldatargeting.mod.client.camera.compat.*"
```

Expected: PASS.

- [ ] **Step 7: Commit Task 3**

```powershell
git add -- src/main/java/com/zeldatargeting/mod/client/camera src/test/java/com/zeldatargeting/mod/client/camera
git commit -m "fix: align SSR camera and movement ownership"
```

---

### Task 4: Remove BTP Configuration and Retired Manual Offset

**Files:**
- Modify: all `TargetingConfig`, `TargetingSettings`, bridge, store, validator, preset, and `GuiTargetingConfig` files.
- Modify: `TargetingSettingsStoreTest` and `TargetingSettingsValidatorTest`.

**Interfaces:**
- Removes: `btpCompatibilityMode`, `btpCameraIntensity`, and `ssrXOffset`.
- Preserves: `ssrCompensationEnabled`.
- Produces: schema-4 saves that remove retired keys.

- [ ] **Step 1: Write retired-key cleanup test**

```java
@Test
public void savingRemovesRetiredCameraCompatibilityKeys() throws Exception {
    File directory = Files.createTempDirectory("zeldatargeting-retired-compat").toFile();
    File configFile = new File(directory, "zeldatargeting.cfg");
    Files.write(configFile.toPath(), (
        "meta.schemaVersion=4\n"
        + "compatibility.btpCompatibilityMode=gentle\n"
        + "compatibility.btpCameraIntensity=0.3\n"
        + "compatibility.ssrXOffset=-0.875\n"
    ).getBytes(StandardCharsets.UTF_8));

    TargetingSettingsStore store = new TargetingSettingsStore(configFile);
    store.save(store.load().getSettings());
    String contents = new String(
        Files.readAllBytes(configFile.toPath()), StandardCharsets.UTF_8
    );

    assertFalse(contents.contains("btpCompatibilityMode"));
    assertFalse(contents.contains("btpCameraIntensity"));
    assertFalse(contents.contains("ssrXOffset"));
    assertTrue(contents.contains("ssrCompensationEnabled"));
}
```

- [ ] **Step 2: Run and witness RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.config.TargetingSettingsStoreTest.savingRemovesRetiredCameraCompatibilityKeys"
```

Expected: FAIL because existing properties survive.

- [ ] **Step 3: Remove retired keys during save**

```java
configuration.remove("compatibility.btpCompatibilityMode");
configuration.remove("compatibility.btpCameraIntensity");
configuration.remove("compatibility.ssrXOffset");
write(configuration, "compatibility", "ssrCompensationEnabled",
    settings.ssrCompensationEnabled);
```

Delete all retired reads, writes, fields, copies, validation, runtime assignments, preset values, diagnostics, and GUI controls.

- [ ] **Step 4: Keep only meaningful compatibility controls**

```java
y = this.addControl(
    ControlSpec.toggle("ssrCompensationEnabled", "SSR Aim Convergence"), y
);
y = this.addControl(
    ControlSpec.toggle("debugCompatibility", "Compatibility Debug Output"), y
);
```

Update validator expectations to cover only supported target priority, HUD anchor, sound theme, and damage-number motion.

- [ ] **Step 5: Verify config and GUI**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.config.*" --tests "com.zeldatargeting.mod.client.gui.*"
rg -n "btpCompatibilityMode|btpCameraIntensity|ssrXOffset|isBetterThirdPersonLoaded|BETTER_THIRD_PERSON" src/main/java src/test/java
```

Expected: tests PASS and identifier search has no matches.

- [ ] **Step 6: Commit Task 4**

```powershell
git add -- src/main/java/com/zeldatargeting/mod/config src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java src/test/java/com/zeldatargeting/mod/config src/test/java/com/zeldatargeting/mod/client/gui
git commit -m "refactor: retire Better Third Person settings"
```

---

### Task 5: Documentation, Full Verification, and Test-Pack Handoff

**Files:**
- Modify: `README.md`
- Modify: `CHANGELOG.md`
- Build: `build/libs/zelda-targeting-1.4.0.jar`
- Install after safety checks: `C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\test\mods\zelda-targeting-1.4.0.jar`

- [ ] **Step 1: Update current documentation**

Document optional SSR 2.9.x, shoulder-aware convergence, vanilla fallback, and BTP removal. Leave historical specs unchanged because the approved SSR-only spec supersedes them.

- [ ] **Step 2: Audit identifiers and whitespace**

```powershell
rg -n "Better Third Person|betterthirdperson|btpCompatibilityMode|btpCameraIntensity|CameraMovementBasis|MovementBasisRemapper" src/main/java src/test/java README.md CHANGELOG.md
git diff --check
```

Expected: no retired current-code/documentation matches and no whitespace errors.

- [ ] **Step 3: Run clean verification**

```powershell
.\gradlew.bat clean test build
```

Expected: `BUILD SUCCESSFUL`, every JUnit test passing, and the 1.4.0 jar produced.

- [ ] **Step 4: Inspect the reobfuscated jar**

```powershell
jar tf build/libs/zelda-targeting-1.4.0.jar | rg "mcmod.info|ShoulderAimSolver|ShoulderSurfingBridge|CameraMovementBasis|MovementBasisRemapper"
```

Expected: metadata, solver, and bridge present; removed movement classes absent.

- [ ] **Step 5: Commit documentation**

```powershell
git add -- README.md CHANGELOG.md
git commit -m "docs: document SSR-only camera support"
```

- [ ] **Step 6: Install safely into the test pack**

Confirm Minecraft is closed and verify the resolved source/destination paths before overwriting only the Zelda jar:

```powershell
Copy-Item -LiteralPath "C:\Users\OneBeyondTheWall\Documents\The Lock on Mod\build\libs\zelda-targeting-1.4.0.jar" -Destination "C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\test\mods\zelda-targeting-1.4.0.jar" -Force
```

Do not enable or delete the existing `.jar.disabled` BTP file.

- [ ] **Step 7: Hand off and monitor manual verification**

Ask the user to test unlocked mouse speed, crosshair/melee alignment at 1-5 blocks, full-circle and point-blank WASD, shoulder swaps, free-look release, normal unlock, and vanilla fallback. Monitor `C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\test\logs\latest.log` for one SSR registration and no Zelda exception.
