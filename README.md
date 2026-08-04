# Salt & Pepper

Salt and black pepper as food seasonings for Minecraft **1.21.1** / **NeoForge 21.1.248**, both
obtained through processes that loosely mirror real life.

Seasonings work on **every food from every mod** — there are no per-food "seasoned" item variants.
A seasoned food is the same item with a modified `minecraft:food` component and a
`saltandpepper:seasonings` component recording what has been applied.

- **Mod id:** `saltandpepper`
- **License:** MIT
- **Java:** 21

---

## Building

```bash
./gradlew build          # jar lands in build/libs/saltandpepper-1.0.0.jar
./gradlew runClient      # dev client
./gradlew runServer      # dev server (needs run/eula.txt with eula=true)
```

Versions are pinned in `gradle.properties` (NeoForge `21.1.248`, Parchment `1.21.1:2024.11.17`).

---

## Progression

### Pepper

```
Wild or planted pepper vine
        │  right-click harvest at age 2  ->  2-3 green peppercorns, resets to age 0
        v
  green_peppercorns
        │  boil: water cauldron sitting on a heat source
        │        - OR - Farmer's Delight cooking pot (3 -> 3, optional)
        v
 blanched_peppercorns
        │  dry: furnace 200t / campfire 600t   (deliberately NOT a smoker - drying isn't smoking)
        v
  black_peppercorns
        │  grind: shapeless 1 -> 2
        │        - OR - Farmer's Delight cutting board + knife (1 -> 3, optional)
        v
   ground_pepper            <- seasoning
```

This mirrors real black pepper production: green drupes are blanched in hot water, then dried until
they blacken and shrivel, then ground.

**Cauldron blanching.** Right-click a `minecraft:water_cauldron` (level >= 1) holding green
peppercorns, main hand, not sneaking, with a block from `#saltandpepper:heat_sources` directly
below it. Campfires must additionally be lit. Converts up to `blanchBatchSize` (default 8) at once,
consumes one level of water, and plays `block.fire.extinguish` with a puff of cloud particles. This
is an interaction event handler — the vanilla cauldron block is not overridden or replaced.

### Salt

```
rock_salt_ore / deepslate_rock_salt_ore
        │  mine with ANY pickaxe (no tier requirement - salt is soft)
        v
   raw_rock_salt            1-3 per ore, Fortune applies (ore_drops), Silk Touch drops the block
        │  smelt 200t / blast 100t
        v
      salt                  <- seasoning        (9 salt <-> 1 salt_block)
```

Ore generates from y -16 to 64 in all overworld biomes at a low rate, with a second, higher-rate
placement in beaches, oceans and dripstone caves. Measured density: **~350 ore blocks per 128x128
column**, against ~3450 coal ore in the same volume — about a tenth as common, a minor resource
rather than another coal.

---

## The pepper vine

`saltandpepper:pepper_vine` is modelled directly on vanilla `CocoaBlock`: `facing` + `age` 0–2,
same random-tick growth pacing, bone meal advances one stage. The block models and blockstate are
copied verbatim from vanilla `cocoa_stage0/1/2.json` and `cocoa.json` with only the texture
references changed, so the pods sit on the log face exactly like cocoa.

- Attaches to anything in `#saltandpepper:pepper_vine_supports` (ships as `#minecraft:jungle_logs`,
  so packs can add modded jungle logs).
- `pepper_seeds` can only be placed against the side of a supporting log; the vine breaks and drops
  if that log is removed.
- Right-click a mature vine to harvest 2–3 green peppercorns and reset it to age 0 (no seeds).
- Breaking it drops 1 seed, plus 2–3 green peppercorns if it was at age 2.

**Wild generation.** Vanilla attaches cocoa via a tree decorator baked into the jungle tree
features, which can't be extended without overwriting them. Instead a small custom feature runs in
`vegetal_decoration`, walks vertical columns near the origin looking for supporting logs with a
horizontally-exposed side, and hangs a mature (`age=2`) vine there. Measured density in a real
jungle: **~10 vines per 64 chunks**, against 23 vanilla cocoa pods in the same area — a bit rarer
than cocoa, so jungles feel foraged rather than infested.

---

## Seasoning system

A single special crafting recipe (`saltandpepper:seasoning`) combines **one food** + **one
seasoning**:

- the food must have a `minecraft:food` component, be in `#saltandpepper:seasonable`
  (defaults to `#c:foods`), and not be in `#saltandpepper:seasoning_blacklist`;
- the seasoning must be in `#saltandpepper:seasonings` and not already applied to that food.

The result is a copy of the food with `nutrition + bonusNutrition` and
`saturationModifier + bonusSaturationModifier`, everything else (canAlwaysEat, eatSeconds, effects,
usingConvertsTo) copied through unchanged, plus the seasoning's id appended to its
`saltandpepper:seasonings` list. Nutrition is clamped to `maxNutrition` (20), the saturation
modifier to 2.0.

Seasoned foods get one gray italic tooltip line — `Seasoned: Salt, Pepper`. The item is never
renamed.

