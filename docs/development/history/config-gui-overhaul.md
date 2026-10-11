# Config GUI Overhaul

Redesign the 5-page config GUI to fix overflow, crowding, and layout jank across all screen sizes.

---

## Problems Identified (from screenshots + code)

- **Page 1 overflows** — 15 buttons on one page, bottom ones cut off at non-fullscreen sizes
- **Header wastes ~80px** — title + dots + hint + page title + description stacked too tall
- **Interaction hint is redundant** — shown every page, users learn it once
- **No visual grouping** — all buttons look identical, no section separators
- **`startY` calculation is broken** — uses `effectiveWidth / 9` (should use height)
- **Bottom nav overlaps content** at small resolutions
- **Page 1 mixes targeting + visuals** — too many unrelated settings crammed together
- **Page 3 mixes entity filtering + audio** — separate concerns on one page

---

## Wave 1 — Fix Layout Engine
*No content changes. Goal: nothing overflows at any resolution.*

- Drop the broken `effectiveWidth`/`effectiveHeight` scaling hack — use `this.width` / `this.height` directly (Forge handles GUI scaling already)
- Fix `startY` — currently `effectiveWidth / 9` (wrong axis), change to fixed `52`
- Remove the interaction hint line from the header (move tip to Done button tooltip)
- Collapse header to: title + dots on line 1 (~y=15), page title (~y=28), description (~y=38) → `startY = 52`
- `spacing` calculated from remaining height: `(this.height - startY - 30) / itemCount`
- `buttonWidth = Math.min(240, this.width - 60)`
- Bottom nav always at `this.height - 28`

---

## Wave 2 — Restructure Pages
*Redistribute settings so no page exceeds ~10 buttons.*

| Page | Title | Key Contents |
|------|-------|-------------|
| 0 | Targeting | Range, Tracking Distance, Angle, LOS, Priority (5 items) |
| 1 | HUD & Visuals | Reticle/health/distance/name toggles + scales, damage prediction group, compact/boss/soft-aim/history toggles |
| 2 | Camera | Enable, Preset, Smoothness, Pitch, Yaw, Auto 3rd Person, Focus Y, Per-Mode Smoothing, BTP Mode |
| 3 | Audio | Entity filters (3) + all audio toggles/volumes/pitches/theme/variety |
| 4 | Damage Numbers | All damage number settings (unchanged) |

- Draw `§8— Section Name —` labels between groups using `drawCenteredString` (not buttons)
- Two-column layout for pages with many simple toggles

---

## Wave 3 — Visual Polish
*Goal: looks intentional, not like a debug menu.*

- Section divider labels between button groups
- Two-column layout for toggle-heavy pages (page 1 visuals, page 3 audio)
- `buttonHeight = 22` (slightly taller, easier to click)
- Title + dots on one line: `"Zelda Targeting Configuration  ● ○ ○ ○ ○"` to save vertical space
- Slightly brighter ON/OFF label text

---

## ROADMAP.md Addition

```markdown
## Phase 5 — Config GUI Overhaul

- [ ] Wave 1: Fix layout engine (overflow, startY, spacing, header)
- [ ] Wave 2: Restructure pages (redistribute settings, max ~10/page)
- [ ] Wave 3: Visual polish (section labels, two-column layout, tighter header)
```

---

## Files to Edit

- `ROADMAP.md` — add Phase 5 (first)
- `src/main/java/com/zeldatargeting/mod/client/gui/GuiTargetingConfig.java` — all 3 waves

## Order

1. Update `ROADMAP.md`
2. Wave 1 → build & verify no overflow
3. Wave 2 → build & verify page counts
4. Wave 3 → build & final check
5. Git commit after each wave
