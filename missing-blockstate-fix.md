# Investigation: Mirage & Moonlight Branch Inventory Item Models Still Broken

## Problem

After previous fixes (adding missing blockstate/model JSONs for stripped branches and roots, then converting branch blockstates to `fabric:type`), the in-world branch rendering may be fixed, but **inventory item icons** for mirage and moonlight branches (regular and stripped) still show the purple/black missing-texture placeholder.

All other tree branches (oak, spruce, etc.) display correctly in inventory.

---

## Investigation

### 1. Comparing DT Core vs Addon Asset Structure

**DT Core** — has item definitions in the NEW Minecraft 26.1.2 format:

```
assets/dynamictrees/items/oak_branch.json
assets/dynamictrees/items/spruce_branch.json
assets/dynamictrees/items/warped_branch.json
... (regular branches only, no stripped branches)
```

Each uses the new format:
```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dynamictrees:item/oak_branch"
  }
}
```
This references the old-style model at `models/item/oak_branch.json`.

The DT core does **NOT** have `items/` definitions for stripped branches (e.g., no `items/stripped_oak_branch.json`). Those work through a fallback: the game auto-generates an item from the blockstate's `fabric:type` model.

**Addon** — has NO `items/` directory at all:

```
❌ src/generated/resources/assets/dtterralith/items/  -- does not exist
```

Only has old deprecated-style models:
```
src/generated/resources/assets/dtterralith/models/item/mirage_branch.json       (existing, ignored)
src/generated/resources/assets/dtterralith/models/item/moonlight_branch.json      (existing, ignored)
src/generated/resources/assets/dtterralith/models/item/stripped_mirage_branch.json  (created earlier, ignored)
src/generated/resources/assets/dtterralith/models/item/stripped_moonlight_branch.json (created earlier, ignored)
```

### 2. Minecraft 26.1.2 Item Model System Change

In Minecraft 1.21.2+ (26.1.x), Mojang overhauled the item model system:

- **Old system** (1.21.1 and earlier): Items were defined by placing JSON files in `assets/<mod>/models/item/<item>.json`. The model could inherit from a parent using `"parent": "parent_model"`.

- **New system** (1.21.2+): Items are now defined in `assets/<mod>/items/<item>.json` with a new wrapper format. The old `models/item/` location is **no longer read** by the game. Models still live in `models/` but must be referenced from the new `items/` definition.

The new `items/<item>.json` format:
```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dynamictrees:item/oak_branch"
  }
}
```

The `"model"` field references a model ID (`dynamictrees:item/oak_branch`), which resolves to `assets/dynamictrees/models/item/oak_branch.json`. The old model JSON format (`parent` + `textures` + `elements`) is still used — it's just now referenced from the new wrapper.

### 3. Root Cause

The addon's branch item models at `models/item/mirage_branch.json` (etc.) are **ignored by the game** because:

1. The game no longer scans `models/item/` for item definitions
2. The game looks in `items/` first — no files found there for the addon
3. Without an explicit item definition, the game falls back to the blockstate model (`fabric:type`)
4. The `fabric:type` blockstate model (`BranchBlockStateModel`) is designed for **block** rendering, not item rendering — it may produce no geometry or incorrect geometry when rendered as an item

**Why this only affects mirage/moonlight:**
- All other trees in the addon (ancient, larch, kapok, etc.) use **DT core branches** (oak, spruce, etc.), which have proper `items/` definitions in the DT core jar
- Only mirage and moonlight have custom branches registered under the `dtterralith` namespace, so they need their own `items/` definitions
- The addon's seed items (mirage_seed.json, etc.) likely work because seeds are handled through a different rendering path

---

## Proposed Fix

Create `src/generated/resources/assets/dtterralith/items/` with new-format item definitions for all 6 branch/root items:

### Files to create

| File | References model |
|---|---|
| `items/mirage_branch.json` | `dtterralith:item/mirage_branch` |
| `items/stripped_mirage_branch.json` | `dtterralith:item/stripped_mirage_branch` |
| `items/mirage_root.json` | `dtterralith:item/mirage_root` (or use blockstate fallback) |
| `items/moonlight_branch.json` | `dtterralith:item/moonlight_branch` |
| `items/stripped_moonlight_branch.json` | `dtterralith:item/stripped_moonlight_branch` |
| `items/moonlight_root.json` | `dtterralith:item/moonlight_root` (or use blockstate fallback) |

### Format

Each file follows the DT core's pattern:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dtterralith:item/mirage_branch"
  }
}
```

This references the existing `models/item/mirage_branch.json` model, which in turn inherits from `dynamictrees:item/branch` (the standard branch item model with cuboid elements for trunk + branches).

### Note on Roots

The DT core has `items/mangrove_roots.json` for its mangrove root item, so roots also need the new-format item definition. Check if `models/item/mirage_root.json` and `models/item/moonlight_root.json` exist — if not, the game will fall back to the blockstate `fabric:type` model for root items, which may or may not work. It's safer to create `items/` definitions for them too.

---

## Verification Steps

1. Create the `items/` directory with the 6 JSON files
2. Rebuild the mod (`gradlew build`)
3. Relaunch Minecraft and check:
   - Inventory icons for mirage and moonlight branches (regular and stripped)
   - Inventory icons for mirage and moonlight roots
   - In-world branch rendering (should still work from the earlier `fabric:type` fix)
4. Check the log for any remaining errors or warnings
