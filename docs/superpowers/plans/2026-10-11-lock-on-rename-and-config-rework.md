# Lock On Rename and Config Screen Rework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Present the mod as "Lock On" and replace the configuration screen's pages, labels, and controls with a player-facing eight-page layout with sliders, dependency greying, tabs, and unobtrusive tooltips.

**Architecture:** Pure, Minecraft-free classes in `client/gui` describe the layout (`ConfigLayout`), format values (`ConfigValueFormat`), time and place tooltips (`ConfigTooltipTimer`, `ConfigTooltipPlacement`), lay out tabs (`ConfigTabLayout`), and map slider positions (`ConfigSliderMath`). `GuiTargetingConfig` stays the Minecraft screen and keeps its `ConfigEditSession`, preview, Save/Cancel, per-page reset, and scrolling; it builds pages from `ConfigLayout` instead of its hard-coded switch.

**Tech Stack:** Java 8, Minecraft Forge 1.12.2 (14.23.5.2859), ForgeGradle 3, JUnit 4.13.2.

**Spec:** `docs/superpowers/specs/2026-10-11-lock-on-rename-and-config-rework-design.md`

## Global Constraints

- Version stays `1.4.1`.
- Mod ID `zeldatargeting`, Java packages, class names, config file name, settings keys, and key-binding IDs do not change.
- No change to `TargetingSettings` fields, `TargetingSettingsStore`, or `TargetingSettingsValidator`.
- All labels, units, help text, choice names, sections, and dependencies come verbatim from spec Part 3.
- Pure classes in `client/gui` added by this plan import no `net.minecraft`, `net.minecraftforge`, or `org.lwjgl` types.
- Build and test from the Bash tool: `JAVA_HOME="C:/Program Files/Java/jdk1.8.0_202" ./gradlew clean test build --no-daemon --console=plain`. Edited files keep CRLF line endings, like the rest of the checkout.
- Commit messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

- A large GUI scale or small window (scaled width 320, height 240): tabs fall back to the single line, and tooltips still avoid the hovered button and the footer. Pinned in Tasks 5 and 6.
- A saved value off the slider's step or below its minimum (for example `validationInterval=1` with a 5-60 slider): opening the screen must not rewrite it; only dragging changes it. Pinned in Task 7 (math) and Task 9 (GUI writes only on drag).
- A saved choice value not in the choice list: the button shows the raw value instead of failing, and cycling starts from the first choice. Pinned in Task 3.
- Reset Page while some options are greyed out: greyed options on the page are reset too. Pinned in Task 9 (page fields come from the layout, not from enabled buttons).
- Changing page or scrolling while a tooltip shows: it hides, and the next one waits the full delay. Pinned in Task 4.

---

### Task 1: Visible rename to Lock On

**Files:**
- Modify: `src/main/java/com/zeldatargeting/mod/ZeldaTargetingMod.java` (`NAME`, three lifecycle log lines)
- Modify: `src/main/resources/mcmod.info` (name, description, credits)
- Modify: `src/main/resources/assets/zeldatargeting/lang/en_us.lang` (`key.categories.zeldatargeting`)
- Modify: `build.gradle` (`archivesBaseName = 'lock-on'`, manifest `Specification-Title` = `lock-on`)
- Modify: `src/main/java/com/zeldatargeting/mod/client/TargetingManager.java` (`[ZT Debug]` prefix)
- Modify: `src/main/java/com/zeldatargeting/mod/client/diagnostics/MouseTurnDiagnostics.java` (`PREFIX`)
- Modify: `src/main/java/com/zeldatargeting/mod/client/diagnostics/AttackAimDiagnostics.java` (`PREFIX`)
- Modify: `README.md`, `CHANGELOG.md`
- Test: `src/test/java/com/zeldatargeting/mod/ModIdentityTest.java`

**Interfaces:**
- Produces: `ZeldaTargetingMod.NAME == "Lock On"` (used as the config screen title in Task 9).

