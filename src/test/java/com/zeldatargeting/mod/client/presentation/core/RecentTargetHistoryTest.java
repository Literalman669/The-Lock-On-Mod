package com.zeldatargeting.mod.client.presentation.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class RecentTargetHistoryTest {
    @Test
    public void releasingALockRecordsTheLivingTarget() {
        RecentTargetHistory<String> history = new RecentTargetHistory<>(3);

        history.track("wolf");
        history.track(null);

        assertEquals(Collections.singletonList("wolf"), history.entries());
    }

    @Test
    public void reacquiredTargetIsRemovedFromTheHistoryMarkers() {
        RecentTargetHistory<String> history = new RecentTargetHistory<>(3);

        history.track("wolf");
        history.track("golem");
        history.track("wolf");

        assertEquals(Collections.singletonList("golem"), history.entries());
    }

    @Test
    public void retainsOnlyTheThreeMostRecentInactiveTargets() {
        RecentTargetHistory<String> history = new RecentTargetHistory<>(3);

        history.track("wolf");
        history.track("golem");
        history.track("endermite");
        history.track("pig");

        assertEquals(
            Arrays.asList("endermite", "golem", "wolf"),
            history.entries()
        );
    }

    @Test
    public void pruningExpiredTargetsFreesHistoryCapacity() {
        RecentTargetHistory<String> history = new RecentTargetHistory<>(3);

        history.track("wolf");
        history.track("golem");
        history.track("endermite");
        history.removeIf("golem"::equals);

        assertEquals(
            Collections.singletonList("wolf"),
            history.entries()
        );
    }
}
