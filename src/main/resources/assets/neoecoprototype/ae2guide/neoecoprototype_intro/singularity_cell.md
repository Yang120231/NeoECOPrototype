---
navigation:
  title: L1 Singularity Cell
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
---

# L1 Singularity Cell

Once it sits in a drive, **120 singularities land every 30 seconds**. It consumes nothing and takes nothing in, and it is drawn from like any other cell.

## How fast

One batch is 120, and the bank jumps by that much each interval - it does not drip. The shipped interval is 600 ticks (30 s), which averages about 4 a second.

| Time in a drive | Ready |
|-----------------|-------|
| 30 s | 120 |
| 1 min | 240 |
| 5 min | 1,200 |
| 30 min | 7,200 |
| 1 hour | 14,400 |
| 24 hours | 345,600 |

| What you want it for | Needs | At the default rate |
|----------------------|-------|---------------------|
| This addon's three L1 interfaces | 6 | the first batch covers it (30 s) |
| one eco MEGA 4G matrix, or one infinite storage component | 128 | two batches, 60 s |
| eco's infinite storage unlock (64 components) | 8,192 | 69 batches, about 35 min |

## How far it can be tuned

Both keys live in the `singularity_cell` section of the server config and are read fresh each time:

| Key | Default | Range |
|-----|---------|-------|
| `ticks_per_batch` | 600 ticks (30 s) | 60 ticks (3 s) minimum |
| `amount_per_batch` | 120 | 1 to 9,223,372,036,854,775,807 - write down anything |

The bank itself is a `long`, and **that same top is where it stops**: past it the figure pins instead of wrapping, and because production is pinned at the same ceiling, whatever you draw after that stays drawn. The world clock running out saturates the bank the same way rather than turning it negative.

## The contents are computed, not saved

Nothing accumulates: the cell stores two numbers - the world tick it started at and how much has been taken - and every question is answered from the current tick, so output depends on start tick, current tick and amount taken and nothing else. Four consequences. Raising `amount_per_batch` re-reads history and grows the contents at once, lowering it can erase what was stored. Pausing or shutting the server stops world time, so contents stop too. The start tick is written the first time a drive or host asks, so a cell in an inventory, a chest or on the ground has not started, while one that already started keeps growing when you take it out. Terminals always read the server value; a client hover syncs once a second and can be a second behind.

## Limits

It holds one content type, so it cannot be partitioned in a cell workbench and cannot be stored inside another cell. It reports the L1 tier, so [the L1 storage system](storage_system.md) takes it without a whitelist entry, and eco's higher tiers do too.

## Recipe

<RecipeFor id="neoecoprototype:simplify_singularity_cell" />

A disassembly recipe returns the four materials.