- [ ] **Step 1: Write the failing test** `ModIdentityTest`:
  - `modNameIsLockOn`: `assertEquals("Lock On", ZeldaTargetingMod.NAME)`.
  - `modInfoDescribesTheModWithoutZelda`: read `/mcmod.info` from the classpath as UTF-8; assert it contains `"name": "Lock On"` and the spec's description sentence verbatim, and that it contains neither `Zelda` nor `Breath of the Wild`.
  - `controlsCategoryIsLockOn`: read `/assets/zeldatargeting/lang/en_us.lang`; assert it contains the line `key.categories.zeldatargeting=Lock On`.

- [ ] **Step 2: Run** `./gradlew test --tests com.zeldatargeting.mod.ModIdentityTest` (with JAVA_HOME as in Global Constraints). Expected: 3 failures.

- [ ] **Step 3: Apply the rename table from spec Part 1.** Lifecycle logs become `"Lock On - Pre-Initialization"`, `"Lock On - Initialization"`, `"Lock On - Post-Initialization"`. Prefixes become `[Lock On Debug] `, `[Lock On Mouse] `, `[Lock On Aim] `. README: title `# Lock On`, every "Zelda Targeting" becomes "Lock On", the Installation/Requirements area gains: "Updating from Zelda Targeting: delete the old `zelda-targeting-*.jar` from your mods folder. Both share one mod ID, and Forge will not start with both installed. Your settings and key bindings carry over." CHANGELOG 1.4.1 gains a `### Changed` entry: "The mod is now called Lock On. Its settings, key bindings, and config file are unchanged; delete the old zelda-targeting JAR when updating." Historical files under `docs/` are not edited.

- [ ] **Step 4: Run the full build** (Global Constraints command). Expected: BUILD SUCCESSFUL, all tests pass, `build/libs/lock-on-1.4.1.jar` exists and `build/libs/zelda-targeting-1.4.1.jar` does not.

- [ ] **Step 5: Commit** `feat: present the mod as Lock On`.

---

### Task 2: Eight configuration pages

**Files:**
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPage.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigPageNavigator.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigPageTest.java`, `ConfigPageNavigatorTest.java`

**Interfaces:**
- Produces: `enum ConfigPage { PRESETS, TARGETING, CAMERA, HUD, COMBAT, SOUND, ACCESSIBILITY, ADVANCED }` with `String getTitle()`, `String getShortTitle()`, `String getDescription()`.
- Produces: `void ConfigPageNavigator.requestPage(int index)` (clamped to `0..totalPages-1`, applied by the existing `applyPendingPage()`).

- [ ] **Step 1: Update tests.** `ConfigPageTest`: assert the eight constants in the order above. Add `titlesMatchTheSpec`: titles `Presets, Targeting, Camera, HUD & Ring, Combat Info, Sound, Accessibility, Advanced`; short titles `Presets, Targeting, Camera, HUD, Combat, Sound, Access, Advanced`; descriptions `Choose how lock-on feels`, `Range, selection, and what can be targeted`, `How the camera follows your target`, `The target panel and the markers on your target`, `Damage readouts and damage numbers`, `Lock-on sounds`, `Colors and motion`, `Defaults are fine for most players`. `ConfigPageNavigatorTest`: keep the existing test; add `requestPageJumpsOnApplyAndClamps` (8 pages: `requestPage(5)` then `applyPendingPage()` gives 5; `requestPage(99)` gives 7; `requestPage(-3)` gives 0).

- [ ] **Step 2: Run** those two test classes. Expected: FAIL (missing constants and method).

- [ ] **Step 3: Implement.** Add the `shortTitle` constructor argument; add `requestPage`. Remove `AUDIO` and `COMPATIBILITY`. `GuiTargetingConfig` will not compile until Task 9 rewrites `addPageControls`; in this task, make its switch compile by giving `SOUND` the old AUDIO controls, `ADVANCED` the old COMPATIBILITY controls, and `COMBAT` no controls, so the screen still opens between tasks.

- [ ] **Step 4: Run the full build.** Expected: all tests pass.

- [ ] **Step 5: Commit** `feat: eight configuration pages with tab titles`.

---

### Task 3: Value formatting and the layout table

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigUnit.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigValueFormat.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigEntry.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigLayout.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigValueFormatTest.java`, `ConfigLayoutTest.java`

