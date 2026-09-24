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
