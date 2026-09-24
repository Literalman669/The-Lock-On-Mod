# Zelda Targeting 1.4 Stage 5 Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the legacy 1.3 configuration flow with a curated, testable 1.4 configuration foundation: preset-first onboarding, validated settings snapshots, transactional configuration editing, responsive pages, and presentation/accessibility controls that are immediately reflected in-game.

**Architecture:** A plain-Java settings model, presets, validation, backup naming, and edit session sit beneath a small Forge configuration store and a legacy runtime bridge. The Minecraft GUI edits only a working snapshot; preview changes apply to runtime only, Save persists, and Cancel restores the exact entry snapshot. A first-run controller opens a dedicated preset chooser at the main menu when a legacy or absent configuration requires a 1.4 choice.

**Tech Stack:** Java 8, Minecraft Forge 1.12.2, Forge `Configuration`, JUnit 4.13.2, Gradle ForgeGradle build and reobfuscation tasks.

## Global Constraints

- Keep the mod client-only; do not introduce packets, common-side gameplay state, or a server dependency.
- Preserve the current 1.3 runtime fields in `TargetingConfig` during Stage 5 so existing targeting, rendering, audio, and camera callers remain compatible.
- Treat every 1.3 config file as legacy. Do not migrate individual old values; create a non-overwriting backup and present the player with the three curated 1.4 presets.
- Never overwrite a prior `zeldatargeting-1.3-backup*.cfg` file. If backup creation fails, leave the old file unchanged and use Balanced in memory without writing a new configuration.
- Every mutable configuration surface must use copy/validate/apply semantics. No GUI interaction may call `saveConfig()` before the user presses Save.
- Preserve the current safe content viewport, footer, and page-navigation protections while adding the new seven-page configuration information architecture.
- Add a test before each behavior change, observe it fail, implement the smallest behavior that makes it pass, then run the focused test again.
- Use `apply_patch` for source and documentation edits. Do not use shell redirection or scripts to write project files.
- Do not change public release version strings from `1.3.0` to `1.4.0` until Stage 6 release QA.

---

## File Map

| File | Responsibility |
| --- | --- |
| `src/main/java/com/zeldatargeting/mod/config/TargetingSettings.java` | Complete mutable, copyable value object for schema-4 user settings. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingPreset.java` | The Cinematic, Balanced, and Snappy curated settings profiles. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsValidator.java` | Central clamping, enum normalization, and safe fallback policy. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsSession.java` | Entry snapshot, working copy, preview, reset, save, and cancel semantics. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsBridge.java` | Explicit mapping between schema-4 settings and existing `TargetingConfig` runtime fields. |
| `src/main/java/com/zeldatargeting/mod/config/LegacyConfigBackup.java` | Safe unique-name calculation and legacy config backup copying. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsStore.java` | Sole Forge `Configuration` persistence owner for the schema-4 file. |
| `src/main/java/com/zeldatargeting/mod/config/ConfigOnboardingGate.java` | Pure first-run prompt eligibility state machine. |
| `src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java` | Small runtime facade that initializes storage, exposes snapshots, previews settings, and persists confirmed settings. |
| `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPage.java` | Seven page definitions, titles, and descriptions for the 1.4 GUI. |
| `src/main/java/com/zeldatargeting/mod/client/gui/GuiPresetSelection.java` | Dedicated modal preset chooser shown from the main menu. |
| `src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java` | Transactional responsive editor backed by `TargetingSettingsSession`. |
| `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPageNavigator.java` | Existing navigation helper updated to use the Stage-5 page count and labels. |
| `src/main/java/com/zeldatargeting/mod/client/ConfigOnboardingClientHandler.java` | Client-tick adapter that opens the chooser once at the eligible main menu. |
| `src/main/java/com/zeldatargeting/mod/proxy/ClientProxy.java` | Registration point for the client onboarding handler. |
| `src/main/java/com/zeldatargeting/mod/client/presentation/core/RingColorPolicy.java` | Palette-aware health-status color policy. |
| `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java` | Uses selected palette, ring controls, and reduced-motion state. |
| `src/main/java/com/zeldatargeting/mod/client/presentation/render/DetailPanelRenderer.java` | Uses selected palette plus HUD scale and opacity. |
| `src/main/java/com/zeldatargeting/mod/client/presentation/render/BossPanelRenderer.java` | Uses the same selected palette as the standard detail panel. |

## Task 1: Build the Pure Settings, Preset, and Validation Layer

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingSettings.java`
- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingPreset.java`
- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsValidator.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/TargetingPresetTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/TargetingSettingsValidatorTest.java`

