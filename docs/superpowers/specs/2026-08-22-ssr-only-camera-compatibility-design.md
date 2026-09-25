# Zelda Targeting 1.4.0 SSR-Only Camera Compatibility Design

Date: 2026-08-22

Status: Historical design; current behavior recorded below

## Runtime Validation Amendment

The first live experiment failed because it **forced** SSR's adaptive crosshair during every lock. That changed SSR's ray mode and displaced the crosshair. Centering SSR's runtime offsets was used as a temporary workaround.

The current implementation leaves SSR's shoulder offsets and crosshair mode untouched. It samples SSR's active mode each frame: static crosshair uses `ShoulderAimSolver` to align the shifted ray, while dynamic or adaptive-item crosshair uses the ordinary eye ray. Packaged Forge 1.12.2 testing confirmed both shoulder sides, melee hits, free look, movement, and a bow hit. The remaining sections document the original design and may describe experiments that were later changed.

## Summary

Zelda Targeting 1.4.0 will support vanilla camera behavior and optionally Shoulder Surfing Reloaded 2.9.x on Minecraft 1.12.2. Better Third Person support will be removed. When Shoulder Surfing Reloaded is active, Zelda Targeting will preserve its shoulder offset, converge lock-on aim from the shoulder ray rather than the player's unshifted eye, and leave movement interpretation to Minecraft and Shoulder Surfing Reloaded.

This design supersedes the Better Third Person compatibility commitments in the original 1.4.0 overhaul design. Vanilla remains the unconditional fallback.

## Problems Being Corrected

### Competing camera ownership

The current compatibility experiment can select Better Third Person, Shoulder Surfing Reloaded, or vanilla at runtime. Better Third Person and Shoulder Surfing Reloaded both modify third-person camera behavior and are documented as incompatible by Shoulder Surfing Reloaded. Supporting both creates ambiguous ownership of yaw, pitch, camera translation, and mouse response.

### Double-rotated movement

Shoulder Surfing Reloaded 2.9.x derives its camera and ray direction directly from the render entity's yaw and pitch. The current `CameraMovementBasis` and `MovementBasisRemapper` create a second yaw basis and rotate WASD input again. Crossing particular angular boundaries around a target therefore reverses or mixes movement.

### Eye-origin aim with a shoulder-origin camera

The current camera core solves lock-on rotation from the player's eye to the target anchor. Shoulder Surfing Reloaded offsets the effective camera and ray origin laterally. Parallel eye and shoulder rays do not converge on the same nearby target, so the visual crosshair, rendered target, and attack trace can disagree.

### Apparent mouse acceleration

Zelda Targeting does not write Minecraft's `mouseSensitivity` setting. The acceleration-like behavior comes from multiple layers applying or reconstructing yaw while optional camera code is active. The artificial movement-basis accumulator also treats differences from Zelda's last applied yaw as camera input, feeding overwritten rotation back into compatibility state.

## Product Decisions

- Better Third Person is no longer a supported or selectable camera environment.
- Shoulder Surfing Reloaded remains optional; Zelda Targeting has no required runtime dependency on it.
- Shoulder Surfing Reloaded keeps its configured shoulder offset while locked.
- Zelda Targeting owns target selection and the desired target-facing rotation.
- Shoulder Surfing Reloaded owns shoulder translation, collision, transparency, and crosshair projection while its perspective is active.
- Minecraft's normal movement handling remains authoritative. Zelda Targeting will not rewrite `InputUpdateEvent` movement values for Shoulder Surfing Reloaded.
- Vanilla camera behavior is used whenever Shoulder Surfing Reloaded is absent, inactive, or cannot be queried safely.
- The mod remains client-only and does not change reach or server-authoritative combat rules.

## Architecture

### Optional SSR bridge

A focused `ShoulderSurfingBridge` will contain all optional reflection and registration logic. Core camera classes will not reference Shoulder Surfing Reloaded types in method signatures.

The bridge reports:

- whether Shoulder Surfing Reloaded is installed;
- whether its shoulder perspective is currently active;
- its interpolated X, Y, and Z offsets needed for aim convergence;
- whether adaptive-crosshair integration was registered successfully.

Reflection handles are resolved once during initialization. A linkage or invocation failure disables only the SSR bridge, logs one warning, and returns control to vanilla behavior.

### Camera host selection

Camera host selection becomes binary:

1. active Shoulder Surfing Reloaded;
2. vanilla fallback.

The Better Third Person host, camera-event rotation branch, BTP detector, and BTP-specific configuration controls are removed. Existing BTP keys in an older properties file are ignored when read, explicitly removed by the settings store, and no longer written.

### Shoulder-aware aim solver

A pure Java `ShoulderAimSolver` will converge the target-facing yaw and pitch from Shoulder Surfing Reloaded's effective ray origin.

Inputs:

- the player's interpolated eye position;
- the selected target anchor;
- the current player yaw and pitch;
- Shoulder Surfing Reloaded's current local offsets.

The solver will:

1. compute the ordinary eye-to-target rotation as an initial estimate;
2. rotate the SSR offset into world space using that estimate;
3. remove the component parallel to the view vector to obtain SSR's lateral ray-origin offset;
4. recompute rotation from the shifted ray origin to the target;
5. repeat for a small fixed number of iterations until the change is negligible.

