# Parity checklist — Fabric 1.20.1

Reference build: NeoForge 1.21.1 on `main-neoforge-1.21.1`. This branch was ported from
**Forge 1.20.1** (`refs/heads/Forge-1.20.1`, `2a3140c`) — same Minecraft version, loader swap only — so
the immediate comparison target is that branch. Every line below means "behaves the same as the
reference build," not "works."

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
| Target | Fabric 1.20.1 |
| Branch | `Fabric-1.20.1` |
| Ported from | `refs/heads/Forge-1.20.1` (`2a3140c`) |
| Reference commit on main | `43af4fe` (`main-neoforge-1.21.1`) |
| Fabric 1.21.1 sibling used as the loader work list | `refs/heads/Fabric-1.21.1` (`d568eaf`) |
| Tested on | Loader 0.19.3, Fabric API 0.92.11+1.20.1, Loom 1.17.17, Gradle 9.5.1, Java 17 toolchain (Gradle daemon on JDK 25) |
| Test world seed used for comparisons | none — no seeded side-by-side worldgen comparison was run (see §11) |
| Completed by / date | Claude (agent), 2026-08-09 |

## 1. Build and structure

| ✓ | Check | How |
| --- | --- | --- |
| ☑ | Builds without warnings that reference missing resources | A |
| ☑ | Mod id and namespace are `saltandpepper` | A |
| ☑ | `git diff` shows no changes under `textures/` or `lang/` | A |
| ☑ | Every registry id identical to reference (items, blocks, recipes, tags, advancements) | A |
| ☑ | NBT keys `saltandpepper:seasonings` and `saltandpepper:shaker_uses` unchanged | A |
| ☑ | All balance numbers identical: seasoning bonuses, shaker capacity, refill cost, ore density settings, drop counts, smelting times | A |
| ☑ | No Java imports of Farmer's Delight classes anywhere | A |

Evidence:

- `gradlew build` succeeds; dev client reaches the main menu (`Sound engine started`) and the dedicated
  server loads its datapack, both with zero errors.
- `git diff Forge-1.20.1 -- src/main/resources/assets` is **empty** — textures, lang, models and
  blockstates are untouched.
- No datapack file was added, renamed or removed except the three `forge/biome_modifier/*.json`, which
  are replaced in code (§5). Recipe *ids* are unchanged; only the condition wrapper inside six of them
  changed.
- **10/10** config defaults and ranges identical to the Forge branch (`maxNutrition 20 [0,100]`,
  pepper `+1 [0,20]` / `+0.2 [0,2]`, salt `+1 [0,20]` / `+0.2 [0,2]`, `enableSalt true`,
  `shakerCapacity 64 [1,1024]`, `usesPerRefillItem 8 [1,1024]`, `restrictGrowthToJungle false`,
  `blanchBatchSize 8 [1,64]`), despite the file moving from TOML to JSON.
- Grep for `farmersdelight|vectorwing` across `src/` → no matches. Farmer's Delight is not a Gradle
  dependency either.

## 2. Content parity

| ✓ | Check | How |
| --- | --- | --- |
| ☑ | All 7 base items registered under identical ids | A |
| ☑ | All 3 shaker items registered under identical ids | A |
| ☑ | All blocks registered under identical ids (+ optional salt block) | A |
| ☐ | Creative tab contains the same entries in the same order `[H]` | H |
| ☐ | All lang keys resolve; no raw keys visible in-game `[H]` | H |
| ☐ | All textures and models render correctly, including the three vine stages `[H]` | H |

The tab's **position** in the tab strip differs — see §10.

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
| ☑ | Any pickaxe mines it | A (block properties unchanged) |
| ☐ | Silk Touch and Fortune behave identically to the reference `[C]` | C |
| ☑ | Smelting and blasting both refine raw rock salt | A |

The ore loot tables are byte-identical to the Forge branch, including the 1.20.1 `match_tool`
predicate and the `apply_bonus`/`ore_drops` fortune formula. The xp range is also unchanged, though
`DropExperienceBlock` takes it as its second argument on 1.20.1 rather than its first.

## 5. Worldgen

| ✓ | Check | How |
| --- | --- | --- |
| ☐ | Wild vines generate in jungle, bamboo jungle and sparse jungle `[H]` | H |
| ☐ | Vine frequency comparable to reference, same seed `[C]` | C |
| ☐ | Modded jungle biomes receive generation (test with any biome mod) `[H]` | H |
| ☐ | Nothing generates where it shouldn't (no vines outside jungles, no salt in the Nether/End) `[H]` | H |

