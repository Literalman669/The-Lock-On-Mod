# Zelda Targeting 1.4.0 Stage 1 Safety Net Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish deterministic tests, extract reusable targeting and camera math, add immediate client-lifecycle cleanup, and record the pre-overhaul baseline without changing the mod's intended combat feel.

**Architecture:** This stage creates pure Java seams around the current targeting and camera calculations, then routes the existing Minecraft-facing classes through those seams. Lifecycle release decisions become a small pure policy used by `TargetingManager`; the full lock-on state machine remains deferred to Stage 2.

**Tech Stack:** Java 8, Minecraft 1.12.2, Forge 14.23.5.2859, ForgeGradle 3.0.197, Gradle 4.9, JUnit 4.13.2

## Global Constraints

- The mod remains a standalone client-only lock-on system that can connect to an unmodified multiplayer server.
- Minecraft remains at 1.12.2, Forge at 14.23.5.2859+, and Java at 8.
- No Epic Fight, Better Combat, or other combat framework dependency may be introduced.
- Optional-mod classes may not appear in core class signatures.
- Better Lock-On source code, assets, translations, naming, and branding may not be copied.
- Stage 1 must not replace the 1.3 configuration schema or bump the public version; those changes belong to later plans.
- Every task ends with focused tests, a clean relevant build, and its own commit.
- Production changes use test-first development: failing test, observed failure, minimal implementation, observed pass.

---

## Plan-Series Boundary

The approved design contains independent subsystems and is therefore split into seven executable plans:

1. Safety net and code boundaries — this plan.
2. Targeting core and lock-session state machine.
3. Camera director, framing, collision, free look, and restoration.
4. Adaptive Hybrid HUD, themes, feedback, audio, and accessibility.
5. Replacement configuration model, backup flow, presets, and GUI.
6. Vanilla, Better Third Person, and Shoulder Surfing camera adapters.
7. Integrated release QA, documentation, metadata, and 1.4.0 artifact.

Each later plan is authored after the preceding stage passes review. This prevents later plans from depending on file names or signatures invalidated by earlier implementation findings.

## File Structure for Stage 1

### New production files

- `src/main/java/com/zeldatargeting/mod/client/math/TargetingMath.java` — pure vector-angle, bearing, and adjacent-cycle calculations.
- `src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java` — pure characterization of the 1.3 strict comparison and threat rules.
- `src/main/java/com/zeldatargeting/mod/client/math/CameraMath.java` — pure wrapped-angle interpolation, clamping, and look-rotation calculations.
- `src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java` — explicit reasons for immediate lifecycle release.
- `src/main/java/com/zeldatargeting/mod/client/session/LifecycleGuard.java` — pure precedence rules for world/player/target lifecycle invalidation.

### New test files

- `src/test/java/com/zeldatargeting/mod/client/math/TargetingMathTest.java`
- `src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java`
- `src/test/java/com/zeldatargeting/mod/client/math/CameraMathTest.java`
- `src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java`
- `src/test/java/com/zeldatargeting/mod/client/session/LifecycleGuardTest.java`

### Modified production files

- `build.gradle` — add the JUnit 4 test dependency.
- `src/main/java/com/zeldatargeting/mod/client/targeting/EntityDetector.java` — delegate angle, bearing, and adjacent-index calculations to `TargetingMath`.
- `src/main/java/com/zeldatargeting/mod/client/targeting/TargetSelector.java` — delegate current strict comparison and threat calculation to `LegacySelectionRules`.
- `src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java` — delegate look rotation and interpolation calculations to `CameraMath`.
- `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java` — apply immediate lifecycle release before normal target processing.

### New development record

- `docs/development/1.4-stage-1-baseline.md` — record the verified source/build baseline and Stage 1 commands.

---

### Task 1: JUnit Harness and Deterministic Targeting Math

**Files:**

- Modify: `build.gradle:64`
- Create: `src/main/java/com/zeldatargeting/mod/client/math/TargetingMath.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/math/TargetingMathTest.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/targeting/EntityDetector.java:45`

**Interfaces:**

- Consumes: Java primitive vector components and current candidate-list indices.
- Produces: `TargetingMath.angleDegrees(double, double, double, double, double, double)`, `TargetingMath.horizontalBearing(double, double)`, and `TargetingMath.adjacentIndex(int, int, boolean)`.

- [ ] **Step 1: Add JUnit and write the failing targeting-math test**

Add this dependency inside the existing `dependencies` block in `build.gradle`:

```groovy
testImplementation 'junit:junit:4.13.2'
```

Create `src/test/java/com/zeldatargeting/mod/client/math/TargetingMathTest.java`:

