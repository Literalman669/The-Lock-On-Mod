# Stage 4 Adaptive Presentation Smoke Test

Use the freshly built `zeldatargeting-1.3.0.jar` from `build/libs` in the Forge 1.12.2 client. This is a client-only test; no server installation is required.

## Setup

1. Start a single-player creative world with a few ordinary living targets nearby.
2. Keep the standard lock-on, target-cycle, and camera-free-look keys bound.
3. Enable the mod from the Forge Mod List and open its config screen once to confirm settings load.

## Required checks

1. **Classic Ring and detail panel**
   - Lock an ordinary target.
   - Confirm the world-space ring has four corner brackets, a health arc, and the target marker.
   - Confirm the compact top-right detail panel follows the existing name, health-bar, distance, damage, hits-to-kill, and vulnerability toggles.

2. **HUD layout safety**
   - Test GUI Scale `Auto` and the highest usable GUI Scale.
   - At each existing HUD anchor (`top-left`, `top-right`, `bottom-left`, `bottom-right`, and `center`), set the saved X/Y offsets to both extremes.
   - Confirm the panel remains completely on screen and does not cover the navigation or hotbar unexpectedly.

3. **Boss panel behavior**
   - With `Boss-Style Panel` off, lock a high-health target; the ordinary detail panel should remain in use.
   - With it on, lock a Wither, Ender Dragon, or a 100+ health target.
   - Confirm the bottom-center boss panel appears without replacing the world-space ring and stays clear of the vanilla boss-bar area.

4. **Transition feedback**
   - Acquire a target, rapidly cycle targets several times, release or let the target die, then reacquire.
   - Confirm one lock, switch, and lost sound per transition, with no repeated sound while holding a stationary lock.

5. **Accessibility-safe target states**
   - Test a low-health target and a one-hit lethal target.
   - Confirm the low-health ring has its diamond marker and the lethal ring has its inner marker, so the states remain distinct without relying only on color.
   - Hit a target critically and lethally, confirming the existing damage-number feedback still appears.

6. **Existing optional visuals**
   - Enable `Soft Aim Indicator`; verify its crosshair nudge still follows the locked target.
   - Enable `Target History`; cycle through at least three targets and verify faint rings remain over recent targets.

## Log review

After testing, inspect `logs/latest.log` for `zeldatargeting` errors or renderer warnings. The expected outcome is no `ERROR`, no uncaught exception, and no warning beginning with `Target ring renderer`, `Target detail panel`, `Boss panel renderer`, `Soft aim renderer`, `Target history renderer`, or `Target presentation snapshot`.
