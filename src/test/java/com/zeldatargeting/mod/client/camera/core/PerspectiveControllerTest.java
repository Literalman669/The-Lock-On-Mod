package com.zeldatargeting.mod.client.camera.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PerspectiveControllerTest {
    private static final float EPSILON = 0.0001F;

    @Test
    public void beginCapturesOnceAndRequestsRearThirdPersonWhenEnabled() {
        PerspectiveController controller = new PerspectiveController();

        PerspectiveDecision first = controller.begin(0, 70.0F, true, 0L);
        PerspectiveDecision repeated = controller.begin(1, 90.0F, true, 10L);

        assertTrue(first.shouldApply());
        assertEquals(1, first.getPerspective());
        assertEquals(70.0F, first.getBaselineFov(), EPSILON);
        assertFalse(repeated.shouldApply());
        assertEquals(0, controller.getCapturedPerspective());
        assertEquals(1, controller.getRequestedPerspective());
    }

    @Test
    public void fallbackUsesEntryAndExitDelaysWithHysteresis() {
        PerspectiveController controller = new PerspectiveController();
        controller.begin(0, 70.0F, true, 0L);

        controller.update(0.50D, CameraProfiles.BALANCED, 0L);
        assertFalse(controller.update(0.50D, CameraProfiles.BALANCED, 99L).shouldApply());
        PerspectiveDecision enter = controller.update(0.50D, CameraProfiles.BALANCED, 100L);
        assertTrue(enter.shouldApply());
        assertEquals(0, enter.getPerspective());
        assertTrue(enter.isFallbackActive());

        controller.update(1.30D, CameraProfiles.BALANCED, 200L);
        assertFalse(controller.update(1.30D, CameraProfiles.BALANCED, 399L).shouldApply());
        PerspectiveDecision exit = controller.update(1.30D, CameraProfiles.BALANCED, 400L);
        assertTrue(exit.shouldApply());
        assertEquals(1, exit.getPerspective());
        assertFalse(exit.isFallbackActive());
    }

    @Test
    public void fallbackTimerRestartsAfterDistanceReturnsToTheBand() {
        PerspectiveController controller = new PerspectiveController();
        controller.begin(0, 70.0F, true, 0L);

        controller.update(0.50D, CameraProfiles.BALANCED, 0L);
        controller.update(0.80D, CameraProfiles.BALANCED, 50L);
        controller.update(0.50D, CameraProfiles.BALANCED, 60L);

        assertFalse(controller.update(0.50D, CameraProfiles.BALANCED, 159L).shouldApply());
        assertTrue(controller.update(0.50D, CameraProfiles.BALANCED, 160L).shouldApply());
    }

    @Test
    public void fallbackDoesNotChangeAnIntentionalThirdPersonSession() {
        PerspectiveController controller = new PerspectiveController();
        controller.begin(1, 70.0F, true, 0L);

        assertFalse(controller.update(0.10D, CameraProfiles.BALANCED, 500L).shouldApply());
        assertEquals(1, controller.getRequestedPerspective());
    }

    @Test
    public void restoreReturnsCapturedStateExactlyOnce() {
        PerspectiveController controller = new PerspectiveController();
        controller.begin(0, 72.0F, true, 0L);

        PerspectiveDecision restore = controller.restore();
        PerspectiveDecision repeated = controller.restore();

        assertTrue(restore.shouldApply());
        assertEquals(0, restore.getPerspective());
        assertEquals(72.0F, restore.getBaselineFov(), EPSILON);
        assertFalse(repeated.shouldApply());
        assertFalse(controller.isActive());
    }

    @Test
    public void disabledAutomaticPerspectiveCapturesAndClearsWithoutCommands() {
        PerspectiveController controller = new PerspectiveController();

        PerspectiveDecision begin = controller.begin(0, 75.0F, false, 0L);
        PerspectiveDecision restore = controller.restore();

        assertFalse(begin.shouldApply());
        assertFalse(restore.shouldApply());
        assertEquals(0, restore.getPerspective());
        assertEquals(75.0F, restore.getBaselineFov(), EPSILON);
        assertFalse(controller.isActive());
    }

    @Test
    public void clearIsIdempotentAndDiscardsCapturedStateWithoutACommand() {
        PerspectiveController controller = new PerspectiveController();
        controller.begin(0, 70.0F, true, 0L);

        controller.clear();
        controller.clear();

        assertFalse(controller.isActive());
        assertFalse(controller.restore().shouldApply());
    }
}