The resulting compensated target delta is passed into the existing `CameraDirector`, preserving camera profiles, smoothing, maximum yaw and pitch bounds, free look, and target prediction. Zero offsets produce the exact vanilla solution. Invalid or non-finite offsets fall back to the vanilla target delta.

The solver is allocation-free in the render path and uses a fixed iteration bound.

### Crosshair integration

When SSR's crosshair mode is `ADAPTIVE`, Zelda Targeting will register an optional adaptive-item callback that reports active while a target is locked. This lets SSR use its dynamic crosshair projection during lock-on regardless of the held item.

Zelda Targeting will not draw a second combat crosshair or mutate SSR's saved offset configuration. The compensated aim solution makes SSR's projected crosshair and effective ray converge on the target anchor while preserving the player's chosen shoulder side and distance.

If callback registration is unavailable, shoulder-aware aim still runs and the vanilla crosshair remains usable. The bridge logs this reduced capability once when compatibility debug output is enabled.

### Movement ownership

The `CameraMovementBasis`, `MovementBasisRemapper`, and `InputUpdateEvent` hook are deleted. During lock-on, Zelda Targeting applies the compensated player yaw and pitch. Minecraft then interprets W, A, S, and D once relative to that facing direction:

- W approaches the target;
- S retreats;
- A and D strafe or orbit around it;
- crossing behind or very close to the target does not change key meaning because no stale secondary yaw exists.

Normal movement is completely untouched when no target is locked.

### Mouse and lifecycle ownership

The runtime adapter applies rotation only for a live tracking snapshot and never stores or reconstructs mouse deltas. On unlock, GUI entry, death, disconnect, dimension change, or compatibility failure, SSR session state is cleared immediately. Presentation-only release easing may continue for Zelda's FOV or transparency, but it cannot write yaw, pitch, movement input, mouse sensitivity, or SSR configuration.

Free look suppresses Zelda rotation for that frame without accumulating a synthetic camera basis. Releasing free look resumes from the player's actual yaw and pitch.

## Configuration Changes

The 1.4.0 compatibility page will retain only SSR and diagnostic controls that are still meaningful. The following BTP fields are removed from the runtime facade, settings snapshot, bridge, validator, presets, persistence writer, and GUI:

- `btpCompatibilityMode`;
- `btpCameraIntensity`.

Existing files containing those keys remain readable. The settings store removes the keys the next time the curated 1.4.0 settings are written.

SSR compatibility stays automatic. The existing compensation toggle becomes the enable switch for shoulder-aware convergence; its old manual X-offset fallback is removed once runtime X, Y, and Z offsets are available. If retaining a manual fallback is necessary for a broken SSR reflection surface, it remains internal and is not presented as a BTP-style mode.

## Failure Handling

- Missing SSR classes select vanilla without warning.
- Reflection failure while SSR is installed logs once and selects vanilla compatibility behavior.
- Non-finite offsets select vanilla aim for that frame.
- Callback-registration failure does not disable shoulder-aware aim.
- No compatibility failure may leave player rotation, perspective, or presentation state stuck after the lock ends.

## Testing Strategy

### Automated tests

- `ShoulderAimSolverTest` covers zero offset, left and right shoulders, vertical offset, close targets, distant targets, targets on opposite world bearings, wraparound at plus/minus 180 degrees, and invalid inputs.
- Geometric assertions verify that the compensated ray passes through the requested target within a small tolerance.
- Camera-host tests prove active SSR wins and every other state uses vanilla; no BTP host remains.
- Lifecycle tests prove unlocked and restored states do not apply rotation or retain SSR session state.
- Movement regression tests prove the adapter no longer registers or performs a second input rotation.
- Configuration tests prove BTP values are not emitted and legacy BTP keys do not prevent loading.
- Existing camera, targeting, GUI, HUD, audio, and damage-number tests remain green.

Each behavior change is implemented test-first: the new regression test must fail for the expected reason before production code is changed.

### Manual test pack

Use `C:\Users\OneBeyondTheWall\curseforge\minecraft\Instances\test` with Shoulder Surfing Reloaded 1.12.2-2.9.6 and the new Zelda Targeting jar.

Verify:

1. With no lock, mouse look matches the same instance without Zelda Targeting installed.
2. Lock targets from front, rear, left, and right at close and medium range.
3. W approaches, S retreats, and A/D orbit consistently on every side of the target.
4. SSR's crosshair rests on the locked entity and empty-hand or melee attacks connect when in reach.
5. Left/right shoulder swaps remain functional and converge correctly without editing SSR offsets.
6. Free look does not accelerate, jump, or reverse movement when released.
7. Unlocking restores unrestricted mouse look immediately.
8. Vanilla first and third person continue to work when SSR is removed.
9. A leftover disabled or installed BTP jar receives no special Zelda integration and exposes no BTP settings.

## Acceptance Criteria

- Better Third Person code paths and user-facing settings are absent.
- Mouse sensitivity is unchanged and no Zelda compatibility state processes mouse deltas while unlocked.
- SSR shoulder offset remains visible during lock-on.
- SSR crosshair and attack ray converge on the locked target at practical combat distances.
- WASD behavior is stable around the full 360-degree target circle and at close range.
- Vanilla fallback remains buildable and functional without SSR present.
- The complete automated suite and Forge `clean test build` pass.
