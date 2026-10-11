package com.zeldatargeting.mod.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Lays out the page tabs as one centered row: full titles when they fit, short
 * titles when they do not, and no tabs at all (a single "page n / 8" line) when
 * even the short titles are too wide.
 */
public final class ConfigTabLayout {
    public static final int PADDING = 6;
    public static final int GAP = 2;
    public static final int MARGIN = 8;
    public static final int HEIGHT = 14;

    public enum Mode {
        FULL,
        SHORT,
        SINGLE_LINE
    }

    public static final class Result {
        private final Mode mode;
        private final List<ConfigRect> tabs;

        private Result(Mode mode, List<ConfigRect> tabs) {
            this.mode = mode;
            this.tabs = tabs;
        }

        public Mode getMode() {
            return mode;
        }

        public List<ConfigRect> getTabs() {
            return tabs;
        }
    }

    private ConfigTabLayout() {
    }

    public static Result layout(int screenWidth, int y, List<String> fullNames, List<String> shortNames,
                                ToIntFunction<String> textWidth) {
        int available = screenWidth - 2 * MARGIN;
        if (rowWidth(fullNames, textWidth) <= available) {
            return new Result(Mode.FULL, row(screenWidth, y, fullNames, textWidth));
        }
        if (rowWidth(shortNames, textWidth) <= available) {
            return new Result(Mode.SHORT, row(screenWidth, y, shortNames, textWidth));
        }
        return new Result(Mode.SINGLE_LINE, Collections.<ConfigRect>emptyList());
    }

    private static int rowWidth(List<String> names, ToIntFunction<String> textWidth) {
        int total = GAP * Math.max(0, names.size() - 1);
        for (String name : names) {
            total += textWidth.applyAsInt(name) + 2 * PADDING;
        }
        return total;
    }

    private static List<ConfigRect> row(int screenWidth, int y, List<String> names, ToIntFunction<String> textWidth) {
        List<ConfigRect> tabs = new ArrayList<>(names.size());
        int x = (screenWidth - rowWidth(names, textWidth)) / 2;
        for (String name : names) {
            int width = textWidth.applyAsInt(name) + 2 * PADDING;
            tabs.add(new ConfigRect(x, y, width, HEIGHT));
            x += width + GAP;
        }
        return Collections.unmodifiableList(tabs);
    }
}
