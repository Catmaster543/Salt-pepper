# PARITY — Fabric 26.1

## How to use this file

Reference build: **NeoForge 26.1** on `Neoforge-26.1`. Every line below means "behaves the same as the
reference build," not "works."

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

- **Target:** Fabric, Minecraft **26.1.2** — the Minecraft version is unchanged from the reference
- **Branch:** `Fabric-26.1`
- **Reference commit:** `e2fa36d` on `Neoforge-26.1`
- **Pinned versions:**

  | Component | Pin | Source |
  |---|---|---|
  | Minecraft | `26.1.2` | **deviates from template** — see below |
  | Fabric Loader | `0.19.3` | template |
  | Fabric Loom | `1.17-SNAPSHOT`, plugin id `net.fabricmc.fabric-loom` | template |
  | Fabric API | `0.155.2+26.1.2` | **deviates from template** — see below |
  | Gradle | `9.5.1` | template |
  | Java | `25` | template |
  | Mappings | **none** | 26.1 is unobfuscated; Yarn is no longer supported and the new Loom plugin does not remap |

  The template pins `minecraft_version=26.1` and `fabric_api_version=0.145.1+26.1`. This mod is on
  **26.1.2**, and the brief's premise is that the Minecraft version does not change — only the loader.
  Taking the template's numbers literally would have silently moved the mod from 26.1.2 to 26.1, so
  both were taken from the matching `+26.1.2` line instead. Everything else is the template's value.

- **Test world seed used for comparisons:** none — no comparative testing was performed, see §11
- **Completed by / date:** Claude (agent pass), 2026-08-08. **Agent pass only — no human or
  comparative testing has been done.**

---

## 1. Build and structure

| ✓ | Check | How |
|---|---|---|
| ☑ | Builds without warnings that reference missing resources | A |
| ☑ | Mod id and namespace are `saltandpepper` | A |
| ☑ | `git diff` shows no changes under `textures/` or `lang/` | A |
| ☑ | Every registry id identical to reference (items, blocks, recipes, tags, advancements) | A |
| ☑ | Component keys `saltandpepper:seasonings` and `saltandpepper:shaker_uses` unchanged | A |
| ☑ | All balance numbers identical: seasoning bonuses, shaker capacity, refill cost, ore density settings, drop counts, smelting times | A |
| ☑ | No Java imports of Farmer's Delight classes anywhere | A |

**Evidence.** `gradlew build` clean. `git diff Neoforge-26.1 -- "*/textures/*"` and `-- "*/lang/*"`
both empty. Registry-name string literals across all seven registration classes compare `IDENTICAL`.
All ten config defaults and ranges compare identical field-by-field. Smelting times, drop counts and
ore density live in recipe / loot table / placed feature JSON that `git diff` shows as untouched.
`grep -i "farmersdelight|vectorwing"` over all Java returns nothing.

*Caveat on line 1:* the build is clean, but launching the **dev** client prints
`Class path entries reference missing files: build\resources\client` — Loom's split source set
expecting a `src/client/resources` directory this mod has no need for. Absent from the built jar. §10 D7.

## 2. Content parity

| ✓ | Check | How |
|---|---|---|
| ☑ | All 7 base items registered under identical ids | A |
| ☑ | All 3 shaker items registered under identical ids | A |
| ☑ | All blocks registered under identical ids (+ optional salt block) | A |
| ☐ | Creative tab contains the same entries in the same order **[H]** | H |
| ☐ | All lang keys resolve; no raw keys visible in-game **[H]** | H |
| ☐ | All textures and models render correctly, including the three vine stages **[H]** | H |

The client reaches the title screen with no missing-model, missing-texture or item-model parse errors,
and the built jar carries all 13 `assets/saltandpepper/items/*.json` definitions unchanged from the
reference. Rendering and tab ordering remain visual checks. Tab **position** is a known deviation
(§10 D6) even though contents and their order are unchanged.

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

Drying and grinding are data-driven recipes, byte-identical to the reference and confirmed loaded.
The cauldron handler was rewritten from `PlayerInteractEvent.RightClickBlock` to `UseBlockCallback`
with its conditions transcribed unchanged; it has not been exercised in-game and is the
highest-value untested item here.

## 4. Salt chain

| ✓ | Check | How |
|---|---|---|
| ☐ | Ore generates at comparable density to the reference, same seed **[C]** | C |
| ☐ | Deepslate variant appears below y=0 **[H]** | H |
| ☑ | Any pickaxe mines it | A |
| ☐ | Silk Touch and Fortune behave identically to the reference **[C]** | C |
| ☑ | Smelting and blasting both refine raw rock salt | A |

