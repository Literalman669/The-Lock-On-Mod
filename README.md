# Zelda Targeting

The definitive lock-on targeting mod for Minecraft 1.12.2. Brings Zelda-style Z-targeting to your combat with smart HUD, full audio feedback, and deep configuration.

## Controls

| Key | Action |
|-----|--------|
| R | Lock on / unlock |
| Q | Cycle target left |
| E | Cycle target right |

## Features

### Targeting
- Configurable range (5–50 blocks), detection angle (15–180°), and max tracking distance
- **4 priority modes** — Nearest, Health, Threat, Angle
- Stable clockwise target cycling with 250ms cooldown to prevent jank
- Line-of-sight requirement toggle
- **Sync range with reach** — Use player reach distance for targeting (vanilla 3/5 blocks, or Reach Entity Attributes mod) instead of fixed range
- Entity type filtering — hostile, neutral, passive, players

### HUD & Visuals
- Animated reticle with configurable scale
- Health bar with color-coded fill and pulsing low-health warning (≤25%)
- Distance, target name, hits-to-kill, damage prediction, vulnerability display
- **Event-driven damage accuracy** — reads real post-armor/enchant/mod damage after first hit; shows `~` prefix on pre-hit estimates so you always know which is which. Works with any mod automatically, no compat needed.
- Lethal target highlighting with animated title flash
- Floating damage numbers with **3 motion presets** (Default / Subtle / Arcade), fade-out, and critical-hit pop emphasis
- **HUD position control** — 5 anchor presets (Top Right, Top Left, Bottom Left, Bottom Right, Center) + X/Y pixel offset nudge
- Compact HUD mode — name + health bar only
- Boss-style panel — wide centered bar at screen bottom for targets ≥100 HP
- Soft aim indicator — fading crosshair nudge toward locked target
- Target history ring — faint world-space ring above last 3 locked targets

### Audio
- **5 sound themes** — Default, Zelda, Modern, Subtle, Cinematic
- 4 distinct events — lock-on, target switch, lethal target, target lost
- Per-event volume, pitch, and enable/disable controls
- Sound variety cycling
- 150ms anti-stack cooldown prevents overlapping blasts on rapid retargeting

### Camera
- **3 lock-on feel presets** — Cinematic, Balanced, Snappy
- Adjustable smoothness, max pitch, max yaw
- Vertical focus Y-offset
- Per-mode smoothing (gentler first-person tracking)
- Auto third-person on lock-on
- Better Third Person compatibility (disabled / gentle / visual-only)
- Shoulder Surfing Reloaded attack alignment compensation

### Mod Compatibility
- **Better Third Person** — Camera modes (disabled / gentle / visual-only); configurable intensity in gentle mode
- **Shoulder Surfing Reloaded** — Crosshair alignment compensation when shoulder-surfing
- **Neat** — HUD offset when both mods are present to avoid overlap with health bars (toggle + offset in HUD Position)
- **Riding** — Option to disable lock-on when mounted (horses, boats, etc.)
- **Entity mods** — Blacklist for custom mobs (registry names or class substrings) in Entity Filtering
- **Reach Entity Attributes** — Optional sync of targeting range with player reach (when "Sync Range With Reach" is enabled)

### Configuration GUI
- **7-page GUI** — Targeting / HUD & Visuals / HUD Position / Camera / Entity Filtering & Audio / Advanced Sound / Damage Numbers
- Compact header with title + dot-based page indicator on one line
- Left-click increase / right-click decrease / Shift for fine-tune on all value buttons
- Section divider labels between button groups
- Hover tooltips on every button
- All settings persist across restarts

## Requirements

- **Minecraft** 1.12.2
- **Forge** 14.23.5.2859+
- **Java** 8+

## Troubleshooting

| Issue | Likely cause | Fix |
|-------|--------------|-----|
| No targets found when pressing R | Range, angle, or filters too strict | Increase Targeting Range and Detection Angle in config; enable the entity types you want (hostile/neutral/passive) |
| Lock-on feels sluggish | Performance settings | Lower `updateFrequency` (e.g. 1) and `validationInterval` in config |
| Damage numbers show ~ | Estimated damage | Hit the target once; real damage is read from the game after the first hit |
| Camera conflicts with other mods | Better Third Person / camera mods | Use "visual_only" or "disabled" BTP compatibility mode |

## Changelog

### 1.3.1 — Mod Compatibility (Current)

**Mod Compatibility Layer**
- Centralized `com.zeldatargeting.mod.compat` package with per-mod handlers
- **ModCompat** — Central init and startup logging for detected mods
- **CompatBTP / CompatSSR** — Refactored from ZeldaTargetingMod for cleaner architecture
- **CompatNeat** — HUD offset when Neat (health bar mod) is present
- **CompatRiding** — Option to disable lock-on when riding mounts
- **CompatEntityFilter** — Entity blacklist for modded mobs (registry names or class substrings)

**New Config Options**
- `disableLockOnWhenRiding` (default true) — Targeting page
- `syncTargetingRangeWithReach` (default false) — Targeting page; use player reach for targeting range (vanilla 3/5 blocks, Reach Entity Attributes)
- `entityBlacklist` — Entity Filtering page, comma-separated
- `neatCompatEnabled`, `neatCompatOffsetY` — HUD Position page

---

### 1.3.0 — Polish & Accuracy

**Config GUI Overhaul**
- Dropped broken GUI scale hack — layout now uses screen dimensions directly
- Collapsed header: title + page dots on one line, fixed `startY`, nav buttons always at `height - 28`
- Removed redundant interaction hint line from header
- Restructured from 5 pages to 7 clean pages: Targeting (5 items), HUD & Visuals, HUD Position, Camera, Entity Filtering & Audio, Advanced Sound, Damage Numbers
- Section divider labels (`— Combat Info —`, `— Display Modes —`, `— Audio —`) drawn between button groups
- `buttonHeight` bumped to 22 for easier clicking

**HUD Position Control**
- New **HUD Position** button on HUD & Visuals page — cycles 5 anchors: Top Right (default), Top Left, Bottom Left, Bottom Right, Center
- **HUD Offset X / Y** — pixel nudge from anchor, ±5 per click, ±1 with Shift
- All HUD modes (normal, compact, boss panel) respect the anchor

**Damage Accuracy**
- Replaced manual damage formula with event-driven cache via `LivingDamageEvent`
- Reads real post-armor, post-enchant, post-mod-pipeline damage after first hit
- Falls back to manual estimate (prefixed `~`) before first hit or after 5s weapon idle
- Works automatically with any mod — no compat code needed

---

### 1.2.0 — Feature Expansion

- 4 target priority modes, stable clockwise cycling with cooldown
- 3 camera lock-on feel presets, vertical focus Y-offset, per-mode smoothing
- Cinematic sound theme (5 total), 150ms per-event anti-stack cooldown, per-event toggles
- Damage number motion presets, critical-hit pop emphasis, low-health pulsing overlay
- Compact HUD, boss-style panel, soft aim indicator, target history ring
- 5-page config GUI with dot-based page indicator and contextual button disabling
- Eliminated per-frame ArrayList allocations in renderer and target search

---

### 1.1.2 — Audio & Visual Polish

- Complete sound effects system with individual volume controls per event
- Smart audio logic (lethal targets get special audio)
- Damage prediction, lethal highlighting, vulnerability display
- Smart HUD positioning to prevent screen-edge cut-off

---

### 1.0.0 — Initial Release

- Full targeting system with intelligent detection
- 3-page configuration GUI
- Better Third Person compatibility
- Basic audio and performance optimizations

---

*Inspired by The Legend of Zelda series.*
