# Agent Prompt — Salt & Pepper Shakers (addendum)

Feature addition to the existing **Salt & Pepper** mod (`saltandpepper`), NeoForge 1.21.1.

This is an **addendum to an existing, working mod**. Read the current source before writing anything. Do not restructure, rename, or "improve" existing systems — the only permitted changes to existing code are the refactor described in §3 and the additions listed here.

---

## 1. Goal

Two portable seasoning containers. The player picks a shaker up onto the cursor, right-clicks a stack of food anywhere in their inventory, and the whole stack is seasoned at once — no crafting table, no per-item crafting.

## 2. New items

| Item id | Notes |
|---|---|
| `empty_shaker` | Ordinary stackable item (stacks to 64). No special behavior. |
| `salt_shaker` | `stacksTo(1)`, holds uses, seasons with salt |
| `pepper_shaker` | `stacksTo(1)`, holds uses, seasons with pepper |

Both filled shakers must be **non-enchantable**: do not add them to any `#minecraft:enchantable/*` tag, and do not use vanilla durability (see §4). This keeps Mending, Unbreaking, and anvil interactions off the table entirely.

Add all three to the `saltandpepper:main` creative tab, positioned after the seasoning items.

## 3. Refactor first — one source of truth

The seasoning rules currently live inside the custom crafting recipe class. Before adding anything, **extract them into a shared helper** (e.g. `SeasoningHelper`) with at least:

- `canSeason(ItemStack food, ResourceLocation seasoningId)` — has a `minecraft:food` component, is in `#saltandpepper:seasonable`, is not in `#saltandpepper:seasoning_blacklist`, and does not already carry `seasoningId` in its `saltandpepper:seasonings` component.
- `season(ItemStack food, ResourceLocation seasoningId, int count)` — returns the seasoned copy with the food component rewritten and the seasonings list appended.

Then make **both** the crafting recipe and the shaker call into it. The two paths must never be able to diverge — a food that can be seasoned by hand must be seasonable by shaker and vice versa, with identical resulting stats. Verify the existing recipe still behaves identically after the refactor.

## 4. Fill level

Store fill in a custom data component `saltandpepper:shaker_uses` (a simple int), **not** vanilla durability.

- Capacity: config `shakerCapacity`, default **64**.
- A freshly crafted shaker starts full.
- Render a fill bar by overriding `isBarVisible`, `getBarWidth`, and `getBarColor` on the shaker item. Colour it to match its contents — near-white for salt, dark grey-brown for pepper — rather than the vanilla green-to-red durability gradient.
- Tooltip line showing remaining uses, e.g. `48 / 64`, via lang key `tooltip.saltandpepper.shaker_uses`.
- When uses hit 0, the stack **converts into `empty_shaker` in place**. It is never destroyed.

## 5. Recipes

**Empty shaker** — shaped, 2 tall in one column:
```
N   (minecraft:iron_nugget)
G   (minecraft:glass)
```
→ 1× `empty_shaker`. Glass body, perforated metal cap.

**Filling** — shapeless: `empty_shaker` + 8× `salt` → `salt_shaker` at full capacity. Same for `ground_pepper` → `pepper_shaker`.

That's 8 seasoning items for 64 uses, an 8× efficiency gain over hand-crafting. This is intentional: the per-food bonus is small, and the shaker's value is convenience. Keep both numbers config-driven (`shakerCapacity`, `usesPerRefillItem` default 8) so they can be retuned without a code change.

## 6. The interaction — this is the core of the feature

Implement `Item#overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player)` on the shaker: this fires when the shaker is **on the cursor** and the player clicks a slot. Also implement `Item#overrideOtherStackedOnMe(...)` for the reverse case (food or salt on the cursor, clicking the shaker sitting in a slot), so the interaction works in either direction.

**Use vanilla `BundleItem` as your structural reference for these two methods**, including how it handles sound and how it mutates stacks consistently on both sides. Inventory clicks run on the client with prediction and on the server authoritatively; anything that behaves differently between them becomes a desync or a duplication exploit.

Only react to `ClickAction.SECONDARY` (right-click). Return `false` for everything else so vanilla behavior is untouched.

### Branch on what's in the clicked slot

