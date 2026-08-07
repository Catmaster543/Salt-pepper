# PARITY — NeoForge 26.1

## How to use this file

Reference build: NeoForge 1.21.1 on `main-neoforge-1.21.1`. Every line below means "behaves the same
as the reference build," not "works."

**Verification column** — be honest, this is the whole point:

| Code | Meaning |
|---|---|
| A | Agent-verified — checked in code, in the build output, or via `git diff` |
| H | Human-verified in-game on this port |
| C | Comparative — verified against the reference build side by side, same world seed where relevant |

Lines marked **[H]** or **[C]** in the checklist below cannot be signed off by an agent. An agent that
marks them A has not tested them.

Leave a line unchecked rather than guessing. An unchecked line is information; a wrongly-checked line
is a bug that ships.

## Port details

- **Target:** NeoForge 26.1 — Minecraft **26.1.2**
- **Branch:** `Neoforge-26.1`
- **Reference commit on main:** `e1f53fb` on `main-neoforge-1.21.1` (there is no bare `main` ref in
  this repo)
- **Pinned versions** — all exact, no ranges:

  | Component | Pin | Note |
  |---|---|---|
  | NeoForge | `26.1.2.94` | latest stable 26.1.2.x; resolves NeoForm `26.1.2-1` |
  | Minecraft | `26.1.2` | read from the NeoForge POM, not assumed |
  | ModDevGradle | `2.0.143` | already above the 2.0.141 floor; unchanged from main |
  | Gradle | `9.2.1` | already above the 9.1.0 floor; wrapper unchanged from main |
  | Java toolchain | `25` | raised from 21 |
  | Parchment | *removed* | obfuscation gone in 26.1, Mojang's own parameter names ship in the sources |

- **Test world seed used for comparisons:** none — no comparative testing was performed, see §11
- **Completed by / date:** Claude (agent pass), 2026-08-07. **Agent pass only — no human or
  comparative testing has been done.**

---

## 1. Build and structure

| ✓ | Check | How |
|---|---|---|
| ☑ | Builds without warnings that reference missing resources | A |
| ☑ | Mod id and namespace are `saltandpepper` | A |
| ☑ | `git diff main` shows no changes under `textures/` or `lang/` | A |
| ☑ | Every registry id identical to reference (items, blocks, recipes, tags, advancements) | A |
| ☑ | Component keys `saltandpepper:seasonings` and `saltandpepper:shaker_uses` unchanged | A |
| ☑ | All balance numbers identical: seasoning bonuses, shaker capacity, refill cost, ore density settings, drop counts, smelting times | A |
| ☑ | No Java imports of Farmer's Delight classes anywhere | A |

**Evidence.** `gradlew build` clean. `git diff e1f53fb -- "*/textures/*"` and `-- "*/lang/*"` both
return empty. Registry-name string literals across all seven registration classes compare `IDENTICAL`
to the reference. `Config.java` is **byte-identical** to main, which covers every seasoning bonus,
shaker capacity and refill cost in one stroke. Ore density lives in `worldgen/placed_feature/` and
drop counts in `loot_table/`, both of which `git diff` shows as untouched. Smelting times are
`cookingtime` fields inside the recipe files — those files did change (see §10 D3), but the change was
proven to be *purely* the ingredient-syntax rewrite (see below), so every `cookingtime` and
`experience` value is preserved. `grep -i "farmersdelight|vectorwing"` over all Java returns nothing.

## 2. Content parity

| ✓ | Check | How |
|---|---|---|
| ☑ | All 7 base items registered under identical ids | A |
| ☑ | All 3 shaker items registered under identical ids | A |
| ☑ | All blocks registered under identical ids (+ optional salt block) | A |
| ☐ | Creative tab contains the same entries in the same order **[H]** | H |
| ☐ | All lang keys resolve; no raw keys visible in-game **[H]** | H |
| ☐ | All textures and models render correctly, including the three vine stages **[H]** | H |

