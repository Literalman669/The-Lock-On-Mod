package com.zeldatargeting.mod.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/** Creates a non-destructive, uniquely named backup of a pre-1.4 config file. */
public final class LegacyConfigBackup {

    public BackupResult backup(File legacyFile) {
        if (legacyFile == null || !legacyFile.isFile()) {
            return BackupResult.noSource();
        }

        try {
            // The old file stays in place until settings are saved, so it is seen on
            // every launch; reuse an identical backup instead of adding another.
            byte[] contents = Files.readAllBytes(legacyFile.toPath());
            File existing = existingBackupOf(legacyFile, contents);
            if (existing != null) {
                return BackupResult.success(existing);
            }
            File backupFile = nextAvailableBackup(legacyFile);
            Files.copy(legacyFile.toPath(), backupFile.toPath());
            return BackupResult.success(backupFile);
        } catch (IOException exception) {
            return BackupResult.failure(exception);
        }
    }

    public File nextAvailableBackup(File legacyFile) {
        File candidate = backupCandidate(legacyFile, 1);
        for (int index = 2; candidate.exists(); index++) {
            candidate = backupCandidate(legacyFile, index);
        }
        return candidate;
    }

    private File existingBackupOf(File legacyFile, byte[] contents) throws IOException {
        for (int index = 1; ; index++) {
            File candidate = backupCandidate(legacyFile, index);
            if (!candidate.exists()) {
                return null;
            }
            if (candidate.isFile() && Arrays.equals(contents, Files.readAllBytes(candidate.toPath()))) {
                return candidate;
            }
        }
    }

    private static File backupCandidate(File legacyFile, int index) {
        File directory = legacyFile.getAbsoluteFile().getParentFile();
        String baseName = "zeldatargeting-1.3-backup";
        return new File(directory, index == 1 ? baseName + ".cfg" : baseName + "-" + index + ".cfg");
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
