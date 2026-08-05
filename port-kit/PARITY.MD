How to use this file

Reference build: NeoForge 1.21.1 on main. Every line below means "behaves the same as the reference build," not "works."

Verification column — be honest, this is the whole point:

Code	Meaning
A	Agent-verified — checked in code, in the build output, or via git diff
H	Human-verified in-game on this port
C	Comparative — verified against the reference build side by side, same world seed where relevant

Lines marked [H] or [C] in the checklist below cannot be signed off by an agent. An agent that marks them A has not tested them.

Leave a line unchecked rather than guessing. An unchecked line is information; a wrongly-checked line is a bug that ships.

Port details
Target: <loader> <minecraft version>
Branch: port/<...>
Reference commit on main: <sha>
Tested on: <loader version>, Java <version>
Test world seed used for comparisons: <seed>
Completed by / date: <...>
1. Build and structure
✓	Check	How
☐	Builds without warnings that reference missing resources	A
☐	Mod id and namespace are saltandpepper	A
☐	git diff main shows no changes under textures/ or lang/	A
☐	Every registry id identical to reference (items, blocks, recipes, tags, advancements)	A
☐	Component keys saltandpepper:seasonings and saltandpepper:shaker_uses unchanged	A
☐	All balance numbers identical: seasoning bonuses, shaker capacity, refill cost, ore density settings, drop counts, smelting times	A
☐	No Java imports of Farmer's Delight classes anywhere	A
2. Content parity
✓	Check	How
☐	All 7 base items registered under identical ids	A
☐	All 3 shaker items registered under identical ids	A
☐	All blocks registered under identical ids (+ optional salt block)	A
☐	Creative tab contains the same entries in the same order [H]	H
☐	All lang keys resolve; no raw keys visible in-game [H]	H
☐	All textures and models render correctly, including the three vine stages [H]	H
3. Pepper chain
✓	Check	How
☐	Seeds place only on supporting logs; rejected elsewhere [H]	H
☐	Vine breaks and drops when its supporting log is removed [H]	H
☐	Three growth stages render correctly and match the reference visually [C]	C
☐	Bone meal advances growth [H]	H
☐	Right-click harvest at max age yields peppercorns and resets the stage [H]	H
☐	Breaking at each age drops the correct items [H]	H
☐	Cauldron over a lit campfire converts green → blanched, consumes one water level, plays sound and particles [H]	H
☐	Cauldron step correctly refuses: unlit campfire, no heat source, empty cauldron, sneaking [H]	H
☐	Furnace and campfire drying both work	A/H
☐	Smoker and blast furnace correctly do not dry peppercorns [H]	H
☐	Grinding yields 2× ground pepper	A
4. Salt chain
✓	Check	How
☐	Ore generates at comparable density to the reference, same seed [C]	C
☐	Deepslate variant appears below y=0 [H]	H
☐	Any pickaxe mines it	A/H
☐	Silk Touch and Fortune behave identically to the reference [C]	C
☐	Smelting and blasting both refine raw rock salt	A
5. Worldgen
✓	Check	How
☐	Wild vines generate in jungle, bamboo jungle and sparse jungle [H]	H
☐	Vine frequency comparable to reference, same seed — spottable but not spammy [C]	C
☐	Modded jungle biomes receive generation (test with any biome mod) [H]	H
☐	Nothing generates where it shouldn't (no vines outside jungles, no salt in the Nether/End) [H]	H
6. Seasoning system
✓	Check	How
☐	Salt and pepper each apply to vanilla foods via crafting	A/H
☐	Both apply to at least one modded food [H]	H
☐	Same seasoning cannot be applied twice	A/H
☐	Salt + pepper stack to the full combined bonus [H]	H
☐	Resulting nutrition and saturation match the reference exactly [C]	C
☐	Tooltip line appears and reads correctly [H]	H
☐	Blacklisted items are rejected	A/H
☐	Config values change the bonus as expected [H]	H
7. Shakers
✓	Check	How
☐	Seasons a full stack of vanilla food in one right-click [H]	H
☐	Seasons a modded food identically [H]	H
☐	Refuses already-seasoned, blacklisted, and non-food items, falling through to normal vanilla click behaviour [H]	H
☐	Both shakers apply to the same food, stacking the bonus [H]	H
☐	Insufficient uses → nothing happens, clear feedback, no partial seasoning, no items lost [H]	H
☐	Refill works by clicking the shaker onto a pile of its seasoning [H]	H
☐	empty_shaker clicked onto a pile of seasoning fills it [H]	H
☐	Becomes empty_shaker at zero uses, never disappears [H]	H
☐	Fill bar renders with the right colour and proportion [H]	H
☐	Not enchantable at a table or anvil [H]	H
☐	Works in the survival inventory, a crafting table grid, and a chest GUI [H]	H
8. Robustness
✓	Check	How
☐	Boots with only this mod installed [H]	H
☐	Boots with Farmer's Delight present; compat recipes appear [H]	H
☐	Boots with Farmer's Delight absent; no errors, no recipe-type spam [H]	H
☐	Boots alongside Salt: Renewed if it exists for this target; built-in salt disables cleanly [H]	H
☐	Dedicated server + separate client: no desync on any interaction [H]	H
☐	Shakers under rapid repeated clicking on a server: no duplication, no item loss [H]	H
☐	No crash when the cauldron empties on the final interaction [H]	H
☐	World save/reload preserves seasoned foods and shaker fill levels [H]	H
9. Cross-port consistency
✓	Check	How
☐	sync-shared.sh reports no unexplained differences in textures or lang	A
☐	A datapack written for the reference build works here unchanged (tag ids stable) [H]	H
☐	Config keys and defaults match the reference, even if the file format differs	A
10. Deviation log

Fill this in even if it's empty — "none" is a meaningful result.

Record anything that could not be made identical to the reference, why, and whether it's permanent or a to-do. This is the per-platform release notes and the answer to "why does the X build behave differently" a year from now.

Deviation	Reason	Permanent?	Player-visible?
e.g. Farmer's Delight compat recipes absent	FD Refabricated not available for this version	No — add when it ships	Yes
e.g. Config is JSON rather than TOML	Loader has no built-in config system	Yes	Yes, file location differs
			
11. Known-unverified

Anything you could not test and why — missing test mod, no server available, feature depends on something absent on this platform. Better recorded here than silently ticked above.

12. Sign-off
 Every line above is either checked, or left unchecked with an entry in §11
 Deviation log is complete
 PARITY.md committed on this branch
 Release tagged v<version>+<loader>-<mcversion>