- [ ] **Step 1: Write the failing preset-contract tests.**

  Cover exact preset identifiers and the player-facing differences: Cinematic has stronger smoothing and automatic third person, Balanced remains the existing intended feel, and Snappy has minimal smoothing with automatic third person disabled.

  ```java
  @Test
  public void presetsExposeStableIdsAndIndependentCopies() {
      TargetingSettings cinematic = TargetingPreset.CINEMATIC.createSettings();
      TargetingSettings balanced = TargetingPreset.BALANCED.createSettings();

      assertEquals("cinematic", cinematic.lockOnPreset);
      assertEquals("balanced", balanced.lockOnPreset);
      assertNotSame(cinematic, TargetingPreset.CINEMATIC.createSettings());
      assertTrue(cinematic.cameraSmoothness > balanced.cameraSmoothness);
  }
  ```

- [ ] **Step 2: Run the new preset test and confirm compilation fails because the model does not exist.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingPresetTest --no-daemon`

  Expected: compilation failure naming the missing settings and preset classes.

- [ ] **Step 3: Implement the schema-4 value object and curated presets.**

  Add all current user-facing runtime values plus these Stage-5 settings: `schemaVersion`, `presentationPalette`, `reducedMotion`, `hudScale`, `hudOpacity`, `ringEnabled`, `ringThickness`, `ringGlowStrength`, and `ringHealthArcEnabled`. Implement a `copy()` that duplicates every field and does not share mutable state.

  ```java
  public final class TargetingSettings {
      public static final int CURRENT_SCHEMA_VERSION = 4;
      public int schemaVersion = CURRENT_SCHEMA_VERSION;
      public String lockOnPreset = "balanced";
      public String presentationPalette = "default";
      public boolean reducedMotion;
      public float hudScale = 1.0F;
      public float hudOpacity = 1.0F;
      public boolean ringEnabled = true;
      public float ringThickness = 1.0F;
      public float ringGlowStrength = 1.0F;
      public boolean ringHealthArcEnabled = true;

      public TargetingSettings copy() { /* explicit field copy */ }
  }
  ```

  Make `TargetingPreset.fromId(String)` return `BALANCED` for unknown or null ids. Each `createSettings()` starts from an independent balanced base and sets every profile-specific value explicitly.

- [ ] **Step 4: Re-run the preset test and confirm it passes.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingPresetTest --no-daemon`

  Expected: all preset assertions pass.

- [ ] **Step 5: Write failing validation tests for malformed settings.**

  Test clamping of ranges, opacity, scales, ring values, invalid enums, empty preset names, and null input. Confirm validation returns a new valid copy without mutating the input.

  ```java
  @Test
  public void validatorClampsAndNormalizesWithoutMutatingSource() {
      TargetingSettings invalid = TargetingPreset.BALANCED.createSettings();
      invalid.targetingRange = -10;
      invalid.hudOpacity = 3.0F;
      invalid.presentationPalette = "not-a-palette";

      TargetingSettings validated = new TargetingSettingsValidator().validate(invalid);

      assertEquals(1.0D, validated.targetingRange, 0.0D);
      assertEquals(1.0F, validated.hudOpacity, 0.0F);
      assertEquals("default", validated.presentationPalette);
      assertEquals(-10.0D, invalid.targetingRange, 0.0D);
  }
  ```

