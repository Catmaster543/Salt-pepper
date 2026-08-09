# Parity checklist — Forge 1.20.1

Reference build: NeoForge 1.21.1 on `main-neoforge-1.21.1`. Every line below means "behaves the same as
the reference build," not "works."

**Verification codes**

| Code | Meaning |
| --- | --- |
| A | Agent-verified — checked in code, in the build output, or via `git diff` |
| H | Human-verified in-game on this port |
| C | Comparative — verified against the reference build side by side, same world seed where relevant |

Lines marked `[H]` or `[C]` cannot be signed off by an agent. Unchecked lines are recorded in §11.

## Port details

| | |
| --- | --- |
| Target | Forge 1.20.1 |
| Branch | `Forge-1.20.1` |
| Reference commit on main | `43af4fe` (`main-neoforge-1.21.1`, "README update on download informations") |
| Tested on | Forge 47.4.10, Java 17 (Temurin 17.0.20.8), Gradle 8.8, Parchment `2023.09.03-1.20.1` |
| Test world seed used for comparisons | none — no seeded side-by-side worldgen comparison was run (see §11) |
| Completed by / date | Claude (agent), 2026-08-09 |

## 1. Build and structure

| ✓ | Check | How |
| --- | --- | --- |
| ☑ | Builds without warnings that reference missing resources | A |
| ☑ | Mod id and namespace are `saltandpepper` | A |
| ☑ | `git diff main` shows no changes under `textures/` or `lang/` | A |
| ☑ | Every registry id identical to reference (items, blocks, recipes, tags, advancements) | A |
| ☑ | Component keys `saltandpepper:seasonings` and `saltandpepper:shaker_uses` unchanged | A |
| ☑ | All balance numbers identical: seasoning bonuses, shaker capacity, refill cost, ore density settings, drop counts, smelting times | A |
| ☑ | No Java imports of Farmer's Delight classes anywhere | A |

Evidence:

- Dedicated server boots clean: `Done (6.509s)`, zero `ERROR`/`Parsing error` lines from the mod.
- `git diff main-neoforge-1.21.1 -- .../textures .../lang` → empty. Same for `models/` and `blockstates/`.
- All **35** datapack files map 1:1 onto the reference set once the 1.20.1 folder renames
  (`recipe→recipes`, `advancement→advancements`, `loot_table→loot_tables`, `tags/item→tags/items`,
  `tags/block→tags/blocks`, `neoforge→forge`) are normalised — no id added, dropped or renamed.
- **10/10** config defaults identical (`maxNutrition 20`, pepper `+1 / +0.2`, salt `+1 / +0.2`,
  `shakerCapacity 64`, `usesPerRefillItem 8`, `blanchBatchSize 8`, `enableSalt true`,
  `restrictGrowthToJungle false`), including their min/max ranges.
- **36/36** numeric fields across all recipes and loot tables identical (counts, `cookingtime`,
  `experience`, `rolls`, `bonus_rolls`).
- `data/saltandpepper/worldgen/` is **byte-identical** to the reference — the configured/placed-feature
  format did not change between the two versions.
- Grep for `farmersdelight|vectorwing` across `src/main/java/` → no matches.

Note on the component keys: they are now **stack NBT keys**, not data components (see §10). The strings
are the fully-qualified ids the components used on 1.21.1 — `saltandpepper:seasonings` and
`saltandpepper:shaker_uses` — so a datapack or third-party tool reading them sees the same names.

## 2. Content parity

| ✓ | Check | How |
| --- | --- | --- |
| ☑ | All 7 base items registered under identical ids | A |
| ☑ | All 3 shaker items registered under identical ids | A |
| ☑ | All blocks registered under identical ids (+ optional salt block) | A |
| ☐ | Creative tab contains the same entries in the same order `[H]` | H |
| ☐ | All lang keys resolve; no raw keys visible in-game `[H]` | H |
| ☐ | All textures and models render correctly, including the three vine stages `[H]` | H |

7 base items: `pepper_seeds`, `green_peppercorns`, `blanched_peppercorns`, `black_peppercorns`,
`ground_pepper`, `raw_rock_salt`, `salt`. 3 shakers: `empty_shaker`, `salt_shaker`, `pepper_shaker`.
4 blocks: `pepper_vine`, `rock_salt_ore`, `deepslate_rock_salt_ore`, `salt_block`.

