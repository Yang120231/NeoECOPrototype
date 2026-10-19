---
navigation:
  title: FE Storage Matrices
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# FE Storage Matrices

FE storage matrices need **AppliedFlux** and store Forge Energy (FE) in the ME network.

## Capacity

Capacity is counted in **bytes**; what a byte is worth in FE is AppliedFlux's own `flux_cell.amount`, 1,048,576 FE per byte out of the box.

| Matrix | Bytes | At the default rate |
| --- | --- | --- |
| <ItemLink id="neoecoprototype:simplify_fe_storage_cell_1m" /> | 1,048,576 | about 1.1 trillion FE |
| <ItemLink id="neoecoprototype:simplify_fe_storage_cell_4m" /> | 4,194,304 | about 4.4 trillion FE |

Raising `flux_cell.amount` loosens every FE cell in the game, ours included, and costs no materials.

## Recipe

<RecipeFor id="neoecoprototype:simplify_fe_cell_housing" />

<RecipeFor id="neoecoprototype:simplify_fe_storage_cell_1m" />

<RecipeFor id="neoecoprototype:simplify_fe_storage_cell_4m" />

A disassembly recipe gives the housing and the component back.

## Where it fits

They report the L1 tier, so [the L1 storage system](storage_system.md) takes them without any whitelist entry, and eco's higher tiers do too. In the L1 storage host panel each gets its own row, named after eco's own FE cell type ("energy") - the same name it carries inside eco's own hosts.