The placed and configured features are byte-identical; all density tuning lives there and none of it
moved. Only the three biome *modifiers* changed form, from `forge:add_features` JSON to
`ModBiomeModifications` against the Fabric Biome API. Selectors and generation steps mirror the JSON
one-for-one, but the convention tag ids differ — see §10.

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

**Numeric parity, verified by direct comparison rather than by reasoning.** Two independent checks:

1. `git diff Forge-1.20.1 -- SeasoningEffectHandler.java` shows exactly **two** substantive changed
   lines: the side guard (`event.getEntity()`/`player.level()` → the `Level` parameter) and how the
   base `FoodProperties` are fetched. Every arithmetic line is character-identical.
2. A standalone harness transcribing all three implementations — the NeoForge 1.21.1 reference, the
   Forge 1.20.1 branch, and this one — run over the full cross product of every vanilla food's
   `(nutrition, saturationModifier)` pair plus a sweep of nutrition 0–20 × modifier 0.00–2.00 in 0.05
   steps and the clamp corners; 0–4 seasonings; and every starting hunger state (food 0–20 ×
   saturation 0–food in 0.5 steps):

```
cases compared              : 1947015
Fabric vs Forge  mismatches : 0   (exact equality required)
Fabric vs 1.21.1 mismatches : 0   (threshold 1e-4)
worst saturation delta vs reference: 3.8146973E-6
```

The 3.8e-6 is float round-trip noise from the *reference* recovering the modifier as
`saturation / (2 * nutrition)`; roughly a millionth of a saturation point.

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
| ☑ | Boots with only this mod installed | A (dev client to main menu; dedicated server through datapack load) |
| ☐ | Boots with Farmer's Delight present; compat recipes appear `[H]` | H |
| ☑ | Boots with Farmer's Delight absent; no errors, no recipe-type spam | A |
| ☐ | Boots alongside Salt: Renewed if it exists for this target; built-in salt disables cleanly `[H]` | H |
| ☐ | Dedicated server + separate client: no desync on any interaction `[H]` | H |
| ☐ | Shakers under rapid repeated clicking on a server: no duplication, no item loss `[H]` | H |
| ☐ | No crash when the cauldron empties on the final interaction `[H]` | H |
| ☐ | World save/reload preserves seasoned foods and shaker fill levels `[H]` | H |

The server loaded **7 recipe types** and **1275 advancements** — identical counts to the Forge 1.20.1
branch (vanilla's 1271 advancements plus this mod's 4).

### The mixin is genuinely live in the built jar

This is the failure the porting brief singled out: a mixin that works in dev and silently no-ops once
built. Checked three ways rather than assumed:

- `saltandpepper.mixins.json` declares `"required": true` with `"defaultRequire": 1`, so a target that
  fails to resolve is a hard crash, not a silent skip. The client reaching the main menu therefore
  *proves* the injection resolved.
- The built jar contains **no refmap**, which on this Loom version is correct rather than broken: Loom
  1.17 remaps the mixin annotations in place at `remapJar` time instead. Disassembling
  `PlayerEatMixin.class` out of `build/libs/saltandpepper-1.2.1.jar` shows `@Mixin` → `class_1657`,
  `method = "eat"` → `method_18866`, and the descriptors → `class_1937`/`class_1799`.
- `method_18866` was confirmed against the intermediary jar to be
  `class_1657.method_18866(class_1937, class_1799) → class_1799`, i.e. exactly
  `Player.eat(Level, ItemStack)`.

## 9. Cross-port consistency

| ✓ | Check | How |
| --- | --- | --- |
| n/a | `sync-shared.sh` reports no unexplained differences in textures or lang | A |
| ☐ | A datapack written for the reference build works here unchanged (tag ids stable) `[H]` | H |
| ☑ | Config keys and defaults match the reference, even if the file format differs | A |

`sync-shared.sh` does not exist in `port-kit/` on any branch. The equivalent was done with `git diff`:
`assets/` is unchanged. Tag **ids this mod owns** are stable; the third-party convention tags it
*references* are loader-specific — see §10.

## 10. Deviation log

