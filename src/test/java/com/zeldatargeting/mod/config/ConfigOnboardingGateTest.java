package com.zeldatargeting.mod.config;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigOnboardingGateTest {

    @Test
    public void opensOnlyForARequiredSelectionAtAnUnobstructedMainMenu() {
        ConfigOnboardingGate gate = new ConfigOnboardingGate();

        assertFalse(gate.shouldOpen(false, true, false));
        assertFalse(gate.shouldOpen(true, false, false));
        assertFalse(gate.shouldOpen(true, true, true));
        assertTrue(gate.shouldOpen(true, true, false));

        gate.markOpened();
        assertFalse(gate.shouldOpen(true, true, false));
    }

    @Test
    public void dismissalDoesNotReopenUntilTheNextSession() {
        ConfigOnboardingGate gate = new ConfigOnboardingGate();

        assertTrue(gate.shouldOpen(true, true, false));
        gate.dismiss();

        assertFalse(gate.shouldOpen(true, true, false));
    }

    @Test
    public void confirmationPreventsFurtherPromptsEvenBeforeTheCallerRefreshesState() {
        ConfigOnboardingGate gate = new ConfigOnboardingGate();

        gate.confirm();

        assertFalse(gate.shouldOpen(true, true, false));
    }
}