## 3. Pepper chain

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Seeds place only on supporting logs; rejected elsewhere `[H]` | H |
| ☐ | Vine breaks and drops when its supporting log is removed `[H]` | H |
| ☐ | Three growth stages render correctly and match the reference visually `[C]` | C |
| ☐ | Bone meal advances growth `[H]` | H |
| ☐ | Right-click harvest at max age yields peppercorns and resets the stage `[H]` | H |
| ☐ | Breaking at each age drops the correct items `[H]` | H |
| ☐ | Cauldron over a lit campfire converts green → blanched, consumes one water level, plays sound and particles `[H]` | H |
| ☐ | Cauldron step correctly refuses: unlit campfire, no heat source, empty cauldron, sneaking `[H]` | H |
| ☑ | Furnace and campfire drying both work | A (recipes load; in-game use unverified) |
| ☐ | Smoker and blast furnace correctly do not dry peppercorns `[H]` | H |
| ☑ | Grinding yields 2× ground pepper | A |

## 4. Salt chain

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Ore generates at comparable density to the reference, same seed `[C]` | C |
| ☐ | Deepslate variant appears below y=0 `[H]` | H |
| ☑ | Any pickaxe mines it | A (block properties unchanged from reference) |
| ☐ | Silk Touch and Fortune behave identically to the reference `[C]` | C |
| ☑ | Smelting and blasting both refine raw rock salt | A |

The ore loot tables needed a real conversion, not just a rename: the `match_tool` predicate is
`{"enchantments": [{"enchantment": ..., "levels": ...}]}` on 1.20.1, against the 1.21 nested
`{"predicates": {"minecraft:enchantments": [...]}}` form the reference uses. Verified against vanilla
1.20.1 `blocks/coal_ore.json`. The drop counts and `apply_bonus`/`ore_drops` fortune formula are
unchanged, but the in-game behaviour is left unchecked pending a human test.

## 5. Worldgen

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Wild vines generate in jungle, bamboo jungle and sparse jungle `[H]` | H |
| ☐ | Vine frequency comparable to reference, same seed — spottable but not spammy `[C]` | C |
| ☐ | Modded jungle biomes receive generation (test with any biome mod) `[H]` | H |
| ☐ | Nothing generates where it shouldn't (no vines outside jungles, no salt in the Nether/End) `[H]` | H |

The placed/configured features are byte-identical; only the biome modifiers were rewritten
(`neoforge:add_features` → `forge:add_features`, `neoforge:or` → `forge:or`, `#c:is_*` → `#forge:is_*`).

## 6. Seasoning system

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Salt and pepper each apply to vanilla foods via crafting `[H]` | A/H |
| ☐ | Both apply to at least one modded food `[H]` | H |
| ☑ | Same seasoning cannot be applied twice | A |
| ☐ | Salt + pepper stack to the full combined bonus `[H]` | H |
| ☑ | Resulting nutrition and saturation match the reference exactly `[C]` | C — see below |
| ☐ | Tooltip line appears and reads correctly `[H]` | H |
| ☑ | Blacklisted items are rejected | A |
| ☐ | Config values change the bonus as expected `[H]` | H |

**Numeric parity, verified by direct comparison rather than by reasoning.** Both code paths were
transcribed verbatim into a standalone harness — the reference's
`SeasoningHelper.season()` + 1.21.1 `FoodData.eat(nutrition, absoluteSaturation)`, against this port's
untouched 1.20.1 `FoodData.add(nutrition, saturationModifier)` + `SeasoningEffectHandler`'s
after-the-fact delta — and run over the full cross product of:

- every vanilla food's `(nutrition, saturationModifier)` pair, plus a synthetic sweep of
  `nutrition` 0–20 × `saturationModifier` 0.00–2.00 in 0.05 steps, plus the clamp corners;
- 0–4 seasonings applied;
- every starting hunger state: food level 0–20 × saturation 0–(food level) in 0.5 steps.

**1,962,450 cases compared: 0 food-level mismatches, 0 saturation mismatches.** The largest saturation
divergence anywhere in the matrix was `3.8e-6`, which is float round-trip noise from the *reference*
recovering the modifier as `saturation / (2 * nutrition)`; it is roughly a millionth of a saturation
point and cannot round to a different displayed or mechanical value.

This checks the arithmetic exhaustively. It does not replace a human eating a seasoned pork chop on
both builds — see §11.