```java
package com.zeldatargeting.mod.client.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TargetingMathTest {
    private static final double EPSILON = 0.0001D;

    @Test
    public void horizontalBearingUsesStableClockwiseMinecraftDirections() {
        assertEquals(0.0D, TargetingMath.horizontalBearing(0.0D, -1.0D), EPSILON);
        assertEquals(90.0D, TargetingMath.horizontalBearing(1.0D, 0.0D), EPSILON);
        assertEquals(180.0D, TargetingMath.horizontalBearing(0.0D, 1.0D), EPSILON);
        assertEquals(270.0D, TargetingMath.horizontalBearing(-1.0D, 0.0D), EPSILON);
    }

    @Test
    public void angleDegreesHandlesNormalizedAndUnnormalizedVectors() {
        assertEquals(0.0D, TargetingMath.angleDegrees(0, 0, 2, 0, 0, 10), EPSILON);
        assertEquals(90.0D, TargetingMath.angleDegrees(0, 0, 1, 1, 0, 0), EPSILON);
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 1, 0, 0, -5), EPSILON);
    }

    @Test
    public void angleDegreesRejectsZeroLengthInputAsOutsideTheCone() {
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 0, 1, 0, 0), EPSILON);
        assertEquals(180.0D, TargetingMath.angleDegrees(0, 0, 1, 0, 0, 0), EPSILON);
    }

    @Test
    public void adjacentIndexWrapsInBothDirections() {
        assertEquals(1, TargetingMath.adjacentIndex(0, 3, true));
        assertEquals(0, TargetingMath.adjacentIndex(2, 3, true));
        assertEquals(2, TargetingMath.adjacentIndex(0, 3, false));
        assertEquals(1, TargetingMath.adjacentIndex(2, 3, false));
    }

    @Test
    public void adjacentIndexDefinesEmptyAndMissingCurrentBehavior() {
        assertEquals(-1, TargetingMath.adjacentIndex(0, 0, true));
        assertEquals(0, TargetingMath.adjacentIndex(-1, 3, true));
        assertEquals(2, TargetingMath.adjacentIndex(-1, 3, false));
    }
}
```

- [ ] **Step 2: Run the test to verify the missing math seam fails**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.math.TargetingMathTest" --no-daemon
```

Expected: `compileTestJava` fails because `TargetingMath` does not exist.

- [ ] **Step 3: Implement the minimal pure targeting math**

Create `src/main/java/com/zeldatargeting/mod/client/math/TargetingMath.java`:

```java
package com.zeldatargeting.mod.client.math;

public final class TargetingMath {
    private TargetingMath() {
    }

    public static double horizontalBearing(double deltaX, double deltaZ) {
        double bearing = Math.toDegrees(Math.atan2(deltaX, -deltaZ));
        return bearing < 0.0D ? bearing + 360.0D : bearing;
    }

    public static double angleDegrees(
            double lookX,
            double lookY,
            double lookZ,
            double targetX,
            double targetY,
            double targetZ) {
        double lookLength = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
        double targetLength = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);
        if (lookLength == 0.0D || targetLength == 0.0D) {
            return 180.0D;
        }
        double dot = (lookX * targetX + lookY * targetY + lookZ * targetZ)
                / (lookLength * targetLength);
        double clampedDot = Math.max(-1.0D, Math.min(1.0D, dot));
        return Math.toDegrees(Math.acos(clampedDot));
    }

    public static int adjacentIndex(int currentIndex, int count, boolean forward) {
        if (count <= 0) {
            return -1;
        }
        if (currentIndex < 0 || currentIndex >= count) {
            return forward ? 0 : count - 1;
        }
        return Math.floorMod(currentIndex + (forward ? 1 : -1), count);
    }
}
```

- [ ] **Step 4: Route `EntityDetector` through `TargetingMath`**

Add the import:

```java
import com.zeldatargeting.mod.client.math.TargetingMath;
```

Replace the duplicated forward/backward index branches in `findNextTarget` with:

```java
int nextIndex = TargetingMath.adjacentIndex(currentIndex, validTargets.size(), forward);
return nextIndex >= 0 ? validTargets.get(nextIndex) : null;
```

Replace `getHorizontalBearing` with:

```java
private double getHorizontalBearing(EntityPlayer player, Entity entity) {
    return TargetingMath.horizontalBearing(entity.posX - player.posX, entity.posZ - player.posZ);
}
```

Replace `getAngleToEntity` with:

```java
private double getAngleToEntity(EntityPlayer player, Entity entity, Vec3d playerLook) {
    return TargetingMath.angleDegrees(
        playerLook.x,
        playerLook.y,
        playerLook.z,
        entity.posX - player.posX,
        entity.posY - player.posY,
        entity.posZ - player.posZ
    );
}
```

- [ ] **Step 5: Run the focused test and clean build**

Run:

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.math.TargetingMathTest" build --no-daemon
```

Expected: five JUnit tests pass and Gradle reports `BUILD SUCCESSFUL` after `reobfJar`.

- [ ] **Step 6: Commit the targeting math seam**

