# 1.4.0 recovery record

On September 24, 2026, this working tree was reconstructed from the surviving
Codex task **Locate Lock-On Mod files** after the local project folder and its
Git history were deleted. The original task began at commit
`b9b3939c8489c43155b39fcb7b2a616e28bf5f83` and ended with a clean tree at
local commit `09f3a91`. That later commit is no longer available as a Git object.

The recovered source, tests, and design documents came from completed file
change records in the task log. The README and CHANGELOG were additionally
restored from a complete late-session command output. The release version in
`build.gradle` and `mcmod.info` was confirmed by the old packaged JAR record.

The old 66-commit local history could not be restored. This branch preserves
the recovered final working state as a new commit. The later 1.4.0 rewrite of
`ROADMAP.md` had no surviving text snapshot, so the existing roadmap is marked
as historical rather than presented as the current plan.

## Verification

Using Java 8, `gradlew.bat clean test build --no-daemon` passed. The test report
contains 49 suites and 175 tests with no failures, errors, or skips, matching
the totals recorded in the old task. The build produced
`build/libs/zelda-targeting-1.4.0.jar`. Its class list includes the final
Shoulder Surfing bridge and offset session, and excludes the earlier shoulder
aim solver and movement basis. On September 24, 2026, the recovered mod loaded
in a Minecraft 1.12.2 Forge development client, and the project owner confirmed
that it worked as expected in-game. Shoulder Surfing Reloaded was not installed
in that test client, so its optional integration remains to be retested.