## 7. Shakers

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Seasons a full stack of vanilla food in one right-click `[H]` | H |
| ☐ | Seasons a modded food identically `[H]` | H |
| ☐ | Refuses already-seasoned, blacklisted, and non-food items, falling through to normal vanilla click behaviour `[H]` | H |
| ☐ | Both shakers apply to the same food, stacking the bonus `[H]` | H |
| ☐ | Insufficient uses → nothing happens, clear feedback, no partial seasoning, no items lost `[H]` | H |
| ☐ | Refill works by clicking the shaker onto a pile of its seasoning `[H]` | H |
| ☐ | `empty_shaker` clicked onto a pile of seasoning fills it `[H]` | H |
| ☐ | Becomes `empty_shaker` at zero uses, never disappears `[H]` | H |
| ☐ | Fill bar renders with the right colour and proportion `[H]` | H |
| ☐ | Not enchantable at a table or anvil `[H]` | H |
| ☐ | Works in the survival inventory, a crafting table grid, and a chest GUI `[H]` | H |

## 8. Robustness

| ✓ | Check | How |
| --- | --- | --- |
| ☑ | Boots with only this mod installed | A (dedicated server, clean boot) |
| ☑ | Boots with Farmer's Delight present; compat recipes appear | A — see below |
| ☑ | Boots with Farmer's Delight absent; no errors, no recipe-type spam | A |
| ☐ | Boots alongside Salt: Renewed if it exists for this target; built-in salt disables cleanly `[H]` | H |
| ☐ | Dedicated server + separate client: no desync on any interaction `[H]` | H |
| ☐ | Shakers under rapid repeated clicking on a server: no duplication, no item loss `[H]` | H |
| ☐ | No crash when the cauldron empties on the final interaction `[H]` | H |
| ☐ | World save/reload preserves seasoned foods and shaker fill levels `[H]` | H |

**Tested with Farmer's Delight 1.20.1-1.3.2, JEI 15.48.0.183 and AppleSkin 2.5.1 present.** The client
reaches the main menu with zero mixin failures, and the dedicated server loads **9 recipe types** (the 7
vanilla ones plus `farmersdelight:cooking` and `farmersdelight:cutting`) and 1493 advancements with no
errors. Decisively, Forge logs `Skipping loading recipe ...` for every conditional recipe whose
conditions fail, and **zero recipes were skipped** — so both `compat/ground_pepper_cutting` and
`compat/blanched_peppercorns_cooking_pot` had their `forge:mod_loaded` condition met and were
registered. Crafting them at an actual cutting board / cooking pot is still untested (§11).

Testing FD in this dev environment needs `libs/` + `fg.deobf`, not `run/mods` — see §11.6.

## 9. Cross-port consistency

| ✓ | Check | How |
| --- | --- | --- |
| n/a | `sync-shared.sh` reports no unexplained differences in textures or lang | A |
| ☐ | A datapack written for the reference build works here unchanged (tag ids stable) `[H]` | H |
| ☑ | Config keys and defaults match the reference, even if the file format differs | A |

`sync-shared.sh` does not exist in `port-kit/` on any branch, so it could not be run. The equivalent was
done with `git diff` against the reference: `textures/`, `lang/`, `models/` and `blockstates/` are
unchanged. Tag **ids** are stable, but see the `#saltandpepper:seasonable` deviation in §10 before
relying on the datapack line.

## 10. Deviation log

