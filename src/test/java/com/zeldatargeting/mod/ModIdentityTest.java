package com.zeldatargeting.mod;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ModIdentityTest {
    private static final String DESCRIPTION =
        "Lock onto mobs with a keypress. The camera follows your target, a ring and panel show"
            + " its health and how many hits it will take, and you can switch between nearby"
            + " targets. Works with Shoulder Surfing Reloaded and Epic Fight.";

    @Test
    public void modNameIsLockOn() {
        assertEquals("Lock On", ZeldaTargetingMod.NAME);
    }

    @Test
    public void modInfoDescribesTheModWithoutZelda() throws IOException {
        String modInfo = resource("/mcmod.info");

        assertTrue(modInfo.contains("\"name\": \"Lock On\""));
        assertTrue(modInfo.contains(DESCRIPTION));
        assertFalse(modInfo.contains("Zelda"));
        assertFalse(modInfo.contains("Breath of the Wild"));
    }

    @Test
    public void controlsCategoryIsLockOn() throws IOException {
        String lang = resource("/assets/zeldatargeting/lang/en_us.lang");

        assertTrue(lang.replace("\r\n", "\n").contains("key.categories.zeldatargeting=Lock On\n"));
    }

    @Test
    public void noShippedTextUsesTheOldName() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Paths.get("src", "main"))) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                if (text.contains("Zelda Targeting")) {
                    offenders.add(file.toString());
                }
            }
        }
        assertEquals("files still saying \"Zelda Targeting\"", Collections.emptyList(), offenders);
    }

    private static String resource(String path) throws IOException {
        try (InputStream in = ModIdentityTest.class.getResourceAsStream(path)) {
            assertNotNull(path + " must be on the classpath", in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            for (int read; (read = in.read(buffer)) > 0; ) {
                out.write(buffer, 0, read);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
