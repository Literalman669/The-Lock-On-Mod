# Lock On: rename and player-facing configuration screen

Date: 2026-10-11
Status: approved in conversation, awaiting written-spec review

## Goal

A new player can open the configuration screen, understand every option without
reading the README, and never meet an option that only matters for tuning or
debugging. The mod presents itself as **Lock On**, described by what it does,
with no Zelda or Breath of the Wild references.

## Decisions already made

| Question | Decision |
|---|---|
| Rename depth | Visible name only. The mod ID `zeldatargeting`, Java packages, class names, config file name, and key-binding IDs stay unchanged, so saves, key bindings, and `zeldatargeting.cfg` keep working. |
| Approach | Rework the existing `GuiTargetingConfig`. Keep `ConfigEditSession`, live preview, Save/Cancel, per-page reset, scrolling, and the reflection-based field binding. Forge's generated `GuiConfig` was rejected: it loses presets, live preview, and layout control. |
| Technical options | Move to a final **Advanced** page. |
| Explanations | Hover tooltips, placed and timed so they do not get in the way. |

## Non-goals

- No change to the settings file format, its keys, schema version, or validation ranges.
- No new gameplay settings. Three damage-number colors (`damageNumbersColor`,
  `criticalDamageColor`, `lethalDamageColor`) stay file-only, as today.
- No Java package or class renames.

## Part 1: rename

| Where | Before | After |
|---|---|---|
| `@Mod` name (`ZeldaTargetingMod.NAME`) | Zelda Targeting | Lock On |
| `mcmod.info` name | Zelda Targeting | Lock On |
| `mcmod.info` description | "A sophisticated lock-on targeting system inspired by The Legend of Zelda..." | "Lock onto mobs with a keypress. The camera follows your target, a ring and panel show its health and how many hits it will take, and you can switch between nearby targets. Works with Shoulder Surfing Reloaded and Epic Fight." |
| `mcmod.info` credits | Inspired by The Legend of Zelda series by Nintendo. Built with Minecraft Forge. | Built with Minecraft Forge. |
| Controls category (`key.categories.zeldatargeting`) | Zelda Targeting | Lock On |
| Config screen title | Zelda Targeting | Lock On |
| JAR (`archivesBaseName`, manifest titles) | `zelda-targeting-1.4.1.jar` | `lock-on-1.4.1.jar` |
| Lifecycle log lines | "Zelda Targeting Mod - Initialization" | "Lock On - Initialization" |
| Debug log prefixes | `[ZT Debug]`, `[ZT Mouse]`, `[ZT Aim]` | `[Lock On Debug]`, `[Lock On Mouse]`, `[Lock On Aim]` |
| Sound theme choice `zelda` | shown as ZELDA | shown as **Chime** (stored value stays `zelda`) |
| README and CHANGELOG | Zelda Targeting | Lock On (historical notes in `docs/` are left as written) |

Because the mod ID is unchanged, an old `zelda-targeting-*.jar` left beside the new
`lock-on-*.jar` makes Forge refuse to start with a duplicate-mod error. The
CHANGELOG and README tell players to remove the old JAR.

## Part 2: screen layout

```
              Lock On                                   (title)
 [Presets][Targeting][Camera][HUD & Ring][Combat Info][Sound][Accessibility][Advanced]
          Range, selection, and what can be targeted     (page description)
   - Section -
   [ setting button or slider ]
   ...  (scrolls when the page is taller than the window)
 [ < Prev ][ Cancel ][ Save ][ Reset Page ][ Next > ]   (footer)
```

- **Tabs** replace the "3 / 7" marker. The current page's tab is highlighted.
  If full names do not fit the window width, tabs use short names (Presets,
  Targeting, Camera, HUD, Combat, Sound, Access, Advanced). If those do not fit
  either, the tab row is replaced by a single centered "3 / 8 - Camera" line and
  the footer's Prev/Next do the navigation. Prev/Next stay in the footer at all
  widths.
- **Reset** becomes **Reset Page**. Its behavior is unchanged: the current page's
  settings return to the selected preset's values.