```powershell
git add build.gradle src/main/java/com/zeldatargeting/mod/client/math/TargetingMath.java src/main/java/com/zeldatargeting/mod/client/targeting/EntityDetector.java src/test/java/com/zeldatargeting/mod/client/math/TargetingMathTest.java
git commit -m "test: establish targeting math safety net"
```

---

### Task 2: Legacy Selection Policy Characterization

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/targeting/TargetSelector.java:32`

**Interfaces:**

- Consumes: primitive candidate scores plus the 1.3 hostile flag, lower-case entity name, and health ratio.
- Produces: `LegacySelectionRules.prefersLower(double, double)`, `LegacySelectionRules.prefersHigher(int, int)`, and `LegacySelectionRules.threatLevel(boolean, String, float)`.

- [ ] **Step 1: Write failing tests for strict ties and legacy threat behavior**

Create `src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java`:

```java
package com.zeldatargeting.mod.client.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacySelectionRulesTest {
    @Test
    public void lowerScoreWinsButEqualScoreKeepsTheFirstCandidate() {
        assertTrue(LegacySelectionRules.prefersLower(3.0D, 4.0D));
        assertFalse(LegacySelectionRules.prefersLower(4.0D, 4.0D));
        assertFalse(LegacySelectionRules.prefersLower(5.0D, 4.0D));
    }

    @Test
    public void higherThreatWinsButEqualThreatKeepsTheFirstCandidate() {
        assertTrue(LegacySelectionRules.prefersHigher(6, 5));
        assertFalse(LegacySelectionRules.prefersHigher(5, 5));
        assertFalse(LegacySelectionRules.prefersHigher(4, 5));
    }

    @Test
    public void hostileHighHealthCreeperReceivesAllLegacyBonuses() {
        assertEquals(6, LegacySelectionRules.threatLevel(true, "entitycreeper", 1.0F));
    }

    @Test
    public void legacyThreatRulesExposeNameAndHealthBonusesForStageTwoReplacement() {
        assertEquals(4, LegacySelectionRules.threatLevel(true, "entityzombie", 0.5F));
        assertEquals(1, LegacySelectionRules.threatLevel(false, "entitycow", 1.0F));
        assertEquals(0, LegacySelectionRules.threatLevel(false, "entitycow", 0.5F));
    }
}
```

- [ ] **Step 2: Run the test to verify the characterization seam is missing**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.math.LegacySelectionRulesTest" --no-daemon
```

Expected: `compileTestJava` fails because `LegacySelectionRules` does not exist.

- [ ] **Step 3: Implement the exact 1.3 selection rules as pure Java**

Create `src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java`:

```java
package com.zeldatargeting.mod.client.math;

public final class LegacySelectionRules {
    private LegacySelectionRules() {
    }

    public static boolean prefersLower(double candidateScore, double currentBestScore) {
        return candidateScore < currentBestScore;
    }

    public static boolean prefersHigher(int candidateScore, int currentBestScore) {
        return candidateScore > currentBestScore;
    }

    public static int threatLevel(boolean hostile, String lowerCaseEntityName, float healthRatio) {
        int threat = hostile ? 3 : 0;
        String entityName = lowerCaseEntityName == null ? "" : lowerCaseEntityName;

        if (entityName.contains("creeper")
                || entityName.contains("witch")
                || entityName.contains("skeleton")
                || entityName.contains("wither")) {
            threat += 2;
        }

        if (entityName.contains("zombie")
                || entityName.contains("spider")
                || entityName.contains("enderman")) {
            threat += 1;
        }

        if (healthRatio > 0.8F) {
            threat += 1;
        }

        return threat;
    }
}
```

- [ ] **Step 4: Route `TargetSelector` through the characterized rules**

Add:

```java
import com.zeldatargeting.mod.client.math.LegacySelectionRules;
```

Replace the candidate comparisons in the distance, health, threat, and angle loops with:

```java
if (LegacySelectionRules.prefersLower(distance, nearestDistance)) {
```

```java
if (LegacySelectionRules.prefersLower(health, lowestHealthValue)) {
```

```java
if (LegacySelectionRules.prefersHigher(threatLevel, highestThreatLevel)) {
```

```java
if (LegacySelectionRules.prefersLower(angle, smallestAngle)) {
```

Replace `calculateThreatLevel` with:

```java
private int calculateThreatLevel(Entity entity) {
    float healthRatio = 0.0F;
    if (entity instanceof EntityLiving) {
        EntityLiving living = (EntityLiving) entity;
        healthRatio = living.getMaxHealth() > 0.0F
            ? living.getHealth() / living.getMaxHealth()
            : 0.0F;
    }
    return LegacySelectionRules.threatLevel(
        entity instanceof IMob,
        entity.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT),
        healthRatio
    );
}
```

This deliberately preserves the 1.3 name-based threat behavior for characterization. Stage 2 replaces it with the approved generic client-visible threat model and updates these tests in the same red-green cycle.

- [ ] **Step 5: Run focused selection tests and clean build**

