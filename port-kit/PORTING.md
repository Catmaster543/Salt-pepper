# Salt & Pepper — Porting Kit

Reference implementation: **NeoForge 1.21.1**, mod id `saltandpepper`.
This document is the thing you hand an agent alongside a fresh loader template. It replaces "the agent knows what this mod is."

---

## 1. Strategy

**Do not build a multiloader repo.** Architectury-style common/fabric/neoforge splits earn their keep when a mod is under active development — one fix, one place. This mod is feature-frozen. The abstraction layer would be pure overhead, and it wouldn't help with the expensive axis anyway: loader differences are small, *Minecraft version* differences are large, and a multiloader repo still needs one branch per MC version.

**Instead: one standalone repo (or branch) per target.** Each is an ordinary single-loader mod that any agent can reason about without special knowledge. The NeoForge 1.21.1 repo stays canonical.

**Non-negotiable across every port:**
- Mod id / namespace stays `saltandpepper`. Never change it. Resource packs, datapacks, and player inventories all key off it.
- Registry names stay identical (`saltandpepper:ground_pepper`, `saltandpepper:pepper_vine`, etc.).
- Tag ids stay identical, so third-party datapacks work everywhere.
- The seasoning data component / NBT key stays `saltandpepper:seasonings`.

---

## 2. Repo layout for a port

```
saltandpepper-<loader>-<mcversion>/
├── src/                    ← the port, written by the agent
├── port-kit/               ← read-only, copied in before the agent starts
│   ├── SPEC.md             ← the behaviour spec (loader-neutral)
│   ├── PORTING.md          ← this document
│   ├── reference-src/      ← full NeoForge 1.21.1 src/main/java, read-only
│   ├── reference-resources/← full NeoForge 1.21.1 src/main/resources
│   └── PARITY.md           ← checklist, filled in by the agent when done
└── ...template files...
```

`reference-src/` and `reference-resources/` are the real answer to "the agent has no clue what this mod is." A working implementation is a better specification than any prose. **Mark them read-only and tell the agent explicitly that they are reference, not code to edit.**

---

## 3. What actually has to be rewritten

Rough split of this mod by portability:

| Category | Same MC version, different loader | Different MC version |
|---|---|---|
| Textures (`assets/.../textures/`) | **Copy verbatim** | Copy verbatim |
| Lang (`en_us.json`) | **Copy verbatim** | Copy verbatim |
| Block/item models, blockstates | **Copy verbatim** | Path/format may change (see §6) |
| Loot tables | **Copy verbatim** | Folder name + format may change |
| Tags | **Copy verbatim** | Folder name may change |
| Vanilla recipes (smelting, shapeless) | **Copy verbatim** | Result JSON shape may change |
| FD compat recipe | ⚠ condition syntax differs | ⚠ both |
| Worldgen configured/placed features | **Copy verbatim** | Format may change |
| Biome injection | ⚠ NeoForge JSON vs Fabric API | ⚠ both |
| Registration, events, config | ⚠ rewrite | ⚠ rewrite |
| Block class, feature class, recipe class | mostly portable | ⚠ version-sensitive |

For a same-version loader port (NeoForge 1.21.1 → Fabric 1.21.1), the majority of the resource tree is a straight file copy. Only the Java and two JSON files genuinely change. Tell the agent this up front so it doesn't "helpfully" regenerate assets that were fine.

**Anti-drift:** keep a `sync-resources.sh` in each port that copies `port-kit/reference-resources/assets/` over `src/main/resources/assets/` (textures, models, lang) and diffs the `data/` tree, reporting differences rather than overwriting. Run it before each release so a texture fix made once lands everywhere.

---

## 4. Loader touchpoint table

Everything in this mod that touches a loader API, and its equivalent elsewhere. Treat every row as **verify against the real API** — versions move.

| Concern | NeoForge 1.21.1 | Fabric 1.21.1 | Forge 1.20.1 |
|---|---|---|---|
| Registration | `DeferredRegister` | `Registry.register(BuiltInRegistries.X, id, obj)` | `DeferredRegister` (same idea, `net.minecraftforge.*` packages) |
| Cauldron interaction | `PlayerInteractEvent.RightClickBlock` | `UseBlockCallback` (fabric-events-interaction-v0) | `PlayerInteractEvent.RightClickBlock` |
| Tooltip line | `ItemTooltipEvent` | `ItemTooltipCallback` | `ItemTooltipEvent` |
| Creative tab | `BuildCreativeModeTabContentsEvent` / tab DeferredRegister | `FabricItemGroup` + `ItemGroupEvents` | `CreativeModeTabEvent` / `BuildContents` |
| Config | `ModConfigSpec` (TOML) | **none built in** — hand-roll a small JSON config, don't take a Cloth Config dependency for six values | `ForgeConfigSpec` (TOML) |
| Biome injection | `neoforge:biome_modifier` JSON | `BiomeModifications.addFeature(...)` in code (Fabric Biome API) | `BiomeLoadingEvent` / Forge biome modifier JSON |
| Conditional recipe (FD) | `"neoforge:conditions": [{"type":"neoforge:mod_loaded","modid":"farmersdelight"}]` | `"fabric:load_conditions": [{"condition":"fabric:all_mods_loaded","values":["farmersdelight"]}]` | `"forge:conditions": [{"type":"forge:mod_loaded","modid":"farmersdelight"}]` |
| Optional dependency declaration | `neoforge.mods.toml`, `type = "optional"` | `fabric.mod.json`, `"suggests"` block | `mods.toml`, `mandatory = false` |
| Custom `Feature`, recipe type, data component | vanilla registries — portable | vanilla registries — portable | ⚠ components don't exist, see §5 |
| Mod entrypoint | `@Mod` class | `ModInitializer` in `fabric.mod.json` | `@Mod` class |

