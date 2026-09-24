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