**Interfaces:**
- Produces: `enum ConfigUnit { NONE, BLOCKS, DEGREES, PERCENT, MULTIPLIER, PIXELS, TICKS_AS_SECONDS, TICK_INTERVAL, SIGNED }`.
- Produces: `ConfigValueFormat.format(double value, ConfigUnit unit) -> String`; `ConfigValueFormat.defaultLine(String defaultText) -> String` returning `"Default: " + defaultText`; `ConfigValueFormat.sliderDefaultLine(double def, double min, double max, ConfigUnit unit) -> String` returning `"Default: <def> - Range: <min> to <max>"` (each formatted with the unit).
- Produces: `enum ConfigEntry.Kind { PRESET, TOGGLE, SLIDER, CHOICE, COLOR, TEXT }`; `final class ConfigEntry` with getters `getKind()`, `getFieldName()` (null for PRESET), `getPresetId()` (PRESET only), `getLabel()`, `getHelp()`, `getMin()`, `getMax()`, `getStep()`, `getUnit()`, `getChoiceValues()`, `getChoiceLabels()`, `getTextHints()`, `getDependencies()` (`List<ConfigEntry.Dependency>`), and `String choiceLabel(String storedValue)`. `ConfigEntry.Dependency` has `getFieldName()` and `isRequiredValue()` (`true`: needs that toggle ON; `false`: needs it OFF).
- Produces: `ConfigLayout.page(ConfigPage) -> List<ConfigLayout.Item>`; `ConfigLayout.Item` has `isSection()`, `getSectionTitle()`, `getEntry()`; `ConfigLayout.labelOf(String fieldName) -> String`; `ConfigLayout.EXCLUDED_FIELDS` = `schemaVersion, lockOnPreset, damageNumbersColor, criticalDamageColor, lethalDamageColor`.

- [ ] **Step 1: Write `ConfigValueFormatTest`:** `16, BLOCKS -> "16 blocks"`; `1, BLOCKS -> "1 block"`; `0.5, BLOCKS -> "0.5 blocks"`; `60, DEGREES -> "60°"`; `0.4, PERCENT -> "40%"`; `1.0, PERCENT -> "100%"`; `1.2, MULTIPLIER -> "1.2x"`; `1.0, MULTIPLIER -> "1.0x"`; `-5, PIXELS -> "-5 px"`; `60, TICKS_AS_SECONDS -> "3.0 s"`; `1, TICK_INTERVAL -> "every 1 tick"`; `10, TICK_INTERVAL -> "every 10 ticks"`; `0, SIGNED -> "+0.00"`; `-0.25, SIGNED -> "-0.25"`; `sliderDefaultLine(16, 1, 64, BLOCKS) -> "Default: 16 blocks - Range: 1 block to 64 blocks"`; `defaultLine("ON") -> "Default: ON"`.

- [ ] **Step 2: Write `ConfigLayoutTest`:**
  - `everySettingIsOnExactlyOnePage`: for each public field of `TargetingSettings` not in `EXCLUDED_FIELDS`, count entries with that field name across all pages; assert exactly 1. Also assert no entry names a field outside `TargetingSettings`.
  - `entryKindsMatchFieldTypes`: TOGGLE needs `boolean`; SLIDER `double`, `float`, or `int`; CHOICE and TEXT `String`; COLOR `int`.
  - `slidersStayInsideValidatorRanges`: for each SLIDER, set the field of a fresh `TargetingSettings` to `min`, run `TargetingSettingsValidator.sanitize`, and assert the value is unchanged; repeat for `max`.
  - `choicesMatchStoredValues`: for each CHOICE, `getChoiceValues()` equals the current `GuiTargetingConfig` choice lists (`targetPriority`: nearest, angle, health, threat; `hudAnchor`: top-left, top-right, bottom-left, bottom-right, center; `soundTheme`: default, zelda, modern, subtle, cinematic; `damageNumbersMotion`: default, subtle, arcade; `presentationPalette`: default, deuteranopia, protanopia, tritanopia), and labels have the same length.
  - `dependenciesNameBooleanFields`: every dependency field is a `boolean` field of `TargetingSettings` that is itself on a page.
  - `spotChecksFromTheSpec`: `targetingRange` is labelled "Lock-On Range", BLOCKS, 1-64 step 1, depends on `syncTargetingRangeWithReach` with required value `false`; `soundTheme`'s label for `zelda` is "Chime"; `softAimIndicator` is labelled "Aim Hint" on HUD; `debugCompatibility` is labelled "Debug Logging" on ADVANCED; `critEmphasis` depends on both `enableDamageNumbers` and `damageNumbersCrits` (required `true`); PRESETS holds three PRESET entries with ids cinematic, balanced, snappy.
  - `unknownChoiceValueShowsRawValue`: `choiceLabel("legacy-theme")` on `soundTheme` returns `"legacy-theme"`.