> **Saturation maths.** In 1.21.1 `FoodProperties` stores *absolute* saturation, not the modifier,
> related by `saturation = nutrition x modifier x 2`. The recipe recovers the food's existing
> modifier, adds the bonus, and converts back — so the bonus scales with how filling the food
> already is. One nutrition point is half a hunger shank; salt + pepper together give +2 nutrition.

Because this is a `CustomRecipe`, it will **not** appear in JEI/EMI. Advancements for first
obtaining `ground_pepper` and `salt` ship as a nudge instead.

### Config — `saltandpepper-common.toml`

| Key | Default | Meaning |
|---|---|---|
| `seasoning.maxNutrition` | `20` | Upper clamp on a seasoned food's nutrition |
| `pepper.bonusNutrition` | `1` | Nutrition added by ground pepper |
| `pepper.bonusSaturationModifier` | `0.2` | Saturation modifier added by ground pepper |
| `salt.bonusNutrition` | `1` | Nutrition added by salt |
| `salt.bonusSaturationModifier` | `0.2` | Saturation modifier added by salt |
| `salt.enableSalt` | `true` | Master switch for this mod's salt content |
| `pepper_vine.restrictGrowthToJungle` | `false` | When true, vines only advance age in jungle biomes |
| `pepper_vine.blanchBatchSize` | `8` | Max peppercorns converted per cauldron interaction |

Ground pepper uses the `pepper.*` bonuses; everything else in `#saltandpepper:seasonings` — this
mod's salt, Salt: Renewed's salt, and anything a pack adds — uses the `salt.*` bonuses.

### Tags

| Tag | Ships with |
|---|---|
| `#saltandpepper:seasonings` (item) | `ground_pepper`, `salt`, and `salt:salt` (optional entry) |
| `#saltandpepper:seasonable` (item) | `#c:foods` |
| `#saltandpepper:seasoning_blacklist` (item) | golden apples, suspicious stew, rotten flesh, poisonous potato, chorus fruit |
| `#saltandpepper:heat_sources` (block) | campfire, soul campfire, fire, soul fire, lava, magma block |
| `#saltandpepper:pepper_vine_supports` (block) | `#minecraft:jungle_logs` |

---

## Mod compatibility

### Farmer's Delight — optional

Declared `optional` / `AFTER` in `neoforge.mods.toml`. Two extra recipes ship under
`data/saltandpepper/recipe/compat/`, each wrapped in a `neoforge:mod_loaded` condition. The jar
contains **no Farmer's Delight classes or imports** — JSON only. Their exact schemas were taken
from a real FD 1.21.1 jar (note `cutting` results nest the stack under `"item"`, and `cookingtime`
is optional).

The condition genuinely short-circuits before recipe-type dispatch, so nothing breaks when FD is
absent: `ConditionalOps.ConditionalDecoder` returns `Optional.empty()` without ever invoking the
inner codec, which is where the serializer lookup happens.

### Salt: Renewed — optional

If mod id `salt` is loaded, this mod's salt stands down automatically and logs one INFO line:

- ore worldgen is skipped via a custom `saltandpepper:salt_enabled` placement modifier;
- `raw_rock_salt`, `salt` and `salt_block` are hidden from the creative tab;
- the salt ore/refining/storage recipes drop out via `neoforge:not(neoforge:mod_loaded)`;
- their `salt:salt` is already an optional entry in `#saltandpepper:seasonings`, so it drives this
  mod's seasoning system instead.

Registry entries can't be conditional, so the items are always registered — only visibility,
worldgen and recipes are gated.

---

## Notes on the 1.21.1 API

A few things differ from what is commonly assumed for this version; each is commented at the
relevant place in the source:

1. **No custom `RecipeType`.** `CraftingRecipe#getType()` returns `RecipeType.CRAFTING`, which is
   what the crafting menu queries, and the JSON `"type"` field dispatches against the *serializer*
   registry. Registering a custom RecipeType and returning it would make the recipe never fire —
   vanilla's `ArmorDyeRecipe` works the same way. Only a serializer is registered here.
2. **`FoodProperties` does have `usingConvertsTo` on 1.21.1** (it is not 1.21.2+ only), and it
   stores `saturation`, not `saturationModifier`. See the saturation note above.
3. **Biome modifiers can't be disabled at runtime**, so the Salt: Renewed worldgen guard lives in a
   placement modifier rather than in the biome modifier.
4. **Tag entries can't carry `neoforge:conditions`** and a jar can only ship one file per tag path,
   so the conditional `salt:salt` entry uses vanilla's `{"id": ..., "required": false}` instead.
5. `#c:foods` exists and is populated on this NeoForge build; `c:tools/knife` is provided by
   Farmer's Delight itself, which is fine because that recipe only loads when FD is present.

Confirmed as stated in the original spec: singular datapack folders (`recipe/`, `loot_table/`,
`advancement/`, `tags/item/`), `assets/<ns>/models/item/*.json` with `minecraft:item/generated`,
`"id"` (not `"item"`) in recipe results, `ItemInteractionResult` from `useItemOn`, and
`CraftingInput` in `Recipe#matches`.

---

## Textures

All art is currently **flat-colour placeholders**. See [TEXTURES.md](TEXTURES.md) for the full
manifest, plus a section listing the exact per-face UV rectangles each pepper vine stage model
samples, read out of the final model JSONs.