Run:

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.math.*" build --no-daemon
```

Expected: nine targeting/selection tests pass and Gradle reports `BUILD SUCCESSFUL` after `reobfJar`.

- [ ] **Step 6: Commit the selection characterization seam**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java src/main/java/com/zeldatargeting/mod/client/targeting/TargetSelector.java src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java
git commit -m "test: characterize legacy target selection"
```

---

### Task 3: Pure Camera Rotation Math

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/math/CameraMath.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/math/CameraMathTest.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java:88`

**Interfaces:**

- Consumes: primitive positions, current/target angles, and interpolation factors.
- Produces: `CameraMath.lookAt(double, double, double)`, `CameraMath.wrapDegrees(float)`, `CameraMath.interpolateAngle(float, float, float)`, `CameraMath.interpolate(float, float, float)`, `CameraMath.clamp(float, float, float)`, and immutable `CameraMath.Rotation`.

- [ ] **Step 1: Write the failing camera-math test**

Create `src/test/java/com/zeldatargeting/mod/client/math/CameraMathTest.java`:

```java
package com.zeldatargeting.mod.client.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CameraMathTest {
    private static final float EPSILON = 0.0001F;

    @Test
    public void wrapDegreesUsesShortestSignedDifference() {
        assertEquals(-170.0F, CameraMath.wrapDegrees(190.0F), EPSILON);
        assertEquals(170.0F, CameraMath.wrapDegrees(-190.0F), EPSILON);
        assertEquals(45.0F, CameraMath.wrapDegrees(405.0F), EPSILON);
    }

    @Test
    public void interpolateAngleCrossesTheBoundaryByTheShortPath() {
        assertEquals(180.0F, CameraMath.interpolateAngle(170.0F, -170.0F, 0.5F), EPSILON);
        assertEquals(-180.0F, CameraMath.interpolateAngle(-170.0F, 170.0F, 0.5F), EPSILON);
    }

    @Test
    public void lookAtMatchesMinecraftYawAndPitchConventions() {
        CameraMath.Rotation forward = CameraMath.lookAt(0.0D, 0.0D, 1.0D);
        assertEquals(0.0F, forward.getYaw(), EPSILON);
        assertEquals(0.0F, forward.getPitch(), EPSILON);

        CameraMath.Rotation right = CameraMath.lookAt(1.0D, 0.0D, 0.0D);
        assertEquals(-90.0F, right.getYaw(), EPSILON);
        assertEquals(0.0F, right.getPitch(), EPSILON);

        CameraMath.Rotation aboveForward = CameraMath.lookAt(0.0D, 1.0D, 1.0D);
        assertEquals(0.0F, aboveForward.getYaw(), EPSILON);
        assertEquals(-45.0F, aboveForward.getPitch(), EPSILON);
    }

    @Test
    public void zeroLengthLookAtReturnsNeutralRotation() {
        CameraMath.Rotation rotation = CameraMath.lookAt(0.0D, 0.0D, 0.0D);
        assertEquals(0.0F, rotation.getYaw(), EPSILON);
        assertEquals(0.0F, rotation.getPitch(), EPSILON);
    }

    @Test
    public void interpolationAndClampHaveDefinedBounds() {
        assertEquals(4.0F, CameraMath.interpolate(0.0F, 10.0F, 0.4F), EPSILON);
        assertEquals(90.0F, CameraMath.clamp(120.0F, -90.0F, 90.0F), EPSILON);
        assertEquals(-90.0F, CameraMath.clamp(-120.0F, -90.0F, 90.0F), EPSILON);
    }
}
```

- [ ] **Step 2: Run the test to verify the missing camera seam fails**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.math.CameraMathTest" --no-daemon
```

Expected: `compileTestJava` fails because `CameraMath` does not exist.

- [ ] **Step 3: Implement `CameraMath`**

Create `src/main/java/com/zeldatargeting/mod/client/math/CameraMath.java`:

```java
package com.zeldatargeting.mod.client.math;

public final class CameraMath {
    private CameraMath() {
    }

    public static Rotation lookAt(double deltaX, double deltaY, double deltaZ) {
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (horizontalDistance == 0.0D && deltaY == 0.0D) {
            return new Rotation(0.0F, 0.0F);
        }
        float yaw = (float) Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        float pitch = (float) -Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        return new Rotation(yaw, clamp(pitch, -90.0F, 90.0F));
    }

    public static float wrapDegrees(float value) {
        float wrapped = value % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public static float interpolateAngle(float current, float target, float factor) {
        return current + wrapDegrees(target - current) * factor;
    }

    public static float interpolate(float current, float target, float factor) {
        return current + (target - current) * factor;
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static final class Rotation {
        private final float yaw;
        private final float pitch;

        public Rotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
```

- [ ] **Step 4: Route `CameraController` through `CameraMath`**

Add:

```java
import com.zeldatargeting.mod.client.math.CameraMath;
```