- [ ] **Step 6: Run the validator test and confirm it fails.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsValidatorTest --no-daemon`

  Expected: compilation failure because `TargetingSettingsValidator` does not exist.

- [ ] **Step 7: Implement one central validator and run the focused tests.**

  Validate finite numeric values, use safe min/max ranges compatible with the current GUI controls, normalize palette/priority/motion/anchor/theme values, and restore a balanced copy for null input. The validator must always set `schemaVersion` to the current version.

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingPresetTest --tests com.zeldatargeting.mod.config.TargetingSettingsValidatorTest --no-daemon`

  Expected: both tests pass.

- [ ] **Step 8: Commit the pure configuration foundation.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/config/TargetingSettings.java src/main/java/com/zeldatargeting/mod/config/TargetingPreset.java src/main/java/com/zeldatargeting/mod/config/TargetingSettingsValidator.java src/test/java/com/zeldatargeting/mod/config/TargetingPresetTest.java src/test/java/com/zeldatargeting/mod/config/TargetingSettingsValidatorTest.java
  git commit -m "feat: add curated configuration settings foundation"
  ```

## Task 2: Add Transactional Editing and the Legacy Runtime Bridge

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsSession.java`
- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsBridge.java`
- Modify: `src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/TargetingSettingsSessionTest.java`
- Replace: `src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java`

- [ ] **Step 1: Write failing session tests that prove preview, reset, save, and cancel semantics.**

  ```java
  @Test
  public void cancelRestoresTheExactEntrySnapshot() {
      TargetingSettings entry = TargetingPreset.BALANCED.createSettings();
      TargetingSettingsSession session = new TargetingSettingsSession(entry, validator);

      session.workingCopy().targetingRange = 48.0D;
      TargetingSettings restored = session.cancel();

      assertEquals(16.0D, restored.targetingRange, 0.0D);
      assertEquals(16.0D, entry.targetingRange, 0.0D);
  }
  ```

  Include reset-to-preset and save validation assertions.

- [ ] **Step 2: Run the session test and confirm it fails because the session is absent.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsSessionTest --no-daemon`

  Expected: compilation failure identifying the missing session class.

- [ ] **Step 3: Implement `TargetingSettingsSession`.**

  Store an immutable-by-convention entry copy and an independent working copy. `preview()` and `save()` return validated copies. `cancel()` returns a copy of the entry snapshot. `reset(TargetingPreset)` replaces the working copy only.

  ```java
  public final class TargetingSettingsSession {
      public TargetingSettingsSession(TargetingSettings active, TargetingSettingsValidator validator) { }
      public TargetingSettings workingCopy() { return working; }
      public TargetingSettings preview() { return validator.validate(working); }
      public void reset(TargetingPreset preset) { working = preset.createSettings(); }
      public TargetingSettings save() { return validator.validate(working); }
      public TargetingSettings cancel() { return entrySnapshot.copy(); }
  }
  ```

- [ ] **Step 4: Run the session test and confirm it passes.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsSessionTest --no-daemon`

- [ ] **Step 5: Write failing bridge tests against representative settings from every current subsystem.**

  Test targeting range, HUD visibility, camera smoothness, entity filtering, audio, damage numbers, and the new presentation values. Capture settings, apply a modified settings object, capture again, and assert values round-trip.

- [ ] **Step 6: Run the bridge test and confirm it fails.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsBridgeTest --no-daemon`

  Expected: compilation failure naming `TargetingSettingsBridge`.

- [ ] **Step 7: Implement the bridge and slim `TargetingConfig` into a runtime facade.**

  Put the explicit field mapping in `TargetingSettingsBridge.captureRuntime()` and `TargetingSettingsBridge.applyToRuntime(TargetingSettings)`. Keep the existing public static fields in `TargetingConfig` for compatibility with rendering and gameplay callers. Add these facade methods:

  ```java
  public static TargetingSettings captureActiveSettings();
  public static void previewSettings(TargetingSettings settings);
  public static void restoreSettings(TargetingSettings settings);
  public static void saveSettings(TargetingSettings settings);
  public static PresentationPalette getPresentationPalette();
  ```

  Do not write configuration inside `previewSettings` or `restoreSettings`.