### Fabric-specific tip that saves most of the work

Configure Loom with **official Mojang mappings** (`officialMojangMappings()`) rather than Yarn. The reference source is Mojmap/Parchment, so class and method names map almost 1:1 and the port becomes mostly mechanical. With Yarn you'd be translating `Level`→`World`, `Item.Properties`→`Item.Settings`, `InteractionResult`→`ActionResult` on every line for no benefit.

### Farmer's Delight on Fabric

FD is a Forge/NeoForge mod; the Fabric side is a separate community port ("Refabricated"). Verify it exists for your target version and check its actual mod id and cooking-pot recipe type before writing the compat file — do not assume they match the NeoForge originals. If it doesn't exist for a target, simply ship that port without the FD recipes; the mod is designed so FD is never required.

---

## 5. The one thing that breaks on old versions

**The seasoning system is the fragile part of this mod.** It works by rewriting the `minecraft:food` data component on a per-`ItemStack` basis. Data components arrived in 1.20.5. Below that they don't exist, and vanilla food properties are static per-`Item`, not per-stack.

- **Forge 1.20.1:** workable. Store the seasoning list in stack NBT and override the Forge item extension hook that returns food properties per-stack-and-entity, returning modified values when the NBT is present. Verify the exact hook name in the Forge version you target.
- **Fabric 1.20.1:** there is no such hook. You would need a **mixin** into the food-consumption path. This is the only place in the whole mod that needs mixins, and it's the main reason a 1.20.1 Fabric port is disproportionately expensive.
- **Do not** fall back to "make a separate Salted Cooked Beef item." That abandons cross-mod food support, which is the entire point of the design.

If a 1.20.1 Fabric port isn't worth a mixin to you, that's a perfectly reasonable place to draw the line.

---

## 6. Version touchpoints above 1.21.1

Minecraft switched to calendar versioning in 2026 (26.1, 26.2, …). NeoForge's own guidance for porting off 1.21.1 is to read the 21.2, 21.4, 21.5, 21.6, 21.9 and 21.11 release notes in sequence, then the 26.1 notes. **Hand those URLs to the agent** — they are the authoritative changelogs and far more reliable than any model's memory of them.

Known landmines between 1.21.1 and current, all of which touch this mod:

- **1.21.2:** `ItemInteractionResult` removed, back to `InteractionResult`. `FoodProperties` split — effects and `usingConvertsTo` move out into a separate `consumable` component. **This directly changes the seasoning code**, which constructs `FoodProperties` by hand.
- **1.21.4:** item models move to `assets/<ns>/items/*.json` definitions. Every item model in this mod is affected. Item registration may require an id set in `Item.Properties`.
- **1.21.5+ / 26.x:** further model, equipment, and datapack format churn; obfuscation removed from 26.1 snapshots onward, which changes nothing functionally but simplifies tooling.

Practical consequence: **port 1.21.1 → 26.x as one deliberate jump, not a chain of intermediate versions**, unless you specifically want intermediate releases. Each intermediate version is a full port's worth of work for a shrinking audience.

---

## 7. Recommended target order

| Priority | Target | Cost | Rationale |
|---|---|---|---|
| 1 | **Fabric 1.21.1** | Low | Same MC version — all resources copy verbatim, only Java changes. Doubles your audience for the least work. Do this first; it also proves the port kit works. |
| 2 | **NeoForge 26.x** | High | Where the game currently is. Six stacked sets of breaking changes, mostly in item models and the food component. |
| 3 | **Fabric 26.x** | Low *after* #2 | Once 26.x resources exist, this is the same cheap loader-swap as #1. |
| 4 | **Forge 1.20.1** | Medium-high | Unlocks the large 1.20.1 pack ecosystem, and the same jar loads on NeoForge 1.20.1 since that's the last interchangeable version. Requires the NBT seasoning rewrite. |
| 5 | Fabric 1.20.1 | High | Needs a mixin. Only if demand justifies it. |

Do them **one at a time, fully finished and tested**, not in parallel. Ports done in parallel drift.

