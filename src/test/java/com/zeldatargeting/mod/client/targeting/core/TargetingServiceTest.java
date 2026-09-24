package com.zeldatargeting.mod.client.targeting.core;

import com.zeldatargeting.mod.client.session.LockOnSession;
import com.zeldatargeting.mod.client.session.LockOnSnapshot;
import com.zeldatargeting.mod.client.session.LockPhase;
import com.zeldatargeting.mod.client.session.LockReleaseReason;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TargetingServiceTest {
    @Test
    public void legacyPriorityNamesMapToTheNewPolicies() {
        assertEquals(TargetPriority.NEAREST, TargetPriority.fromConfig(null));
        assertEquals(TargetPriority.NEAREST, TargetPriority.fromConfig("nearest"));
        assertEquals(TargetPriority.HEALTH, TargetPriority.fromConfig("HEALTH"));
        assertEquals(TargetPriority.THREAT, TargetPriority.fromConfig("threat"));
        assertEquals(TargetPriority.ANGLE, TargetPriority.fromConfig("angle"));
    }

    @Test
    public void acquisitionScansTheConeOnceAndUsesConfiguredPriority() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> near = observation("near", 1, 4.0F, true, true, true, 2.0D, 20.0D, TargetAnchor.HEAD);
        TargetObservation<String> hurt = observation("hurt", 2, 1.0F, true, true, true, 6.0D, 10.0D, TargetAnchor.HEAD);
        provider.setCandidates(candidate(near), candidate(hurt));
        TargetingService<String> service = idleService(provider, new TargetHistory(4));

        LockOnSnapshot<String> snapshot = service.acquire(100L, options(TargetPriority.HEALTH, 20.0D, false));

        assertEquals(1, provider.scanCount);
        assertTrue(provider.lastAcquisitionCone);
        assertEquals(LockPhase.ACQUIRING, snapshot.getPhase());
        assertEquals("hurt", snapshot.getTarget().getReference());
        assertEquals(TargetPriority.HEALTH, snapshot.getPriority());
    }

    @Test
    public void acquisitionCapturesTheNormalizedCameraProfile() {
        FakeProvider provider = new FakeProvider();
        provider.setCandidates(candidate(observation(
            "pig", 1, 8.0F, true, true, true,
            4.0D, 20.0D, TargetAnchor.HEAD
        )));
        TargetingService<String> service = idleService(provider, new TargetHistory(4));
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
    }

    @Test
    public void tickCompletesAcquisitionAndDirectlyObservesOnlyTheWinner() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> near = observation("near", 1, 10.0F, true, true, true, 2.0D, 20.0D, TargetAnchor.HEAD);
        TargetObservation<String> far = observation("far", 2, 10.0F, true, true, true, 6.0D, 10.0D, TargetAnchor.HEAD);
        provider.setCandidates(candidate(near), candidate(far));
        provider.putObservation(near);
        provider.putObservation(far);
        TargetingService<String> service = idleService(provider, new TargetHistory(4));
        TargetingOptions options = options(TargetPriority.NEAREST, 20.0D, false);
        service.acquire(100L, options);

        LockOnSnapshot<String> snapshot = service.tick(120L, options);

        assertEquals(LockPhase.LOCKED, snapshot.getPhase());
        assertEquals("near", snapshot.getTarget().getReference());
        assertEquals(1, provider.scanCount);
        assertEquals(1, provider.observeCount);
        assertEquals("near", provider.lastObservedReference);
    }

    @Test
    public void acquisitionWithoutCandidatesLeavesSessionIdle() {
        FakeProvider provider = new FakeProvider();
        TargetingService<String> service = idleService(provider, new TargetHistory(4));

        LockOnSnapshot<String> snapshot = service.acquire(100L, options(TargetPriority.NEAREST, 20.0D, false));

        assertEquals(1, provider.scanCount);
        assertEquals(LockPhase.IDLE, snapshot.getPhase());
        assertNull(snapshot.getTarget());
    }

    @Test
    public void cyclingScansOutsideTheConeWrapsAndHonorsCooldownBoundary() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> north = observation("north", 1, 10.0F, true, true, true, 4.0D, 0.0D, TargetAnchor.HEAD);
        TargetObservation<String> east = observation("east", 2, 10.0F, true, true, true, 4.0D, 90.0D, TargetAnchor.HEAD);
        TargetObservation<String> west = observation("west", 3, 10.0F, true, true, true, 4.0D, 270.0D, TargetAnchor.HEAD);
        provider.setCandidates(candidate(west), candidate(east), candidate(north));
        TargetingService<String> service = lockedService(provider, new TargetHistory(4), west);
        TargetingOptions options = options(TargetPriority.NEAREST, 20.0D, false);

        assertEquals("north", service.cycle(true, 1000L, options).getTarget().getReference());
        assertEquals(1, provider.scanCount);
        assertFalse(provider.lastAcquisitionCone);
        long transitionAtFirstCycle = service.snapshot().getTransitionId();

        assertEquals("north", service.cycle(true, 1249L, options).getTarget().getReference());
        assertEquals(transitionAtFirstCycle, service.snapshot().getTransitionId());
        assertEquals(1, provider.scanCount);

        assertEquals("east", service.cycle(true, 1250L, options).getTarget().getReference());
        assertEquals(2, provider.scanCount);
    }

    @Test
    public void manualCycleIgnoresHistoryAndSelectsAdjacentBearing() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> north = observation("north", 1, 10.0F, true, true, true, 4.0D, 0.0D, TargetAnchor.HEAD);
        TargetObservation<String> east = observation("east", 2, 10.0F, true, true, true, 4.0D, 90.0D, TargetAnchor.HEAD);
        TargetObservation<String> west = observation("west", 3, 10.0F, true, true, true, 4.0D, 270.0D, TargetAnchor.HEAD);
        provider.setCandidates(candidate(north), candidate(east), candidate(west));
        TargetHistory history = new TargetHistory(4);
        history.record(3);
        TargetingService<String> service = lockedService(provider, history, north);

        LockOnSnapshot<String> snapshot = service.cycle(true, 1000L, options(TargetPriority.NEAREST, 20.0D, false));

        assertEquals("east", snapshot.getTarget().getReference());
        assertEquals(1, history.mostRecent());
        assertTrue(history.contains(3));
    }

    @Test
    public void quickSwitchOnDeathAvoidsTheDroppedTarget() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> pig = observation("pig", 1, 0.0F, false, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        TargetObservation<String> zombie = observation("zombie", 2, 12.0F, true, true, true, 5.0D, 10.0D, TargetAnchor.HEAD);
        provider.putObservation(pig);
        provider.putObservation(zombie);
        provider.setCandidates(candidate(pig), candidate(zombie));
        TargetHistory history = new TargetHistory(4);
        TargetingService<String> service = lockedService(provider, history,
            observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD));

        LockOnSnapshot<String> snapshot = service.tick(100L, options(TargetPriority.NEAREST, 20.0D, true));

        assertEquals(LockPhase.SWITCHING, snapshot.getPhase());
        assertEquals("zombie", snapshot.getTarget().getReference());
        assertEquals(1, history.mostRecent());
        assertEquals(1, provider.scanCount);
        assertFalse(provider.lastAcquisitionCone);
    }

    @Test
    public void deathWithoutQuickSwitchBeginsTargetDeadRelease() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> livePig = observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        provider.putObservation(observation("pig", 1, 0.0F, false, true, true, 4.0D, 20.0D, TargetAnchor.HEAD));
        TargetingService<String> service = lockedService(provider, new TargetHistory(4), livePig);

        LockOnSnapshot<String> snapshot = service.tick(100L, options(TargetPriority.NEAREST, 20.0D, false));

        assertEquals(LockPhase.RELEASING, snapshot.getPhase());
        assertEquals(LockReleaseReason.TARGET_DEAD, snapshot.getReleaseReason());
        assertNull(snapshot.getTarget().getReference());
        assertEquals(0, provider.scanCount);
    }

    @Test
    public void outOfRangeObservationBeginsOutOfRangeRelease() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> tracked = observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        provider.putObservation(observation("pig", 1, 8.0F, true, true, true, 11.0D, 20.0D, TargetAnchor.HEAD));
        TargetingService<String> service = lockedService(provider, new TargetHistory(4), tracked);

        LockOnSnapshot<String> snapshot = service.tick(100L, options(TargetPriority.NEAREST, 10.0D, false));

        assertEquals(LockPhase.RELEASING, snapshot.getPhase());
        assertEquals(LockReleaseReason.OUT_OF_RANGE, snapshot.getReleaseReason());
    }

    @Test
    public void dimensionMismatchClearsTheSessionImmediately() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> tracked = observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        provider.putObservation(observation("pig", 1, 8.0F, true, true, false, 4.0D, 20.0D, TargetAnchor.HEAD));
        TargetHistory history = new TargetHistory(4);
        history.record(9);
        TargetingService<String> service = lockedService(provider, history, tracked);

        LockOnSnapshot<String> snapshot = service.tick(100L, options(TargetPriority.NEAREST, 20.0D, false));

        assertEquals(LockPhase.IDLE, snapshot.getPhase());
        assertEquals(LockReleaseReason.DIMENSION_CHANGED, snapshot.getReleaseReason());
        assertNull(snapshot.getTarget());
        assertEquals(0, history.size());
    }

    @Test
    public void occlusionRecoversBeforeGraceAndReleasesAtExactExpiry() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> visible = observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        TargetObservation<String> hidden = hiddenObservation("pig", 1, 8.0F, 4.0D, 20.0D, TargetAnchor.HEAD);
        TargetingService<String> service = lockedService(provider, new TargetHistory(4), visible);
        TargetingOptions options = options(TargetPriority.NEAREST, 20.0D, false);

        provider.putObservation(hidden);
        assertEquals(LockPhase.OCCLUDED_GRACE, service.tick(100L, options).getPhase());

        provider.putObservation(visible);
        assertEquals(LockPhase.LOCKED, service.tick(849L, options).getPhase());

        provider.putObservation(hidden);
        assertEquals(LockPhase.OCCLUDED_GRACE, service.tick(1000L, options).getPhase());
        LockOnSnapshot<String> beforeExpiry = service.tick(1749L, options);
        assertEquals(LockPhase.OCCLUDED_GRACE, beforeExpiry.getPhase());
        assertEquals(749L, beforeExpiry.getOcclusionDurationMillis());

        LockOnSnapshot<String> expired = service.tick(1750L, options);
        assertEquals(LockPhase.RELEASING, expired.getPhase());
        assertEquals(LockReleaseReason.OCCLUDED, expired.getReleaseReason());
    }

    @Test
    public void anchorCyclingWrapsThroughProviderWithoutLeavingLocked() {
        FakeProvider provider = new FakeProvider();
        TargetObservation<String> pig = observation("pig", 1, 8.0F, true, true, true, 4.0D, 20.0D, TargetAnchor.HEAD);
        provider.putObservation(pig);
        TargetingService<String> service = lockedService(provider, new TargetHistory(4), pig);

        assertEquals(TargetAnchor.CENTER, service.cycleAnchor(true, 100L).getTarget().getAnchor());
        assertEquals(TargetAnchor.LOWER_BODY, service.cycleAnchor(true, 110L).getTarget().getAnchor());
        LockOnSnapshot<String> wrapped = service.cycleAnchor(true, 120L);

        assertEquals(TargetAnchor.HEAD, wrapped.getTarget().getAnchor());
        assertEquals(LockPhase.LOCKED, wrapped.getPhase());
        assertEquals(Arrays.asList(TargetAnchor.CENTER, TargetAnchor.LOWER_BODY, TargetAnchor.HEAD),
            provider.requestedAnchors);
        assertEquals(0, provider.scanCount);
    }

    @Test
    public void optionsRejectEveryNegativeLimit() {
        int rejected = 0;
        rejected += rejects(-1.0D, 750L, 200L, 250L) ? 1 : 0;
        rejected += rejects(20.0D, -1L, 200L, 250L) ? 1 : 0;
        rejected += rejects(20.0D, 750L, -1L, 250L) ? 1 : 0;
        rejected += rejects(20.0D, 750L, 200L, -1L) ? 1 : 0;
        assertEquals(4, rejected);
    }

    @Test
    public void optionsNormalizeMissingBlankAndUnknownProfilesToBalanced() {
        assertEquals("balanced", optionsWithProfile(null).getCameraProfileName());
        assertEquals("balanced", optionsWithProfile("   ").getCameraProfileName());
        assertEquals("balanced", optionsWithProfile("unknown").getCameraProfileName());
        assertEquals("snappy", optionsWithProfile("  SNAPPY  ").getCameraProfileName());
    }

    private static boolean rejects(double distance, long grace, long fade, long cooldown) {
        try {
            new TargetingOptions(
                TargetPriority.NEAREST,
                "balanced",
                distance,
                grace,
                fade,
                cooldown,
                false
            );
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    private static TargetingOptions options(TargetPriority priority, double distance, boolean quickSwitch) {
        return new TargetingOptions(
            priority,
            "balanced",
            distance,
            750L,
            200L,
            250L,
            quickSwitch
        );
    }

    private static TargetingOptions optionsWithProfile(String profile) {
        return new TargetingOptions(
            TargetPriority.NEAREST,
            profile,
            20.0D,
            750L,
            200L,
            250L,
            false
        );
    }

    private static TargetingService<String> idleService(FakeProvider provider, TargetHistory history) {
        return new TargetingService<>(provider, new LockOnSession<String>(), history);
    }

    private static TargetingService<String> lockedService(
            FakeProvider provider,
            TargetHistory history,
            TargetObservation<String> target) {
        LockOnSession<String> session = new LockOnSession<>();
        session.beginAcquire(target, TargetPriority.NEAREST, "balanced", 1L);
        session.completeAcquire(2L);
        return new TargetingService<>(provider, session, history);
    }

    private static TargetCandidate<String> candidate(TargetObservation<String> observation) {
        return new TargetCandidate<>(
            observation,
            observation.getDistance() * observation.getDistance(),
            observation.getHorizontalBearingDegrees(),
            false,
            false,
            1.0D
        );
    }

    private static TargetObservation<String> observation(
            String reference,
            int entityId,
            float health,
            boolean alive,
            boolean present,
            boolean sameDimension,
            double distance,
            double bearing,
            TargetAnchor anchor) {
        return new TargetObservation<>(
            reference, entityId, reference, health, 20.0F, alive, present, sameDimension,
            distance, bearing, sameDimension, anchor,
            new TargetPoint(1.0D, focusY(anchor), 3.0D), 0.9D, 1.8D
        );
    }

    private static TargetObservation<String> hiddenObservation(
            String reference,
            int entityId,
            float health,
            double distance,
            double bearing,
            TargetAnchor anchor) {
        return new TargetObservation<>(
            reference, entityId, reference, health, 20.0F, true, true, true,
            distance, bearing, false, anchor,
            new TargetPoint(1.0D, focusY(anchor), 3.0D), 0.9D, 1.8D
        );
    }

    private static double focusY(TargetAnchor anchor) {
        if (anchor == TargetAnchor.HEAD) {
            return 3.0D;
        }
        if (anchor == TargetAnchor.LOWER_BODY) {
            return 1.0D;
        }
        return 2.0D;
    }

    private static final class FakeProvider implements TargetProvider<String> {
        private final List<TargetCandidate<String>> candidates = new ArrayList<>();
        private final Map<String, TargetObservation<String>> observations = new HashMap<>();
        private final List<TargetAnchor> requestedAnchors = new ArrayList<>();
        private int scanCount;
        private int observeCount;
        private boolean lastAcquisitionCone;
        private String lastObservedReference;

        @SafeVarargs
        private final void setCandidates(TargetCandidate<String>... candidates) {
            this.candidates.clear();
            this.candidates.addAll(Arrays.asList(candidates));
        }

        private void putObservation(TargetObservation<String> observation) {
            observations.put(observation.getReference(), observation);
        }

        @Override
        public void collectCandidates(List<TargetCandidate<String>> output, boolean acquisitionCone) {
            scanCount++;
            lastAcquisitionCone = acquisitionCone;
            output.clear();
            output.addAll(candidates);
        }

        @Override
        public TargetObservation<String> observe(String reference, TargetAnchor preferredAnchor) {
            observeCount++;
            lastObservedReference = reference;
            requestedAnchors.add(preferredAnchor);
            TargetObservation<String> stored = observations.get(reference);
            if (stored == null) {
                return null;
            }
            return new TargetObservation<>(
                stored.getReference(), stored.getEntityId(), stored.getName(),
                stored.getHealth(), stored.getMaxHealth(), stored.isAlive(), stored.isPresent(),
                stored.isSameDimension(), stored.getDistance(), stored.getHorizontalBearingDegrees(),
                stored.isVisible(), preferredAnchor,
                new TargetPoint(1.0D, focusY(preferredAnchor), 3.0D),
                stored.getWidth(), stored.getHeight()
            );
        }
    }
}