- [ ] **Step 8: Run bridge, session, and existing default tests.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsSessionTest --tests com.zeldatargeting.mod.config.TargetingSettingsBridgeTest --tests com.zeldatargeting.mod.config.LegacyConfigDefaultsTest --no-daemon`

  Expected: all pass; the updated legacy-default test asserts Balanced is the safe no-file fallback instead of asserting direct saves.

- [ ] **Step 9: Commit the transaction and bridge layer.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/config/TargetingSettingsSession.java src/main/java/com/zeldatargeting/mod/config/TargetingSettingsBridge.java src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java src/test/java/com/zeldatargeting/mod/config/TargetingSettingsSessionTest.java src/test/java/com/zeldatargeting/mod/config/TargetingSettingsBridgeTest.java src/test/java/com/zeldatargeting/mod/config/LegacyConfigDefaultsTest.java
  git commit -m "feat: add transactional configuration runtime bridge"
  ```

## Task 3: Safely Replace Persistence with Schema 4 and Legacy Backups

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/config/LegacyConfigBackup.java`
- Create: `src/main/java/com/zeldatargeting/mod/config/TargetingSettingsStore.java`
- Modify: `src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/LegacyConfigBackupTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/TargetingSettingsStoreTest.java`

- [ ] **Step 1: Write failing backup tests that exercise normal and collision names.**

  In a temporary directory, create `zeldatargeting.cfg`, then pre-create `zeldatargeting-1.3-backup.cfg` and assert the next calculated destination is `zeldatargeting-1.3-backup-2.cfg`. Confirm copy contents match and original contents remain unchanged.

- [ ] **Step 2: Run the backup test and confirm it fails.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.LegacyConfigBackupTest --no-daemon`

  Expected: compilation failure naming `LegacyConfigBackup`.

- [ ] **Step 3: Implement unique backup naming and safe copying.**

  Use `java.nio.file.Files.copy` without replace-existing behavior. Return a result object that distinguishes a successful backup, no source file, and a failed attempt. Never delete, truncate, or rename the source legacy file.

  ```java
  public BackupResult backup(File legacyFile) {
      if (!legacyFile.isFile()) return BackupResult.noSource();
      File destination = nextAvailableBackup(legacyFile);
      Files.copy(legacyFile.toPath(), destination.toPath());
      return BackupResult.success(destination);
  }
  ```

- [ ] **Step 4: Re-run the backup tests and confirm they pass.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.LegacyConfigBackupTest --no-daemon`

- [ ] **Step 5: Write failing store tests for schema-4 round trips and legacy detection.**

  Use a temp config file. Assert a saved setting stores `meta.schemaVersion=4`, re-loads all representative values, and does not require preset selection. For a legacy file without the schema marker, assert it requests a preset and does not write a schema-4 config before a user confirms one.

- [ ] **Step 6: Run the store tests and confirm they fail.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingSettingsStoreTest --no-daemon`

- [ ] **Step 7: Implement `TargetingSettingsStore` as the only Forge `Configuration` owner.**

  Add `load()` and `save(TargetingSettings)`. `load()` returns a `LoadResult` containing a validated settings copy, whether a preset selection is required, and whether a backup failure occurred. A current schema loads normally. No marker or an earlier schema calls `LegacyConfigBackup`, returns Balanced in memory, and requires a chooser.

  Persist grouped categories: `meta`, `preset`, `targeting`, `camera`, `hud`, `ring`, `audio`, `entities`, `damageNumbers`, `compatibility`, and `accessibility`.

- [ ] **Step 8: Connect `TargetingConfig.init`, `loadConfig`, and `saveSettings` to the store.**

  Remove direct legacy category reads and writes from `TargetingConfig`. Initialization loads through the store, applies the returned active settings through the bridge, and retains `requiresPresetSelection` for the client onboarding adapter. Confirm that legacy detection itself does not save a new file.