**On the model check specifically** — this is the condition attached to skipping datagen, so it gets a
straight answer. I could not open the creative tab; an agent cannot drive the GUI. What I did instead:

1. All 13 `assets/saltandpepper/items/*.json` were **generated from the `models/item/` directory
   listing**, not typed, and verified 1:1 against it. The format was copied verbatim from vanilla
   26.1's own `assets/minecraft/items/apple.json`.
2. I then **deliberately injected two faults** and re-ran the client to find out whether such errors
   are actually silent, which was the stated worry:
   - typo'd model path with an illegal character → **caught**, `ClientItemInfoLoader: Couldn't parse
     item model 'saltandpepper:salt'`
   - realistic typo, valid path that doesn't exist (`item/slat`) → **caught**, `ModelDiscovery:
     Missing block model: saltandpepper:item/slat`
   - **a missing `items/*.json` entirely → NOT caught, completely silent**
3. The clean run shows neither message for any of our 13 items.

So the typo class is covered by the log, and the silent class is covered by generating the files from
the directory listing rather than by hand. What remains genuinely unverified is whether each item
renders the *right* texture — that needs eyes, and the line stays unchecked.

## 3. Pepper chain

| ✓ | Check | How |
|---|---|---|
| ☐ | Seeds place only on supporting logs; rejected elsewhere **[H]** | H |
| ☐ | Vine breaks and drops when its supporting log is removed **[H]** | H |
| ☐ | Three growth stages render correctly and match the reference visually **[C]** | C |
| ☐ | Bone meal advances growth **[H]** | H |
| ☐ | Right-click harvest at max age yields peppercorns and resets the stage **[H]** | H |
| ☐ | Breaking at each age drops the correct items **[H]** | H |
| ☐ | Cauldron over a lit campfire converts green → blanched, consumes one water level, plays sound and particles **[H]** | H |
| ☐ | Cauldron step correctly refuses: unlit campfire, no heat source, empty cauldron, sneaking **[H]** | H |
| ☑ | Furnace and campfire drying both work | A |
| ☐ | Smoker and blast furnace correctly do not dry peppercorns **[H]** | H |
| ☑ | Grinding yields 2× ground pepper | A |

Drying and grinding are data-driven recipes; both now load (server reports 1526 recipes, up from 1516
before the syntax fix, i.e. all 10 previously-failing files recovered) and both were proven
byte-equivalent to main modulo ingredient syntax. `PepperVineBlock` needed three real changes —
`updateShape`'s new parameter list, `Level#isClientSide`/`#random` becoming accessor-only, and
`InteractionResult.SUCCESS` — each copied from vanilla `CocoaBlock`/`SweetBerryBushBlock` rather than
invented, but none of it has been exercised in game.

## 4. Salt chain

| ✓ | Check | How |
|---|---|---|
| ☐ | Ore generates at comparable density to the reference, same seed **[C]** | C |
| ☐ | Deepslate variant appears below y=0 **[H]** | H |
| ☑ | Any pickaxe mines it | A |
| ☐ | Silk Touch and Fortune behave identically to the reference **[C]** | C |
| ☑ | Smelting and blasting both refine raw rock salt | A |

Both ore blocks still omit `requiresCorrectToolForDrops`, and the `#minecraft:mineable/pickaxe` tag
file is untouched. Loot tables (Silk Touch, Fortune `ore_drops`) are untouched, so the *data* is
identical — but "behaves identically" is a [C] line and stays unchecked.

## 5. Worldgen

| ✓ | Check | How |
|---|---|---|
| ☐ | Wild vines generate in jungle, bamboo jungle and sparse jungle **[H]** | H |
| ☐ | Vine frequency comparable to reference, same seed — spottable but not spammy **[C]** | C |
| ☐ | Modded jungle biomes receive generation (test with any biome mod) **[H]** | H |
| ☐ | Nothing generates where it shouldn't (no vines outside jungles, no salt in the Nether/End) **[H]** | H |

Unverified, but lower risk than on the Fabric port: every worldgen file — the two configured features,
the three placed features and the three `neoforge:add_features` biome modifiers — is **untouched**, and
all of them parsed without error on the dedicated server. `WildPepperVineFeature` and
`SaltEnabledFilter` compiled without a single change. The `ChunkPos` API did change in 26.1
(`containing`/`pack`/`unpack`) but this mod never used it.

## 6. Seasoning system

| ✓ | Check | How |
|---|---|---|
| ☑ | Salt and pepper each apply to vanilla foods via crafting | A |
| ☐ | Both apply to at least one modded food **[H]** | H |
| ☑ | Same seasoning cannot be applied twice | A |
| ☐ | Salt + pepper stack to the full combined bonus **[H]** | H |
| ☐ | Resulting nutrition and saturation match the reference exactly **[C]** | C |
| ☐ | Tooltip line appears and reads correctly **[H]** | H |
| ☑ | Blacklisted items are rejected | A |
| ☐ | Config values change the bonus as expected **[H]** | H |

The A lines mean "matching logic is unchanged from the reference and the recipe is registered and
loads", not "observed in game". The eligibility logic in `SeasoningHelper` is untouched apart from the
`ResourceLocation`→`Identifier` rename.

**The one line to watch is "nutrition and saturation match the reference exactly" [C].** The arithmetic
is unchanged and `FoodConstants.saturationByModifier` survives 26.1 verbatim
(`nutrition * modifier * 2.0F`), so the numbers *should* be identical — but the food component was
split and this is the single most consequential behavioural surface in the port. See §10 D2.

## 7. Shakers

| ✓ | Check | How |
|---|---|---|
| ☐ | Seasons a full stack of vanilla food in one right-click **[H]** | H |
| ☐ | Seasons a modded food identically **[H]** | H |
| ☐ | Refuses already-seasoned, blacklisted, and non-food items, falling through to normal vanilla click behaviour **[H]** | H |
| ☐ | Both shakers apply to the same food, stacking the bonus **[H]** | H |
| ☐ | Insufficient uses → nothing happens, clear feedback, no partial seasoning, no items lost **[H]** | H |
| ☐ | Refill works by clicking the shaker onto a pile of its seasoning **[H]** | H |
| ☐ | `empty_shaker` clicked onto a pile of seasoning fills it **[H]** | H |
| ☐ | Becomes `empty_shaker` at zero uses, never disappears **[H]** | H |
| ☐ | Fill bar renders with the right colour and proportion **[H]** | H |
| ☐ | Not enchantable at a table or anvil **[H]** | H |
| ☐ | Works in the survival inventory, a crafting table grid, and a chest GUI **[H]** | H |

Unverified in game — but the feared rework did not happen. `Item#overrideStackedOnOther` and
`#overrideOtherStackedOnMe` survive 26.1 with **byte-identical signatures**, and vanilla `BundleItem`
still uses both, so the structural reference the shakers were derived from is intact. What bundles
*did* change is how they read their contents (`getSelectedItem` now returns an `ItemStackTemplate`),
which the shakers never used. The only edits to `ShakerItem` were the `Identifier` rename, the
`appendHoverText` signature (§10 D4) and `sendSystemMessage` (§10 D5). No interaction logic was
touched.

## 8. Robustness

| ✓ | Check | How |
|---|---|---|
| ☐ | Boots with only this mod installed **[H]** | H |
| ☐ | Boots with Farmer's Delight present; compat recipes appear **[H]** | H |
| ☐ | Boots with Farmer's Delight absent; no errors, no recipe-type spam **[H]** | H |
| ☐ | Boots alongside Salt: Renewed if it exists for this target; built-in salt disables cleanly **[H]** | H |
| ☐ | Dedicated server + separate client: no desync on any interaction **[H]** | H |
| ☐ | Shakers under rapid repeated clicking on a server: no duplication, no item loss **[H]** | H |
| ☐ | No crash when the cauldron empties on the final interaction **[H]** | H |
| ☐ | World save/reload preserves seasoned foods and shaker fill levels **[H]** | H |

All [H], so all unchecked — but two have strong agent-level evidence worth recording:

- The dev client reaches the title screen and the dev dedicated server reaches `Done (0.289s)!`, both
  with only this mod present and no errors in either log.
- **Farmer's Delight absent** behaves correctly: 1526 recipes and 1621 advancements load with zero
  parse errors and no recipe-type spam. The two `farmersdelight:` recipes are skipped by their
  `neoforge:mod_loaded` condition before the unknown recipe type is ever dispatched.

## 9. Cross-port consistency

| ✓ | Check | How |
|---|---|---|
| ☑ | `sync-shared.sh` reports no unexplained differences in textures or lang | A |
| ☐ | A datapack written for the reference build works here unchanged (tag ids stable) **[H]** | H |
| ☑ | Config keys and defaults match the reference, even if the file format differs | A |

`sync-shared.sh` now exists in the repo; the textures/lang half of the guarantee was additionally
verified directly with `git diff` and is recorded in §1. Config parity is absolute here — `Config.java`
is byte-identical to main and the format is still `saltandpepper-common.toml`, because `ModConfigSpec`
came through 26.1 completely unchanged.

**Caveat on the datapack line:** tag ids are stable, but a third-party datapack that *overrides* one of
this mod's recipes would need its ingredient syntax updated for 26.1 — that is a vanilla-wide change,
not something this port introduced.

## 10. Deviation log

| Deviation | Reason | Permanent? | Player-visible? |
|---|---|---|---|
| **D1** — `ItemBlockRenderTypes.setRenderLayer` call for the pepper vine removed | 26.1 deleted the class. Terrain layers are now `ChunkSectionLayer`s assigned automatically from sprite contents, so a texture with transparent texels lands in cutout unasked. Vanilla `CocoaBlock` likewise has no registration any more | Yes | No, if the automatic assignment behaves — **needs the visual check in §11** |
| **D2** — `SeasoningHelper.season` no longer copies eat-time, effects or `usingConvertsTo` | The food component was split: `FoodProperties` is now `(nutrition, saturation, canAlwaysEat)` and nothing else. Eat duration, animation, sound and consume effects live in `minecraft:consumable`, and `usingConvertsTo` in `minecraft:use_remainder`. We rewrite only `minecraft:food`, so those are preserved by construction instead of by remembering to copy them — strictly safer than the 1.21.1 code | Yes | No — same resulting food behaviour |
| **D3** — All 13 recipe files rewritten to the new ingredient syntax | 26.1 replaced `{"item": "x"}` / `{"tag": "y"}` with the plain forms `"x"` / `"#y"`. 10 of 11 shipping recipes hard-failed to parse before this. Proven mechanical: every file satisfies `new == transform(main)` where `transform` only rewrites ingredient syntax | Yes | No |
| **D4** — `ShakerItem.appendHoverText` signature changed | 26.1 added a `TooltipDisplay` parameter and swapped `List<Component>` for `Consumer<Component>` | Yes | No |
| **D5** — action-bar feedback uses `sendSystemMessage(component, true)` | `ServerPlayer#displayClientMessage` was removed; `sendSystemMessage(Component, boolean overlay)` is the replacement, `overlay = true` being the action bar | Yes | No |
| **D6** — `pepper_seeds` is a plain `BlockItem` with `useItemDescriptionPrefix()` | `ItemNameBlockItem` was removed. This is exactly how vanilla now registers cocoa beans (`createBlockItemWithCustomItemName`), so the item keeps its own translation key as before | Yes | No |
| **D7** — `SeasoningRecipe` restructured: no-arg constructor, `MapCodec`/`StreamCodec` pair, no `canCraftInDimensions` | `CustomRecipe` no longer takes a `CraftingBookCategory`, `SimpleCraftingRecipeSerializer` was deleted in favour of a `RecipeSerializer` record, `assemble` lost its `HolderLookup.Provider`, and `canCraftInDimensions` was replaced by the inherited `placementInfo()`. Matching logic itself is unchanged | Yes | No |
| **D8** — Parchment removed | Obfuscation was removed in 26.1; Mojang's parameter names ship with the sources | Yes | No |
| **D9** — Farmer's Delight compat recipes ship but are untested | FD's `26.1` branch exists upstream but its newest commit (3 May 2026) is titled "(ERRORS) More fixes...", i.e. active development, not a release. The recipes were migrated to the new ingredient syntax alongside the others and are correctly skipped when FD is absent | No — retest when FD 26.1 releases | Only if FD's schema changed too |

**Explicitly not deviations** (both were called out in the brief as things to cover, both turned out to
be non-issues): the mod contains **no `ChunkPos` usage** anywhere, and it ships **no `pack.mcmeta`**, so
there was no pack format to bump. For the record, 26.1.2 is resource-pack format 84.0 / data-pack
format 101.1, and since 1.21.9 a single `pack_format` field is replaced by a `min_format`/`max_format`
range.

## 11. Known-unverified

1. **Everything marked [H] or [C] above.** No human play-testing, no comparative same-seed run. Agent
   pass only.
2. **The creative-tab visual check (§2).** The condition attached to skipping datagen. I could not
   drive the GUI. Partial automated cover is described in §2 — importantly, a *missing* item
   definition is silent in the log, which is why the files were generated from the directory listing
   rather than typed. Whether each item shows the *correct* texture is still unverified.
3. **D1 — the vine's cutout layer.** The highest-value single visual check in this port. The symptom if
   the automatic assignment is wrong is opaque black or white squares around the pod's transparent
   pixels. Two seconds to confirm in game; unverified here.
4. **§6 [C] — seasoned food nutrition/saturation versus the reference.** The arithmetic and the vanilla
   helper are unchanged, but this is the subsystem most affected by the food component split and it
   deserves a side-by-side number check.
5. **The shaker interaction has not been clicked once**, and the cauldron interaction has not been
   triggered once. Both compile and both had their changed lines copied from current vanilla usage.
6. **Farmer's Delight present** (§8) — not installed; see D9.
7. **Salt: Renewed on 26.1** — unknown whether a build exists.
8. **Modded-food seasoning** (§6) — no second food mod installed.
9. **`build/libs` also contains stale `saltandpepper-fabric-1.2.1.jar` and
   `saltandpepper-neoforge-1.2.1.jar`** left over from other branches' builds. They are gitignored
   build output, not artifacts of this port; `gradlew clean` clears them.
10. **Environment note, not a port issue:** this machine runs Avast Web/Mail Shield TLS interception and
    its CA is absent from the JDK truststore, so every Java-side download fails with a PKIX error unless
    the JVM is pointed at the Windows root store
    (`JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`). Deliberately **not** committed —
    a fresh clone on this machine will hit it.
11. **`port-kit/neoforge-26.1-template/` was missing**, so the build-tool versions were sourced from the
    NeoForge 26.1 release notes and the NeoForge Maven repository instead of the MDK. All pins are
    recorded in "Port details" above; they should be diffed against the real MDK if it turns up.

## 12. Sign-off

- ☐ Every line above is either checked, or left unchecked with an entry in §11
- ☑ Deviation log is complete
- ☐ PARITY.md committed on this branch
- ☐ Release tagged `v1.2.1+neoforge-26.1`

**Not signed off.** Sections 2–8 are substantially unverified. The port compiles, builds, and loads
cleanly on both client and dedicated server, and every invariant in the brief is mechanically verified —
that is a different and weaker claim than "it works." It should not be released until a human has
played it, starting with the pepper vine's render layer (§11.3).