- [ ] **Step 3: Run** both test classes. Expected: compile failure (classes missing).

- [ ] **Step 4: Implement** the four classes. `ConfigLayout` holds spec Part 3 as data, in table order, with section titles as Items. Use these units: Lock-On Range and Keep Lock Up To BLOCKS; Search Angle, Max Turn Sideways, and Max Turn Up/Down DEGREES; Smoothness, Ring Glow, Panel Size, Panel Opacity, and all volumes PERCENT; Ring Thickness, Number Size, Marker Size, Readout Text Size, and all pitches MULTIPLIER; Nudge sliders PIXELS; Number Duration TICKS_AS_SECONDS; Number Height BLOCKS; Aim Height SIGNED; both check rates TICK_INTERVAL. Ranges and steps are the spec's. Volume entries use the shared help "How loud this sound is, on top of Volume."; pitch entries use "How high this sound plays."; the blacklist keeps its two current hint lines as text hints.

- [ ] **Step 5: Run** both test classes, then the full build. Expected: all pass.

- [ ] **Step 6: Commit** `feat: player-facing config layout, labels, units, and help text`.

---

### Task 4: Tooltip timing

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigTooltipTimer.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigTooltipTimerTest.java`

**Interfaces:**
- Produces: `ConfigTooltipTimer` with `static final long REST_DELAY_MILLIS = 600L`, `void hover(Object target, long nowMillis)` (null means nothing hovered), `void interact()` (click, drag, scroll, or page change), `boolean isVisible(long nowMillis)`, and `Object getTarget()`.

- [ ] **Step 1: Write tests:** `showsAfterResting` (hover A at 0; not visible at 599; visible at 600); `movingToAnotherTargetRestartsTheDelay` (A at 0, B at 500, not visible at 1000, visible at 1100); `leavingHidesAtOnce` (visible, then `hover(null, t)`, not visible); `interactionHidesAndSuppressesUntilLeave` (visible A, `interact()`, still hovering A at +5000: not visible; `hover(null)` then A again: visible after 600 more); `interactionWhileNothingHoveredIsHarmless`; `pageChangeWaitsTheFullDelay` (visible A, `interact()`, `hover(B)`: B is not suppressed but needs the full 600 from its own start).

- [ ] **Step 2: Run** the test class. Expected: compile failure.

- [ ] **Step 3: Implement.** Track the current target, when it was entered, and a suppressed target that is cleared when the hovered target changes. Compare targets by identity.

- [ ] **Step 4: Run** the test class. Expected: PASS.

- [ ] **Step 5: Commit** `feat: delayed, click-aware tooltip timing`.

---

### Task 5: Tooltip placement

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigRect.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigTooltipPlacement.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigTooltipPlacementTest.java`

**Interfaces:**
- Produces: `final class ConfigRect(int x, int y, int width, int height)` with getters, `getRight()`, `getBottom()`, and `boolean intersects(ConfigRect other)`.
- Produces: `ConfigTooltipPlacement.place(int screenWidth, int footerTop, ConfigRect hovered, int columnLeft, int columnRight, IntUnaryOperator heightForWidth) -> ConfigRect`, with constants `GAP = 8`, `MAX_WIDTH = 200`, `MIN_WIDTH = 120`, `MARGIN = 4`.

