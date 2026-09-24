package com.zeldatargeting.mod.client.camera.vanilla;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VanillaCameraOffsetApplierTest {
    private static final double EPSILON = 0.000001D;

    @Test
    public void translationUsesCameraSpaceDirectionForEachPerspective() {
        assertEquals(-2.9D, VanillaCameraOffsetApplier.translationFor(1, 2.9D), EPSILON);
        assertEquals(2.9D, VanillaCameraOffsetApplier.translationFor(2, 2.9D), EPSILON);
        assertEquals(0.0D, VanillaCameraOffsetApplier.translationFor(0, 2.9D), EPSILON);
        assertEquals(0.0D, VanillaCameraOffsetApplier.translationFor(1, Double.NaN), EPSILON);
    }
}