- [ ] **Step 9: Run the full configuration test group.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.TargetingPresetTest --tests com.zeldatargeting.mod.config.TargetingSettingsValidatorTest --tests com.zeldatargeting.mod.config.TargetingSettingsSessionTest --tests com.zeldatargeting.mod.config.TargetingSettingsBridgeTest --tests com.zeldatargeting.mod.config.LegacyConfigBackupTest --tests com.zeldatargeting.mod.config.TargetingSettingsStoreTest --no-daemon`

  Expected: all configuration behavior is green.

- [ ] **Step 10: Commit the schema-4 storage replacement.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/config/LegacyConfigBackup.java src/main/java/com/zeldatargeting/mod/config/TargetingSettingsStore.java src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java src/test/java/com/zeldatargeting/mod/config/LegacyConfigBackupTest.java src/test/java/com/zeldatargeting/mod/config/TargetingSettingsStoreTest.java
  git commit -m "feat: add schema 4 configuration storage"
  ```

## Task 4: Add Preset-First Main-Menu Onboarding

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/config/ConfigOnboardingGate.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/ConfigOnboardingClientHandler.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/GuiPresetSelection.java`
- Modify: `src/main/java/com/zeldatargeting/mod/proxy/ClientProxy.java`
- Modify: `src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java`
- Create: `src/test/java/com/zeldatargeting/mod/config/ConfigOnboardingGateTest.java`

- [ ] **Step 1: Write failing tests for the pure onboarding gate.**

  Test that it opens once only when a selection is required and the main menu is present; it must not open over the loading screen or an existing dialog. Dismissal must defer the prompt until the next launch, while acceptance clears the requirement.

  ```java
  @Test
  public void dismissalDoesNotReopenUntilTheNextSession() {
      ConfigOnboardingGate gate = new ConfigOnboardingGate();
      assertTrue(gate.shouldOpen(true, true, false));
      gate.dismiss();
      assertFalse(gate.shouldOpen(true, true, false));
  }
  ```

- [ ] **Step 2: Run the gate test and confirm it fails.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.ConfigOnboardingGateTest --no-daemon`

- [ ] **Step 3: Implement the gate and make the TargetingConfig requirement explicit.**

  Add `TargetingConfig.requiresPresetSelection()` and `TargetingConfig.confirmPreset(TargetingPreset)`. Confirmation applies the selected settings and writes through the store. Closing the GUI without confirmation leaves storage untouched.

- [ ] **Step 4: Re-run the gate test and confirm it passes.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.ConfigOnboardingGateTest --no-daemon`

- [ ] **Step 5: Implement the main-menu client adapter and chooser screen.**

  Register one `ClientTickEvent` handler. At END phase, when `Minecraft.currentScreen` is a stable `GuiMainMenu`, no competing GUI is open, and the gate permits it, show `GuiPresetSelection`.

  The chooser contains three responsive buttons with a one-sentence summary:

  - Cinematic — smooth camera and automatic third person.
  - Balanced — responsive default for most play styles.
  - Snappy — direct camera response with no automatic third person.

  It must include a close button that records dismissal only for the running client session.

- [ ] **Step 6: Register the handler in `ClientProxy` and verify the client compiles.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.config.ConfigOnboardingGateTest --no-daemon`

  Expected: tests pass and production client classes compile.

- [ ] **Step 7: Perform the first short in-game onboarding checkpoint.**

  Build and launch the development client. Move a test `zeldatargeting.cfg` aside first rather than deleting it. Verify:

  1. The chooser appears only after reaching the main menu.
  2. Choosing each preset opens no error screen and writes schema 4.
  3. Closing without choosing keeps the config unmodified and uses Balanced for that session.
  4. Restarting after a dismissed chooser presents it again.

