# Epic Fight and Shoulder Surfing Reloaded playtest

The packaged CurseForge profile `Zelda Targeting SSR 1.12.2 Test` has Minecraft 1.12.2, Forge 14.23.5.2859, EpicFightMod-1.12.2-2.2.8.jar, ShoulderSurfing-1.12.2-2.9.6.jar, and Zelda Targeting 1.4.0. The test world is `SSR Compatibility Test`; a stationary pig with `NoAI:1b` is near the player at `(1002.5, 4, 1000.5)`. The world is in Survival/Peaceful with cheats and natural mob spawning disabled for repeatable combat checks.

The original bug was reproduced with SSR's Adaptive crosshair behaving statically: at two blocks from the pig, the shoulder-compensated camera yaw was about -141 degrees while the pig's player-eye bearing was -90 degrees. Epic Fight Battle mode used the camera yaw for its melee collider; a punch left the pig at 10/10 HP.

With the compatibility change, Zelda Targeting uses the eye bearing in Epic Fight Battle mode and asks SSR's Adaptive crosshair to use its dynamic projection only during the lock. The body yaw, eye yaw, and target bearing were all -90 degrees. A Battle-mode punch reduced the pig from 9 to 8 HP; Mining mode restored SSR's static shoulder ray and reduced it from 8 to 7; switching back to Battle mode reduced it from 7 to 6. Swapping shoulders and punching reduced it from 6 to 5. After free look, another Battle-mode punch at point-blank range reduced it from 5 to 4. Unlock restored SSR's ordinary crosshair and retained its shoulder camera.

The SSR config file hash was unchanged after the automatic crosshair transitions and after swapping the shoulder back to its initial side. The client log reported successful registration of the adaptive callback and no compatibility errors. The optional Epic Fight link uses reflection; neither Epic Fight nor SSR is bundled into Zelda Targeting's JAR.

After removing temporary diagnostics, the clean packaged build launched with all three mods and a Battle-mode punch reduced the pig from 4 to 3 HP. The final JAR was copied into the test profile, and the client was left paused in the test world with two additional stationary pigs for review.

Epic Fight and Zelda Targeting both default to R. In the test profile, Zelda Targeting's lock key is rebound to F so Epic Fight keeps R for Battle/Mining mode.
