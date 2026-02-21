# 🎯 Enhanced Target Lock-On System

The definitive targeting mod that 1.12.2 has been missing

Transform your Minecraft combat with modern targeting mechanics, complete audio feedback, and intelligent visual cues.

## 🎮 Key Features

### 🎨 Advanced Visuals
- Animated targeting reticles with real-time health bars and pulsing low-health warning
- Damage prediction & lethal highlighting — know when enemies are in kill range
- Smart HUD positioning — prevents UI cut-off at screen edges
- Distance tracking, entity identification, vulnerability display, and hits-to-kill counter
- Floating damage numbers with **3 motion presets** (default / subtle / arcade), fade-out, and critical-hit pop emphasis

### 🔊 Complete Audio System
- **5 Sound Themes** — Default, Zelda, Modern, Subtle, Cinematic
- 4 distinct sound events — lock-on, target switch, lethal target, target lost
- Individual volume + pitch controls per event; sound variety cycling
- Anti-stacking cooldown prevents overlapping blasts during rapid retargeting

### 📹 Intelligent Camera
- **3 lock-on feel presets** — Cinematic, Balanced, Snappy (each sets smoothness + limits)
- Vertical focus offset for fine-tuning the camera aim point
- Per-mode smoothing override (gentler first-person tracking)
- Better Third Person compatibility (disabled / gentle / visual-only modes)
- Debug compatibility log for diagnosing SSR/BTP conflicts

### 🎯 Smart Targeting
- **4 target priority modes** — Nearest, Health, Threat, Angle
- Stable clockwise target cycling (Q/E) with switch cooldown to prevent jank
- Configurable detection range, tracking distance, and FOV angle
- Entity type filtering (hostile / neutral / passive / players)

### 🔧 Polished Configuration GUI
- **5-page GUI** with dot-based page indicator, hover tooltips, and contextual control disabling
- Left-click increase / right-click decrease / Shift for fine-tune on all value buttons
- All settings persist across restarts

## 🎮 Quick Start
1. Press R to lock onto nearest enemy
2. Use Q/E to cycle between targets
3. Press R again to unlock
4. Customize in Mod Options

## ⚙️ Configuration Highlights
- **Targeting**: 5-50 block range, 15-180° detection angle
- **Visuals**: Scalable reticles, damage prediction, lethal indicators
- **Audio**: Individual volume sliders for all 4 sound types
- **Camera**: Smoothness control, auto third-person mode

## 🔧 Technical Excellence
- Universal Mod Compatibility - Works with weapon mods, entity mods, and modpacks
- Multiplayer ready with optimized performance
- Zero dependencies - Complete standalone functionality

## 📋 Requirements
- **Minecraft**: 1.12.2
- **Forge**: 14.23.5.2859+
- **Java**: 8+

## 📝 Changelog

### Version 1.1.2 - Polish & QoL Pass (Current)

**Targeting**
- 4 target priority modes: Nearest, Health, Threat, Angle
- Stable clockwise target cycling with switch cooldown (no more jank)
- Improved fallback when target becomes invalid mid-combat

**Camera**
- 3 lock-on feel presets: Cinematic / Balanced / Snappy
- Vertical focus Y-offset control
- Per-mode smoothing (gentler in first-person)
- Debug compatibility log for SSR/BTP diagnostics

**Audio**
- Cinematic sound theme added (5 themes total)
- 150ms per-event cooldown prevents sound stacking on rapid retargeting
- Per-event enable/disable toggles in GUI

**Combat Feedback**
- Damage number motion presets: Default / Subtle / Arcade
- Critical-hit pop emphasis (surge scale + color flash)
- Low-health pulsing red warning overlay on health bar (≤25%)

**GUI**
- 5-page config GUI with dot-based page indicator
- Target priority button, per-mode smoothing toggle, motion preset button, crit emphasis toggle
- Contextual button disabling (crit emphasis grays out when crits are off)

**Performance**
- Eliminated per-frame `ArrayList` allocation in `TargetRenderer` HUD
- Eliminated per-tick `ArrayList` allocation in `EntityDetector` target search

---

### Version 1.1.2 - Enhanced Audio & Visual System (Previous)
- **Complete Target Lock Sound Effects System** - Professional audio feedback for all targeting actions
- **Individual Sound Controls** - Separate enable/disable and volume controls for each sound type
- **Smart Audio Logic** - Context-aware sound selection (lethal targets get special dramatic audio)
- **Enhanced Visual Feedback** - Damage prediction, lethal target highlighting, vulnerability display
- **Smart HUD Positioning** - Intelligent UI positioning that prevents cut-off at screen edges
- **Universal Mod Compatibility** - Confirmed compatibility with weapon and entity mods

### Version 1.0.0 - Initial Release
- Full targeting system with intelligent target detection
- 3-page configuration GUI with advanced controls
- Better Third Person compatibility
- Basic audio system and performance optimizations

---

**Experience Minecraft combat like never before. Download Zelda Targeting today and transform your adventures!**

*Inspired by The Legend of Zelda series - bringing Nintendo's legendary game design to Minecraft.*
