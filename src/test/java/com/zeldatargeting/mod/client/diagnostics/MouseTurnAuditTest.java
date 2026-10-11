package com.zeldatargeting.mod.client.diagnostics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MouseTurnAuditTest {
    @Test
    public void degreesPerCountMatchesVanillaAtDefaultSensitivity() {
        // Vanilla 1.12.2: f = s * 0.6 + 0.2; turn = dx * f^3 * 8 * 0.15
        assertEquals(0.15F * 8.0F * 0.125F, MouseTurnAudit.degreesPerCount(0.5F), 0.00001F);
    }

    @Test
    public void lowerSensitivityTurnsLessPerCount() {
        assertTrue(MouseTurnAudit.degreesPerCount(0.25F) < MouseTurnAudit.degreesPerCount(0.5F));
    }

    @Test
    public void rotationExplainedByTheMouseIsNotReported() {
        float perCount = MouseTurnAudit.degreesPerCount(0.5F);

        assertNull(MouseTurnAudit.unexplainedFrameTurn(20, -4, 20 * perCount, 4 * perCount, 0.5F));
    }

    @Test
    public void extraYawDuringAFrameIsReported() {
        float perCount = MouseTurnAudit.degreesPerCount(0.5F);

        String report = MouseTurnAudit.unexplainedFrameTurn(10, 0, 10 * perCount + 6.0F, 0.0F, 0.5F);

        assertTrue(report, report.contains("yaw"));
    }

    @Test
    public void smallRoundingDifferencesAreIgnored() {
        float perCount = MouseTurnAudit.degreesPerCount(0.5F);

        assertNull(MouseTurnAudit.unexplainedFrameTurn(10, 10, 10 * perCount + 0.1F, -10 * perCount - 0.1F, 0.5F));
    }

    @Test
    public void yawWrappingAroundIsNotMistakenForAFastTurn() {
        assertEquals(2.0F, MouseTurnAudit.wrappedDelta(179.0F, -179.0F), 0.0001F);
        assertEquals(-2.0F, MouseTurnAudit.wrappedDelta(-179.0F, 179.0F), 0.0001F);
    }
}
