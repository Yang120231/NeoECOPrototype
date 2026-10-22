# Third-Party Notices

Neo ECO Prototype is an addon. No dependency JAR is included in this repository or in the source release; each project below stays the property of its authors, and users are pointed at the official download pages.

## Dependencies

| Component | Mod ID | Version this addon builds against | License as published upstream |
|---|---|---|---|
| Neo ECO AE Extension | `neoecoae` | `21.2.9` (also the declared floor) | GNU GPL-3.0 |
| Applied Energistics 2 | `ae2` | `19.2.17` | LGPL v3 / GPL v3 for code; **textures and models CC BY-NC-SA 3.0** |
| GuideME | `guideme` | `21.1.1` | see upstream |
| LDLib2 | `ldlib2` | `2.2.40` (hard dependency, inherited from upstream's floor) | LGPL-3.0 per mod metadata |
| Mega Cells | `megacells` | `4.11.0` (optional) | LGPL-3.0 per mod metadata |
| Extended AE | `extendedae` | `1.21-2.2.28` (compile-time reference only) | GNU LGPLv3 |
| AE2 OmniCells | `ae2omnicells` | `1.1.6` (optional) | see upstream |
| AppliedFlux | `appflux` | `1.21-2.1.5` (optional) | see upstream |
| Beyond Dimensions | `beyonddimensions` | `0.7.30` (optional) | see upstream |
| Just Enough Items | `jei` | `19.57.0.445` (compile-time only) | MIT |
| Jade | `jade` | Curse file `7428884` (compile-time only) | CC-BY-NC-SA-4.0 per mod metadata |
| Mekanism / Applied Mekanistics / AE2-JEI-Integration | — | local runtime only, never bundled | see upstream |

Development JARs are resolved from local files, so a fresh clone cannot compile until they are placed; the build then stops with the exact missing paths instead of failing later as unresolved symbols. The `21.2.9` build of Neo ECO AE Extension used as the baseline was handed over by its author and has no public download page yet - a packaging note, not a stability one.

One model, not a texture, is copied rather than referenced: `models/block/simplify_casing.json` carries the 13-element geometry of upstream `assets/neoecoae/models/block/casing_base.json` with its own texture slots. It is inlined on purpose - this addon's CI rejects any asset that points into the `neoecoae` namespace, because an upstream rename would otherwise turn our casing into a silent missing model. Geometry only; the surface artwork is our own.

## Artwork and model assets

- `Neo-TiX` - original lead artist and artwork contributor; attribution retained with permission.
- `kyln默寒寒冰` - original artwork contributor; attribution retained with permission.

Of the 320 PNGs this addon ships, in three groups:

1. **262 original** - drawn by the artists above. **All Rights Reserved (ARR)**: not to be extracted, reused or redistributed separately without permission. Permission to use the artwork as part of NeoECOPrototype does not transfer ownership or grant a separate redistribution license.
2. **50 adapted from Neo ECO AE Extension** - 9 byte-for-byte copies of cell-art textures and 41 that keep upstream's pixel layout with a different palette. Upstream publishes under GPL-3.0 with no separate asset clause, so these carry upstream's licensing and attribution requirements and are not relicensed by this addon.
3. **8 adapted from Applied Energistics 2** - four pattern-provider faces, one interface face and three part backs, each keeping AE2's pixel layout with a different palette. AE2's asset notice reads, verbatim: *"Textures and Models (c) 2020, Ridanisaurus Rid, (c) 2013 - 2020 AlgorithmX2 et al / License: CC BY-NC-SA 3.0"*. That license requires attribution, carries a non-commercial term, and requires derivatives to be shared under the same terms. **This addon makes no ARR claim over those eight files**, and anyone reusing them follows AE2's terms rather than ours.

The counts above were produced by comparing file content, not file names, against whole-jar indexes of `neoecoae-21.2.9.jar` (md5 `d4b17512930fef8020bfe42bf5e79dc6`, 755 PNGs) and `ae2-19.2.17.jar` (md5 `1d9768edc90fb50f065949f2628ea991`, 655 PNGs). "Same layout, different palette" means the per-pixel colour-equality sequence matches at equal size with at least three distinct colours, so flat glow sheets cannot match by accident; the palettes themselves were chosen file by file, and the 25 distinct colour maps found across those files are the record of that. The project code remains GPL-3.0-only, and GPL does not apply to the ARR artwork in group 1.

## Player skins bundled for the named dolls

`textures/block/fumo/skins/{reliqwq,yang120,kouooki,tedxenon}.png` are the actual player skins of four named people, shipped so their dolls show the right face without reaching the session service. A player skin is that player's own content, and Mojang's usage terms do not license us to redistribute it, so each of these four files stands on that person's own say-so. **Adding another doll's face requires asking first, not fetching.**

## Distribution rule

Do not copy dependency JARs into this repository or bundle them into a release without checking each upstream project's current redistribution terms. Link users to the official project pages instead.

Upstream projects: <https://github.com/DancingSnow0517/NeoECOAEExtension> · <https://github.com/AppliedEnergistics/Applied-Energistics-2> · <https://github.com/AppliedEnergistics/GuideME> · <https://github.com/Low-Drag-MC/LDLib2> · <https://github.com/mezz/JustEnoughItems> · <https://github.com/Snownee/Jade>
