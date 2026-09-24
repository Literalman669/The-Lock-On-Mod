# Zelda Targeting 1.4 Stage 3 Camera Smoke Test

## Test Build

- Status: accepted after full in-game validation
- Tested source commit: `efb3ffaa341266c0e012978f5d9eef7bbbebda23`
- Artifact: `build/libs/zelda-targeting-1.3.0.jar`
- Artifact SHA-256: `3532D4A6417AAE47DB8B1179B1D52EFD0EC332FB392B63080CFACA5BE3AD41E5`
- Minecraft: 1.12.2
- Forge: 14.23.5.2859
- Java: 8
- Public version and configuration schema remain 1.3.0 until a later overhaul stage.

## Automated Verification

- `clean test build`: passed
- `reobfJar`: passed
- Tests: 104 passed, 0 failures, 0 errors across 21 suites
- Pure camera core forbidden-import scan: passed
- Removed `CameraController` and `previousPerspective` reference scan: passed
- Packaged camera classes and `mcmod.info` inspection: passed

## Log Baseline

- Log: `run/logs/latest.log`
- Previous log before launch: 612 lines, last written 2026-07-31 15:06:01 America/New_York
- The development client rotated `latest.log` at launch.
- Fresh-session baseline after the main menu loaded: 560 lines, last written 2026-08-01 18:10:29 America/New_York
- Known pre-test Forge development warnings: Maven cache-path format, unsigned development Forge, Java 9 `module-info.class` scanning, and offline Realms authorization
- Focused regression appended-log audit: 36 lines after the 560-line baseline; normal dimension unload, integrated-server join, and clean shutdown only. No Zelda Targeting camera, rendering, exception, error, fatal, or OpenGL entry appeared.
- Acceptance-session audit: normal configuration saves, world pauses, and clean shutdown only. No Zelda Targeting error, fatal, or exception entry appeared.

## In-Game Checklist

All eight items passed during the staged manual checks. For a future regression, report the failed item and one concise observation.

1. For the same medium target in open space, acquire with Cinematic, Balanced, and Snappy. Confirm each feels distinct, Snappy adds no visible FOV change, and unlock never returns the view to the acquisition angle.
2. Repeat acquisition and unlock in vanilla first person, rear third person, and front third person. With automatic third person both off and on, confirm the original perspective returns exactly once.
3. Hold Left Alt while locked, look freely in several directions, then release Left Alt. Confirm a smooth return to the target with no replayed lock or switch sound.
4. Lock small, tall, wide, moving, and very near targets. Confirm focus follows the resolved anchor and large shapes frame farther away without abrupt distance jumps.
5. Back toward a wall, stand under a low ceiling, and enter a narrow corridor. Confirm no wall clipping or camera extension through blocks. Move through the transparency range and confirm the player fades and fully restores.
6. With automatic third person enabled, keep the camera obstructed long enough to fall back to first person, then clear the obstruction long enough to return to rear third person without flicker.
7. Verify manual unlock, target death, sustained occlusion, player death, world exit while locked, and world re-entry. Confirm no stale target, perspective, FOV, distance, player alpha, or free-look state.
8. Open the configuration GUI while locked and confirm presentation restores immediately. Close it and confirm the same lock resumes without reacquisition feedback.

## Manual Results

- Focused camera-follow regression: PASS. The tester reported that the revised camera feel is "a fuck ton lot better."
- Checklist items 1 through 7: PASS during the prior full Stage 3 smoke run.
- Checklist item 8: PASS after the development metadata and Config entry point were repaired; opening and changing settings while testing produced normal configuration-save entries only.
- Responsive configuration regression: PASS. A single Next click lands on HUD & Visuals, long pages scroll within their content area, and the footer remains clear and interactive at the tested GUI scale.
- Final camera/HUD follow-up: PASS. The tester reported that everything was working well; supplied screenshots show the target HUD tracking a moving villager without errors.

## Known Compatibility Boundary

- This stage provides the standalone vanilla camera implementation. Optional Better Third Person and Shoulder Surfing adapters remain deferred to the compatibility stage.