- [ ] **Step 8: Commit onboarding.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/config/ConfigOnboardingGate.java src/main/java/com/zeldatargeting/mod/client/ConfigOnboardingClientHandler.java src/main/java/com/zeldatargeting/mod/client/gui/GuiPresetSelection.java src/main/java/com/zeldatargeting/mod/proxy/ClientProxy.java src/main/java/com/zeldatargeting/mod/config/TargetingConfig.java src/test/java/com/zeldatargeting/mod/config/ConfigOnboardingGateTest.java
  git commit -m "feat: add preset-first configuration onboarding"
  ```

## Task 5: Rebuild the In-Game Configuration Screen Around a Working Copy

**Files:**

- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPage.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPageNavigator.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigContentViewport.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigPageTest.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigEditSessionIntegrationTest.java`
- Extend: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigContentViewportTest.java`

- [ ] **Step 1: Write failing tests for the Stage-5 page list and save/cancel adapter contract.**

  Assert exactly seven ordered pages: Presets, Targeting, Camera, HUD, Audio, Compatibility, Accessibility. Test that the GUI-facing session adapter previews changes without persistence, cancel restores entry values, and save exposes only validated settings.

- [ ] **Step 2: Run the focused GUI tests and confirm they fail.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.client.gui.ConfigPageTest --tests com.zeldatargeting.mod.client.gui.ConfigEditSessionIntegrationTest --no-daemon`

- [ ] **Step 3: Add the page enum and update navigation labels.**

  ```java
  public enum ConfigPage {
      PRESETS("Presets", "Choose the overall feel"),
      TARGETING("Targeting", "Range, selection, and filters"),
      CAMERA("Camera", "Follow, focus, and third-person behavior"),
      HUD("HUD", "Panel, target ring, and placement"),
      AUDIO("Audio", "Target feedback and sound tuning"),
      COMPATIBILITY("Compatibility", "Vanilla-safe compatibility controls"),
      ACCESSIBILITY("Accessibility", "Palette, opacity, and reduced motion");
  }
  ```

  Retain the existing page navigator's protection against a double-next action and make it navigate enum ordinals rather than hard-coded page numbers.

- [ ] **Step 4: Rework `GuiTargetingConfig` to own one `TargetingSettingsSession`.**

  On opening, construct the session from `TargetingConfig.captureActiveSettings()`. Every toggle, cycle, slider, and text/value control updates the session's working copy then calls `TargetingConfig.previewSettings(session.preview())`. Do not call `TargetingConfig.saveConfig()` from any option button.

  Replace footer behavior with four actions:

  - Save: validate, persist through `TargetingConfig.saveSettings`, then close.
  - Cancel: `TargetingConfig.restoreSettings(session.cancel())`, then close.
  - Reset: replace the working copy with the selected/current preset and retain the screen.
  - Previous/Next: page navigation only.

- [ ] **Step 5: Implement responsive page contents.**

  Use the existing viewport to keep all controls above the footer at every supported GUI scale. Pages must expose the following settings:

  | Page | Required settings |
  | --- | --- |
  | Presets | Cinematic, Balanced, Snappy, current selected preset summary. |
  | Targeting | Range, max tracking distance, detection angle, line of sight, target priority, entity categories, player targeting. |
  | Camera | Camera lock, smoothness, yaw/pitch limits, auto third person, focus offset, existing BTP/SSR safe settings. |
  | HUD | Detail panel visibility, reticle, target name, distance, anchor/offset, scale/opacity, ring visibility/thickness/glow/health arc. |
  | Audio | Enable state, master volume, event sounds, theme, variety, grouped advanced volumes and pitches. |
  | Compatibility | `Vanilla / no adapter` selection plus descriptive inactive states for Better Third Person and Shoulder Surfing Reloaded. |
  | Accessibility | Palette, reduced motion, compact HUD, soft aim indicator, target history, boss style panel, damage-number accessibility settings. |

- [ ] **Step 6: Extend viewport tests with a small-height footer case.**

  The test must prove both the final visible control and the four footer buttons remain non-overlapping at a 240-pixel logical height.