- [ ] **Step 1: Write tests,** each asserting the result does not intersect `hovered`, its bottom is at most `footerTop - MARGIN`, and it lies inside `[MARGIN, screenWidth - MARGIN]`:
  - `prefersTheRightOfTheColumn`: screen 854, column 217-637, hovered (217,100,420,20), height 30 for any width: x = 645, width 200, y = 100.
  - `fallsBackToTheLeft`: column 330-750 on an 854 screen (right space 92): result right edge = 322.
  - `narrowSideSpaceShrinksToMinimum`: right space exactly 120: width 120 on the right.
  - `goesBelowWithoutSideRoom`: screen 320, column 24-296, hovered (24,80,272,20), footerTop 210: placed at y = 104, horizontally centered on the hovered button and clamped.
  - `goesAboveWhenBelowWouldReachTheFooter`: same screen, hovered at y 180: bottom = 176.
  - `sideTooltipNearTheFooterIsPushedUp`: right side, hovered y 190, height 40, footerTop 210: bottom = 206.

- [ ] **Step 2: Run** the test class. Expected: compile failure.

- [ ] **Step 3: Implement** the order right, left, below, above from spec Part 4. Side width = `min(MAX_WIDTH, available)` and is used only when `available >= MIN_WIDTH`. Below and above use width `min(MAX_WIDTH, screenWidth - 2 * MARGIN)` with a 4 px gap from the button. Side placements clamp y into `[MARGIN, footerTop - MARGIN - height]`.

- [ ] **Step 4: Run** the test class. Expected: PASS.

- [ ] **Step 5: Commit** `feat: tooltip placement that avoids the hovered setting and footer`.

---

### Task 6: Tab row layout

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigTabLayout.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigTabLayoutTest.java`

**Interfaces:**
- Consumes: `ConfigRect` (Task 5).
- Produces: `ConfigTabLayout.layout(int screenWidth, int y, List<String> fullNames, List<String> shortNames, ToIntFunction<String> textWidth) -> ConfigTabLayout.Result`; `Result.getMode()` returns `enum Mode { FULL, SHORT, SINGLE_LINE }`; `Result.getTabs()` returns `List<ConfigRect>` (empty for SINGLE_LINE). Constants: `PADDING = 6` per side, `GAP = 2`, `MARGIN = 8`, `HEIGHT = 14`. The row is centered.

- [ ] **Step 1: Write tests** using a `textWidth` of `6 * length`: `fullNamesWhenTheyFit` (width 854: FULL, 8 tabs, non-overlapping, centered within 1 px); `shortNamesWhenFullDoNotFit` (width 480: SHORT); `singleLineWhenNothingFits` (width 320: SINGLE_LINE, no tabs).

- [ ] **Step 2: Run.** Expected: compile failure.

- [ ] **Step 3: Implement.** Total width = sum of `(textWidth + 2 * PADDING)` + `GAP * (n - 1)`; it fits when total <= `screenWidth - 2 * MARGIN`.

- [ ] **Step 4: Run.** Expected: PASS.

- [ ] **Step 5: Commit** `feat: tab row that degrades to short names or a single line`.

---

### Task 7: Slider math and slider widget

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigSliderMath.java`
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigSlider.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigSliderMathTest.java`

**Interfaces:**
- Produces: `ConfigSliderMath.valueAt(double fraction, double min, double max, double step) -> double` (fraction clamped to [0,1], snapped to `min + k * step`, clamped to [min,max], rounded to 3 decimals, like `cycleDouble` today) and `ConfigSliderMath.fractionOf(double value, double min, double max) -> double` (clamped to [0,1]).
- Produces: `ConfigSlider extends GuiButton`, constructed as `ConfigSlider(int id, int x, int y, int width, ConfigEntry entry, double initialValue, DoubleConsumer onDrag, Function<Double, String> label)`, with `double getValue()`, `boolean isDragging()`, and `void setDisplayedValue(double value)` (redraws without calling `onDrag`).

- [ ] **Step 1: Write `ConfigSliderMathTest`:** `ends` (`valueAt(0,1,64,1)=1`, `valueAt(1,1,64,1)=64`); `snapsToStep` (`valueAt(0.5,0.01,1.0,0.05)` is 0.51; `valueAt(0.33,0,1,0.1)` is 0.3); `clampsOutsideFraction` (`valueAt(-1,...)=min`, `valueAt(2,...)=max`); `neverOvershootsMax` (`valueAt(1,5,60,5)=60`); `fractionClampsBelowMin` (`fractionOf(1,5,60)=0`); `offStepValueIsNotRewrittenByFraction`: `fractionOf(0.37,0.01,1.0)` is between 0.35 and 0.37 (display only; Task 9 never writes it back without a drag).

- [ ] **Step 2: Run.** Expected: compile failure.

- [ ] **Step 3: Implement `ConfigSliderMath`.**

- [ ] **Step 4: Implement `ConfigSlider`.** Draw like `ConfigGuiButton` (shaded rectangles, same colors), plus an 8 px handle at `x + fractionOf(value) * (width - 8)`, and the label centered. `mousePressed` (when enabled and visible) starts dragging and applies the position; `mouseDragged` (called from `drawButton`) applies the position while dragging; `mouseReleased` stops. Applying a position computes `valueAt`, and calls `onDrag` only when the value changed. No click sound on drag updates.

- [ ] **Step 5: Run the full build.** Expected: all tests pass.

- [ ] **Step 6: Commit** `feat: snapping slider control for numeric settings`.

---

### Task 8: Dependency evaluation

**Files:**
- Create: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigDependencies.java`
- Test: `src/test/java/com/zeldatargeting/mod/client/gui/ConfigDependenciesTest.java`

