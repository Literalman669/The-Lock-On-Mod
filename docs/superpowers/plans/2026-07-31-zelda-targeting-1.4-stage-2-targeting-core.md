# Zelda Targeting 1.4.0 Stage 2 Targeting Core Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy target selector, tracker, and manager-owned lock booleans with deterministic candidate scoring, generic anchors, stable cycling/history, and one tested lock-session state model.

**Architecture:** Pure generic value objects and policies live under `client.targeting.core`; they contain no Minecraft or optional-mod types and are tested with string target references. `LockOnSession<T>` is the only mutable lock owner and publishes immutable `LockOnSnapshot<T>` values. `TargetingService<T>` coordinates a reusable candidate buffer through a `TargetProvider<T>` boundary, while `EntityDetector` becomes the Minecraft 1.12.2 adapter and `TargetingManager` remains input/presentation orchestration only.

**Tech Stack:** Java 8, Minecraft 1.12.2, Forge 14.23.5.2859, ForgeGradle 3.0.197, Gradle 4.9, JUnit 4.13.2

## Global Constraints

- The mod remains a standalone client-only lock-on system that can connect to an unmodified multiplayer server.
- Minecraft remains at 1.12.2, Forge at 14.23.5.2859+, and Java at 8.
- No Epic Fight, Better Combat, or other combat framework dependency may be introduced.
- Optional-mod classes may not appear in core class signatures.
- Better Lock-On source code, assets, translations, naming, and branding may not be copied.
- Stage 2 must not replace the 1.3 configuration schema or bump the public version.
- The current 16-block acquisition range, 20-block tracking distance, 60-degree acquisition cone, required line of sight, 250-millisecond cycle cooldown, 750-millisecond occlusion grace, and 200-millisecond release fade remain the baseline.
- Quick switch is implemented and tested in the core but remains disabled by the 1.3 configuration adapter until the replacement schema arrives in Stage 5.
- Anchor cycling is implemented and tested in the core; its configurable input binding is deferred to the replacement input/configuration stage.
- Manual cycling always chooses the adjacent bearing-sorted candidate; history affects automatic replacement only.
- Candidate scans may allocate only on acquisition, cycling, or quick switch. Locked ticks validate one target directly.
- Every production change uses a failing test first, and every task ends with focused tests, `build`, `reobfJar`, and an atomic commit.

---

## Stage 2 File Structure

### New pure targeting files

- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchor.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetPoint.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetObservation.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchorResolver.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetPriority.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCandidate.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetScorer.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRules.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetHistory.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetProvider.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java`

### New session files

- `src/main/java/com/zeldatargeting/mod/client/session/LockPhase.java`
- `src/main/java/com/zeldatargeting/mod/client/session/LockOnSnapshot.java`
- `src/main/java/com/zeldatargeting/mod/client/session/LockOnSession.java`

### Modified Minecraft-facing files

- `src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/EntityDetector.java`
- `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`

### Removed legacy files after integration

- `src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/TargetSelector.java`
- `src/main/java/com/zeldatargeting/mod/client/targeting/TargetTracker.java`
- `src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java`

### New tests and record

- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchorResolverTest.java`
- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetObservationTest.java`
- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetScorerTest.java`
- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRulesTest.java`
- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetHistoryTest.java`
- `src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java`
- `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java`
- `docs/development/1.4-stage-2-targeting-core.md`

---