---

## 8. Per-port agent prompt template

Fill in the bracketed parts.

> You are porting an existing, feature-complete Minecraft mod to a new platform. **This is a port, not a redesign. Do not add, remove, or "improve" any feature.**
>
> **Target:** Minecraft `[VERSION]`, `[LOADER]`, Java `[21/25]`.
> **Source of truth:** the reference implementation in `port-kit/reference-src/` and `port-kit/reference-resources/` (NeoForge 1.21.1). These directories are **read-only reference** — never edit them, never ship them.
> **Behaviour spec:** `port-kit/SPEC.md`.
> **Porting notes, including a loader touchpoint table:** `port-kit/PORTING.md`. Read it before writing any code.
>
> **Method:**
> 1. Read `SPEC.md`, then read every file in `reference-src/`. Write a short inventory of every place the code touches a loader API or a version-sensitive vanilla API. Show me this inventory **before** writing code.
> 2. Copy across everything portable — textures, lang, models, blockstates, loot tables, tags, vanilla recipes — verbatim first, adjusting only where `[VERSION]` demands a different path or format. Do not regenerate assets that can be copied.
> 3. Port the Java, one subsystem at a time, in this order: registration → items/blocks → recipes and tags → the seasoning system → the cauldron interaction → worldgen → config → compat.
> 4. Fill in `port-kit/PARITY.md` (§9 below), marking every line, and flag anything you had to change behaviourally.
>
> **Invariants — violating any of these is a failed port:**
> - Mod id and namespace stay `saltandpepper`.
> - Every registry id, tag id, and the `saltandpepper:seasonings` key stay byte-identical to the reference.
> - The seasoning system must remain generic — it modifies any food item from any mod via its food data. Never introduce per-food item variants.
> - Farmer's Delight stays a soft, JSON-only dependency. No Java imports of FD classes. The game must boot with FD absent.
>
> **Where you are uncertain about an API on `[VERSION]`, check the real sources or the official changelogs rather than guessing, and say so in your inventory.** Relevant NeoForge release notes: `[URLS]`.

---

## 9. PARITY.md — checklist every port must pass

Copy this into each port and have the agent fill it in.

**Content parity**
- [ ] All 7 items registered under identical ids
- [ ] All 3 blocks registered under identical ids (+ optional salt block)
- [ ] Creative tab contains exactly the same entries in the same order
- [ ] All lang keys present; no untranslated keys in-game

**Pepper chain**
- [ ] Seeds place only on supporting logs; vine breaks when the log is removed
- [ ] Three growth stages render correctly and match the reference visually
- [ ] Bone meal advances growth
- [ ] Right-click harvest at max age yields peppercorns and resets the stage
- [ ] Cauldron over a lit campfire converts green → blanched, consumes one water level, plays sound and particles
- [ ] Furnace and campfire drying both work; smoker and blast furnace correctly do **not**
- [ ] Grinding yields 2× ground pepper

**Salt chain**
- [ ] Ore generates at comparable density to the reference (compare with the same world seed if the target version allows)
- [ ] Deepslate variant appears below y=0
- [ ] Any pickaxe mines it; Silk Touch and Fortune behave identically
- [ ] Smelting and blasting both refine raw rock salt

**Seasoning**
- [ ] Salt and pepper each apply to vanilla foods
- [ ] Both apply to at least one **modded** food (test with any food mod)
- [ ] Same seasoning cannot be applied twice
- [ ] Salt + pepper stack to the full bonus
- [ ] Tooltip line appears and reads correctly
- [ ] Blacklisted items are rejected
- [ ] Config values change the bonus as expected

**Robustness**
- [ ] Boots with only this mod
- [ ] Boots with Farmer's Delight present — compat recipes appear
- [ ] Boots with Farmer's Delight **absent** — no errors, no recipe-type spam
- [ ] Boots alongside Salt: Renewed (if it exists for this target) — built-in salt disables cleanly
- [ ] Dedicated server + separate client: no desync, no client-side item duplication
- [ ] No crash when the cauldron empties on the final interaction

**Cross-port consistency**
- [ ] A world saved on the reference build and opened on this port keeps seasoned foods seasoned (where the MC version makes this possible at all — note it explicitly if not)
- [ ] `sync-resources.sh` reports no unexplained differences in the shared asset tree

---

## 10. Honest expectations

- Fabric 1.21.1 is a genuinely easy port. A capable agent with the reference source should get it close to right in one pass, with the biome injection and config being the two places it'll need a second look.
- The 26.x port is not an easy port and shouldn't be framed as one. Budget for the item model format change and the food component split to each need real debugging.
- 1.20.1 is a rewrite of one subsystem, not a port, and it's fine to decide it isn't worth it.
- Whatever you do, resist letting features diverge between ports. The moment one port has something another doesn't, the parity checklist stops being checkable and you own N separate mods instead of one mod in N shapes.
