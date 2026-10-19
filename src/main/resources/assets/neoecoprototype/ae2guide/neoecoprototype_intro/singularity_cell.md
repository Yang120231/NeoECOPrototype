---
navigation:
  title: L1 Singularity Cell
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
---

# L1 Singularity Cell

The L1 Singularity Cell is a storage cell that **consumes nothing**: once it sits in a drive its contents grow with world time, and it is drawn from like any other cell.

## How fast

| Key | Default | Clamp |
|-----|---------|-------|
| `ticks_per_batch` | 600 ticks (30 s) | 60 ticks minimum |
| `amount_per_batch` | 120 | 262,144 maximum |

The shipped rate is about 4 singularities a second. There is no cap - contents are counted in a `long`. Both keys live in the `singularity_cell` section of the server config.

| What you want it for | Needs | At the default rate |
|----------------------|-------|---------------------|
| This addon's three L1 interfaces | 6 | the first batch covers it |
| one eco MEGA 4G matrix, or one infinite storage component | 128 | 32 s |
| eco's infinite storage unlock (64 components) | 8,192 | about 34 min |

## The contents are computed, not saved

Nothing accumulates: the cell stores two numbers - the world tick it started at and how much has been taken - and every question is answered from the current tick, so output depends on start tick, current tick and amount taken and nothing else. Four consequences. Raising `amount_per_batch` re-reads history and grows the contents at once, lowering it can erase what was stored. Pausing or shutting the server stops world time, so contents stop too. The start tick is written the first time a drive or host asks, so a cell in an inventory, a chest or on the ground has not started, while one that already started keeps growing when you take it out. Terminals always read the server value; a client hover syncs once a second and can be a second behind.

## Limits

It holds one content type, so it cannot be partitioned in a cell workbench and cannot be stored inside another cell. It reports the L1 tier, so [the L1 storage system](storage_system.md) takes it without a whitelist entry, and eco's higher tiers do too.

## Recipe

<RecipeFor id="neoecoprototype:simplify_singularity_cell" />

A disassembly recipe returns the four materials.