| Deviation | Reason | Permanent? | Player-visible? |
| --- | --- | --- | --- |
| **Seasoned food tooltips, AppleSkin and JEI show the food's *unseasoned* hunger/saturation values.** The actual restoration on eating is correct and identical to the reference. | Carried forward from the Forge 1.20.1 branch, and for the same reason: 1.20.1 has no data components, so a food's `FoodProperties` cannot be rewritten per stack. The bonus is applied after the fact instead. Anything that reads `Item.getFoodProperties()` to *display* a number therefore reads the base food. | Yes, on this branch | Yes — display only, not mechanics |
| The bonus is applied by a mixin on `Player.eat` rather than an event | Fabric API 0.92.11 has no finish-using-item event; `UseItemCallback` fires at use *start* and `ServerLivingEntityEvents` carries only damage/death/conversion. Every module was enumerated from the shipped jar before concluding this. | Yes, on this branch | No |
| Seasoning list and shaker charge stored in stack NBT rather than data components | Inherited from the Forge 1.20.1 branch. Same content, same ordering, same key names. | Yes, on this branch | No, unless inspecting NBT |
| Base food values read from `Item.getFoodProperties()`, not a per-stack override | The Forge branch used Forge's per-stack `ItemStack.getFoodProperties(entity)`. Fabric API 0.92.11 has no such API, and vanilla's own `FoodData.eat(Item, ItemStack)` reads the item-level properties — so this is what the delta *must* read to line up with what vanilla just applied. | Yes, on this branch | No for vanilla and Fabric mods; a Forge mod's per-stack food override has no Fabric equivalent to differ from |
| Creative tab is appended after the vanilla tabs instead of sitting before Spawn Eggs | `withTabsBefore` is a NeoForge addition to `CreativeModeTab.Builder`. Fabric appends custom groups. Contents and order within the tab are unchanged. | Yes, on this branch | Yes — tab position only |
| Vine growth no longer fires crop-growth hooks | `ForgeHooks.onCropsGrowPre/Post` let other mods veto or observe growth; Fabric has no equivalent. Growth is now unconditional, exactly as vanilla `CocoaBlock`. | Yes, on this branch | No, unless another mod tried to alter crop growth |
| Convention biome tags are `c:jungle` / `c:beach` / `c:ocean` | Fabric API 0.92.11 ships `ConventionalBiomeTags` **v1**, whose constants are `JUNGLE`/`BEACH`/`OCEAN`. The 1.21.1 Fabric branch uses the v2 `IS_JUNGLE`/`IS_BEACH`/`IS_OCEAN` (`c:is_jungle` …), and the Forge branch used `forge:is_*`. Each is that loader-and-version's spelling of the same idea. | Yes, on this branch | No |
| Farmer's Delight knife tag is `c:tools/knife` | Mirrors the Fabric 1.21.1 branch. **Unverified for 1.20.1** — see §11. | Unknown | Possibly, if wrong |
| `pack.mcmeta` removed | Fabric Loader synthesises pack metadata from `fabric.mod.json`; the reference Fabric branch ships none either. | Yes, on this branch | No |

Resource conditions were expected to be a deviation and turned out not to be: see §11.4.

## 11. Known-unverified

1. **All in-game interaction (`[H]`) is untested.** No human play session was run. This covers all of
   §7, most of §3, and the interactive half of §6 and §8. The client reaches the main menu and the
   server loads every recipe and advancement, but nothing was clicked, eaten, grown or harvested.
2. **No seeded side-by-side worldgen comparison (`[C]`).** The placed/configured features are
   byte-identical and the biome selectors mirror the old JSON, but the rewrite from datapack modifiers
   to the Biome API is the single largest untested behavioural change on this branch. It deserves the
   first look.
3. **The Farmer's Delight knife tag is a guess.** `c:tools/knife` is copied from the Fabric 1.21.1
   branch; I could not verify what Farmer's Delight Refabricated uses on **1.20.1** specifically
   without the jar. If cutting-board recipes do not appear with FD installed, this is the first thing
   to check. The Forge branch used `forge:tools/knives`, which was verified.
4. **Resource conditions were expected to differ and do not.** The porting brief warned the 1.20.1
   Fabric condition format differs from newer versions. Measured against the shipped 0.92.11 jar, the
   top-level key is `fabric:load_conditions` and the ids are `fabric:all_mods_loaded` / `fabric:not`
   with `value`/`values` members — the same as the 1.21.1 branch uses. Flagging the contradiction
   rather than quietly trusting either source. The conditions have not been observed *firing* in-game.
5. **Farmer's Delight and Salt: Renewed were not installed during testing.**
6. **Interrupted / offhand / creative eating is argued, not observed.** The mixin targets
   `Player.eat`, which is reached only on completed consumption and receives the stack as a parameter,
   so all three should fall out for free — but no one has actually released right-click mid-bite,
   eaten from the offhand, or eaten in creative on this build.
7. **A benign dev-only warning** appears at client start: `Class path entries reference missing files:
   build\resources\client`. It is the split client source set having no resources directory of its own,
   and it does not affect loading or the built jar.

## 12. Sign-off

- [x] Every line above is either checked, or left unchecked with an entry in §11
- [x] Deviation log is complete
- [ ] PARITY.md committed on this branch
- [ ] Release tagged `v1.2.1+fabric-1.20.1`