Both ore blocks still omit `requiresCorrectToolForDrops` and the `#minecraft:mineable/pickaxe` tag is
untouched. Loot tables are untouched, so the data is identical — but "behaves identically" is [C].

## 5. Worldgen

| ✓ | Check | How |
|---|---|---|
| ☐ | Wild vines generate in jungle, bamboo jungle and sparse jungle **[H]** | H |
| ☐ | Vine frequency comparable to reference, same seed — spottable but not spammy **[C]** | C |
| ☐ | Modded jungle biomes receive generation (test with any biome mod) **[H]** | H |
| ☐ | Nothing generates where it shouldn't (no vines outside jungles, no salt in the Nether/End) **[H]** | H |

**This is the third reimplementation of this mod's biome injection, so it got explicit attention.**
Everything that determines density was checked mechanically against the reference:

- All five `WildPepperVineFeature` constants identical: `HORIZONTAL_RADIUS=7`, `SCAN_ABOVE=4`,
  `SCAN_BELOW=32`, `COLUMN_ATTEMPTS=10`, `MAX_VINES_PER_PLACEMENT=2`.
- All three placed features and both configured features **untouched** — every `count`, height range
  and placement modifier carries over verbatim, including the `saltandpepper:salt_enabled` filter.
- Biome targeting maps 1:1: the coastal JSON's five entries (`#c:is_beach`, `#c:is_ocean`,
  `#minecraft:is_beach`, `#minecraft:is_ocean`, `minecraft:dripstone_caves`) become five or'd
  `BiomeSelectors`, and the generation steps match (`underground_ores`, `vegetal_decoration`).

A trap worth recording: `git diff main...Fabric-1.21.1` (three-dot, from the merge base) shows
`MAX_VINES_PER_PLACEMENT` changing 1 → 2 and looks like a Fabric change. It is not — it came from
commit `e1f53fb` on main and is already present on both sides. The tip-to-tip two-dot diff, which the
brief specified, correctly omits it. Following the three-dot diff would have double-applied a density
change.

The inputs are provably identical; **the output is still unverified** and needs a same-seed comparison.

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

`SeasoningHelper` and `SeasoningRecipe` changed only in registry accessors and the `Nullable` import.
The 26.1 food-component work — rewriting only `minecraft:food` as `(nutrition, saturation,
canAlwaysEat)` and leaving `minecraft:consumable` / `minecraft:use_remainder` alone — carried across
untouched, as did the `ItemStackTemplate`-era `RecipeSerializer` codec pair. The A lines mean "logic
identical to the reference and the recipe loads", not "observed in game".

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

Untested in-game, but this subsystem needed the least work of anything: the inventory hooks are
vanilla `Item` methods, so `ShakerItem` and `EmptyShakerItem` changed only in registry accessors and
the `Nullable` import. No interaction logic was touched on either the version jump or this loader swap.

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

All [H], all unchecked — but two have strong agent-level evidence:

- The dev client reaches the title screen and the dev dedicated server reaches `Done (0.552s)!`, both
  with only this mod present and no errors in either log.
- **Farmer's Delight absent** behaves correctly, and this is the sharpest cross-loader result in the
  port: the Fabric server loaded **1526 recipes and 1621 advancements — exactly the counts the
  NeoForge 26.1 reference produces**, measured the same way. That confirms the resource conditions are
  evaluated in both directions: the four `fabric:not(fabric:all_mods_loaded[salt])` salt recipes
  loaded, and the two `fabric:all_mods_loaded[farmersdelight]` recipes were excluded. Had
  `fabric:load_conditions` been ignored as an unknown key, the count would have read 1528.

## 9. Cross-port consistency

| ✓ | Check | How |
|---|---|---|
| ☑ | `sync-shared.sh` reports no unexplained differences in textures or lang | A |
| ☐ | A datapack written for the reference build works here unchanged (tag ids stable) **[H]** | H |
| ☑ | Config keys and defaults match the reference, even if the file format differs | A |

Textures/lang verified directly with `git diff` (§1). Config parity: the same ten keys under the same
five section names, same defaults, same ranges — only the file and format differ (§10 D3).

## 10. Deviation log