### Task 1: Generic Target Observations and Anchor Geometry

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchor.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetPoint.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetObservation.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchorResolver.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchorResolverTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetObservationTest.java`

**Interfaces:**

- Produces: `TargetAnchor`, immutable `TargetPoint`, immutable generic `TargetObservation<T>`, and `TargetAnchorResolver.resolve(TargetAnchor, double, double, double, double, double)`.
- Consumers: `TargetCandidate<T>`, `LockOnSession<T>`, `EntityDetector`, and later camera/HUD stages.

- [ ] **Step 1: Write the failing anchor and detached-observation tests**

Create `TargetAnchorResolverTest.java` with five tests using literal bounds:

```java
package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TargetAnchorResolverTest {
    private static final double EPSILON = 0.0001D;

    @Test
    public void headUsesEyeHeightAndClampsToBoundingBoxTop() {
        TargetPoint normal = TargetAnchorResolver.resolve(TargetAnchor.HEAD, 4, 10, 8, 12, 1.62D);
        TargetPoint clamped = TargetAnchorResolver.resolve(TargetAnchor.HEAD, 4, 10, 8, 11, 2.5D);
        assertEquals(4.0D, normal.getX(), EPSILON);
        assertEquals(11.62D, normal.getY(), EPSILON);
        assertEquals(8.0D, normal.getZ(), EPSILON);
        assertEquals(11.0D, clamped.getY(), EPSILON);
    }

    @Test
    public void centerUsesBoundingBoxMidpoint() {
        assertEquals(11.0D,
            TargetAnchorResolver.resolve(TargetAnchor.CENTER, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void lowerBodyUsesFirstQuarterOfBoundingBoxHeight() {
        assertEquals(10.5D,
            TargetAnchorResolver.resolve(TargetAnchor.LOWER_BODY, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void missingAnchorFallsBackToCenter() {
        assertEquals(11.0D,
            TargetAnchorResolver.resolve(null, 4, 10, 8, 12, 1.62D).getY(), EPSILON);
    }

    @Test
    public void anchorCyclingWrapsInBothDirections() {
        assertEquals(TargetAnchor.CENTER, TargetAnchor.HEAD.cycle(true));
        assertEquals(TargetAnchor.LOWER_BODY, TargetAnchor.CENTER.cycle(true));
        assertEquals(TargetAnchor.HEAD, TargetAnchor.LOWER_BODY.cycle(true));
        assertEquals(TargetAnchor.LOWER_BODY, TargetAnchor.HEAD.cycle(false));
    }
}
```

Create `TargetObservationTest.java`:

```java
package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TargetObservationTest {
    @Test
    public void withoutReferenceRetainsDetachedPresentationValues() {
        TargetObservation<String> source = new TargetObservation<>(
            "entity", 42, "Pig", 8.0F, 10.0F, true, true, true,
            6.5D, 90.0D, false, TargetAnchor.HEAD,
            new TargetPoint(1.0D, 2.0D, 3.0D), 0.9D, 1.0D
        );

        TargetObservation<String> detached = source.withoutReference();

        assertNull(detached.getReference());
        assertEquals(42, detached.getEntityId());
        assertEquals("Pig", detached.getName());
        assertEquals(8.0F, detached.getHealth(), 0.0001F);
        assertEquals(TargetAnchor.HEAD, detached.getAnchor());
        assertEquals(2.0D, detached.getFocus().getY(), 0.0001D);
    }
}
```

- [ ] **Step 2: Run the tests and observe missing-type compilation failures**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.targeting.core.TargetAnchorResolverTest" --tests "com.zeldatargeting.mod.client.targeting.core.TargetObservationTest" --no-daemon
```

Expected: `compileTestJava` fails because the four production types do not exist.

- [ ] **Step 3: Implement the anchor and point types**

`TargetAnchor` values are `HEAD`, `CENTER`, and `LOWER_BODY`. Its `cycle(boolean forward)` method advances by enum order through `Math.floorMod(ordinal() + (forward ? 1 : -1), values().length)`.

`TargetPoint` is a final immutable value with `double x`, `y`, and `z`, a three-argument constructor, and getters.

Implement `TargetAnchorResolver` exactly as:

```java
public static TargetPoint resolve(
        TargetAnchor anchor,
        double centerX,
        double minimumY,
        double centerZ,
        double maximumY,
        double eyeHeight) {
    TargetAnchor resolved = anchor == null ? TargetAnchor.CENTER : anchor;
    double height = Math.max(0.0D, maximumY - minimumY);
    double y;
    switch (resolved) {
        case HEAD:
            y = Math.min(maximumY, minimumY + Math.max(0.0D, eyeHeight));
            break;
        case LOWER_BODY:
            y = minimumY + height * 0.25D;
            break;
        case CENTER:
        default:
            y = minimumY + height * 0.5D;
            break;
    }
    return new TargetPoint(centerX, y, centerZ);
}
```

- [ ] **Step 4: Implement `TargetObservation<T>`**

Use the constructor signature exercised by the test and final fields for:

```java
T reference;
int entityId;
String name;
float health;
float maxHealth;
boolean alive;
boolean present;
boolean sameDimension;
double distance;
double horizontalBearingDegrees;
boolean visible;
TargetAnchor anchor;
TargetPoint focus;
double width;
double height;
```

Normalize `name == null` to `""`, `anchor == null` to `CENTER`, and `focus == null` to `(0,0,0)`. Provide getters for every field. `withoutReference()` returns a new observation with all values unchanged except a null reference.

- [ ] **Step 5: Run focused tests and clean Forge build**

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.targeting.core.TargetAnchorResolverTest" --tests "com.zeldatargeting.mod.client.targeting.core.TargetObservationTest" build --no-daemon
```

Expected: six new tests pass and `reobfJar` succeeds.

- [ ] **Step 6: Commit the observation boundary**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/targeting/core src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetAnchorResolverTest.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetObservationTest.java
git commit -m "feat: define target observations and anchors"
```

---

### Task 2: Deterministic Candidate Scoring

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetPriority.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCandidate.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetScorer.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetScorerTest.java`

**Interfaces:**

- Produces: `TargetPriority.fromConfig(String)`, `TargetCandidate<T>`, `TargetScorer.selectBest(List<TargetCandidate<T>>, TargetPriority)`, and `TargetScorer.prefers(TargetCandidate<T>, TargetCandidate<T>, TargetPriority)`.
- Threat ordering is lexicographic and weight-free: targeting the player, hostile marker, attack damage, current health, distance, view angle, then entity ID.

- [ ] **Step 1: Write failing scoring-policy tests**

Create seven tests in `TargetScorerTest` using these literal fixtures:

```java
@Test public void emptyCandidateListHasNoWinner() { assertNull(TargetScorer.selectBest(Collections.emptyList(), TargetPriority.NEAREST)); }

@Test public void nearestUsesDistanceThenAngleThenEntityId() {
    assertEquals("close", select(TargetPriority.NEAREST, candidate("wide", 3, 4, 30, 10, false, false, 1), candidate("close", 9, 3, 40, 10, false, false, 1)));
    assertEquals("center", select(TargetPriority.NEAREST, candidate("wide", 3, 4, 30, 10, false, false, 1), candidate("center", 9, 4, 10, 10, false, false, 1)));
}

@Test public void healthUsesCurrentHealthThenDistanceThenEntityId() {
    assertEquals("hurt", select(TargetPriority.HEALTH, candidate("healthy", 1, 1, 1, 20, false, false, 1), candidate("hurt", 2, 9, 9, 4, false, false, 1)));
}

@Test public void angleUsesThreeDimensionalAngleThenDistanceThenEntityId() {
    assertEquals("center", select(TargetPriority.ANGLE, candidate("near", 1, 1, 20, 10, false, false, 1), candidate("center", 2, 9, 2, 10, false, false, 1)));
}

@Test public void threatPrioritizesEntitiesActivelyTargetingThePlayer() {
    assertEquals("aggro", select(TargetPriority.THREAT, candidate("boss", 1, 1, 1, 100, true, false, 20), candidate("aggro", 2, 16, 5, 5, false, true, 1)));
}

@Test public void threatUsesHostilityDamageHealthAndDistanceWithoutClassNames() {
    assertEquals("hostile", select(TargetPriority.THREAT, candidate("neutral", 1, 1, 1, 100, false, false, 20), candidate("hostile", 2, 20, 20, 1, true, false, 1)));
    assertEquals("damage", select(TargetPriority.THREAT, candidate("health", 1, 1, 1, 100, true, false, 2), candidate("damage", 2, 20, 20, 1, true, false, 8)));
}

@Test public void allPolicyTiesUseLowerEntityId() {
    assertEquals("lower", select(TargetPriority.NEAREST, candidate("higher", 9, 4, 10, 10, false, false, 1), candidate("lower", 2, 4, 10, 10, false, false, 1)));
}

private static TargetCandidate<String> candidate(
        String reference, int entityId, double distanceSquared, double angle,
        float health, boolean hostile, boolean targetingPlayer, double attackDamage) {
    TargetObservation<String> observation = new TargetObservation<>(
        reference, entityId, reference, health, 100.0F, true, true, true,
        Math.sqrt(distanceSquared), 0.0D, true, TargetAnchor.HEAD,
        new TargetPoint(0.0D, 1.0D, 0.0D), 1.0D, 2.0D
    );
    return new TargetCandidate<>(
        observation, distanceSquared, angle, hostile, targetingPlayer, attackDamage
    );
}

@SafeVarargs
private static String select(TargetPriority priority, TargetCandidate<String>... candidates) {
    return TargetScorer.selectBest(Arrays.asList(candidates), priority)
        .getObservation()
        .getReference();
}
```

The fixture constructs a visible `TargetObservation<String>` and a `TargetCandidate<String>` whose constructor is:

```java
TargetCandidate(TargetObservation<T> observation,
                double distanceSquared,
                double viewAngleDegrees,
                boolean hostile,
                boolean targetingPlayer,
                double attackDamage)
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.targeting.core.TargetScorerTest" --no-daemon
```

Expected: compilation fails on missing `TargetPriority`, `TargetCandidate`, and `TargetScorer`.

- [ ] **Step 3: Implement priority parsing and immutable candidates**

`TargetPriority` values are `NEAREST`, `HEALTH`, `THREAT`, and `ANGLE`. `fromConfig` is null-safe, locale-independent, accepts `health`, `threat`, and `angle`, and defaults every other value to `NEAREST`.

`TargetCandidate<T>` stores the six constructor values, rejects a null observation with `IllegalArgumentException`, and exposes getters plus `getEntityId()` delegating to its observation.

- [ ] **Step 4: Implement the one-pass scorer**

`selectBest` returns null for null/empty input and otherwise scans without sorting or allocating. `prefers(candidate,current,priority)` follows:

```java
NEAREST: distanceSquared asc, viewAngleDegrees asc, entityId asc
HEALTH:  observation.health asc, distanceSquared asc, entityId asc
ANGLE:   viewAngleDegrees asc, distanceSquared asc, entityId asc
THREAT:  targetingPlayer true first,
         hostile true first,
         attackDamage desc,
         observation.health desc,
         distanceSquared asc,
         viewAngleDegrees asc,
         entityId asc
```

Use `Boolean.compare`, `Double.compare`, `Float.compare`, and a final `Integer.compare`; never subtract floating-point scores.

- [ ] **Step 5: Run scoring tests and clean build**

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.targeting.core.*" build --no-daemon
```

Expected: thirteen Stage 2 core tests pass and Forge reobfuscation succeeds.

- [ ] **Step 6: Commit deterministic scoring**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetPriority.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCandidate.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetScorer.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetScorerTest.java
git commit -m "feat: add deterministic target scoring"
```

---

### Task 3: Stable Cycling and Fixed-Capacity History

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRules.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetHistory.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRulesTest.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetHistoryTest.java`

**Interfaces:**

- Produces: in-place `sortByBearing`, adjacent selection, cooldown boundary, automatic replacement with one avoided ID, and an allocation-free fixed integer history.

- [ ] **Step 1: Write failing cycle tests**

Cover four behaviors with literal candidates:

```java
@Test
public void bearingOrderUsesEntityIdForEqualBearings() {
    List<TargetCandidate<String>> candidates = new ArrayList<>(Arrays.asList(
        candidate("west", 4, 270.0D),
        candidate("east-high", 9, 90.0D),
        candidate("north", 1, 0.0D),
        candidate("east-low", 3, 90.0D)
    ));
    TargetCycleRules.sortByBearing(candidates);
    assertEquals("north", candidates.get(0).getObservation().getReference());
    assertEquals("east-low", candidates.get(1).getObservation().getReference());
    assertEquals("east-high", candidates.get(2).getObservation().getReference());
    assertEquals("west", candidates.get(3).getObservation().getReference());
}

@Test
public void adjacentSelectionWrapsBothDirections() {
    List<TargetCandidate<String>> candidates = Arrays.asList(
        candidate("north", 1, 0.0D), candidate("east", 2, 90.0D), candidate("west", 3, 270.0D));
    assertEquals("north", TargetCycleRules.selectAdjacent(candidates, 3, true).getObservation().getReference());
    assertEquals("west", TargetCycleRules.selectAdjacent(candidates, 1, false).getObservation().getReference());
}

@Test
public void missingCurrentStartsAtDirectionalEdge() {
    List<TargetCandidate<String>> candidates = Arrays.asList(
        candidate("north", 1, 0.0D), candidate("east", 2, 90.0D), candidate("west", 3, 270.0D));
    assertEquals("north", TargetCycleRules.selectAdjacent(candidates, 99, true).getObservation().getReference());
    assertEquals("west", TargetCycleRules.selectAdjacent(candidates, 99, false).getObservation().getReference());
}

@Test public void cooldownAllowsTheExactBoundary() {
    assertFalse(TargetCycleRules.isCooldownElapsed(1000L, 1249L, 250L));
    assertTrue(TargetCycleRules.isCooldownElapsed(1000L, 1250L, 250L));
}

private static TargetCandidate<String> candidate(String reference, int entityId, double bearing) {
    TargetObservation<String> observation = new TargetObservation<>(
        reference, entityId, reference, 10.0F, 10.0F, true, true, true,
        4.0D, bearing, true, TargetAnchor.HEAD,
        new TargetPoint(0.0D, 1.0D, 0.0D), 1.0D, 2.0D
    );
    return new TargetCandidate<>(observation, 16.0D, 10.0D, false, false, 1.0D);
}
```

The adjacent API is:

```java
public static <T> TargetCandidate<T> selectAdjacent(
    List<TargetCandidate<T>> sortedCandidates,
    int currentEntityId,
    boolean forward)
```

- [ ] **Step 2: Write failing history tests**

```java
@Test
public void newestUniqueTargetIsStoredFirst() {
    TargetHistory history = new TargetHistory(3);
    history.record(4); history.record(7); history.record(4);
    assertEquals(2, history.size());
    assertEquals(4, history.get(0));
    assertEquals(7, history.get(1));
}

@Test
public void capacityDropsTheOldestTarget() {
    TargetHistory history = new TargetHistory(3);
    history.record(1); history.record(2); history.record(3); history.record(4);
    assertEquals(4, history.get(0));
    assertEquals(3, history.get(1));
    assertEquals(2, history.get(2));
    assertFalse(history.contains(1));
}

@Test
public void clearRemovesAllHistory() {
    TargetHistory history = new TargetHistory(3);
    history.record(4);
    history.clear();
    assertEquals(0, history.size());
    assertEquals(-1, history.mostRecent());
    assertFalse(history.contains(4));
}
```

- [ ] **Step 3: Verify RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.targeting.core.TargetCycleRulesTest" --tests "com.zeldatargeting.mod.client.targeting.core.TargetHistoryTest" --no-daemon
```

- [ ] **Step 4: Implement cycle rules**

`sortByBearing` uses `Collections.sort` on the provided list and compares `observation.horizontalBearingDegrees`, then entity ID. `selectAdjacent` assumes the caller has sorted the list and uses `TargetingMath.adjacentIndex`. `isCooldownElapsed` treats `lastCycleMillis == Long.MIN_VALUE` as immediately eligible.

Add:

```java
public static <T> TargetCandidate<T> selectAutomaticReplacement(
        List<TargetCandidate<T>> candidates,
        TargetPriority priority,
        int avoidedEntityId)
```

It selects the best non-avoided candidate when one exists, otherwise falls back to the avoided candidate. It performs two one-pass scans at most and does not allocate a filtered list.

- [ ] **Step 5: Implement fixed-capacity history**

`TargetHistory` owns a final `int[]`, validates capacity greater than zero, keeps newest IDs at index zero, moves an existing ID to the front without duplication, exposes `size()`, `get(int)`, `mostRecent()`, `contains(int)`, and `clear()`, and never exposes its backing array.

- [ ] **Step 6: Run all core tests and build**

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.targeting.core.*" build --no-daemon
```

Expected: twenty Stage 2 core tests pass.

- [ ] **Step 7: Commit cycle/history behavior**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRules.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetHistory.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetCycleRulesTest.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetHistoryTest.java
git commit -m "feat: stabilize target cycling and history"
```

---

### Task 4: Single Lock-Session State Machine

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/session/LockPhase.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/session/LockOnSnapshot.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/session/LockOnSession.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/session/LockReleaseReason.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java`

**Interfaces:**

- Produces: the only mutable lock owner, immutable snapshots, explicit phases, transition IDs, occlusion grace, detached release presentation, and immediate lifecycle clearing.

- [ ] **Step 1: Write eight failing state-machine tests**

Use `TargetObservation<String>` fixtures and literal times to cover:

1. `IDLE -> ACQUIRING -> LOCKED` with strictly increasing transition IDs and the acquisition priority retained in every snapshot.
2. Invalid completion/switch/release calls return false and leave the snapshot unchanged.
3. `LOCKED -> SWITCHING -> LOCKED` replaces the target exactly once.
4. Invisible observation enters `OCCLUDED_GRACE` and records zero initial duration.
5. Visibility recovery before 750 ms returns to `LOCKED` without an acquisition transition.
6. Continued occlusion at 750 ms enters `RELEASING` with reason `OCCLUDED`, a null reference, and retained entity ID/name/health.
7. Normal `MANUAL` release remains presentational for 199 ms and becomes `IDLE` at 200 ms.
8. changing the anchor updates the observation and transition ID without leaving `LOCKED`.
9. `clear(PLAYER_DEAD, now)` immediately returns `IDLE` with no target observation.

Use these public methods:

```java
boolean beginAcquire(TargetObservation<T> target, TargetPriority priority, long nowMillis)
boolean completeAcquire(long nowMillis)
boolean beginSwitch(TargetObservation<T> target, TargetPriority priority, long nowMillis)
boolean completeSwitch(long nowMillis)
boolean updateObservation(TargetObservation<T> target, long nowMillis, long occlusionGraceMillis)
boolean changeAnchor(TargetObservation<T> target, long nowMillis)
boolean beginRelease(LockReleaseReason reason, long nowMillis)
boolean advanceRelease(long nowMillis, long releaseFadeMillis)
void clear(LockReleaseReason reason, long nowMillis)
LockOnSnapshot<T> snapshot()
```

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.session.LockOnSessionTest" --no-daemon
```

- [ ] **Step 3: Add phases and complete release reasons**

`LockPhase` values:

```java
IDLE, ACQUIRING, LOCKED, OCCLUDED_GRACE, SWITCHING, RELEASING
```

Extend `LockReleaseReason` without removing Stage 1 values:

```java
NONE, MANUAL, WORLD_UNAVAILABLE, PLAYER_UNAVAILABLE, PLAYER_DEAD,
TARGET_UNAVAILABLE, TARGET_DEAD, TARGET_REMOVED, OUT_OF_RANGE,
OCCLUDED, DIMENSION_CHANGED, DISCONNECTED, QUICK_SWITCH_FAILED
```

- [ ] **Step 4: Implement immutable `LockOnSnapshot<T>`**

Constructor fields and getters:

```java
LockPhase phase;
long transitionId;
long transitionStartedAtMillis;
TargetObservation<T> target;
long occlusionDurationMillis;
LockReleaseReason releaseReason;
TargetPriority priority;
```

`isTracking()` is true for `ACQUIRING`, `LOCKED`, `OCCLUDED_GRACE`, and `SWITCHING`. `isPresentationVisible()` is true when `target != null`, including `RELEASING`.

- [ ] **Step 5: Implement `LockOnSession<T>` transition guards**

The session stores the same seven values mutably plus `occlusionStartedAtMillis`. Every phase change increments `transitionId` once and updates `transitionStartedAtMillis`.

Rules:

- acquisition begins only from `IDLE` with a non-null referenced target;
- acquisition and switching normalize a null priority to `NEAREST` and publish that priority in every later snapshot until the next acquisition/switch;
- switching begins only from a tracking phase and clears occlusion timing;
- target IDs must match during observation updates;
- anchor changes require the same target ID, a different anchor, and a tracking phase; they increment the transition ID without changing the phase;
- first invisible locked observation enters grace;
- visible grace observation returns to locked;
- grace expiration calls normal release with `OCCLUDED`;
- normal release replaces the target with `withoutReference()`;
- fade completion and `clear` remove the target entirely;
- release reason is `NONE` for acquisition/switch/lock phases and retained for release/idle cleanup.

- [ ] **Step 6: Run session tests and clean build**

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.session.*" build --no-daemon
```

Expected: existing lifecycle tests and nine new session tests pass.

- [ ] **Step 7: Commit the state model**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/session src/test/java/com/zeldatargeting/mod/client/session/LockOnSessionTest.java
git commit -m "feat: add lock session state machine"
```

---

### Task 5: Targeting Service Coordination

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetProvider.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java`

**Interfaces:**

- Produces: one coordinator for acquisition, direct locked-target observation, cooldown cycling, quick switch, release, and immediate lifecycle clear.

- [ ] **Step 1: Write the failing service tests with a real in-memory provider**

Define this boundary:

```java
public interface TargetProvider<T> {
    void collectCandidates(List<TargetCandidate<T>> output, boolean acquisitionCone);
    TargetObservation<T> observe(T reference, TargetAnchor preferredAnchor);
}
```

`TargetingServiceTest` uses a private `FakeProvider` that clears/fills the supplied list, counts scans, and returns observations from a `Map<String, TargetObservation<String>>`. Its `observe` implementation copies the stored observation with the requested anchor and a matching literal focus point. Do not mock it.

Write eleven tests:

1. acquisition performs one cone scan, chooses the configured policy, and begins `ACQUIRING`;
2. the next tick completes acquisition and directly observes only the winner;
3. no candidates leave the session idle;
4. cycling performs one non-cone scan, wraps by bearing, and enforces the exact 250 ms boundary;
5. manual cycling remains adjacent even when history contains another target;
6. quick switch on death avoids the dropped target when another candidate exists;
7. death without quick switch enters normal `TARGET_DEAD` release;
8. out-of-range observation enters `OUT_OF_RANGE` release;
9. dimension mismatch clears immediately with `DIMENSION_CHANGED`;
10. occlusion enters grace, recovers before 750 ms, and expires at 750 ms.
11. core anchor cycling wraps, asks the provider for the new preferred anchor, and remains in the current tracking phase.

- [ ] **Step 2: Verify RED**

```powershell
.\gradlew.bat test --tests "com.zeldatargeting.mod.client.targeting.core.TargetingServiceTest" --no-daemon
```

- [ ] **Step 3: Implement immutable `TargetingOptions`**

Constructor and getters:

```java
TargetingOptions(TargetPriority priority,
                 double maxTrackingDistance,
                 long occlusionGraceMillis,
                 long releaseFadeMillis,
                 long cycleCooldownMillis,
                 boolean quickSwitchEnabled)
```

Validate non-negative distances/times and normalize null priority to `NEAREST`.

- [ ] **Step 4: Implement `TargetingService<T>`**

Fields:

```java
TargetProvider<T> provider;
LockOnSession<T> session;
TargetHistory history;
ArrayList<TargetCandidate<T>> candidateBuffer = new ArrayList<>(16);
long lastCycleMillis = Long.MIN_VALUE;
```

Public methods:

```java
TargetingService(TargetProvider<T> provider,
                 LockOnSession<T> session,
                 TargetHistory history)
LockOnSnapshot<T> snapshot()
LockOnSnapshot<T> acquire(long nowMillis, TargetingOptions options)
LockOnSnapshot<T> cycle(boolean forward, long nowMillis, TargetingOptions options)
LockOnSnapshot<T> cycleAnchor(boolean forward, long nowMillis)
LockOnSnapshot<T> tick(long nowMillis, TargetingOptions options)
LockOnSnapshot<T> release(LockReleaseReason reason, long nowMillis)
LockOnSnapshot<T> clear(LockReleaseReason reason, long nowMillis)
TargetHistory getHistory()
```

Implementation rules:

- `acquire` scans only from idle, calls `TargetScorer.selectBest`, and passes `options.getPriority()` to `session.beginAcquire`;
- `cycle` checks cooldown before scanning, sorts the reused buffer, records the old ID, passes `options.getPriority()` to `session.beginSwitch`, and never consults history for adjacency;
- `cycleAnchor` does not scan candidates; it cycles the snapshot anchor, directly observes the current reference with that preferred anchor, and calls `session.changeAnchor`;
- `tick` first completes `ACQUIRING`/`SWITCHING`, advances release fade, then observes one tracked reference;
- null/not-present observation releases as `TARGET_REMOVED`;
- dimension mismatch clears immediately;
- death attempts automatic replacement only when quick switch is enabled;
- automatic replacement records and avoids the dropped ID while another candidate exists;
- distance compares the observation's linear distance to `maxTrackingDistance`;
- visibility delegates to session grace logic;
- lifecycle `clear` also clears history and resets the cooldown sentinel;
- every provider scan clears `candidateBuffer` before filling it.

- [ ] **Step 5: Run service/core/session tests and build**

```powershell
.\gradlew.bat clean test --tests "com.zeldatargeting.mod.client.targeting.core.*" --tests "com.zeldatargeting.mod.client.session.*" build --no-daemon
```

Expected: all pure Stage 2 tests pass and no Minecraft client is required.

- [ ] **Step 6: Commit the service**

```powershell
git add src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetProvider.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingOptions.java src/main/java/com/zeldatargeting/mod/client/targeting/core/TargetingService.java src/test/java/com/zeldatargeting/mod/client/targeting/core/TargetingServiceTest.java
git commit -m "feat: coordinate targeting through one service"
```

---

### Task 6: Minecraft Adapter and Manager Integration

**Files:**

- Rewrite: `src/main/java/com/zeldatargeting/mod/client/targeting/EntityDetector.java`
- Rewrite: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/math/LegacySelectionRules.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/targeting/TargetSelector.java`
- Delete: `src/main/java/com/zeldatargeting/mod/client/targeting/TargetTracker.java`
- Delete: `src/test/java/com/zeldatargeting/mod/client/math/LegacySelectionRulesTest.java`

**Interfaces:**

- `EntityDetector implements TargetProvider<EntityLivingBase>`.
- `TargetingManager` owns `TargetingService<EntityLivingBase>` but no `isActive`, `currentTarget`, selector, or tracker state.

- [ ] **Step 1: Add a compile-time integration expectation before the rewrite**

Add this assertion to a new test method in `TargetingServiceTest`:

```java
@Test
public void legacyPriorityNamesMapToTheNewPolicies() {
    assertEquals(TargetPriority.NEAREST, TargetPriority.fromConfig(null));
    assertEquals(TargetPriority.NEAREST, TargetPriority.fromConfig("nearest"));
    assertEquals(TargetPriority.HEALTH, TargetPriority.fromConfig("HEALTH"));
    assertEquals(TargetPriority.THREAT, TargetPriority.fromConfig("threat"));
    assertEquals(TargetPriority.ANGLE, TargetPriority.fromConfig("angle"));
}
```

Temporarily change the expected `"nearest"` mapping to `ANGLE`, run the focused test, observe the failure, then restore `NEAREST` and rerun. This proves the legacy config adapter contract before manager integration.

- [ ] **Step 2: Rewrite `EntityDetector` as the provider adapter**

Implement:

```java
public final class EntityDetector implements TargetProvider<EntityLivingBase> {
    @Override
    public void collectCandidates(List<TargetCandidate<EntityLivingBase>> output,
                                  boolean acquisitionCone) {
        output.clear();
        EntityPlayer player = mc.player;
        if (player == null || mc.world == null) {
            return;
        }
        double range = TargetingConfig.getTargetingRange();
        AxisAlignedBB box = new AxisAlignedBB(
            player.posX - range, player.posY - range, player.posZ - range,
            player.posX + range, player.posY + range, player.posZ + range);
        for (EntityLivingBase entity : mc.world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            TargetCandidate<EntityLivingBase> candidate = buildCandidate(player, entity, acquisitionCone);
            if (candidate != null) {
                output.add(candidate);
            }
        }
    }

    @Override
    public TargetObservation<EntityLivingBase> observe(
            EntityLivingBase target,
            TargetAnchor preferredAnchor) {
        EntityPlayer player = mc.player;
        if (target == null || player == null || mc.world == null) {
            return null;
        }
        return buildObservation(player, target, preferredAnchor);
    }
}
```

`buildCandidate` returns null for every rejected condition listed below; otherwise it calls `buildObservation(player, entity, TargetAnchor.HEAD)`, calculates the three-dimensional view angle, hostile/aggro/attack-damage values, and returns the exact `TargetCandidate` defined in Task 2. `buildObservation` tests the preferred anchor first, then each remaining anchor in Head/Center/Lower order, and fills every observation field from the current player/entity state. These helpers are private and do not retain an entity list.

Candidate collection requirements:

- query `EntityLivingBase.class`, not `EntityLiving.class`;
- reject the local player, dead entities, spectator players, disabled type categories, out-of-range entities, and acquisition-cone failures;
- keep behind-camera entities eligible for cycling by skipping only the cone check when `acquisitionCone` is false;
- test Head, Center, and Lower Body points in that order and accept the first ray not blocked when line of sight is required;
- create one `TargetObservation<EntityLivingBase>` and one `TargetCandidate<EntityLivingBase>` per accepted scan result;
- hostile is `entity instanceof IMob`;
- targeting-player is `entity instanceof EntityLiving && ((EntityLiving) entity).getAttackTarget() == player`;
- attack damage comes from `SharedMonsterAttributes.ATTACK_DAMAGE`, using zero if the attribute is absent;
- width/height come from the entity bounding box;
- `observe` never scans a candidate list and returns present/alive/dimension/distance/visibility data for only the supplied target.

Use `TargetingMath.angleDegrees` and `horizontalBearing`; do not reintroduce class-name threat rules.

- [ ] **Step 3: Replace manager lock ownership with the service**

Fields become:

```java
private final CameraController cameraController;
private final TargetingService<EntityLivingBase> targetingService;
private int previousPerspective;
```

Construct the service with `new EntityDetector()`, `new LockOnSession<EntityLivingBase>()`, and `new TargetHistory(3)`.

Build current legacy options in one method:

```java
private TargetingOptions currentOptions() {
    return new TargetingOptions(
        TargetPriority.fromConfig(TargetingConfig.targetPriority),
        TargetingConfig.getMaxTrackingDistance(),
        750L,
        200L,
        250L,
        false
    );
}
```

Input and tick flow:

- toggle checks `snapshot().isTracking()`;
- acquire/cycle/release call the service and pass before/after snapshots to one transition handler;
- lifecycle guard uses the snapshot reference and calls `targetingService.clear(reason, now)`;
- normal END ticks call `targetingService.tick(now, currentOptions())`;
- camera tracks only when the resulting snapshot is tracking and has a non-null reference;
- `getCurrentTarget()` derives from the snapshot observation;
- `isActive()` derives from `snapshot.isTracking()`;
- acquisition transition plays lock sound, initializes camera target, and records/switches perspective;
- switching transition plays one switch sound and updates the camera target;
- releasing transition plays one lost sound and restores perspective/camera;
- lifecycle clear restores immediately without sound;
- `LOCKED` and grace recovery transitions do not replay feedback.

Use `System.currentTimeMillis()` only at the manager boundary; pure core methods receive the timestamp.

- [ ] **Step 4: Delete superseded legacy selector/tracker code**

Delete the four files listed above only after `EntityDetector` and `TargetingManager` no longer import them. Confirm with:

```powershell
rg -n "LegacySelectionRules|TargetSelector|TargetTracker" src
```

Expected: no matches.

- [ ] **Step 5: Run the complete automated suite and clean Forge build**

```powershell
.\gradlew.bat clean test build --no-daemon
git diff --check
```

Expected: 59 tests pass, the production source compiles against Forge 1.12.2, and `reobfJar` succeeds.

- [ ] **Step 6: Perform focused in-game targeting smoke tests**

```powershell
.\gradlew.bat runClient --no-daemon
```

In one creative local world:

1. Lock the nearest visible living target and confirm one lock sound/marker.
2. Place two or more living targets around the player and confirm Q/E wrap in opposite stable bearing directions.
3. Tap Q/E faster than 250 ms and confirm only the eligible cycle is applied.
4. Put the locked target briefly behind a wall and return within 750 ms; confirm the lock recovers without a second lock sound.
5. Keep it occluded beyond 750 ms; confirm release occurs once.
6. Kill a locked target; with legacy options quick switch remains disabled and release reason is `TARGET_DEAD`.
7. Exit to title while locked and confirm immediate clear with no stale HUD/camera.

- [ ] **Step 7: Commit the Minecraft integration**

```powershell
git add -A src/main/java/com/zeldatargeting/mod/client src/test/java/com/zeldatargeting/mod/client
git commit -m "refactor: route lock-on through targeting service"
```

---

### Task 7: Stage 2 Verification Record

**Files:**

- Create: `docs/development/1.4-stage-2-targeting-core.md`

- [ ] **Step 1: Record the delivered boundaries and behavior**

Create the document with:

```markdown
# Zelda Targeting 1.4 Stage 2 Targeting Core

## Delivered Boundaries

- `LockOnSession<T>` is the only mutable lock-state owner.
- `LockOnSnapshot<T>` publishes immutable target and transition state.
- `TargetScorer` implements deterministic Nearest, Health, Threat, and Angle policies.
- `TargetCycleRules` preserves adjacent manual bearing order and isolates automatic history avoidance.
- `EntityDetector` is the Minecraft-facing `EntityLivingBase` candidate/observation adapter.
- `TargetingService` owns reusable scan storage and coordinates acquisition, validation, cycling, grace, quick switch, release, and lifecycle clear.

## Compatibility Baseline

- Minecraft 1.12.2
- Forge 14.23.5.2859
- Java 8
- Client-only operation with unmodified multiplayer servers
- Public mod version and configuration schema remain 1.3.0 until later stages

## Verification

```powershell
.\gradlew.bat clean test build --no-daemon
git diff --check
git status --short --branch
```

- All 59 tests pass.
- Forge `reobfJar` succeeds.
- Manual acquisition, cycling, cooldown, occlusion recovery/expiry, death release, and world-exit cleanup pass.
```

- [ ] **Step 2: Run final Stage 2 verification**

```powershell
.\gradlew.bat clean test build --no-daemon
git diff --check
git status --short --branch
```

Also extract `mcmod.info` from `build/libs/zelda-targeting-1.3.0.jar` and assert the packaged version remains `1.3.0` exactly as in Stage 1.

- [ ] **Step 3: Commit the verification record**

```powershell
git add docs/development/1.4-stage-2-targeting-core.md
git commit -m "docs: record 1.4 stage two targeting core"
```

- [ ] **Step 4: Confirm the clean Stage 2 series**

```powershell
git log -8 --oneline
git status --short --branch
```

Expected: eight Stage 2 commits including this plan commit, and no tracked modifications.

---

## Stage 2 Completion Gate

Stage 2 is complete only when:

- `LockOnSession<T>` is the sole owner of phase, target, transition timing, occlusion timing, and release reason;
- consumers receive immutable snapshots;
- snapshots retain the active targeting priority; camera profile is added with the camera subsystem in Stage 3;
- all four policies and final entity-ID ties are deterministic;
- class-name threat scoring is deleted;
- manual cycle order is stable and history-independent;
- automatic replacement avoids the most recently dropped target when possible;
- candidate eligibility uses `EntityLivingBase`, includes enabled players/modded living entities, and excludes spectators;
- Head, Center, and Lower Body anchors are resolved generically and line-of-sight falls through in that order;
- locked ticks observe one entity without a candidate rescan;
- occlusion recovery and 750 ms expiration are tested;
- normal release retains detached values for 200 ms while lifecycle clear removes references immediately;
- quick switch is implemented/tested but disabled by the legacy config adapter;
- the focused in-game smoke matrix passes;
- all 59 tests, clean build, reobfuscation, metadata verification, and diff checks pass; and
- the Stage 2 branch is clean.