In `updateCamera`, replace wrapped difference, pitch clamp, and interpolation calls with:

```java
float yawVelocity = CameraMath.wrapDegrees(targetYaw - prevTargetYaw);
float pitchVelocity = targetPitch - prevTargetPitch;

float feedTargetYaw = targetYaw + yawVelocity;
float feedTargetPitch = CameraMath.clamp(targetPitch + pitchVelocity, -90.0F, 90.0F);

float yawDiff = CameraMath.wrapDegrees(feedTargetYaw - currentYaw);
float pitchDiff = feedTargetPitch - currentPitch;

currentYaw = CameraMath.interpolateAngle(currentYaw, feedTargetYaw, adaptiveSmoothing);
currentPitch = CameraMath.clamp(
    CameraMath.interpolate(currentPitch, feedTargetPitch, adaptiveSmoothing),
    -90.0F,
    90.0F
);
```

In `calculateTargetRotation`, replace the first yaw/pitch trigonometry block with:

```java
CameraMath.Rotation desiredRotation = CameraMath.lookAt(
    targetX - playerX,
    targetY - playerY,
    targetZ - playerZ
);
float newYaw = desiredRotation.getYaw();
float newPitch = desiredRotation.getPitch();
```

Replace adjustment wrapping and clamping with:

```java
float yawDiff = CameraMath.wrapDegrees(newYaw - currentPlayerYaw);
float pitchDiff = newPitch - currentPlayerPitch;

yawDiff = CameraMath.clamp(yawDiff, -TargetingConfig.maxYawAdjustment, TargetingConfig.maxYawAdjustment);
pitchDiff = CameraMath.clamp(pitchDiff, -TargetingConfig.maxPitchAdjustment, TargetingConfig.maxPitchAdjustment);

targetYaw = currentPlayerYaw + yawDiff;
targetPitch = CameraMath.clamp(currentPlayerPitch + pitchDiff, -90.0F, 90.0F);
```

Replace the Shoulder Surfing yaw/pitch trigonometry block with:

```java
CameraMath.Rotation shoulderRotation = CameraMath.lookAt(dX, dY, dZ);
targetYaw = shoulderRotation.getYaw();
targetPitch = shoulderRotation.getPitch();
```

Delete the now-unused private `interpolateAngle` and `interpolateFloat` methods and remove imports that are no longer referenced.

- [ ] **Step 5: Run camera tests and the clean build**

Run:

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.math.*" build --no-daemon
```

Expected: fourteen targeting, selection, and camera math tests pass and Gradle reports `BUILD SUCCESSFUL` after `reobfJar`.

- [ ] **Step 6: Commit the camera math seam**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/math/CameraMath.java src/main/java/com/zeldatargeting/mod/client/targeting/CameraController.java src/test/java/com/zeldatargeting/mod/client/math/CameraMathTest.java
git commit -m "refactor: isolate camera rotation math"
```

---

### Task 4: Legacy Configuration Characterization

**Files:**

- Create: `src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java`

**Interfaces:**

- Consumes: existing public static values and `TargetingConfig.resetToDefaults()`.
- Produces: a regression contract for the 1.3 defaults that Stage 5 can explicitly replace rather than accidentally lose.

- [ ] **Step 1: Mutate representative legacy values and write the characterization tests**

Create `src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java`:

