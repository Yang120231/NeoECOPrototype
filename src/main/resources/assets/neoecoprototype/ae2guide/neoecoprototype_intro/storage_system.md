---
navigation:
  title: L1 Storage System
  icon: neoecoprototype:simplify_storage_controller
  position: 10
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_storage_controller
  - neoecoprototype:simplify_drive
  - neoecoprototype:simplify_energy_cell
  - neoecoprototype:simplify_item_storage_cell_1k
  - neoecoprototype:simplify_item_storage_cell_16k
  - neoecoprototype:simplify_item_storage_cell_64k
  - neoecoprototype:simplify_item_storage_cell_1m
  - neoecoprototype:simplify_item_storage_cell_4m
  - neoecoprototype:simplify_fluid_storage_cell_1k
  - neoecoprototype:simplify_fluid_storage_cell_16k
  - neoecoprototype:simplify_fluid_storage_cell_64k
  - neoecoprototype:simplify_fluid_storage_cell_1m
  - neoecoprototype:simplify_fluid_storage_cell_4m
  - neoecoprototype:simplify_chemical_storage_cell_1k
  - neoecoprototype:simplify_chemical_storage_cell_16k
  - neoecoprototype:simplify_chemical_storage_cell_1m
  - neoecoprototype:simplify_chemical_storage_cell_4m
---

# L1 Storage System

The L1 Storage System is an extensible multiblock that does the job of AE2's <ItemLink id="ae2:drive" /> - it mounts storage matrices into the ME network - but it carries its own energy buffer, and it brings eco's L4 shape forward to the L1 tier. It idles at **1/8** of what eco's L4 equivalent costs.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Parts

| Part | Holds | What it does | Idle draw | What its absence costs |
|------|-------|--------------|-----------|------------------------|
| Storage host | - | Fixes the tier at L1; **its front face is the control face**, and the auto-build panel lives there | 32.25 AE/t | The structure never forms |
| Storage matrix drive | one storage matrix | Mounts the matrix into the network; insert a cell by right-clicking, take it out sneak-right-clicking empty-handed | 32 AE/t each | One slot of capacity, plus that drive's draw |
| Energy cell | - | Buffers **100,000 AE** for the structure and shares it with the network | 0 | Buffer space |
| Storage interface | - | Turns invisible once formed and hands the cluster's grid node to outside ME cables | 2 AE/t | The structure never forms |
| Storage network interface | - | The one the player opens: same cluster behaviour plus eco's interface UI | 2 AE/t | No GUI; nothing else changes |
| Storage vent | - | Appearance and an idle draw, that is all - **there is no heat or shutdown mechanic** | 4 AE/t | 4 AE/t less, no function lost |
| Storage casing | - | Fills out the frame | 2 AE/t | The structure never forms |

Absence has only two honest answers: the slot is fixed (the structure never forms) or the member is optional (you lose its function and its draw). Nothing in this system is punished by temperature. Members not named above draw **2 AE/t** - that is eco's shared 16 AE/t component baseline, scaled to L1.

## The shape of a column

## Building it

1. Place the host facing out, then frame the rest with casings.
2. Put the storage interface **behind and to the left of the host** - a fixed slot that turns invisible once the structure forms.
3. Starting one block right of the casing on the host's right side, lay storage matrix drives horizontally, three per column.
4. Directly behind those drives, one column: energy cell at the bottom, storage vent in the middle, energy cell on top.
5. Fill the remaining positions with casings.

Lengthening means adding whole columns: three drives plus their backing column. If you would rather not place blocks one by one, use the **auto-build panel** in the host's GUI - what it plans is the authoritative shape, and those steps are the order it lays them out.

The host's **32.25 AE/t** is not a typo: eco's baseline for a storage host is 258 AE/t, and L1 scales it by 1/8.

## Which matrix

| What to store | Matrix family | Needs |
|---------------|---------------|-------|
| Items, fluids, chemicals (1K - 4M) | item / fluid / chemical storage matrices | chemicals also need Mekanism and Applied Mekanistics |
| A few resources, each nearly unlimited | [Small Bulk Matrices](small_bulk_matrices.md) | MEGA Cells |
| Many resources at once | universal / quantum / complex universal matrices (1K / 1M) | AE2 Omni Cells |
| One item, infinitely | infinite matrix (`concrete`) / KubeJS infinite matrix | - |
| FE | [FE Storage Matrices](flux_storage.md) | AppliedFlux |
| Mekanism's spent nuclear waste, and only that | spent nuclear waste storage matrices (1K / 1M) | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| Singularities, no Matter Condenser build | [L1 Singularity Cell](singularity_cell.md) | - |

Partition a matrix in AE2's cell workbench to give it a whitelist. The singularity cell is the exception: it holds only what it grows itself and cannot be partitioned.

## Other mods' cells

L1 drives mount L1 cells and small bulk cells by default. eco's higher-tier cells - including eco's own MEGA Cells long cells - insert but do not mount unless a server admin whitelists them (`additional_storage_cells`).
