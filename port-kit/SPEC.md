# Agent Prompt — "Salt & Pepper" mod for Minecraft 1.21.1 (NeoForge)

Paste everything from **Part 1** onward into your coding agent. **Part 3** is the texture manual for you (the agent needs it too, so leave it in).

---

# Part 1 — Task

Build a small, clean, self-contained Minecraft mod adding salt and black pepper as food seasonings, both obtained through processes that loosely mirror real life.

- **Minecraft:** 1.21.1
- **Loader:** NeoForge (use the latest 21.1.x)
- **Java:** 21
- **Build:** official NeoForge MDK for 1.21.1 (ModDevGradle), Parchment mappings for 1.21.1
- **Mod id / namespace:** `saltandpepper`
- **Mod name:** Salt & Pepper
- **License:** MIT

Scope discipline: this is a *simple* mod. No new GUIs, no custom multiblocks, no block entities unless unavoidable. Prefer vanilla systems and data-driven JSON over Java wherever possible.

## Critical version notes — verify these against the actual API before writing code

These are 1.21.1-specific and are the usual source of silent breakage. Check each in the decompiled sources / your mappings rather than trusting memory:

1. **Datapack folders are singular in 1.21+**: `data/saltandpepper/recipe/`, `loot_table/`, `advancement/`, `tags/item/`, `tags/block/`, `worldgen/configured_feature/`, `worldgen/placed_feature/`. NeoForge biome modifiers go in `data/saltandpepper/neoforge/biome_modifier/`.
2. **Item models still live in `assets/saltandpepper/models/item/*.json`** with `minecraft:item/generated`. The `assets/<ns>/items/` item-definition format is 1.21.4+ and must **not** be used here.
3. **Recipe result JSON in 1.21 uses `"id"`, not `"item"`** for vanilla recipe types (`{"id": "saltandpepper:ground_pepper", "count": 2}`). Farmer's Delight may differ — dump a real recipe from the FD jar and copy its exact shape.
4. **`FoodProperties` in 1.21.1** is a record with nutrition, saturation modifier, canAlwaysEat, eatSeconds, and an effects list. (`usingConvertsTo` and the split-out `consumable` component are 1.21.2+ — do not use them.)
5. **`BlockBehaviour#useItemOn` returns `ItemInteractionResult` in 1.21.1** (it reverts to `InteractionResult` in 1.21.2).
6. **`Recipe#matches` takes `CraftingInput`** (not `CraftingContainer`), and `assemble` takes `(CraftingInput, HolderLookup.Provider)`.

If any of the above turns out to be wrong for the exact NeoForge build you pull, follow the real API and note the deviation in a comment.

---

## Content overview

| Item id | Purpose |
|---|---|
| `pepper_seeds` | Places the pepper vine on a jungle log |
| `green_peppercorns` | Harvested, unprocessed |
| `blanched_peppercorns` | After boiling |
| `black_peppercorns` | After drying |
| `ground_pepper` | Seasoning item |
| `raw_rock_salt` | Ore drop |
| `salt` | Seasoning item |

| Block id | Purpose |
|---|---|
| `pepper_vine` | Cocoa-pod-style plant on jungle logs |
| `rock_salt_ore` | Stone-variant ore |
| `deepslate_rock_salt_ore` | Deepslate variant |

Single creative tab `saltandpepper:main`, icon `ground_pepper`.

---

## The pepper vine (cocoa-pod style)

`saltandpepper:pepper_vine` — model the implementation directly on vanilla `CocoaBlock`.

- **Blockstate properties:** `facing` (horizontal) and `age` 0–2 (three stages, same as cocoa).
- **Attachment:** must be placed against the side of a block in block tag `saltandpepper:pepper_vine_supports`, which ships containing `#minecraft:jungle_logs`. Keep it a tag so packs can add modded jungle logs.
- **Placement:** right-click the side of a supporting log with `pepper_seeds`. Register `pepper_seeds` as an `ItemNameBlockItem` (or equivalent) pointing at the vine, and implement `getStateForPlacement` to reject non-supporting faces, exactly like cocoa beans.
- **Survival:** breaks and drops when the supporting block is removed (`canSurvive` + `updateShape`).
- **Growth:** random tick, same pacing as cocoa. Bone meal advances one stage.
- **Optional config** `restrictGrowthToJungle` (default **false**): when true, the vine only advances age in biomes tagged `#minecraft:is_jungle` or `#c:is_jungle`.
- **Right-click harvest at age 2:** drops 2–3 `green_peppercorns`, resets `age` to 0, plays `block.sweet_berry_bush.pick_berries`. No seeds from right-click harvest.
- **Breaking:** loot table with a `minecraft:block_state_property` condition — at `age=2` drop 2–3 `green_peppercorns` + 1 `pepper_seeds`; below that, 1 `pepper_seeds`.