- [ ] **Step 7: Run all GUI-focused tests and confirm they pass.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.client.gui.ConfigPageNavigatorTest --tests com.zeldatargeting.mod.client.gui.ConfigContentViewportTest --tests com.zeldatargeting.mod.client.gui.ConfigPageTest --tests com.zeldatargeting.mod.client.gui.ConfigEditSessionIntegrationTest --no-daemon`

- [ ] **Step 8: Perform the second in-game configuration checkpoint.**

  Verify at normal and large GUI scales:

  1. Every page is reachable from both directions, with no skipped second page.
  2. Long pages scroll and never overlap the footer.
  3. A changed setting previews immediately, Cancel restores the original result, and restart confirms it was not saved.
  4. Save persists through a restart.
  5. Reset only changes the current working copy until Save.

- [ ] **Step 9: Commit the transactional GUI.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/client/gui/ConfigPage.java src/main/java/com/zeldatargeting/mod/client/gui/ConfigPageNavigator.java src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java src/main/java/com/zeldatargeting/mod/client/gui/ConfigContentViewport.java src/test/java/com/zeldatargeting/mod/client/gui/ConfigPageTest.java src/test/java/com/zeldatargeting/mod/client/gui/ConfigEditSessionIntegrationTest.java src/test/java/com/zeldatargeting/mod/client/gui/ConfigContentViewportTest.java
  git commit -m "feat: rebuild configuration screen around working copies"
  ```

## Task 6: Apply the New Presentation and Accessibility Settings at Runtime

**Files:**

- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/core/RingColorPolicy.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/render/DetailPanelRenderer.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/render/BossPanelRenderer.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationPalette.java`
- Create: `src/test/java/com/zeldatargeting/mod/client/presentation/render/PresentationSettingsIntegrationTest.java`
- Extend: `src/test/java/com/zeldatargeting/mod/client/presentation/core/RingColorPolicyTest.java`

- [ ] **Step 1: Write failing tests for palette-aware colors and reduced motion.**

  Assert the default overload keeps current results, a chosen palette changes the visual color policy deterministically, and reduced motion disables health-arc animation without disabling the health value itself.

- [ ] **Step 2: Run the render tests and confirm they fail.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.client.presentation.core.RingColorPolicyTest --tests com.zeldatargeting.mod.client.presentation.render.PresentationSettingsIntegrationTest --no-daemon`

- [ ] **Step 3: Add palette-aware policy overloads.**

  Keep existing callers compatible while adding explicit palette input.

  ```java
  public static int colorFor(PresentationStatus status) {
      return colorFor(status, PresentationPalette.DEFAULT);
  }

  public static int colorFor(PresentationStatus status, PresentationPalette palette) {
      PresentationStatus safeStatus = status == null ? PresentationStatus.NORMAL : status;
      PresentationPalette safePalette = palette == null ? PresentationPalette.DEFAULT : palette;
      if (safeStatus == PresentationStatus.LETHAL) return safePalette.getLethalColor();
      if (safeStatus == PresentationStatus.WARNING || safeStatus == PresentationStatus.LOW_HEALTH) return safePalette.getWarningColor();
      if (safeStatus == PresentationStatus.OCCLUDED) return OCCLUDED_COLOR;
      return safePalette.getHealthColor();
  }
  ```

  Use the shared health-status thresholds already driving the panel and ring; do not introduce independent low-health or lethal thresholds.

- [ ] **Step 4: Connect the renderer components to the active schema-4 values.**

  `TargetRingRenderer` reads ring enabled, thickness, glow strength, health-arc mode, palette, and reduced-motion state through the `TargetingConfig` facade. Both `DetailPanelRenderer` and `BossPanelRenderer` read the same palette, HUD scale, HUD opacity, and reduced-motion state. Preserve the good 3D neon ring geometry, one health-depleting arc, and the synchronized green/orange/red palette behavior the user approved.

- [ ] **Step 5: Run the focused rendering tests and then the full unit suite.**

  Run: `./gradlew.bat test --tests com.zeldatargeting.mod.client.presentation.core.RingColorPolicyTest --tests com.zeldatargeting.mod.client.presentation.render.PresentationSettingsIntegrationTest --no-daemon`

  Run: `./gradlew.bat test --no-daemon`

  Expected: tests pass with no regression in existing target history, camera, GUI navigation, or ring geometry tests.