- **Numbers become sliders.** Drag to set; the value snaps to the step size and
  stops at the ends instead of wrapping around. The label shows the value with
  its unit (see the table). Right-click no longer changes values; it is not needed.
- **On/off and choice buttons** keep click-to-change (left click forward, right
  click back) and show friendly choice names.
- **Dependent options grey out** while the option they depend on is off. A greyed
  option's tooltip says which option to turn on.

## Part 3: pages and settings

Every field of `TargetingSettings` appears on exactly one page, except
`schemaVersion` (internal), `lockOnPreset` (set by the Presets page buttons), and
the three file-only damage-number colors.

Help text style: one or two plain sentences, second person, no internal terms.
The tooltip adds a grey last line: `Default: <value>` for on/off and choices, and
`Default: <value> - Range <min>-<max>` for sliders, using the same units as the label.

### 1. Presets ("Choose how lock-on feels")

| Button | Help |
|---|---|
| Cinematic | Slow, smooth camera that switches to third person when you lock on. Good for exploring and big fights. |
| Balanced | The default. A steady camera that keeps up with most fights. |
| Snappy | A fast camera that stays in your current view. Good for quick, close combat. |

Footer note under the buttons: "Picking a preset previews it. Save keeps it."

### 2. Targeting ("Range, selection, and what can be targeted")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| targetingRange | Lock-On Range | slider 1-64, step 1 | 16 blocks | How far away a mob can be when you press lock. |
| maxTrackingDistance | Keep Lock Up To | slider 1-128, step 1 | 20 blocks | Once locked, the lock breaks if the target gets further away than this. |
| maxAngle | Search Angle | slider 15-180, step 5 | 60° | How far from the center of your view a mob can be and still be picked. |
| requireLineOfSight | Need Clear View | toggle | ON | Only lock onto mobs you can actually see, not ones behind walls. |
| targetPriority | Pick Target By | choice | Nearest / Most Centered / Lowest Health / Biggest Threat | Which mob gets picked first when several are in range. |
| syncTargetingRangeWithReach | Match Range to Reach | toggle | OFF | Use your melee reach as the lock-on range instead of Lock-On Range. |
| disableLockOnWhenRiding | Turn Off While Riding | toggle | ON | Stop lock-on while you ride a horse, boat, or other mount. |
| quickSwitchOnKill | Switch Target on Kill | toggle | ON | When your target dies, lock onto the next one in range instead of letting go. |
| *section: What Can Be Targeted* | | | | |
| targetHostileMobs | Hostile Mobs | toggle | ON | Zombies, skeletons, creepers, and other monsters. |
| targetNeutralMobs | Neutral Mobs | toggle | ON | Mobs like wolves, endermen, and golems that are neither monsters nor farm animals. |
| targetPassiveMobs | Animals | toggle | ON | Pigs, cows, sheep, and other passive animals. |
| targetPlayers | Players | toggle | OFF | Other players. Usually off outside of PvP. |
| entityBlacklist | Never Target | text (existing editor) | count or summary | Mobs that are never picked. Click to edit the list. |

Dependencies: Lock-On Range greys out while Match Range to Reach is ON.

### 3. Camera ("How the camera follows your target")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| enableCameraLockOn | Camera Follows Target | toggle | ON | Turn your view toward the target while locked. Off keeps the camera fully under your mouse. |
| cameraSmoothness | Smoothness | slider 0.01-1.0, step 0.05 | 40% | Higher is slower and softer; lower snaps to the target faster. |
| autoThirdPerson | Third Person on Lock | toggle | OFF | Switch to third person when you lock on, and back when you let go. |
| recenterWithoutTarget | Level View Without Target | toggle | ON | When you press lock and nothing is in range, ease your view back to the horizon. |

Dependencies: Smoothness and Third Person on Lock grey out while Camera Follows Target is OFF.

