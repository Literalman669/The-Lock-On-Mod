# Lock On

Lock On is a client-only lock-on targeting mod for Minecraft 1.12.2. It combines responsive target selection, a configurable camera, and clear combat feedback.

## 1.4.0 highlights

- Rebuilt targeting state and camera flow with safer lock restoration.
- Adventure Crest HUD with a compact target plaque and a distinct boss banner.
- A tubular, neon target ring whose health arc shrinks with the target's remaining health.
- Shared green, orange, and red health states across the target ring and HUD.
- Preset-first, seven-page configuration flow with transactional Save, Cancel, and Reset actions.
- Optional Shoulder Surfing Reloaded 2.9.x and Epic Fight 2.2.8 integration, including when both are installed.
- Accessibility controls for palette, opacity, compact HUD, soft indicators, and reduced motion.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2859 or a compatible 1.12.2 Forge build

The mod is client-only. Install it on the client; a server-side installation is not required.

Updating from Zelda Targeting: delete the old `zelda-targeting-*.jar` from your mods folder. Both share one mod ID, and Forge will not start with both installed. Your settings and key bindings carry over.

## Controls

The default key bindings are available from Minecraft's Controls menu under Lock On:

- **R** acquires or releases a target.
- **Z** and **X** cycle between eligible targets.
- **Left Alt** temporarily enables free look.
- **V** moves the lock between the target's head, body, and feet.

## Configuration

Open **Mods**, select **Lock On**, then choose **Config**.

The configuration has eight pages, reached from the tabs along the top:

1. **Presets**: Cinematic, Balanced, or Snappy, each described on the page.
2. **Targeting**: lock-on range, how targets are picked, and what can be targeted.
3. **Camera**: how the camera follows your target.
4. **HUD & Ring**: the target panel and the markers on your target.
5. **Combat Info**: damage readouts and damage numbers.
6. **Sound**: lock-on sounds and their volumes.
7. **Accessibility**: color palette and Reduced Motion.
8. **Advanced**: camera tuning, sound pitch, check rates, and compatibility options. The defaults are fine for most players.

Rest the mouse on any setting for a moment to see what it does and its default. Numbers are sliders: drag them to set a value. Options that do not apply right now are greyed out, and their tip says which option to turn on. Every change previews immediately; choose **Save** to keep it, **Cancel** to discard it, or **Reset Page** to return the current page to the selected preset.

### Choosing targets

- **Never Target** (Targeting page) lists entities that are never targeted, separated by commas. `minecraft:iron_golem` blocks one mob, `iceandfire:` blocks every mob from that mod, and a plain word such as `golem` blocks any mob whose name contains it.
- **Turn Off While Riding** (on by default) blocks lock-on while you ride a horse, boat, or other mount, and releases an existing lock when you mount.
- **Switch Target on Kill** (on by default) moves the lock to the next eligible target when the current one dies.
- **Level View Without Target** (Camera page, on by default) eases your view back to the horizon when you press lock and nothing is in range.
- **Match Range to Reach** (off by default) replaces the targeting range with your melee reach: 3 blocks in survival, 6 in creative, plus any extra reach other mods grant.
- **Target Check Rate** and **Filter Check Rate** (Advanced page) set how often, in ticks, a lock re-checks line of sight and range, and whether the target still passes your filters. Higher values save work but react more slowly.

**Only My Hits** (Combat Info page, under Damage Numbers) limits damage numbers to your own melee hits. Arrows and other projectiles are not counted.

### Target ring feedback

While locked on, the ring reacts to the fight: it flashes and pops on your critical hits, sends out a warning-colored shockwave when the target drops to low health, and leaves a burst where the target dies. With **Reduced Motion** on, these become a brief color flash with no movement.

## Compatibility

Vanilla first and third person are the automatic fallback. Shoulder Surfing Reloaded 2.9.x is optional: Lock On keeps SSR's chosen shoulder side and camera offset while locked. With SSR's default **Adaptive** crosshair, Lock On asks SSR for its dynamic, eye-based crosshair for as long as a lock lasts, so every target in reach can be hit whatever the shoulder offset; the crosshair returns to normal when the lock ends. With SSR's dynamic crosshair, it uses the normal eye ray. A user-selected **Static** crosshair starts its attack ray at the shoulder camera, so it cannot reach targets closer than the camera's sideways offset (about 2.5 blocks with a wide offset); Lock On aligns the aim as closely as that ray allows. Lock On does not change SSR's offsets or saved configuration. **Shoulder Surfing / Epic Fight Aim** on the Advanced page turns this alignment off.

Epic Fight 2.2.8 is optional. In its Battle mode with SSR active, Lock On faces the player's body and Epic Fight's melee attacks toward the target. With SSR's default **Adaptive** crosshair, the lock's dynamic crosshair (see above) keeps the crosshair and Epic Fight's attack direction in agreement. The chosen shoulder view remains in place, and SSR's saved crosshair setting is untouched. SSR's **Dynamic** mode also works; a user-selected **Static** mode keeps its static crosshair position while Epic Fight's melee attacks face the target.

Epic Fight and Lock On both use **R** by default. Rebind either **Toggle Battle/Mining Mode** or **Acquire/Release Target** in Minecraft's Controls menu when using both mods.

## Target HUD

Regular targets use a compact, gold-edged plaque with their name, health bar, and enabled combat readouts. Boss targets use a wider banner near the top center of the screen. The boss banner is enabled by default for new settings and appears for the Wither, Ender Dragon, or targets with at least 100 maximum health. When locked onto a vanilla boss, its duplicate vanilla health bar is hidden until the lock ends. Existing configurations keep their saved **Boss Banner** choice; turn it on under **HUD & Ring** to see it.

Better Third Person is not supported. It conflicts with Shoulder Surfing Reloaded's camera ownership model and should not be combined with it.

## Building from source

From a Windows PowerShell prompt in the repository:

```powershell
.\gradlew.bat clean test build
```

The installable reobfuscated JAR is written to `build\libs`.

## Release validation

Before publishing, copy the packaged JAR into a normal Minecraft 1.12.2 Forge profile and verify the mod list, configuration GUI, target acquisition, target switching, camera restoration, target ring, HUD panel, and audio feedback.