**A) Slot contains this shaker's seasoning item** (`salt` for the salt shaker, `ground_pepper` for the pepper shaker) → **refill**.
- `needed = ceil((capacity - currentUses) / usesPerRefillItem)`, clamped to the slot's count.
- If the shaker is already full or `needed` is 0, do nothing, return `false`.
- Consume that many items from the slot, add `consumed * usesPerRefillItem` uses, clamped to capacity.
- Also accept an `empty_shaker` on the cursor clicked onto a pile of seasoning — same path, producing the appropriate filled shaker.

**B) Slot contains a valid seasonable food** (per `SeasoningHelper.canSeason`) → **season**.
- Let `count` = the slot stack's size.
- **If `currentUses < count`: do nothing.** Play a soft failure click and, server-side only, send an action bar message along the lines of "Not enough pepper — needs 64, has 41" (lang key, not a hardcoded string). Return `true` so the click is consumed rather than falling through to a vanilla stack swap.
- Otherwise: replace the slot's contents with the seasoned equivalent of the whole stack, subtract `count` uses, play a shaking sound with slightly randomised pitch.

**C) Anything else** → return `false`, vanilla behavior.

### Explicitly rejected alternative — do not implement it

Do **not** partially season a stack when uses are insufficient. That requires splitting the stack and finding a home for the unseasoned remainder while the cursor is occupied by the shaker, which means inventory insertion or item drops inside an inventory-click handler running on both sides. All-or-nothing is deliberate: deterministic, no leftovers, no desync surface. The player can split the stack themselves if they want a partial batch.

### Multiplayer requirements

- Stack mutations must be identical on client and server.
- Never spawn item entities from this handler.
- Action bar feedback and any logging: server side only.
- Test on a dedicated server with a separate client, including rapid repeated clicking.

## 7. Sound and feel

One short shake sound on a successful season — a vanilla sound is fine, randomise the pitch slightly. A quieter, lower-pitched variant of the same sound on refill. A dull click on failure. No particles.

## 8. Advancement

Grant on first obtaining any filled shaker. Parent it to the existing ground pepper / salt advancement.

## 9. Textures — three new files

All 16×16 PNG-32 with alpha, in `src/main/resources/assets/saltandpepper/textures/item/`:

| File | What it should read as |
|---|---|
| `empty_shaker.png` | Clear glass body with a metal cap, visibly empty. Should read as "container", not "item". |
| `salt_shaker.png` | Same silhouette, white contents visible through the glass. |
| `pepper_shaker.png` | Same silhouette, dark contents. |

Keep the silhouette identical across all three so they read as one family and the difference is purely the contents. Generate obvious placeholders at these exact paths so the mod runs before the real art lands, and add these three rows to `TEXTURES.md`.

Total texture count for the mod rises from 12 to 15, plus the icon.

## 10. Acceptance checklist

- [ ] Existing crafting-table seasoning still works exactly as before after the `SeasoningHelper` refactor
- [ ] Shaker seasons a full stack of vanilla food in one right-click
- [ ] Shaker seasons a **modded** food identically
- [ ] Shaker refuses already-seasoned and blacklisted foods, and non-food items, falling through to normal vanilla click behavior
- [ ] Salt shaker and pepper shaker can both be applied to the same food, stacking the bonus
- [ ] Insufficient uses → nothing happens, clear feedback, no partial seasoning, no items lost
- [ ] Refill works by clicking the shaker onto a pile of its seasoning
- [ ] `empty_shaker` clicked onto a pile of seasoning fills it
- [ ] Shaker becomes `empty_shaker` at zero uses, never disappears
- [ ] Fill bar renders with the right colour and correct proportion
- [ ] Not enchantable at a table or anvil
- [ ] Dedicated server + separate client: no desync, no duplication under rapid clicking
- [ ] Works in the survival inventory, a crafting table's inventory grid, and a chest GUI

## 11. Note for the porting kit

Both interaction hooks are vanilla `Item` methods, not loader APIs, so this feature adds **no new rows to the loader touchpoint table** — it ports unchanged to Fabric and Forge. Two things do need adding to the kit:

- The custom `saltandpepper:shaker_uses` component joins `saltandpepper:seasonings` in the "no data components below 1.20.5" problem described in the porting kit §5. On 1.20.1 it becomes stack NBT.
- Add the §10 checklist items to `PARITY.md`.