### 4. HUD & Ring ("The target panel and the markers on your target")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| *section: Target Panel* | | | | |
| showTargetName | Show Name | toggle | ON | Show the target's name on the panel. |
| showHealthBar | Show Health Bar | toggle | ON | Show the target's health bar on the panel. |
| showDistance | Show Distance | toggle | ON | Show how far away the target is. |
| compactHudMode | Compact Panel | toggle | OFF | A smaller panel with just the name and health. |
| bossStylePanel | Boss Banner | toggle | ON | Show a wide banner at the top of the screen for bosses and mobs with 100 or more health. |
| hudAnchor | Panel Position | choice | Top Left / Top Right / Bottom Left / Bottom Right / Center | Which corner of the screen the panel sits in. |
| hudOffsetX | Nudge Sideways | slider -500-500, step 5 | 0 px | Move the panel left or right from its corner. |
| hudOffsetY | Nudge Up/Down | slider -500-500, step 5 | 0 px | Move the panel up or down from its corner. |
| hudScale | Panel Size | slider 0.75-1.5, step 0.05 | 100% | How large the panel is drawn. |
| hudOpacity | Panel Opacity | slider 0.35-1.0, step 0.05 | 100% | How see-through the panel is. |
| *section: Markers on Your Target* | | | | |
| showReticle | Show Target Markers | toggle | ON | The ring, corner brackets, and arrow drawn on the mob you are locked onto. |
| ringEnabled | Glowing Ring | toggle | ON | The ring around your target's feet. |
| ringHealthArcEnabled | Health Arc on Ring | toggle | ON | The ring shrinks as the target loses health. |
| ringThickness | Ring Thickness | slider 0.5-2.5, step 0.1 | 1.0x | How thick the ring is. |
| ringGlowStrength | Ring Glow | slider 0-1.5, step 0.1 | 100% | How strongly the ring glows. |
| reticleColor | Bracket Color | color (existing cycle) | White, Gold, ... | Color of the corner brackets around your target. |
| softAimIndicator | Aim Hint | toggle | OFF | A small marker beside your crosshair that leans toward your target when your view drifts off it. |
| targetHistoryEnabled | Recent Target Markers | toggle | OFF | Small markers on the last few mobs you were locked onto. |

Dependencies: everything under "Markers on Your Target" except the first toggle
greys out while Show Target Markers is OFF; Health Arc, Ring Thickness, and Ring
Glow also grey out while Glowing Ring is OFF. Nudge sliders, Panel Size, and
Panel Opacity do not depend on anything.

### 5. Combat Info ("Damage readouts and damage numbers")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| *section: On the Target Panel* | | | | |
| showDamagePrediction | Show Your Damage | toggle | ON | Show how much damage your next hit will do. |
| showHitsToKill | Show Hits to Kill | toggle | ON | Show how many hits it will take to defeat the target. |
| showVulnerabilities | Show Weak/Resist | toggle | ON | Tag targets that take extra or reduced damage. |
| highlightLethalTargets | One-Hit Warning | toggle | ON | Highlight the target when your next hit will defeat it. |
| *section: Damage Numbers* | | | | |
| enableDamageNumbers | Damage Numbers | toggle | ON | Numbers that float up from mobs when they take damage. |
| damageNumbersOwnHitsOnly | Only My Hits | toggle | OFF | Only show numbers for your own melee hits. |
| damageNumbersScale | Number Size | slider 0.5-3.0, step 0.1 | 1.0x | How large the numbers are. |
| damageNumbersDuration | Number Duration | slider 20-200, step 10 (ticks) | 3.0 s | How long each number stays on screen. |
| damageNumbersOffset | Number Height | slider 0-2, step 0.1 | 0.5 blocks | How far above the mob the numbers start. |
| damageNumbersMotion | Number Motion | choice | Normal / Subtle / Arcade | How much the numbers move as they float up. |
| damageNumbersColors | Colored Numbers | toggle | ON | Color critical and finishing hits differently. |
| damageNumbersCrits | Critical Hit Style | toggle | ON | Give critical-hit numbers their own color and animation. |
| critEmphasis | Critical Pop | toggle | ON | Critical-hit numbers pop in bigger when they appear. |
| damageNumbersFadeOut | Fade Out | toggle | ON | Numbers fade away instead of disappearing at once. |

