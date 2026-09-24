# Zelda Targeting

Zelda Targeting is a client-only lock-on targeting mod for Minecraft 1.12.2. It combines responsive target selection, a configurable camera, combat feedback, and a Zelda-inspired visual presentation.

## 1.4.0 highlights

- Rebuilt targeting state and camera flow with safer lock restoration.
- Adaptive Hybrid HUD with a responsive target panel and boss presentation.
- A tubular, neon target ring whose health arc shrinks with the target's remaining health.
- Shared green, orange, and red health states across the target ring and HUD.
- Preset-first, seven-page configuration flow with transactional Save, Cancel, and Reset actions.
- Optional Shoulder Surfing Reloaded 2.9.x integration with reversible lock centering.
- Accessibility controls for palette, opacity, compact HUD, soft indicators, and reduced motion.

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.2859 or a compatible 1.12.2 Forge build

The mod is client-only. Install it on the client; a server-side installation is not required.

## Controls

The default key bindings are available from Minecraft's Controls menu under Zelda Targeting:

- **R** acquires or releases a target.
- **Z** and **X** cycle between eligible targets.
- **Left Alt** temporarily enables free look.

## Configuration

Open **Mods**, select **Zelda Targeting**, then choose **Config**.

The configuration is arranged as seven pages:

1. Presets
2. Targeting
3. Camera
4. HUD
5. Audio
6. Compatibility
7. Accessibility

Preset choices are previewed first. Choose **Save** to keep the preview, **Cancel** to discard it, or **Reset** to restore the current page's default settings.

## Compatibility

Vanilla first and third person are the automatic fallback. Shoulder Surfing Reloaded 2.9.x is optional: Zelda Targeting temporarily centers SSR's runtime camera while locked so the target, crosshair, attack ray, and movement share one origin. The exact configured shoulder offset is restored on unlock and is never rewritten in SSR's configuration.

Better Third Person is not supported. It conflicts with Shoulder Surfing Reloaded's camera ownership model and should not be combined with it.

## Building from source

From a Windows PowerShell prompt in the repository:

```powershell
.\gradlew.bat clean test build
```

The installable reobfuscated JAR is written to `build\libs`.

## Release validation

Before publishing, copy the packaged JAR into a normal Minecraft 1.12.2 Forge profile and verify the mod list, configuration GUI, target acquisition, target switching, camera restoration, target ring, HUD panel, and audio feedback.
