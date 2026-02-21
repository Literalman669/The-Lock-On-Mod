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

### Configuration GUI
- **6-page GUI** — Targeting / HUD & Visuals / Camera / Entity Filtering & Audio / Advanced Sound / Damage Numbers
- Compact header with title + dot-based page indicator on one line
- Left-click increase / right-click decrease / Shift for fine-tune on all value buttons
- Section divider labels between button groups
- Hover tooltips on every button
- All settings persist across restarts

## Requirements

- **Minecraft** 1.12.2
- **Forge** 14.23.5.2859+
- **Java** 8+

## Changelog

### 1.3.0 — Polish & Accuracy (Current)

**Config GUI Overhaul**
- Dropped broken GUI scale hack — layout now uses screen dimensions directly
- Collapsed header: title + page dots on one line, fixed `startY`, nav buttons always at `height - 28`
- Removed redundant interaction hint line from header
- Restructured from 5 pages to 6 clean pages: Targeting (5 items), HUD & Visuals, Camera, Entity Filtering & Audio, Advanced Sound, Damage Numbers
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