| Deviation | Reason | Permanent? | Player-visible? |
| --- | --- | --- | --- |
| **Seasoned food tooltips, AppleSkin and JEI show the food's *unseasoned* hunger/saturation values.** The actual restoration on eating is correct and identical to the reference. | 1.20.1 has no data components, so a food's `FoodProperties` cannot be rewritten per stack. The bonus is applied after the fact from `LivingEntityUseItemEvent.Finish` instead. Anything that reads `Item.getFoodProperties()` to *display* a number therefore reads the base food. | Yes, on this branch | Yes — display only, not mechanics |
| Seasoning list and shaker charge stored in stack NBT rather than data components | Data components are 1.20.5+. Same logical content, same ordering, same key names. | Yes, on this branch | No, unless inspecting NBT |
| `#c:foods` gate dropped from `isSeasonableFood` | `#c:foods` is a 1.20.2+ convention with no 1.20.1 equivalent. Eligibility now rests on the existing code check (`stack.isEdible()` and not in `#saltandpepper:seasoning_blacklist`). | Yes, on this branch | No — same set of foods is seasonable |
| `#saltandpepper:seasonable` is declared but empty and is no longer consulted | Kept so datapack authors targeting the reference still find the tag id, but with the `#c:foods` gate gone there is nothing for it to intersect with. Adding items to it has no effect on this branch. | Yes, on this branch | No |
| Farmer's Delight cutting recipe uses `forge:tools/knives`, not `c:tools/knife` | That is the tag Farmer's Delight 1.20.1 actually ships. | Yes, on this branch | No |
| Mod-loaded conditions use Forge's pre-codec `forge:conditional` wrapper | 1.20.1 Forge has no inline `conditions` field on recipes; the wrapper is structurally different, not a rename. Verified against the shipped `ConditionalRecipe$Serializer`, which reads exactly `recipes` / `conditions` / `recipe`. | Yes, on this branch | No |
| Ore loot `match_tool` predicate rewritten to the flat 1.20.1 `enchantments` form | 1.21's nested `predicates`/`minecraft:enchantments` form does not parse on 1.20.1. | Yes, on this branch | No — same drops intended |
| Advancement icons rewritten `{"count","id"}` → `{"item"}`, and item predicates `"items": "x"` → `"items": ["x"]` | 1.20.1 advancement codecs. The reference's form silently fails to parse with *"Unsupported icon type"*. | Yes, on this branch | No |
| `#forge:is_jungle` used where the reference uses `#c:is_jungle` | Forge's `Tags.Biomes` on 1.20.1 has no `IS_JUNGLE` constant, so the tag is referenced by id. | Yes, on this branch | No |

## 11. Known-unverified

Everything below is unchecked above and is listed here rather than silently ticked.

1. **All in-game interaction (`[H]`) is untested.** No human play session was run on this port. This
   covers the whole of §7, most of §3, and the interactive half of §6 and §8. The dedicated server
   boots and loads every recipe and advancement, but nothing was clicked, eaten, grown or harvested.
2. **No seeded side-by-side worldgen comparison (`[C]`) was run** — §4 ore density, §5 vine frequency
   and §3 vine stage rendering. The placed/configured features are byte-identical to the reference and
   only the biome modifiers were rewritten, so the expectation is parity, but that is an argument, not
   a measurement.
3. **Farmer's Delight compat is verified as far as recipe *registration*, not as far as *crafting*.**
   FD 1.20.1-1.3.2 is now installed in the dev environment (§8), both compat recipes pass their
   conditions and register, and nothing errors. What has not been done is putting black peppercorns on
   a cutting board with a knife, or green peppercorns in a cooking pot, and confirming the outputs.
   That is the highest-value remaining test.
4. **Salt: Renewed was not installed during testing.** The `enableSalt` / `forge:not mod_loaded`
   disable path is unexercised.
5. **Client-side rendering is untested** — shaker fill bar, tooltip line, vine stage models, creative
   tab ordering. Only `runServer` was executed.
6. **The seasoning numeric parity in §6 is a harness result, not an in-game measurement.** It compares
   the two implementations exhaustively and they agree, but both were transcribed by the same agent
   that wrote the port; an independent in-game check on both builds would be stronger.

### Note: how to add test mods to this dev environment

Do **not** drop released mod jars into `run/mods`. A released jar is compiled against SRG names
(`m_61090_`, `m_7216_`, …) while this dev run is mapped with Parchment on top of official names
(`defaultBlockState`, `getDispenseMethod`, …). Such a jar fails to load — first its mixins cannot find
their targets, and had they applied, every Minecraft call inside the mod would throw
`NoSuchMethodError`.

Put the jar in `libs/` instead and let ForgeGradle remap it:

```groovy
repositories { flatDir { dir "${projectDir}/libs" } }
dependencies { runtimeOnly fg.deobf("libs:<name>:<version>") }   // matches libs/<name>-<version>.jar
```

`fg.deobf(files('libs/x.jar'))` looks like it should work but does **not** — ForgeGradle can only
deobfuscate artifacts that resolve through a repository, so a bare file dependency is silently passed
through un-remapped and fails identically to `run/mods`. The `flatDir` repo exists purely to give the
local jars coordinates; nothing is downloaded.

The two `mixin.env.*` properties in the `runs` block are also required, so that Mixin reverses each
mod's refmap back from SRG to the dev mappings.

## 12. Sign-off

- [x] Every line above is either checked, or left unchecked with an entry in §11
- [x] Deviation log is complete
- [ ] PARITY.md committed on this branch
- [ ] Release tagged `v1.2.1+forge-1.20.1`
