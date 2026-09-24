package com.zeldatargeting.mod.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Creates a non-destructive, uniquely named backup of a pre-1.4 config file. */
public final class LegacyConfigBackup {

    public BackupResult backup(File legacyFile) {
        if (legacyFile == null || !legacyFile.isFile()) {
            return BackupResult.noSource();
        }

        File backupFile = nextAvailableBackup(legacyFile);
        try {
            Files.copy(legacyFile.toPath(), backupFile.toPath());
            return BackupResult.success(backupFile);
        } catch (IOException exception) {
            return BackupResult.failure(exception);
        }
    }

    public File nextAvailableBackup(File legacyFile) {
        File directory = legacyFile.getAbsoluteFile().getParentFile();
        String baseName = "zeldatargeting-1.3-backup";
        File candidate = new File(directory, baseName + ".cfg");
        int suffix = 2;

        while (candidate.exists()) {
            candidate = new File(directory, baseName + "-" + suffix + ".cfg");
            suffix++;
        }

        return candidate;
    }

    public static final class BackupResult {
        private final File backupFile;
        private final IOException failure;
        private final boolean sourcePresent;

        private BackupResult(File backupFile, IOException failure, boolean sourcePresent) {
            this.backupFile = backupFile;
            this.failure = failure;
            this.sourcePresent = sourcePresent;
        }

        public static BackupResult success(File backupFile) {
            return new BackupResult(backupFile, null, true);
        }

        public static BackupResult noSource() {
            return new BackupResult(null, null, false);
        }

        public static BackupResult failure(IOException failure) {
            return new BackupResult(null, failure, true);
        }

        public boolean isSuccess() {
            return backupFile != null;
        }

        public boolean isFailure() {
            return failure != null;
        }

        public boolean hasSource() {
            return sourcePresent;
        }

        public File getBackupFile() {
            return backupFile;
        }

        public IOException getFailure() {
            return failure;
        }
    }
}
