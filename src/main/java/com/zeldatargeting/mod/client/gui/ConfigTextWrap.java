package com.zeldatargeting.mod.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** Word-wraps plain text so each line fits a pixel width. */
public final class ConfigTextWrap {
    private ConfigTextWrap() {
    }

    public static List<String> wrap(String text, int maxWidth, ToIntFunction<String> textWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (line.length() > 0 && textWidth.applyAsInt(candidate) > maxWidth) {
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            } else {
                line.setLength(0);
                line.append(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
