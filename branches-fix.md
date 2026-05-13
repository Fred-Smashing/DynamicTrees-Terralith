# Mirage & Moonlight Branch Rendering Investigation

## Problem

When in-game, two tree types — **Mirage** (`dtterralith:mirage`) and **Moonlight** (`dtterralith:moonlight_spruce` / `dtterralith:moonlight_pine`) — have branches that do not render (invisible in world) and show a default purple/black missing texture in inventory.

---

## Log Analysis

### Initial Log (before any changes)
The following "Missing model for variant" warnings were present:

```
Block{dtterralith:stripped_mirage_branch}[radius=1-8,waterlogged=true/false]
Block{dtterralith:stripped_moonlight_branch}[radius=1-8,waterlogged=true/false]
Block{dtterralith:mirage_root}[grounded=true/false,radius=1-8,waterlogged=true/false]
Block{dtterralith:moonlight_root}[grounded=true/false,radius=1-8,waterlogged=true/false]
```

Additionally, 5 leaves properties were missing `particle_chance`:
- `dtterralith:spruce_round`
- `dtterralith:oak_scruffy`
- `dtterralith:mangrove_scruffy`
- `dtterralith:birch_scruffy`
- `dtterralith:acacia_deciduous`

### Log After Adding Missing Blockstate/Model Files
The "Missing model for variant" warnings for stripped branches and roots are **gone** — those files are now loading correctly. However, the user reports the main branches remain invisible.

Notably, `mirage_branch` and `moonlight_branch` **never had** missing model warnings in either log — their blockstate and model files already existed.

---

## Investigation Findings

### 1. Block Registration (Family System)
Dynamic Trees registers the following blocks based on `families/mirage.json` and `families/moonlight.json`:

| Block | Derived From | Status |
|---|---|---|
| `dtterralith:mirage_branch` | `mirage` + `_branch` | Blockstate + model existed |
| `dtterralith:stripped_mirage_branch` | `stripped_` + branch name | **Was missing** — now added |
| `dtterralith:mirage_root` | `mirage` + `_root` | **Was missing** — now added |
| `dtterralith:moonlight_branch` | `moonlight` + `_branch` | Blockstate + model existed |
| `dtterralith:stripped_moonlight_branch` | `stripped_` + branch name | **Was missing** — now added |
| `dtterralith:moonlight_root` | `moonlight` + `_root` | **Was missing** — now added |

### 2. Family Configuration

**Mirage** (`families/mirage.json`):
- `primitive_log`: `minecraft:stripped_warped_stem`
- `primitive_stripped_log`: `minecraft:stripped_warped_stem`
- `max_branch_radius`: 8
- `generate_surface_root`: true

**Moonlight** (`families/moonlight.json`):
- `primitive_log`: `minecraft:stripped_spruce_log`
- `primitive_stripped_log`: `minecraft:stripped_spruce_log`
- `max_branch_radius`: 8
- `generate_surface_root`: true

### 3. Root Cause: Blockstate Model Approach Mismatch

The **primary issue** is a mismatch between how the Dynamic Trees core registers custom models vs. how this addon references them.

**Dynamic Trees core approach** (e.g., `assets/dynamictrees/blockstates/oak_branch.json`):
```json
{
  "variants": {
    "": {
      "fabric:type": "dynamictrees:branch",
      "family": "dynamictrees:oak",
      "textures": {
        "bark": "minecraft:block/oak_log",
        "rings": "minecraft:block/oak_log_top"
      }
    }
  }
}
```
The DT core registers `UnbakedBranchModel` via `CustomUnbakedBlockStateModel.register("dynamictrees:branch", ...)` in `DTModelLoadingPlugin.registerModels()`. This is specifically for the **`fabric:type`** blockstate variant mechanism.

**Addon approach** (e.g., `assets/dtterralith/blockstates/mirage_branch.json`):
```json
{
  "variants": {
    "": {
      "model": "dtterralith:block/mirage_branch"
    }
  }
}
```
This references a model file that uses `"loader": "dynamictrees:branch"` — a **model loader** mechanism separate from `fabric:type`. The model loader for `dynamictrees:branch` is **not explicitly registered** (only the `fabric:type` variant is registered via `CustomUnbakedBlockStateModel`).

