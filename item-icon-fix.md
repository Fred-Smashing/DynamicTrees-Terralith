# Seed Item Icon Fix — Analysis

## Problem

Inventory icons for seed items display a purple "missing texture" (default) texture instead of the real seed texture. This affects **all 31 seed items** in the Dynamic Trees Terralith addon.

## Root Cause

**Minecraft 26.1 (1.21) introduced a new item model resolution system** that relies on files in `assets/<namespace>/items/<id>.json`. The addon provides all the old-style models and textures, but is **missing the new-style `items/` definition files** for its seed items.

## How Minecraft 26.1 Resolves Item Models

### 1. The `ITEM_MODEL` Data Component

In `Item.Properties`, a `DependantName` model field is initialized that maps `ResourceKey<Item> -> Identifier` via `ResourceKey::identifier`. This means **every item's `ITEM_MODEL` data component defaults to its own registry key**.

For example:
- Item `dtterralith:ancient_seed` → `ITEM_MODEL` = `dtterralith:ancient_seed`

### 2. Model Lookup Chain

```
ItemStack rendering
  → ItemModelResolver.appendItemLayers()
    → itemStack.get(DataComponents.ITEM_MODEL) → "dtterralith:ancient_seed"
    → ModelManager.getItemModel("dtterralith:ancient_seed")
      → bakedItemStackModels.getOrDefault(id, MissingModels.item())
```

The `bakedItemStackModels` map is populated by `ClientItemInfoLoader`, which **only** reads files from `assets/<namespace>/items/<id>.json`. If no such file exists for an item, the `MissingModels.item()` (purple/black checkerboard texture) is returned.

### 3. The `items/` File Format

Dynamic Trees 1.8.0-BETA01 provides these for its own seeds (e.g., `assets/dynamictrees/items/oak_seed.json`):

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dynamictrees:item/oak_seed"
  }
}
```

This tells Minecraft: *"For item `dynamictrees:oak_seed`, use the model at `dynamictrees:item/oak_seed`"* — which is the auto-generated model at `assets/dynamictrees/models/item/oak_seed.json` that references the texture.

## What Currently Exists vs. What's Missing

| Resource | Path | Status |
|----------|------|--------|
| Textures | `assets/dtterralith/textures/item/<seed>.png` | ✅ Present (31 files) |
| Old-style models | `assets/dtterralith/models/item/<seed>.json` | ✅ Present (31 files) |
| **New-style item definitions** | **`assets/dtterralith/items/<seed>.json`** | **❌ MISSING** (only 4 branch item files exist) |

The Dynamic Trees `SeedItemModelGenerator` only generates `models/item/` files via the old NeoForge `ItemModelProvider` system. It does **not** generate the new `items/` format required by Minecraft 26.1.

## Affected Seeds (31 total)

Species with `"generate_seed": true`:

1. `amethyst_seed`
2. `ancient_seed`
3. `birch_pine_seed`
4. `black_poplar_seed`
5. `blue_pine_seed`
6. `cedar_seed`
7. `dwarf_pine_seed`
8. `dwarf_spruce_seed`
9. `ebony_seed`
10. `giga_spruce_seed`
11. `jacaranda_seed`
12. `kapok_seed`
13. `larch_seed`
14. `lush_acacia_seed`
15. `maple_seed`
16. `mega_ancient_seed`
17. `mega_blue_pine_seed`
18. `mega_larch_seed`
19. `mega_mirage_seed`
20. `mega_oasis_seed`
21. `mega_orange_pine_seed`
22. `mega_red_pine_seed`
23. `mirage_seed`
24. `moonlight_pine_seed`
25. `moonlight_spruce_seed`
26. `oasis_seed`
27. `orange_pine_seed`
28. `poplar_seed`
29. `red_pine_seed`
30. `sakura_seed`
31. `silverleaf_poplar_seed`

## Required Fix

For each affected seed, create a file at `src/generated/resources/assets/dtterralith/items/<seed>.json` with the following content:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "dtterralith:item/<seed>"
  }
}
```

This bridges the gap between the new Minecraft 26.1 item model system and the existing `models/item/` files that Dynamic Trees' data generator already creates.

Alternatively, a programmatic solution could be implemented in the addon's Java code or in the Dynamic Trees data generation pipeline to auto-generate these files, which would be more maintainable.