Dependencies: every option under "Damage Numbers" except the first greys out
while Damage Numbers is OFF. Critical Pop also greys out while Critical Hit Style is OFF.

### 6. Sound ("Lock-on sounds")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| enableSounds | Sounds | toggle | ON | All lock-on sounds. |
| soundVolume | Volume | slider 0-1, step 0.1 | 100% | Overall volume of lock-on sounds. |
| soundTheme | Sound Style | choice | Default / Chime / Modern / Subtle / Cinematic | The set of sounds used. |
| enableSoundVariety | Vary Sounds | toggle | OFF | Rotate between a few sounds instead of repeating the same one. |
| *section: Events* | | | | |
| enableTargetLockSound | Lock-On Sound | toggle | ON | Plays when you lock onto a target. |
| targetLockVolume | Lock-On Volume | slider 0-1, step 0.1 | 70% | |
| enableTargetSwitchSound | Switch Sound | toggle | ON | Plays when you switch to another target. |
| targetSwitchVolume | Switch Volume | slider 0-1, step 0.1 | 40% | |
| enableLethalTargetSound | One-Hit Sound | toggle | ON | Plays instead of the lock-on sound when one hit will defeat the target. |
| lethalTargetVolume | One-Hit Volume | slider 0-1, step 0.1 | 80% | |
| enableTargetLostSound | Lock Lost Sound | toggle | ON | Plays when the lock ends. |
| targetLostVolume | Lock Lost Volume | slider 0-1, step 0.1 | 50% | |

Volume help (shared): "How loud this sound is, on top of Volume."
Dependencies: everything greys out while Sounds is OFF; each event's volume also
greys out while that event's toggle is OFF.

### 7. Accessibility ("Colors and motion")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| presentationPalette | Color Palette | choice | Default / Red-Green (Deuteranopia) / Red-Green (Protanopia) / Blue-Yellow (Tritanopia) | Health colors on the ring and panel, adjusted for color vision. |
| reducedMotion | Reduced Motion | toggle | OFF | Replace pulsing, popping, and shockwave effects with simple color flashes. |

### 8. Advanced ("Defaults are fine for most players")

| Field | Label | Control | Display | Help |
|---|---|---|---|---|
| *section: Camera Tuning* | | | | |
| maxYawAdjustment | Max Turn Sideways | slider 0-180, step 5 | 90° | The furthest the camera turns left or right toward a target in one go. |
| maxPitchAdjustment | Max Turn Up/Down | slider 0-90, step 5 | 60° | The furthest the camera tilts up or down toward a target. |
| cameraFocusYOffset | Aim Height | slider -1-1, step 0.05 | +0.00 | Aim higher (positive) or lower (negative) on the target's body. |
| perModeSmoothingEnabled | Softer in First Person | toggle | OFF | Use a slower camera in first person than in third person. |
| *section: Display Tuning* | | | | |
| reticleScale | Marker Size | slider 0.5-3.0, step 0.1 | 1.0x | How large the markers around your target are drawn. |
| damagePredictionScale | Readout Text Size | slider 0.5-2.0, step 0.1 | 1.0x | Text size of the damage and hits-to-kill readouts. |
| *section: Sound Pitch* | | | | |
| targetLockPitch | Lock-On Pitch | slider 0.5-2.0, step 0.1 | 1.2x | |
| targetSwitchPitch | Switch Pitch | slider 0.5-2.0, step 0.1 | 1.0x | |
| lethalTargetPitch | One-Hit Pitch | slider 0.5-2.0, step 0.1 | 1.5x | |
| targetLostPitch | Lock Lost Pitch | slider 0.5-2.0, step 0.1 | 0.8x | |
| *section: Performance* | | | | |
| updateFrequency | Target Check Rate | slider 1-20, step 1 (ticks) | every 1 tick | How often the lock re-checks range and line of sight. Higher saves work but reacts slower. |
| validationInterval | Filter Check Rate | slider 5-60, step 5 (ticks) | every 10 ticks | How often the lock re-checks whether the target still passes your filters. |
| *section: Compatibility* | | | | |
| ssrCompensationEnabled | Shoulder Surfing / Epic Fight Aim | toggle | ON | Line up your attacks with Shoulder Surfing Reloaded's camera and Epic Fight's combat. Leave on unless those mods misbehave. |
| debugCompatibility | Debug Logging | toggle | OFF | Write detailed aim and camera information to the game log, for bug reports. |