**Interfaces:**
- Consumes: `ConfigEntry`, `ConfigLayout.labelOf` (Task 3).
- Produces: `ConfigDependencies.blockingDependency(ConfigEntry entry, TargetingSettings settings) -> ConfigEntry.Dependency` (the first unmet dependency, or null) and `ConfigDependencies.blockedMessage(ConfigEntry.Dependency) -> String` (`"Turn on <label> to change this."` or `"Turn off <label> to change this."`).

- [ ] **Step 1: Write tests:** `ringOptionsBlockedWhenMarkersOff` (`showReticle=false` blocks `ringThickness`, message "Turn on Show Target Markers to change this."); `rangeBlockedWhileMatchingReach` (`syncTargetingRangeWithReach=true` blocks `targetingRange`, message "Turn off Match Range to Reach to change this."); `nothingBlockedAtDefaults` (with `new TargetingSettings()`, no entry on any page is blocked); `firstUnmetDependencyWins` (`enableSounds=false` and `enableTargetLockSound=false`: `targetLockVolume` reports Sounds first).

- [ ] **Step 2: Run.** Expected: compile failure.

- [ ] **Step 3: Implement** with reflection on `TargetingSettings` fields, in the dependency order declared in `ConfigLayout`.

- [ ] **Step 4: Run.** Expected: PASS.

- [ ] **Step 5: Commit** `feat: dependency rules for greying out config options`.

---

### Task 9: Rebuild the configuration screen

