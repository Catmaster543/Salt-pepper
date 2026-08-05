# Texture manifest — Salt & Pepper

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
| `empty_shaker.png` | Portable container | Clear glass body with a metal cap, visibly empty. Should read as "container", not "item". |
| `salt_shaker.png` | Filled with salt | Same silhouette, white contents visible through the glass. |
| `pepper_shaker.png` | Filled with pepper | Same silhouette, dark contents. |

**The three shakers must share one silhouette.** Only the contents change between them — that is what
makes them read as a family and makes the fill state legible at a glance in a hotbar. Draw the glass
and cap once and reuse it for all three.

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
(extract from the client jar, or from your resource pack tooling). Draw into the same regions those use. The exact per-face UV rectangles are listed in [UV reference](#uv-reference-pepper-vine-stages) below — check that section if anything renders wrong.

Stage 2 is what generates wild in jungles, so it needs to be spottable against jungle foliage while still looking at home there.

### Salt ore

| File | Purpose | Notes |
|---|---|---|
| `rock_salt_ore.png` | Stone-variant ore | Draw the salt specks over vanilla stone-grey background so it blends with surrounding stone. Pale, slightly translucent-looking crystals. |
| `deepslate_rock_salt_ore.png` | Deepslate variant | Identical crystal pattern over the darker deepslate background. Copy the ore-speck placement from the stone version so they read as the same ore. |
| `salt_block.png` | Storage block | All six faces use this one texture. Dense packed white crystal. |

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
        │   ├── salt.png
        │   ├── empty_shaker.png
        │   ├── salt_shaker.png
        │   └── pepper_shaker.png
        └── block/
            ├── pepper_vine_stage0.png
            ├── pepper_vine_stage1.png
            ├── pepper_vine_stage2.png
            ├── rock_salt_ore.png
            ├── deepslate_rock_salt_ore.png
            └── salt_block.png
```

**Total: 16 textures at 16×16, plus one mod icon.** (13 for v1, plus the three shakers.)

---

# UV reference — pepper vine stages

Read directly out of the final model JSONs:

- `assets/saltandpepper/models/block/pepper_vine_stage0.json`
- `assets/saltandpepper/models/block/pepper_vine_stage1.json`
- `assets/saltandpepper/models/block/pepper_vine_stage2.json`

These are copied verbatim from vanilla `cocoa_stage0/1/2.json` — every `from`/`to` and every explicit `uv` — with only the texture reference changed. So the vanilla cocoa textures work as drop-in templates at every stage.

## How to read these numbers

- UV values are `[x1, y1, x2, y2]` in **pixels on a 16×16 texture**.
- Origin `[0,0]` is the **top-left** corner of the image; `y` increases **downward**.
- A rectangle where `x1 > x2` is **mirrored horizontally** (this is how the stem plane's east face is drawn).
- Every stage's model has two elements: the **pod** (a box) and the **stem** (a flat plane connecting the pod to the log).

## Stage 0 — `pepper_vine_stage0.png`

Pod box `from [6,7,11] → to [10,12,15]` (4×5×4 px)

| Face | UV rect `[x1,y1,x2,y2]` | Region size | Notes |
|---|---|---|---|
| `up` | `[0, 0, 4, 4]` | 4×4 | top cap |
| `down` | `[0, 0, 4, 4]` | 4×4 | same region as `up` |
| `north` | `[11, 4, 15, 9]` | 4×5 | pod body |
| `south` | `[11, 4, 15, 9]` | 4×5 | same region |
| `west` | `[11, 4, 15, 9]` | 4×5 | same region |
| `east` | `[11, 4, 15, 9]` | 4×5 | same region |

Stem plane `from [8,12,12] → to [8,16,16]` (flat, 4×4 px)

| Face | UV rect | Region size | Notes |
|---|---|---|---|
| `west` | `[12, 0, 16, 4]` | 4×4 | |
| `east` | `[16, 0, 12, 4]` | 4×4 | **mirrored** (x1 > x2) |

**Pixels you must paint for stage 0:** `(0,0)–(3,3)`, `(11,4)–(14,8)`, `(12,0)–(15,3)`. Everything else can stay transparent.

## Stage 1 — `pepper_vine_stage1.png`

Pod box `from [5,5,9] → to [11,12,15]` (6×7×6 px)

| Face | UV rect `[x1,y1,x2,y2]` | Region size | Notes |
|---|---|---|---|
| `up` | `[0, 0, 6, 6]` | 6×6 | top cap |
| `down` | `[0, 0, 6, 6]` | 6×6 | same region as `up` |
| `north` | `[9, 4, 15, 11]` | 6×7 | pod body |
| `south` | `[9, 4, 15, 11]` | 6×7 | same region |
| `west` | `[9, 4, 15, 11]` | 6×7 | same region |
| `east` | `[9, 4, 15, 11]` | 6×7 | same region |

Stem plane `from [8,12,12] → to [8,16,16]` (flat, 4×4 px)

| Face | UV rect | Region size | Notes |
|---|---|---|---|
| `west` | `[12, 0, 16, 4]` | 4×4 | identical to stage 0 |
| `east` | `[16, 0, 12, 4]` | 4×4 | **mirrored** |

**Pixels you must paint for stage 1:** `(0,0)–(5,5)`, `(9,4)–(14,10)`, `(12,0)–(15,3)`.

## Stage 2 — `pepper_vine_stage2.png` (mature — the one that generates wild)

Pod box `from [4,3,7] → to [12,12,15]` (8×9×8 px)

| Face | UV rect `[x1,y1,x2,y2]` | Region size | Notes |
|---|---|---|---|
| `up` | `[0, 0, 8, 8]` | 8×8 | top cap |
| `down` | `[0, 0, 8, 8]` | 8×8 | same region as `up` |
| `north` | `[8, 4, 16, 13]` | 8×9 | pod body |
| `south` | `[8, 4, 16, 13]` | 8×9 | same region |
| `west` | `[8, 4, 16, 13]` | 8×9 | same region |
| `east` | `[8, 4, 16, 13]` | 8×9 | same region |

Stem plane `from [8,12,12] → to [8,16,16]` (flat, 4×4 px)

| Face | UV rect | Region size | Notes |
|---|---|---|---|
| `east` | `[16, 0, 12, 4]` | 4×4 | **mirrored** |
| `west` | `[12, 0, 16, 4]` | 4×4 | identical to stages 0 and 1 |

**Pixels you must paint for stage 2:** `(0,0)–(7,7)`, `(8,4)–(15,12)`, `(12,0)–(15,3)`.

## Summary map

All three stages share the stem region and anchor their pod body to the **bottom-right** area of the image, growing leftward and downward as the pod matures:

```
stage 0        stage 1        stage 2
+--------+     +--------+     +--------+
|CC..SSSS|     |CCC.SSSS|     |CCCCSSSS|   rows 0-3   C = top/bottom cap
|CC..SSSS|     |CCC.SSSS|     |CCCCSSSS|             S = stem plane
|....xBBB|     |..xxBBBB|     |xxxxBBBB|   rows 4-7   B = pod body
|....xBBB|     |..xxBBBB|     |xxxxBBBB|             x = unused by that stage
|........|     |..xxBBBB|     |xxxxBBBB|   rows 8-12
|........|     |........|     |xxxxBBBB|
+--------+     +--------+     +--------+
```

The three cap regions are nested at the top-left corner (4×4 → 6×6 → 8×8), and the three body
regions are anchored to the right edge at row 4 and grow left and down (4×5 → 6×7 → 8×9). Drawing
each stage's art inside the *larger* stage-2 rectangles and then cropping is the easiest workflow.

## Placeholder note

The shipped placeholders paint exactly these rectangles in flat colours — cap, body and stem in
three different shades per stage — so you can see the layout in-game before drawing anything. They
are not art; replace them.