```java
package com.zeldatargeting.mod.config;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyConfigDefaultsTest {
    private static final double DOUBLE_EPSILON = 0.0001D;
    private static final float FLOAT_EPSILON = 0.0001F;

    @After
    public void restoreDefaults() {
        TargetingConfig.resetToDefaults();
    }

    @Test
    public void resetRestoresTargetingAndCameraDefaults() {
        TargetingConfig.targetingRange = 50.0D;
        TargetingConfig.maxTrackingDistance = 90.0D;
        TargetingConfig.maxAngle = 180.0D;
        TargetingConfig.requireLineOfSight = false;
        TargetingConfig.cameraSmoothness = 0.9F;
        TargetingConfig.autoThirdPerson = true;

        TargetingConfig.resetToDefaults();

        assertEquals(16.0D, TargetingConfig.targetingRange, DOUBLE_EPSILON);
        assertEquals(20.0D, TargetingConfig.maxTrackingDistance, DOUBLE_EPSILON);
        assertEquals(60.0D, TargetingConfig.maxAngle, DOUBLE_EPSILON);
        assertTrue(TargetingConfig.requireLineOfSight);
        assertEquals("nearest", TargetingConfig.targetPriority);
        assertEquals(0.4F, TargetingConfig.cameraSmoothness, FLOAT_EPSILON);
        assertEquals(60.0F, TargetingConfig.maxPitchAdjustment, FLOAT_EPSILON);
        assertEquals(90.0F, TargetingConfig.maxYawAdjustment, FLOAT_EPSILON);
        assertTrue(TargetingConfig.enableCameraLockOn);
        assertFalse(TargetingConfig.autoThirdPerson);
        assertEquals("balanced", TargetingConfig.lockOnPreset);
    }

    @Test
    public void resetRestoresHudAndEntityFilterDefaults() {
        TargetingConfig.showReticle = false;
        TargetingConfig.bossStylePanel = true;
        TargetingConfig.targetPlayers = true;

        TargetingConfig.resetToDefaults();

        assertTrue(TargetingConfig.showReticle);
        assertTrue(TargetingConfig.showHealthBar);
        assertTrue(TargetingConfig.showDistance);
        assertTrue(TargetingConfig.showTargetName);
        assertEquals(1.0F, TargetingConfig.reticleScale, FLOAT_EPSILON);
        assertEquals(0xFF0000, TargetingConfig.reticleColor);
        assertFalse(TargetingConfig.compactHudMode);
        assertFalse(TargetingConfig.softAimIndicator);
        assertFalse(TargetingConfig.targetHistoryEnabled);
        assertFalse(TargetingConfig.bossStylePanel);
        assertEquals("top-right", TargetingConfig.hudAnchor);
        assertTrue(TargetingConfig.targetHostileMobs);
        assertTrue(TargetingConfig.targetNeutralMobs);
        assertTrue(TargetingConfig.targetPassiveMobs);
        assertFalse(TargetingConfig.targetPlayers);
    }

    @Test
    public void resetRestoresFeedbackAndPerformanceDefaults() {
        TargetingConfig.enableSounds = false;
        TargetingConfig.enableDamageNumbers = false;
        TargetingConfig.updateFrequency = 20;
        TargetingConfig.validationInterval = 1;

        TargetingConfig.resetToDefaults();

        assertTrue(TargetingConfig.enableSounds);
        assertEquals(1.0F, TargetingConfig.soundVolume, FLOAT_EPSILON);
        assertEquals("default", TargetingConfig.soundTheme);
        assertTrue(TargetingConfig.enableDamageNumbers);
        assertEquals(1.0F, TargetingConfig.damageNumbersScale, FLOAT_EPSILON);
        assertEquals(60, TargetingConfig.damageNumbersDuration);
        assertEquals("default", TargetingConfig.damageNumbersMotion);
        assertTrue(TargetingConfig.showDamagePrediction);
        assertTrue(TargetingConfig.showHitsToKill);
        assertTrue(TargetingConfig.showVulnerabilities);
        assertEquals(1, TargetingConfig.updateFrequency);
        assertEquals(10, TargetingConfig.validationInterval);
    }
}
```

- [ ] **Step 2: Prove the test detects a legacy-default regression**

Temporarily change the expected targeting range in the first test from `16.0D` to `17.0D`, then run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.config.LegacyConfigDefaultsTest" --no-daemon
```

Expected: one test fails with expected `17.0` but was `16.0`.

- [ ] **Step 3: Restore the correct expectation and run the characterization tests**

Restore the assertion to:

```java
assertEquals(16.0D, TargetingConfig.targetingRange, DOUBLE_EPSILON);
```

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.config.LegacyConfigDefaultsTest" --no-daemon
```

Expected: three tests pass.

- [ ] **Step 4: Commit the legacy configuration contract**

```powershell
git add src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java
git commit -m "test: characterize legacy configuration defaults"
```

---

### Task 5: Immediate Client-Lifecycle Cleanup

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/session/LifecycleGuard.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/session/LifecycleGuardTest.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java:62`

**Interfaces:**

- Consumes: booleans describing world, player, player-alive, target, and dimension state.
- Produces: `LifecycleGuard.evaluate(boolean, boolean, boolean, boolean, boolean)` returning `LockReleaseReason`.

- [ ] **Step 1: Write the failing lifecycle policy test**

Create `src/test/java/com/zeldatargeting/mod/client/session/LifecycleGuardTest.java`:

```java
package com.zeldatargeting.mod.client.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LifecycleGuardTest {
    @Test
    public void validClientAndTargetDoNotRelease() {
        assertEquals(
            LockReleaseReason.NONE,
            LifecycleGuard.evaluate(true, true, true, true, true)
        );
    }

    @Test
    public void missingWorldHasHighestPrecedence() {
        assertEquals(
            LockReleaseReason.WORLD_UNAVAILABLE,
            LifecycleGuard.evaluate(false, false, false, false, false)
        );
    }

    @Test
    public void missingPlayerReleasesBeforePlayerStateChecks() {
        assertEquals(
            LockReleaseReason.PLAYER_UNAVAILABLE,
            LifecycleGuard.evaluate(true, false, false, true, true)
        );
    }

    @Test
    public void deadPlayerReleasesImmediately() {
        assertEquals(
            LockReleaseReason.PLAYER_DEAD,
            LifecycleGuard.evaluate(true, true, false, true, true)
        );
    }

    @Test
    public void missingTargetAndDimensionMismatchHaveExplicitReasons() {
        assertEquals(
            LockReleaseReason.TARGET_UNAVAILABLE,
            LifecycleGuard.evaluate(true, true, true, false, true)
        );
        assertEquals(
            LockReleaseReason.DIMENSION_CHANGED,
            LifecycleGuard.evaluate(true, true, true, true, false)
        );
    }
}
```

- [ ] **Step 2: Run the lifecycle test to verify the new policy is missing**

Run:

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.session.LifecycleGuardTest" --no-daemon
```