### Models — do not author the geometry from scratch

Copy the vanilla `cocoa_stage0.json`, `cocoa_stage1.json`, `cocoa_stage2.json` block models out of the client jar **verbatim**, including every `from`/`to` and every explicit `uv` entry, and change only the texture references to `saltandpepper:block/pepper_vine_stage0/1/2`. Do the same for `blockstates/pepper_vine.json` — copy vanilla `cocoa.json`'s variant/rotation structure.

This guarantees the pods sit correctly on the log face and that the UV layout matches something the artist can reference. **Then**, after the models are final, write the exact per-stage UV regions into `TEXTURES.md` (see Deliverables) — read them out of the model files you produced, don't guess. The artist needs to know precisely which pixel rectangles of each 16×16 texture land on which face.

### Worldgen — wild vines in jungles

Vanilla attaches cocoa via a tree decorator baked into the jungle tree features, which we can't extend without overwriting those features. Instead write a small custom `Feature`:

- Pick random positions in the chunk within a reasonable y band.
- Find blocks in `#saltandpepper:pepper_vine_supports` that have at least one horizontally-exposed (air) side.
- Place `pepper_vine` at `age=2` facing that exposed side, with a low per-log chance so jungles feel foraged rather than infested.
- Register a configured + placed feature in the `vegetal_decoration` step.
- NeoForge `add_features` biome modifier targeting `#minecraft:is_jungle` **and** `#c:is_jungle` so modded jungle analogues are covered.

Tune density so a player wandering a jungle finds their first vine within a minute or two, but a single tree rarely carries more than one or two pods.

---

## Pepper processing chain

```
Wild or planted pepper vine
        │ right-click harvest at age 2
        ▼
  green_peppercorns
        │ boil: water cauldron over a heat source  ──OR──  Farmer's Delight cooking pot (optional)
        ▼
 blanched_peppercorns
        │ dry: furnace smelting (or campfire, slower)
        ▼
  black_peppercorns
        │ grind: shapeless craft, 1 → 2
        ▼
   ground_pepper  ← seasoning item
```

This mirrors real black pepper production: green drupes are blanched in hot water, then dried until they blacken and shrivel, then ground.

### Step 1 — Cauldron blanching (custom code, no block entity)

Implement with a NeoForge `PlayerInteractEvent.RightClickBlock` handler. Do **not** override or replace the vanilla cauldron block.

Conditions, all required:
- Target block is `minecraft:water_cauldron` with `level >= 1`.
- The block **directly below** is in block tag `saltandpepper:heat_sources`. Ship that tag containing `minecraft:campfire`, `minecraft:soul_campfire`, `minecraft:fire`, `minecraft:soul_fire`, `minecraft:lava`, `minecraft:magma_block`. For campfires, additionally require `lit=true` in code, since tags can't express blockstates.
- Held item is `saltandpepper:green_peppercorns`, main hand, player not sneaking.

