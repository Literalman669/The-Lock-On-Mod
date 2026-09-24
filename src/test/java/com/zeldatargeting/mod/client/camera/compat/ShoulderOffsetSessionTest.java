package com.zeldatargeting.mod.client.camera.compat;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShoulderOffsetSessionTest {

    @Test
    public void inactiveShoulderCameraDoesNotChangeOffsets() {
        FakeOffsetAccess offsets = new FakeOffsetAccess(1.625D, 0.25D);
        ShoulderOffsetSession session = new ShoulderOffsetSession();

        assertFalse(session.begin(false, offsets));
        assertEquals(1.625D, offsets.x, 0.0D);
        assertEquals(0.25D, offsets.y, 0.0D);
        assertEquals(0, offsets.centerCount);
    }

    @Test
    public void activeSessionCentersRuntimeOffsetsAndRestoresExactValues() {
        FakeOffsetAccess offsets = new FakeOffsetAccess(1.625D, -0.25D);
        ShoulderOffsetSession session = new ShoulderOffsetSession();

        assertTrue(session.begin(true, offsets));
        assertTrue(session.isActive());
        assertEquals(0.0D, offsets.x, 0.0D);
        assertEquals(0.0D, offsets.y, 0.0D);

        session.end(offsets);

        assertFalse(session.isActive());
        assertEquals(1.625D, offsets.x, 0.0D);
        assertEquals(-0.25D, offsets.y, 0.0D);
        assertEquals(1, offsets.restoreCount);
    }

    @Test
    public void repeatedBeginDoesNotReplaceTheOriginalOffsets() {
        FakeOffsetAccess offsets = new FakeOffsetAccess(-0.875D, 0.1D);
        ShoulderOffsetSession session = new ShoulderOffsetSession();

        assertTrue(session.begin(true, offsets));
        assertTrue(session.begin(true, offsets));
        session.end(offsets);

        assertEquals(-0.875D, offsets.x, 0.0D);
        assertEquals(0.1D, offsets.y, 0.0D);
        assertEquals(1, offsets.centerCount);
        assertEquals(1, offsets.restoreCount);
    }

    private static final class FakeOffsetAccess
            implements ShoulderOffsetSession.OffsetAccess {
        private double x;
        private double y;
        private int centerCount;
        private int restoreCount;

        private FakeOffsetAccess(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public ShoulderOffsetSession.Offset read() {
            return new ShoulderOffsetSession.Offset(x, y);
        }

        @Override
        public void center() {
            x = 0.0D;
            y = 0.0D;
            centerCount++;
        }

        @Override
        public void restore(ShoulderOffsetSession.Offset offset) {
            x = offset.getX();
            y = offset.getY();
            restoreCount++;
        }
    }
}
