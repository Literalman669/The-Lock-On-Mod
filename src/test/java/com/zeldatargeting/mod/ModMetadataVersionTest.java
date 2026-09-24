package com.zeldatargeting.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.Mod;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModMetadataVersionTest {
    @Test
    public void forgeAnnotationMatchesModMetadata() throws Exception {
        Mod annotation = ZeldaTargetingMod.class.getAnnotation(Mod.class);
        assertNotNull(annotation);

        InputStream stream = getClass().getResourceAsStream("/mcmod.info");
        assertNotNull(stream);
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonArray metadata = new JsonParser().parse(reader).getAsJsonArray();
            assertEquals(metadata.get(0).getAsJsonObject().get("version").getAsString(), annotation.version());
        }
    }
}