While the models load silently (no warnings), the `"loader"` path may not properly receive the `Connections` data and `BlockState` variant context that the `fabric:type` blockstate path provides, potentially resulting in **empty geometry** (invisible branches).

### 4. Additional Issues Found

- **5 leaves properties** missing `"particle_chance"` field (errors logged at startup)
- `spruce_round.json`, `oak_scruffy.json`, `mangrove_scruffy.json`, `birch_scruffy.json`, `acacia_deciduous.json` — none of these are used by mirage/moonlight species, so they are **not** the cause of the branch invisibility but should still be fixed

---

## Changes Already Applied

The following 8 files were created in `src/generated/resources/assets/dtterralith/` to fix the "Missing model for variant" warnings:

| File | Purpose |
|---|---|
| `blockstates/stripped_mirage_branch.json` | Blockstate for stripped mirage branch |
| `blockstates/stripped_moonlight_branch.json` | Blockstate for stripped moonlight branch |
| `blockstates/mirage_root.json` | Blockstate for mirage root (uses `fabric:type`) |
| `blockstates/moonlight_root.json` | Blockstate for moonlight root (uses `fabric:type`) |
| `models/block/stripped_mirage_branch.json` | Model for stripped mirage branch |
| `models/block/stripped_moonlight_branch.json` | Model for stripped moonlight branch |
| `models/item/stripped_mirage_branch.json` | Item model for stripped mirage branch |
| `models/item/stripped_moonlight_branch.json` | Item model for stripped moonlight branch |

---

## Proposed Changes

### Fix 1: Convert Branch Blockstates to use `fabric:type`

The primary fix is to convert the **main** branch blockstates (and the stripped ones I already created) from the `"model"` → `"loader"` approach to the `"fabric:type"` approach, matching the DT core's pattern.

**`blockstates/mirage_branch.json`:**
```json
{
  "variants": {
    "": {
      "fabric:type": "dynamictrees:branch",
      "textures": {
        "bark": "minecraft:block/stripped_warped_stem",
        "rings": "minecraft:block/stripped_warped_stem_top"
      }
    }
  }
}
```

**`blockstates/moonlight_branch.json`:**
```json
{
  "variants": {
    "": {
      "fabric:type": "dynamictrees:branch",
      "textures": {
        "bark": "minecraft:block/stripped_spruce_log",
        "rings": "minecraft:block/stripped_spruce_log_top"
      }
    }
  }
}
```

**`blockstates/stripped_mirage_branch.json`:**
```json
{
  "variants": {
    "": {
      "fabric:type": "dynamictrees:branch",
      "textures": {
        "bark": "minecraft:block/stripped_warped_stem",
        "rings": "minecraft:block/stripped_warped_stem_top"
      }
    }
  }
}
```

**`blockstates/stripped_moonlight_branch.json`:**
```json
{
  "variants": {
    "": {
      "fabric:type": "dynamictrees:branch",
      "textures": {
        "bark": "minecraft:block/stripped_spruce_log",
        "rings": "minecraft:block/stripped_spruce_log_top"
      }
    }
  }
}
```

(The root blockstates already use `fabric:type` correctly and do not need changes.)

### Fix 2: Add `particle_chance` to Leaves Properties

Add `"particle_chance": 0` (or appropriate value) to the 5 leaves properties files that are missing it:

- `leaves_properties/spruce_round.json`
- `leaves_properties/oak_scruffy.json`
- `leaves_properties/mangrove_scruffy.json`
- `leaves_properties/birch_scruffy.json`
- `leaves_properties/acacia_deciduous.json`

### Potential Cleanup

After converting to `fabric:type`, the model files `models/block/mirage_branch.json` and `models/block/moonlight_branch.json` (and the stripped variants) are no longer referenced by any blockstate. They can either be removed or kept for reference. The item model files (`models/item/*.json`) should be kept as they handle inventory rendering.
