package com.zeldatargeting.mod.client.targeting.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TargetHistoryTest {
    @Test
    public void newestUniqueTargetIsStoredFirst() {
        TargetHistory history = new TargetHistory(3);
        history.record(4);
        history.record(7);
        history.record(4);
        assertEquals(2, history.size());
        assertEquals(4, history.get(0));
        assertEquals(7, history.get(1));
    }

    @Test
    public void capacityDropsTheOldestTarget() {
        TargetHistory history = new TargetHistory(3);
        history.record(1);
        history.record(2);
        history.record(3);
        history.record(4);
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
}