Pitch help (shared): "How high this sound plays."
Dependencies: each sound pitch greys out while its event sound is OFF or Sounds is OFF.

## Part 4: tooltips

**Timing** (state machine in a pure class, `ConfigTooltipTimer`):
- A tooltip appears after the mouse rests on the same setting for 600 ms.
- It hides at once when the mouse leaves the setting, the page scrolls, or the
  setting is clicked or dragged.
- After a click or drag, the tooltip for that setting stays hidden until the
  mouse leaves it and returns.
- Tabs and footer buttons never show tooltips.

**Placement** (pure class, `ConfigTooltipPlacement`):
- Input: screen size, the hovered button's rectangle, the settings column's left
  and right edges, the footer top, and the tooltip's size.
- Preferred: to the right of the settings column, top aligned with the hovered
  button, with an 8 px gap; otherwise to the left of the column.
- When neither side has room for the tooltip at its maximum width (200 px,
  shrinking to a 120 px minimum), it goes directly below the hovered button,
  or above it when below would reach the footer.
- It is always clamped inside the screen and never overlaps the hovered button
  or the footer.

**Content**: help sentences wrapped to the tooltip width, then a grey
`Default: ...` line. A greyed-out setting shows "Turn on <option> to change this."
instead of its help text.

## Part 5: structure

The screen keeps `GuiTargetingConfig` as the Minecraft-facing class. Data and
logic move into small, testable pieces in `client/gui`:

| Unit | Purpose |
|---|---|
| `ConfigPage` (updated) | The eight pages, their titles, short tab names, and descriptions. |
| `ConfigLayout` (new) | Declares each page's sections and settings in order: field, label, control kind, range, step, unit, choice names, help text, and dependency. Pure data, no Minecraft types. |
| `ConfigValueFormat` (new) | Turns a stored value into display text with its unit (blocks, °, %, x, px, s, ticks) and builds the `Default: ...` line. |
| `ConfigTooltipTimer` (new) | The tooltip timing state machine. |
| `ConfigTooltipPlacement` (new) | The tooltip placement math. |
| `ConfigTabLayout` (new) | Chooses full names, short names, or the single-line fallback for the tab row at a given width. |
| `ConfigSlider` (new) | A `GuiButton` subclass that draws the track and handle and maps drag position to a snapped, clamped value. |

`GuiTargetingConfig` reads `ConfigLayout` to build each page instead of its
current hard-coded switch statement. Reading and writing settings still goes
through the existing reflection on `TargetingSettings` and the `ConfigEditSession`.

## Part 6: testing

New unit tests, in the existing JUnit style:
- **Coverage:** every `TargetingSettings` field appears on exactly one page,
  except the documented exclusions; every layout entry names a real field of the
  right type; every slider's range matches `TargetingSettingsValidator`.
- **Dependencies:** each dependency names a boolean field that exists.
- `ConfigValueFormat`: units, rounding, the ticks-to-seconds display, and default lines.
- `ConfigTooltipTimer`: rest delay, hide on leave, scroll, and click, and suppression until re-entry.
- `ConfigTooltipPlacement`: right side, left side, below, and above cases, small
  screens, and the no-overlap guarantee with the button and footer.
- `ConfigTabLayout`: full, short, and fallback choices at different widths.
- Existing tests that name pages (`ConfigPageTest`, `ConfigPageNavigatorTest`)
  are updated for eight pages. All existing settings, store, and session tests
  must keep passing unchanged.

Manual check in the test pack: open each page at GUI scales Normal and Large,
hover settings near each screen edge, drag every slider to both ends, toggle each
dependency parent, Save, Cancel, and Reset Page.