Expected: `compileTestJava` fails because `LifecycleGuard` and `LockReleaseReason` do not exist.

- [ ] **Step 3: Implement release reasons and precedence**

Create `src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java`:

```java
package com.zeldatargeting.mod.client.session;

public enum LockReleaseReason {
    NONE,
    MANUAL,
    WORLD_UNAVAILABLE,
    PLAYER_UNAVAILABLE,
    PLAYER_DEAD,
    TARGET_UNAVAILABLE,
    DIMENSION_CHANGED
}
```

Create `src/main/java/com/zeldatargeting/mod/client/session/LifecycleGuard.java`:

```java
package com.zeldatargeting.mod.client.session;

public final class LifecycleGuard {
    private LifecycleGuard() {
    }

    public static LockReleaseReason evaluate(
            boolean worldAvailable,
            boolean playerAvailable,
            boolean playerAlive,
            boolean targetAvailable,
            boolean sameDimension) {
        if (!worldAvailable) {
            return LockReleaseReason.WORLD_UNAVAILABLE;
        }
        if (!playerAvailable) {
            return LockReleaseReason.PLAYER_UNAVAILABLE;
        }
        if (!playerAlive) {
            return LockReleaseReason.PLAYER_DEAD;
        }
        if (!targetAvailable) {
            return LockReleaseReason.TARGET_UNAVAILABLE;
        }
        if (!sameDimension) {
            return LockReleaseReason.DIMENSION_CHANGED;
        }
        return LockReleaseReason.NONE;
    }
}
```

- [ ] **Step 4: Apply the lifecycle guard before normal tick processing**

Add imports to `TargetingManager`:

```java
import com.zeldatargeting.mod.client.session.LifecycleGuard;
import com.zeldatargeting.mod.client.session.LockReleaseReason;
```

Immediately after the END-phase check in `onClientTick`, add:

```java
if (isActive) {
    Minecraft mc = Minecraft.getMinecraft();
    boolean playerAvailable = mc.player != null;
    boolean targetAvailable = currentTarget != null;
    boolean sameDimension = playerAvailable
        && targetAvailable
        && mc.player.dimension == currentTarget.dimension;
    LockReleaseReason releaseReason = LifecycleGuard.evaluate(
        mc.world != null,
        playerAvailable,
        playerAvailable && mc.player.isEntityAlive(),
        targetAvailable,
        sameDimension
    );
    if (releaseReason != LockReleaseReason.NONE) {
        ZeldaTargetingMod.getLogger().debug("Lock-on cleared: " + releaseReason);
        disableLockOn(false);
        return;
    }
}
```

Replace the current no-argument `disableLockOn` implementation with these two methods:

```java
private void disableLockOn() {
    disableLockOn(true);
}

private void disableLockOn(boolean playFeedback) {
    if (isActive) {
        if (playFeedback) {
            TargetingSounds.playTargetLostSound();
        }

        if (TargetingConfig.autoThirdPerson) {
            Minecraft.getMinecraft().gameSettings.thirdPersonView = previousPerspective;
        }

        isActive = false;
        currentTarget = null;
        targetTracker.clearTarget();
        cameraController.resetCamera();
        ZeldaTargetingMod.getLogger().debug("Lock-on disabled");
    }
}
```

This preserves audible feedback for manual and normal target-loss paths while suppressing sounds during disconnect, death, and world teardown.

- [ ] **Step 5: Run lifecycle tests and full clean build**

Run:

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.session.LifecycleGuardTest" build --no-daemon
```

Expected: five lifecycle tests pass and Gradle reports `BUILD SUCCESSFUL` after `reobfJar`.

- [ ] **Step 6: Perform the focused in-game lifecycle smoke test**

Run the development client:

```powershell
.\gradlew.bat runClient --no-daemon
```

Verify in one local world:

1. Lock a living target and confirm ordinary unlock still plays target-lost feedback.
2. Lock a target, return to the title screen, and confirm no target-lost sound plays during teardown.
3. Re-enter a world and confirm the HUD and camera do not retain the prior target.
4. Lock a target, let the player die, and confirm the lock clears before respawn.

Expected: no crash, no stale HUD, no retained camera rotation, and no lifecycle teardown sound.

- [ ] **Step 7: Commit lifecycle cleanup**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java src/main/java/com/zeldatargeting/mod/client/session/LifecycleGuard.java src/main/java/com/zeldatargeting/mod/client/TargetingManager.java src/test/java/com/zeldatargeting/mod/client/session/LifecycleGuardTest.java
git commit -m "fix: clear lock state on client lifecycle exit"
```

