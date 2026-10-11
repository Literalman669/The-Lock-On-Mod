package com.zeldatargeting.mod.client.gui;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigTextWrapTest {
    private static final ToIntFunction<String> SIX_PER_CHAR = text -> 6 * text.length();
    private static final String CINEMATIC =
        "Slow, smooth camera that switches to third person when you lock on. Good for exploring and big fights.";

    @Test
    public void longPresetHelpWrapsInsideTheColumn() {
        List<String> lines = ConfigTextWrap.wrap(CINEMATIC, 300, SIX_PER_CHAR);

        assertTrue(lines.size() > 1);
        for (String line : lines) {
            assertTrue(line, SIX_PER_CHAR.applyAsInt(line) <= 300);
        }
        assertEquals(CINEMATIC, String.join(" ", lines));
    }

    @Test
    public void shortTextStaysOnOneLine() {
        assertEquals(Collections.singletonList("Good for quick combat."),
            ConfigTextWrap.wrap("Good for quick combat.", 300, SIX_PER_CHAR));
    }

    @Test
    public void aWordWiderThanTheColumnGetsItsOwnLine() {
        assertEquals(Arrays.asList("a", "abcdefghij", "b"),
            ConfigTextWrap.wrap("a abcdefghij b", 30, SIX_PER_CHAR));
    }
}
