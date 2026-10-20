---
navigation:
  title: Small Bulk Matrices
  icon: neoecoprototype:simplify_small_bulk_storage_cell
  position: 20
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_small_bulk_storage_cell
  - neoecoprototype:simplify_small_bulk_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_fluid_storage_cell
  - neoecoprototype:simplify_small_bulk_fluid_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_chemical_storage_cell
  - neoecoprototype:simplify_small_bulk_chemical_storage_cell_expanded
  - neoecoprototype:simplify_small_bulk_expansion_card
  - neoecoprototype:simplify_small_bulk_storage_matrix_housing
  - neoecoprototype:simplify_small_bulk_fluid_storage_matrix_housing
  - neoecoprototype:simplify_small_bulk_chemical_storage_matrix_housing
---

# Small Bulk Matrices

The **Small Bulk Matrix** is the extreme member of the <ItemLink id="ae2:drive" /> family: an ordinary matrix holds many kinds in limited amounts, this one holds very few kinds in nearly unlimited amounts. **3** kinds by default, **10** after an expansion card.

| Aspect | Value |
|--------|-------|
| Kind limit | 3, or 10 with the expansion card |
| Amount per kind | nearly unlimited |
| Needs | MEGA Cells (4.11.0 is the version measured); the chemical variant also needs Mekanism and Applied Mekanistics |

## Partitioning

Partitioning is a cell whitelist: only the kinds you allow enter the cell, everything else passes by. Edit it in AE2's cell workbench; the fluid and chemical cells partition against their own key types.

## Compression chains

Only the item variant has one. Fitting a MEGA Cells **compression upgrade card** in the cell workbench makes every form on the chain (nugget, ingot, block...) count towards the one kind the cell is partitioned to, and the terminal shows that kind.

## Going to 10 kinds

Put the cell and the **expansion card** together in the cell workbench: out comes the expanded cell, and a MEGA Cells 1M component is returned to you. The card itself is a crafting-table recipe: 4 Green Crystal Matrices + 4 MEGA Cells 1M components + 1 Energized Superconductive Ingot.

## Taking it apart

The fluid and chemical base cells can be disassembled into their housing plus one 1M component. **The item base cell and all three expanded cells have no disassembly recipe** - keep the expanded ones, they cannot be undone.

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_cell" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_cell_expanded" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_expansion_card" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_storage_matrix_housing" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_fluid_storage_matrix_housing" />
  <ItemIcon id="neoecoprototype:simplify_small_bulk_chemical_storage_matrix_housing" />
</ItemGrid>