- [ ] **Step 6: Perform the third in-game presentation checkpoint.**

  Using the new configuration screen:

  1. Change palette and confirm panel, arrow, and ring update together.
  2. Damage a target through full, low, and lethal health; confirm the panel and ring switch at identical thresholds and the health arc depletes.
  3. Change ring thickness and glow strength; confirm the ring remains one readable 3D tube rather than overlapping bands.
  4. Enable reduced motion and verify visual state stays clear without pulsing or animated depletion.
  5. Change HUD scale/opacity and verify no clipping at the tested GUI scales.

- [ ] **Step 7: Commit runtime presentation configuration.**

  ```powershell
  git add src/main/java/com/zeldatargeting/mod/client/presentation/core/RingColorPolicy.java src/main/java/com/zeldatargeting/mod/client/presentation/render/TargetRingRenderer.java src/main/java/com/zeldatargeting/mod/client/presentation/render/DetailPanelRenderer.java src/main/java/com/zeldatargeting/mod/client/presentation/render/BossPanelRenderer.java src/main/java/com/zeldatargeting/mod/client/presentation/core/PresentationPalette.java src/test/java/com/zeldatargeting/mod/client/presentation/core/RingColorPolicyTest.java src/test/java/com/zeldatargeting/mod/client/presentation/render/PresentationSettingsIntegrationTest.java
  git commit -m "feat: apply presentation settings at runtime"
  ```

## Task 7: Verify, Document, and Hand Off the Configuration Foundation

**Files:**

- Modify: `README.md`
- Modify: `docs/superpowers/specs/2026-08-07-zelda-targeting-1.4-stage-5-configuration-design.md`
- Create: `docs/testing/1.4-stage-5-configuration-checklist.md`

- [ ] **Step 1: Write the manual QA checklist before the final build.**

  Capture the three checkpoints above plus legacy backup collision handling, selector dismissal/restart behavior, Save/Cancel/Reset persistence checks, each of the seven pages, GUI scale coverage, and the runtime presentation controls.

- [ ] **Step 2: Update the README configuration section.**

  Describe the three presets, first-run selection, how Cancel and Reset behave, and that Better Third Person/Shoulder Surfing Reloaded remain inactive until the following adapter delivery.

- [ ] **Step 3: Mark the Stage-5 specification as implemented.**

  Add implementation status, the feature branch name, and a concise note that no 1.3 value migration is performed by design.

- [ ] **Step 4: Run the full build and reobfuscation verification.**

  Run: `./gradlew.bat test build reobfJar --rerun-tasks --no-daemon`

  Expected: all unit tests, compile tasks, packaging, and reobfuscation succeed.

- [ ] **Step 5: Inspect the final artifact and working tree.**

  ```powershell
  Get-ChildItem build\libs\*.jar
  git status --short
  git log --oneline master..HEAD
  ```

  Expected: the reobfuscated JAR exists, intended documentation changes are visible, and all Stage-5 commits are ready for review.

- [ ] **Step 6: Commit final documentation and run a final status check.**

  ```powershell
  git add README.md docs/testing/1.4-stage-5-configuration-checklist.md docs/superpowers/specs/2026-08-07-zelda-targeting-1.4-stage-5-configuration-design.md
  git commit -m "docs: document stage 5 configuration foundation"
  git status --short
  ```

  Expected: clean feature branch ready to merge into local `master` after the user confirms the final in-game checklist.

## Completion Criteria

- A first-time or legacy-config player receives a one-time-at-main-menu curated preset choice and a preserved legacy file backup.
- No settings are silently migrated from 1.3, and backup collisions never overwrite an older backup.
- All configuration values are represented by one validated schema-4 settings snapshot.
- GUI edits preview immediately but only Save writes; Cancel and Reset have the documented transactional behavior.
- The seven page configuration GUI remains readable and footer-safe at tested GUI scales.
- Selected presentation/accessibility settings affect the HUD and target ring consistently in real time.
- Unit tests plus `test build reobfJar` are green, and the user completes the listed in-game checkpoints.
