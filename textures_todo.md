# Machine active textures TODO (art)

Assets / blockstate gaps for **working vs idle** visuals. Code already sets `canActive=true` and may switch models; art is missing, unused, or idle/active are identical.

Naming convention used by most machines:

| Suffix | Meaning |
|--------|---------|
| `_front` / `_front_active` | Face the block faces (model `north`) |
| `_back` / `_back_active` | Opposite face (model `south`) |
| `_leftright` / `_leftright_active` | Left + right (model `east` + `west`) |
| `_leftrightback` / `_leftrightback_active` | Left + right + back (model `east` + `west` + `south`) |
| `_sides` / `_sides_active` | All four horizontal faces when they share one texture |
| `_top` / `_bottom` (+ `_active` if needed) | Up / down |

Wire models with `parent: block/cube` (or orientable parents) and map faces to the semantic files above. Blockstates with `facing` + `active` should list every combination and point `active=true` at `*_active` models.

---

## A. Blockstate missing `active` variants **and** no visual difference

These never show a working look until both blockstate and textures/models exist.

### `batch_crafter`

- Block id: `ic2r:batch_crafter`
- Idle model: `models/block/machine/misc/batch_crafter.json` (reuses industrial workbench faces)
- Active model: `models/block/machine/misc/batch_crafter_active.json` (same textures as idle)
- Blockstate: `blockstates/batch_crafter.json` — only `facing=*`, no `active`
- **Art needed:** dedicated idle/active faces (or at least glowing front/sides while crafting). Then wire `_active` model + `facing,active` blockstate variants.

### `solar_generator`

- Block id: `ic2r:solar_generator`
- Idle: `models/block/generator/electric/solar_generator.json`
- Active: `.../solar_generator_active.json` — **only** `"parent": idle` (no override)
- Blockstate: no `active`
- **Art needed:** e.g. `_top_active` / `_sides_active` for daylight generation, then override textures on `_active` model + blockstate variants.

### `steam_repressurizer`

- Block id: `ic2r:steam_repressurizer`
- Idle/active models share tank side/top/bottom textures
- Blockstate: no `active`
- **Art needed:** working faces (steam/pressure indicators), model + blockstate.

### `water_generator`

- Block id: `ic2r:water_generator`
- Active model parents idle only
- Blockstate: has `facing` only
- **Art needed:** `_front_active` (and optional side actives), model + `facing,active` blockstate.

### `wind_generator`

- Block id: `ic2r:wind_generator`
- Same situation as water generator
- **Art needed:** active faces + model + blockstate.

---

## B. Blockstate has `active`, but active model matches idle

Switching state does nothing visually until textures differ.

### `electric_kinetic_generator`

- Active: parent-only → same as idle
- Path: `models/block/generator/kinetic/electric_kinetic_generator*.json`
- **Art needed:** active front/sides (or indicator lights).

### `water_kinetic_generator`

- Active: parent-only
- Path: `models/block/generator/kinetic/water_kinetic_generator*.json`
- **Art needed:** rotor/flow active faces.

### `wind_kinetic_generator`

- Active: parent-only
- Path: `models/block/generator/kinetic/wind_kinetic_generator*.json`
- **Art needed:** rotor/active faces.

### `eu_to_fe_converter`

- Active file is a full copy of idle (MV transformer faces)
- Path: `models/block/wiring/eu_to_fe_converter*.json`
- **Art needed:** distinct active (conversion/energy) faces; currently reuses transformer art.

### `trade_o_mat`

- Active: parent-only
- Path: `models/block/personal/trade_o_mat*.json`
- **Art needed:** trading/active front (or sides).

### `uu_scanner`

- Active: parent-only
- Path: `models/block/machine/uu/uu_scanner*.json` (textures under `scanner_*`)
- **Art needed:** `scanner_front_active` etc., then map on `_active` model.

---

## C. Reference examples (already correct)

Use these as layout/style references when painting:

- Processing: `macerator`, `compressor`, `electric_furnace` — front (+ sometimes top) `_active`
- Oriented cube with leftrightback: `generator` active model, transformers
- Cropmatron (after code fix): front / back / leftright `_active`
- Creative generator: `creative_generator_side_active` on all sides when active

---

## Checklist for each machine above

1. Paint or export `*_active.png` under `assets/ic2r/textures/block/...`
2. Edit `models/block/.../<id>_active.json` to reference those textures (do not leave parent-only)
3. Ensure `blockstates/<id>.json` includes `active=false` / `active=true` for every facing (if facings exist)
4. In-game: place block, power/run it, confirm idle ↔ working swap without purple/black missing textures

---

## Out of scope here (already fixed in code/resources)

- `creative_generator` — blockstate active variants wired
- `cropmatron` — active model uses `front` / `back` / `leftright` active textures
- `nuclear_reactor` / `electrolyzer` — active models use semantic `front` + `leftrightback` mapping
- Logic: `Ic2rTileEntity.setActive` now updates server `active` blockstate (fixes client resync “random idle”)