| Deviation | Reason | Permanent? | Player-visible? |
|---|---|---|---|
| **D1** — Pepper vine growth no longer fires a crop-grow event | NeoForge's `CommonHooks.canCropGrow` / `fireCropGrowPost` have no Fabric equivalent. The 1-in-5 pacing is unchanged; what is lost is other mods' ability to veto or accelerate growth | Yes | No — only to other mods |
| **D2** — No in-game config screen | `IConfigScreenFactory` is NeoForge-only; ModMenu ruled out rather than add a dependency for ten values | Yes | Yes — config edited by hand |
| **D3** — Config is `config/saltandpepper.json`, not `saltandpepper-common.toml` | Fabric ships no config system, so this is the hand-rolled Gson config carried over from the Fabric 1.21.1 port. Keys, sections, defaults and ranges identical | Yes | Yes — different file and format |
| **D4** — The `saltandpepper.configuration.*` lang keys are now dead | Follows from D2. Left in `en_us.json` because the lang invariant forbids touching it | Yes | No |
| **D5** — `#c:foods` membership may differ slightly | Provided by NeoForge on one side and fabric-convention-tags-v2 on the other | Yes | Marginally — edge-case foods |
| **D6** — Creative tab sits after the vanilla tabs instead of before Spawn Eggs | `CreativeModeTab.Builder.withTabsBefore` is a NeoForge addition, not vanilla; Fabric appends custom tabs with no ordering control. Contents and their order unchanged | Yes | Yes — tab position |
| **D7** — Dev client logs `Class path entries reference missing files: build\resources\client` | Loom's `splitEnvironmentSourceSets()` expects a `src/client/resources` this mod does not need. Dev only, absent from the jar | No | No |
| **D8** — Minecraft and Fabric API pinned off-template | Template targets 26.1; this mod is on 26.1.2 and the port must not change the Minecraft version. See Port details | No | No |

**Explicitly not deviations.** Two things the 1.21.1 Fabric port needed have simply ceased to exist on
26.1, on both loaders:

- The pepper vine's cutout render-layer registration. Fabric's `fabric-blockrenderlayer-v1` module is
  **gone** from 26.1's Fabric API, because terrain layers are now `ChunkSectionLayer`s assigned
  automatically from sprite contents. The NeoForge 26.1 build had already dropped its equivalent
  `ItemBlockRenderTypes` call for the same reason. Nothing to port.
- Farmer's Delight is **not** absent: FD Refabricated has a `fabric/latest/26.1` branch, so the two
  compat recipes ship, migrated to Fabric's condition syntax. They remain untested (§11).

## 11. Known-unverified

1. **Everything marked [H] or [C].** No human play-testing, no comparative same-seed run. Agent pass only.
2. **Worldgen output (§5).** Density inputs are mechanically identical to the reference, but nothing
   was observed generating. This needs the same-seed comparison against the NeoForge 26.1 build.
3. **The cauldron interaction (§3) has never been triggered.** It is the only handler genuinely
   rewritten in this port. One judgement call inside it is unverified: 1.21.1's
   `sidedSuccess(true/false)` was mapped to `InteractionResult.SUCCESS` on the client and `CONSUME` on
   the server, matching what `sidedSuccess` used to return (`SwingSource.CLIENT` / `NONE`). If the arm
   double-swings or fails to swing, that mapping is where to look.
4. **The shaker interaction has not been clicked once** (§7).
5. **Farmer's Delight present** (§8) — FD Refabricated 26.1 exists but was not installed. Its recipe
   schema on 26.1 was not re-verified; the two compat files carry the reference's bodies unchanged.
6. **Salt: Renewed on 26.1** — unknown whether a Fabric build exists.
7. **Modded-food seasoning** (§6) — no second food mod installed.
8. **`build/libs` also holds stale jars from the other three branches** (`…-fabric-1.21.1-…`,
   `…-neoforge-1.21.1-…`, `…-neoforge-26.1.2-…`). Gitignored build output, not artifacts of this port;
   `gradlew clean` clears them.
9. **Environment note, not a port issue:** this machine runs Avast Web/Mail Shield TLS interception
   whose CA is absent from the JDK truststore, so Java-side downloads fail with PKIX errors unless the
   JVM is pointed at the Windows root store
   (`JAVA_TOOL_OPTIONS=-Djavax.net.ssl.trustStoreType=Windows-ROOT`). Deliberately not committed.
10. **`port-kit/fabric-26.1-template/` does not exist under that name** — the template is at
    `port-kit/fabric-example-mod-26.1/fabric-example-mod-26.1/` (nested twice). Versions were taken
    from it, with the two documented exceptions in Port details.

## 12. Sign-off

- ☐ Every line above is either checked, or left unchecked with an entry in §11
- ☑ Deviation log is complete
- ☐ PARITY.md committed on this branch
- ☐ Release tagged `v1.2.1+fabric-26.1`

**Not signed off.** Sections 2–8 are substantially unverified. The port compiles, builds, and loads
cleanly on both client and dedicated server, every invariant in the brief is mechanically verified,
and the recipe/advancement counts match the reference exactly — but that is a weaker claim than "it
works." It should not be released until a human has played it, starting with the worldgen comparison
(§11.2) and the cauldron interaction (§11.3).