Effect (server side only; client just receives the success result):
- Consume up to `batchSize` peppercorns (config, default 8) from the stack.
- Return the same count of `blanched_peppercorns` (insert into inventory, drop at the player's feet on overflow).
- Decrement the cauldron water level by 1; at level 0 it reverts to `minecraft:cauldron`.
- Sound `block.fire.extinguish` plus a few white smoke / `minecraft:cloud` particles above the cauldron.
- Cancel the event and set a sided success cancellation result so the arm swings and no other interaction fires.

Handle creative mode (don't consume) and guard against double-firing across sides.

### Step 2 — Farmer's Delight alternative (optional dependency, never required)

- Declare Farmer's Delight in `neoforge.mods.toml` as `type = "optional"`, ordering `AFTER`.
- Ship `data/saltandpepper/recipe/compat/blanched_peppercorns_cooking_pot.json` of type `farmersdelight:cooking`, wrapped in `"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": "farmersdelight"}]`. Ingredients: `green_peppercorns` (consider 2–3 entries for a worthwhile batch), result `blanched_peppercorns`, `cookingtime` ~200.
- **Test that the game boots with FD absent.** The condition should short-circuit before recipe-type dispatch; if it doesn't, move FD recipes to a conditionally-loaded data path instead.
- No Java-side FD imports anywhere. JSON only — the jar must never reference an FD class.

### Step 3 — Drying

- `minecraft:smelting`: `blanched_peppercorns` → `black_peppercorns`, 200 ticks, 0.1 xp.
- `minecraft:campfire_cooking`: same, 600 ticks.
- **No smoker or blast furnace recipe** — drying isn't smoking.

### Step 4 — Grinding

- Shapeless: 1× `black_peppercorns` → 2× `ground_pepper`.
- Optional FD compat under the same condition: `farmersdelight:cutting`, `black_peppercorns` + `#c:tools/knife` → 3× `ground_pepper`.

---

## Salt

Deliberately minimal: mine it, smelt it, season with it. No salt crystals, no evaporation, no ice/snow melting, no dissolving-in-water behavior.

### Ore

- `saltandpepper:rock_salt_ore` (stone base) and `saltandpepper:deepslate_rock_salt_ore`.
- Both in `#minecraft:mineable/pickaxe` with **no tier requirement** — any pickaxe works, salt is soft.
- Hardness/resistance similar to coal ore.
- Loot: Silk Touch drops the block; otherwise 1–3 `raw_rock_salt` with `minecraft:apply_bonus` (`ore_drops` formula) for Fortune. Small xp drop (0–2).

### Worldgen

- Ore feature, vein size ~6–9, y range roughly −16 to 64, uniform-ish placement, modest frequency.
- Use the deepslate variant below y=0 via the standard `TagMatchTest` on `#minecraft:deepslate_ore_replaceables` / `#minecraft:stone_ore_replaceables` two-target setup.
- Bias toward salt-plausible biomes: add the feature to all overworld biomes at a low rate, plus a second higher-rate placement in `#c:is_beach`, `#c:is_ocean`, and `#minecraft:is_ocean` / dripstone caves. Keep the total abundance modest — this should feel like a minor resource, not another coal.

### Refining

- `minecraft:smelting`: 1× `raw_rock_salt` → 1× `salt`, 200 ticks, 0.1 xp.
- `minecraft:blasting`: same, 100 ticks.

*(Note: Salt: Renewed uses a crafting step here instead of smelting. Smelting is intentional in this mod, matching the pepper drying step.)*

### Optional storage block

`saltandpepper:salt_block` — 9× salt ⇄ 1 block, plain decorative/storage. Implement it, but keep it easy to strip: one block, one recipe pair, one texture.

### Compat guard for Salt: Renewed

Config `enableSalt`, default `true`. If the mod id `salt` is loaded at runtime:
- Force `enableSalt` off, log a single INFO line explaining why.
- Disable our ore worldgen (a biome-modifier-level condition or a feature-side early-out), hide `raw_rock_salt`, `salt`, and `salt_block` from the creative tab, and disable their recipes.
- Ship a conditional item tag file adding `salt:salt` to `#saltandpepper:seasonings` under a `neoforge:mod_loaded` condition, so their salt drives our seasoning system instead.

Registry entries can't be conditional, so always register the items — just gate visibility, worldgen, and recipes.

---

## Seasoning system (most important part — read carefully)

**Do not create per-food seasoned item variants.** Transmute the food item in place using data components, so the system works with every food from every mod automatically.

### Custom data component

Register `saltandpepper:seasonings` on `Registries.DATA_COMPONENT_TYPE` — a `List<ResourceLocation>` (or a small record wrapping one) with both a `Codec` and a `StreamCodec`.

### Custom recipe

Register recipe type + serializer `saltandpepper:seasoning` as a `CustomRecipe` (special recipe, `SimpleCraftingRecipeSerializer`), enabled by one JSON at `data/saltandpepper/recipe/seasoning.json`:

```json
{ "type": "saltandpepper:seasoning" }
```

**`matches`** — true when the grid holds exactly:
- one stack that has a `minecraft:food` component, is in `#saltandpepper:seasonable`, and is **not** in `#saltandpepper:seasoning_blacklist`; and
- one stack in `#saltandpepper:seasonings` whose id is not already in the food's `saltandpepper:seasonings` list; and
- nothing else.

**`assemble`** — a copy of the food stack (count 1) with:
- `minecraft:food` replaced by a new `FoodProperties`: `nutrition = old + bonusNutrition`, `saturationModifier = old + bonusSaturationModifier`, all other fields (canAlwaysEat, eatSeconds, effects) copied unchanged.
- `saltandpepper:seasonings` set to the old list plus the applied seasoning's id.

Clamp nutrition to `maxNutrition` (config, default 20) and the saturation modifier to 2.0f.

### Balance / config

NeoForge common config (`saltandpepper-common.toml`):

- `pepper.bonusNutrition` — default `1`
- `pepper.bonusSaturationModifier` — default `0.2`
- `salt.bonusNutrition` — default `1`
- `salt.bonusSaturationModifier` — default `0.2`

Comment in the code that **saturation restored = nutrition × saturationModifier × 2**, so the modifier scales with how filling the food already is. One nutrition point = half a hunger shank. Salt and pepper stack, for +2 nutrition total on a fully seasoned food.

### Tooltip

`ItemTooltipEvent` handler: if a stack has a non-empty `saltandpepper:seasonings` component, append one gray italic line, e.g. `Seasoned: Salt, Pepper`, via lang keys `tooltip.saltandpepper.seasoned`, `seasoning.saltandpepper.salt`, `seasoning.saltandpepper.pepper`. **Do not rename the item.**

### Tags to ship

- `saltandpepper:seasonings` (item) — `ground_pepper`, `salt`.
- `saltandpepper:seasonable` (item) — defaults to `#c:foods`. Verify that tag exists on this NeoForge build; if named differently, use the real one and note it.
- `saltandpepper:seasoning_blacklist` (item) — `minecraft:golden_apple`, `minecraft:enchanted_golden_apple`, `minecraft:suspicious_stew`, `minecraft:rotten_flesh`, `minecraft:poisonous_potato`, `minecraft:chorus_fruit`.
- `saltandpepper:heat_sources` (block), `saltandpepper:pepper_vine_supports` (block).

### Recipe viewer note

A `CustomRecipe` won't show in JEI/EMI. Acceptable for v1. Add an advancement granted on first obtaining `ground_pepper` and one for `salt` so players get a nudge.

---

## Deliverables

1. Compiling, runnable NeoForge 1.21.1 mod under sensible packages (e.g. `com.example.saltandpepper` — rename the root as appropriate).
2. All resources at the exact paths in Part 3. Datagen is fine, but **final built paths must match Part 3 exactly** — the user supplies textures by hand.
3. **Placeholder textures** at every path in the manifest (flat colors / obvious placeholder patterns, distinguishable from each other) so the mod runs before real art exists. Never invent texture paths outside the manifest.
4. `en_us.json` with every lang key filled.
5. `TEXTURES.md` at the repo root: Part 3 verbatim, **plus** an added section listing the exact UV rectangles each pepper vine stage model samples, per face, read from the final model JSONs.
6. `README.md` with build instructions and both progression chains.
7. `src/main/resources/icon.png` referenced from `neoforge.mods.toml` via `logoFile`.

## Acceptance checklist

- [ ] Boots with only this mod installed.
- [ ] Boots with Farmer's Delight installed; FD recipes work.
- [ ] Boots with Farmer's Delight **absent**; no errors, no missing-recipe-type spam.
- [ ] Boots alongside Salt: Renewed; built-in salt auto-disables, no duplicate ore veins, their salt seasons food through our system.
- [ ] Wild pepper vines generate on jungle tree trunks in jungle, bamboo jungle, and sparse jungle — visible but not spammy.
- [ ] `pepper_seeds` can only be placed on the side of a supporting log; the vine breaks if the log is removed.
- [ ] Full pepper chain completable in survival: find vine → harvest → cauldron over lit campfire → furnace → craft → season cooked beef.
- [ ] Full salt chain completable: mine ore with a wooden pickaxe → smelt → season.
- [ ] A food cannot receive the same seasoning twice; salt + pepper both apply and both show in the tooltip.
- [ ] No crash when the cauldron empties on the final interaction.
- [ ] Multiplayer-safe: no client-side duplication, no cauldron level desync.

---

# Part 2 — Ambiguities to resolve by asking, not guessing

- Exact NeoForge and Parchment versions available at build time.
- Whether `c:foods` is the correct common food tag on this build.
- Whether the FD `neoforge:mod_loaded` condition genuinely short-circuits before recipe-type dispatch on 1.21.1.
- Whether disabling an already-registered biome modifier at runtime is cleanly possible, or whether the salt worldgen guard belongs inside the feature instead.

---

# Part 3 — Texture manifest (the manual)

Everything lives under `src/main/resources/assets/saltandpepper/textures/`.

**General rules**
- Format: **PNG-32 with alpha** (RGBA). Indexed or greyscale PNGs can render oddly — save as full RGBA.
- Size: **16×16** for everything below. Higher is allowed (32×32, 64×64) if square and a power of two, but stay consistent across the whole set or the mod looks mismatched.
- Hard pixel edges read better than gradients at this size; 4–8 colors per texture is plenty.
- Filenames are **case-sensitive and must match exactly** — a typo means a magenta/black checker.
- Animated textures would need a matching `<name>.png.mcmeta`; none of these need one.

## Items — `textures/item/`

| File | Purpose | What it should read as |
|---|---|---|
| `pepper_seeds.png` | Plantable seed | Small scattered seeds, vanilla seed silhouette. Must read as "plantable" and stay distinct from the peppercorns. |
| `green_peppercorns.png` | Fresh harvest | Cluster of round green berries, bright and fresh. |
| `blanched_peppercorns.png` | After boiling | Same silhouette, duller olive / slightly darkened — clearly the same thing, changed. |
| `black_peppercorns.png` | After drying | Same cluster, dark brown-black and wrinkled. Highest contrast of the three. |
| `ground_pepper.png` | Seasoning | A pinch or small pile of dark powder. Must be instantly distinguishable from whole peppercorns in a hotbar. |
| `raw_rock_salt.png` | Ore drop | Chunky, irregular off-white/grey mineral lumps. Should read as *unrefined* — rough, dirty, rock-like. |
| `salt.png` | Seasoning | Fine white crystals. Same silhouette family as `ground_pepper` so the pair reads as a matched set. |

## Blocks — `textures/block/`

### Pepper vine (3 stages)

| File | Stage |
|---|---|
| `pepper_vine_stage0.png` | Young — small, mostly green |
| `pepper_vine_stage1.png` | Growing — larger, colouring up |
| `pepper_vine_stage2.png` | **Mature / harvestable** — full peppercorn cluster, visually obvious |

**Important — these are not simple full-face textures.** They use vanilla cocoa's UV layout: the pod is a small box, and different pixel rectangles of the 16×16 image map to its side faces, its top/bottom, and the little stem plane connecting it to the log. Each stage uses a *different-sized* region, because the pod grows.

Before drawing, open the vanilla files as templates:
```
assets/minecraft/textures/block/cocoa_stage0.png
assets/minecraft/textures/block/cocoa_stage1.png
assets/minecraft/textures/block/cocoa_stage2.png
```
(extract from the client jar, or from your resource pack tooling). Draw into the same regions those use. The agent will also write the exact per-face UV rectangles into `TEXTURES.md` once the models are final — check that section if anything renders wrong.

Stage 2 is what generates wild in jungles, so it needs to be spottable against jungle foliage while still looking at home there.

### Salt ore

| File | Purpose | Notes |
|---|---|---|
| `rock_salt_ore.png` | Stone-variant ore | Draw the salt specks over vanilla stone-grey background so it blends with surrounding stone. Pale, slightly translucent-looking crystals. |
| `deepslate_rock_salt_ore.png` | Deepslate variant | Identical crystal pattern over the darker deepslate background. Copy the ore-speck placement from the stone version so they read as the same ore. |
| `salt_block.png` | Storage block (optional) | All six faces use this one texture. Dense packed white crystal. |

## Non-texture image

| File | Location | Size | Purpose |
|---|---|---|---|
| `icon.png` | `src/main/resources/icon.png` | 128×128 or 256×256 | Mod list icon. Not a 16×16 block texture — make this a proper illustrated logo. |

## How to swap in your art

1. Build and run once with placeholders to confirm everything loads.
2. Drop your PNGs over the placeholders at identical paths and filenames.
3. In a dev environment, **F3 + T** reloads resources without restarting.
4. Magenta/black checker = missing or misnamed file. Stretched or warning-spamming = wrong dimensions. The log names the exact failing path in `Failed to load texture` lines.

## Full path reference (copy-paste)

```
src/main/resources/
├── icon.png
└── assets/saltandpepper/
    ├── lang/en_us.json
    ├── models/          (agent-generated — don't edit)
    ├── blockstates/     (agent-generated — don't edit)
    └── textures/
        ├── item/
        │   ├── pepper_seeds.png
        │   ├── green_peppercorns.png
        │   ├── blanched_peppercorns.png
        │   ├── black_peppercorns.png
        │   ├── ground_pepper.png
        │   ├── raw_rock_salt.png
        │   └── salt.png
        └── block/
            ├── pepper_vine_stage0.png
            ├── pepper_vine_stage1.png
            ├── pepper_vine_stage2.png
            ├── rock_salt_ore.png
            ├── deepslate_rock_salt_ore.png
            └── salt_block.png        (optional)
```

**Total for v1: 12 textures at 16×16, plus one mod icon** (13 with the optional salt block). The vine dropping from 4 crop stages to 3 cocoa stages saved you one; salt added three.
