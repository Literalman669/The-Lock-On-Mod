# Shoulder Surfing Reloaded playtest

- Minecraft 1.12.2, Forge 14.23.5.2859, ShoulderSurfing-1.12.2-2.9.6.jar, and the packaged Zelda Targeting 1.4.0 JAR were tested in the CurseForge profile `Zelda Targeting SSR 1.12.2 Test`.
- The profile is installed at `C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\Zelda Targeting SSR 1.12.2 Test`. It includes a superflat world named `SSR Compatibility Test` with cheats enabled. The world was tested in Survival and Peaceful.
- With SSR's default adaptive crosshair and empty hand, lock-on kept the player visibly over the shoulder. Empty-hand attacks reduced a pig's health from 10 to 9 and then from 9 to 8 on the opposite shoulder. After the final build, another attack reduced it from 8 to 7.
- W/S changed target distance in the expected direction, A/D moved around the locked target, and Left Alt free look returned to the target when released. Unlock left SSR's shoulder position in place.
- Holding a bow exercised SSR's adaptive dynamic crosshair; a charged arrow hit the locked pig. The SSR config file hash stayed unchanged during the final lock and bow tests.
- The packaged profile loaded both mods without compatibility errors in `latest.log`.
- ForgeGradle `runClient` with SSR in `run/mods` fails in SSR's coremod `EntityRenderer.getMouseOver` transformer due to its deobfuscated development mappings. Use the packaged CurseForge profile for combined-mod playtesting.

The bridge detects SSR during mod initialization, after SSR has initialized `Config.CLIENT`. It reads the shoulder offset, camera distance, and dynamic-crosshair flag. Static mode uses `ShoulderAimSolver`; dynamic mode leaves the eye-ray direction unchanged. It never writes SSR configuration or runtime offsets.