**Files:**
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java`
- Modify: `src/main/java/com/zeldatargeting/mod/client/gui/ConfigGuiButton.java` (selected-tab highlight)

**Interfaces:**
- Consumes: everything from Tasks 2-8, plus `ZeldaTargetingMod.NAME`.

- [ ] **Step 1: Build pages from `ConfigLayout`.** Delete `ControlSpec` and the hard-coded switch. For each Item: sections render as today; TOGGLE, CHOICE, COLOR, and TEXT use `ConfigGuiButton`; SLIDER uses `ConfigSlider`; PRESET uses `ConfigGuiButton` with the preset name, followed by its help drawn as a grey line under the button (Presets page only). Displays: toggles `Label: §aON` or `§cOFF`; choices `Label: <choiceLabel>`; sliders `Label: <ConfigValueFormat.format>`; color and text as today. Left click on a toggle or choice moves forward, right click moves back; sliders ignore right click. A slider's `onDrag` writes the field (via the existing reflection, cast for double, float, and int), calls `TargetingConfig.previewSettings`, and refreshes greying. Nothing writes a field on page build.

- [ ] **Step 2: Apply greying.** After every change and on build, set `enabled = blockingDependency(...) == null` for every control. Reset Page collects field names from `ConfigLayout.page(current)` entries (not from enabled buttons) and calls the existing `resetFields`.

- [ ] **Step 3: Header and navigation.** Title `ZeldaTargetingMod.NAME` at y 8. Tabs from `ConfigTabLayout.layout(width, 22, ...)`; clicking a tab calls `requestPage(i)` plus the existing rebuild request. The current tab draws highlighted (add a `selected` flag to `ConfigGuiButton`, drawn as a gold `0xFFFFAA00` border). In SINGLE_LINE mode draw `"<n> / 8 - <title>"` centered at y 24. The page description sits at y 40, and content starts at 54. Footer: `< Prev`, `Cancel`, `Save`, `Reset Page`, `Next >`. The old "n / 7" marker is removed.

- [ ] **Step 4: Tooltips.** Each frame in `drawScreen`, find the visible control (enabled or not) under the mouse, excluding tabs and footer buttons, and call `timer.hover(entryOrNull, now)` using `System.nanoTime() / 1_000_000L`. Call `timer.interact()` on any mouse click, slider drag, scroll, page change, and rebuild. When visible: the lines are `listFormattedStringToWidth(help or blockedMessage, width - 8)` plus a grey default line (toggle `defaultLine("ON"/"OFF")`, choice `defaultLine(choiceLabel)`, slider `sliderDefaultLine`, color `defaultLine(ReticleColor.describe(default))`, text `defaultLine("none")`, preset none). Defaults come from `TargetingPreset.fromId(workingCopy.lockOnPreset).createSettings()`. Place with `ConfigTooltipPlacement.place(width, footerY, buttonRect, columnLeft, columnRight, w -> lines(w).size() * 10 + 8)`. Draw after `super.drawScreen` with the vanilla tooltip look (`drawGradientRect` background `0xF0100010`, border `0x505000FF` to `0x5028007F`), at z 300.

- [ ] **Step 5: Run the full build.** Expected: BUILD SUCCESSFUL and all tests pass.

- [ ] **Step 6: Commit** `feat: rebuilt config screen with tabs, sliders, greying, and tooltips`.

---

### Task 10: Docs, packaging, and in-game check

**Files:**
- Modify: `README.md` (Configuration section), `CHANGELOG.md`

- [ ] **Step 1: Docs.** In the README Configuration section, list the eight pages and their purpose, and mention tooltips and Reset Page; rename settings mentioned elsewhere in the README to their new labels (for example "Disable While Riding" becomes "Turn Off While Riding"). Add a CHANGELOG 1.4.1 `### Changed` entry: "The config screen has eight pages with tabs, plain-language labels, sliders with units, hover help, and options that grey out when they do not apply. Tuning and debug options moved to an Advanced page."

- [ ] **Step 2: Full build.** Expected: BUILD SUCCESSFUL, all tests pass, `build/libs/lock-on-1.4.1.jar`.

- [ ] **Step 3: Install in the test pack** `C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\Zelda Targeting SSR 1.12.2 Test\mods` only while no `javaw` process is running: copy `lock-on-1.4.1.jar` in and remove `zelda-targeting-1.4.1.jar` (our own build output; both share one mod ID).

- [ ] **Step 4: Commit** `docs: describe the new config screen`.

- [ ] **Step 5: Hand off for the manual check** from spec Part 6: every page at GUI scales Normal and Large, hovering settings near each screen edge, dragging every slider to both ends, toggling each dependency parent, then Save, Cancel, and Reset Page.