---

### Task 6: Record and Verify the Stage 1 Baseline

**Files:**

- Create: `docs/development/1.4-stage-1-baseline.md`

**Interfaces:**

- Consumes: committed Stage 1 source, test results, build output, and the recovered 1.3 source baseline.
- Produces: a stable verification record for Stage 2 planning and regression comparison.

- [ ] **Step 1: Write the baseline record**

Create `docs/development/1.4-stage-1-baseline.md` with this content:

```markdown
# Zelda Targeting 1.4 Stage 1 Baseline

## Recovered Release Baseline

- Source baseline: `b9b3939c8489c43155b39fcb7b2a616e28bf5f83`
- Published artifact: CurseForge file `7688484`, `zelda-targeting-1.3.0.jar`
- Published SHA-256: `51EDC7E883D9575498D91D25F3700C2C75AEFFE54B6E173D8CED49351A363147`
- Java: Oracle JDK 8u202
- Gradle: 4.9 wrapper
- Forge: 1.12.2-14.23.5.2859
- Warm-cache clean build: 7 seconds, 8 executed Gradle tasks, including `reobfJar`

## Pre-Overhaul Structure

- `GuiTargetingConfig.java`: 1,236 lines
- `TargetRenderer.java`: 671 lines
- `TargetingConfig.java`: 502 lines
- `DamageNumbersRenderer.java`: 287 lines
- `TargetingSounds.java`: 287 lines
- `CameraController.java`: 235 lines
- Automated tests before Stage 1: none

## Pre-Overhaul Hot-Path Observations

- `TargetingManager` updates camera tracking on every END client tick while locked.
- `TargetTracker` performs full target validation every 10 client ticks.
- `EntityDetector` scans on acquisition and cycle input, reuses two internal target lists, and receives a new result list from Forge's world query.
- `CameraController` performs scalar math per locked tick and does not create a collection.
- `TargetRenderer` runs from the render event every frame and owns HUD, world markers, soft aim, history, and boss presentation in one 671-line class.

## Stage 1 Verification Commands

```powershell
.\gradlew.bat clean test build --no-daemon
git diff --check
git status --short --branch
```

## Stage 1 Required Outcomes

- Targeting, camera, configuration, and lifecycle tests all pass.
- Forge `reobfJar` succeeds from a clean build.
- The lifecycle smoke test clears state on title-screen exit and player death.
- No public version or configuration schema changes occur.
- The tracked worktree is clean after the final Stage 1 commit.
```

- [ ] **Step 2: Run the complete Stage 1 verification**

Run:

```powershell
.\gradlew.bat clean test build --no-daemon
git diff --check
git status --short --branch
```

Expected: twenty-two JUnit tests pass, `reobfJar` completes, `git diff --check` reports nothing, and only the uncommitted baseline document appears before its commit.

- [ ] **Step 3: Confirm the artifact metadata remains 1.3.0**

Run:

```powershell
$builtJar = (Resolve-Path 'build\libs\zelda-targeting-1.3.0.jar').Path
$metadataDir = Join-Path (Get-Location) 'build\verification\metadata'
New-Item -ItemType Directory -Force -Path $metadataDir | Out-Null
Push-Location $metadataDir
& 'C:\Program Files\Java\jdk1.8.0_202\bin\jar.exe' xf $builtJar 'mcmod.info'
Select-String -Path 'mcmod.info' -Pattern '"version": "1.3.0"'
Pop-Location
```

Expected: one matching `"version": "1.3.0"` line. The extracted metadata remains under ignored `build/verification`; no tracked or user-owned file is overwritten or deleted.

- [ ] **Step 4: Commit the verified baseline record**

```powershell
git add docs/development/1.4-stage-1-baseline.md
git commit -m "docs: record 1.4 stage one baseline"
```

- [ ] **Step 5: Confirm the final Stage 1 commit series and clean tree**

Run:

```powershell
git log -8 --oneline
git status --short --branch
```

Expected: six Stage 1 implementation commits plus the existing design and plan commits are visible, and the branch has no tracked modifications.

---

## Stage 1 Completion Gate

Stage 1 is complete only when:

- all twenty-two tests pass from `clean`;
- Forge reobfuscation succeeds;
- target bearing, detection angle, and cycle-index behavior route through `TargetingMath`;
- camera look rotation and interpolation route through `CameraMath`;
- legacy configuration defaults have a regression contract;
- missing world, missing player, player death, missing target, and dimension change have explicit lifecycle release reasons;
- normal unlock retains feedback while lifecycle teardown suppresses it;
- the in-game lifecycle smoke test passes;
- the baseline document is committed; and
- the tracked worktree is clean.
