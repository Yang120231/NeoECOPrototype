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

One <ItemLink id="neoecoprototype:simplify_storage_controller" /> host, plus any number of <ItemLink id="neoecoprototype:simplify_drive" /> drives and <ItemLink id="neoecoprototype:simplify_energy_cell" /> energy cells. It idles at **1/8** of eco's L4 equivalent.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Parts

| Part | Role |
|------|------|
| Storage host | sets the tier and carries the panel; **its front face is the panel** |
| Storage drive | holds the matrices and mounts them into the network |
| Energy cell | energy buffer |
| Storage interface | supplies the stored resources outward |
| Storage network interface | joins the structure to the ME network |
| Storage vent | heat management |
| Storage casing | fills out the frame |

Placing blocks one by one is error-prone, so use the **auto-build panel** in the host's own GUI: preview the shape, then fill it in one click.

## Which matrix

| What to store | Matrix family | Needs |
|---------------|---------------|-------|
| Items, fluids, chemicals (1K - 4M) | item / fluid / chemical storage matrices | chemicals also need Mekanism and Applied Mekanistics |
| A few resources, each nearly unlimited | [Small Bulk Matrices](small_bulk_matrices.md) | MEGA Cells |
| Many resources at once | universal / quantum / complex universal matrices | AE2 Omni Cells |
| One item, infinitely | infinite matrix (`concrete`) / KubeJS infinite matrix | - |
| FE | [FE Storage Matrices](flux_storage.md) | AppliedFlux |
| Singularities, no condenser | [L1 Singularity Cell](singularity_cell.md) | - |

Every matrix can be partitioned: edit its whitelist in AE2's cell workbench.

## Other mods' cells

L1 drives mount L1 cells and small bulk cells by default. eco's higher-tier cells - including eco's own MEGA Cells long cells - insert but do not mount unless a server admin whitelists them (`additional_storage_cells`).
