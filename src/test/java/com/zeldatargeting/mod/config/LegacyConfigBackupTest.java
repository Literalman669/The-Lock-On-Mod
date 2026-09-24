package com.zeldatargeting.mod.config;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LegacyConfigBackupTest {

    @Test
    public void backupUsesTheNextAvailableNameWithoutChangingTheLegacyFile() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-backup-test").toFile();
        File legacy = new File(directory, "zeldatargeting.cfg");
        File firstBackup = new File(directory, "zeldatargeting-1.3-backup.cfg");
        Files.write(legacy.toPath(), "legacy-data".getBytes(StandardCharsets.UTF_8));
        Files.write(firstBackup.toPath(), "older-backup".getBytes(StandardCharsets.UTF_8));

        LegacyConfigBackup.BackupResult result = new LegacyConfigBackup().backup(legacy);

        File expected = new File(directory, "zeldatargeting-1.3-backup-2.cfg");
        assertTrue(result.isSuccess());
        assertEquals(expected.getCanonicalFile(), result.getBackupFile().getCanonicalFile());
        assertEquals("legacy-data", new String(Files.readAllBytes(expected.toPath()), StandardCharsets.UTF_8));
        assertEquals("legacy-data", new String(Files.readAllBytes(legacy.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void backupReportsMissingSourceWithoutCreatingAnything() throws Exception {
        File directory = Files.createTempDirectory("zeldatargeting-backup-missing-test").toFile();
        LegacyConfigBackup.BackupResult result = new LegacyConfigBackup().backup(new File(directory, "zeldatargeting.cfg"));

        assertFalse(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals(null, result.getBackupFile());
    }
}
