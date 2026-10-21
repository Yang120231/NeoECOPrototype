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

The **Small Bulk Matrix** is the extreme member of the <ItemLink id="ae2:drive" /> family: an ordinary matrix holds many types in limited amounts, this one holds very few types in nearly unlimited amounts. **3** types by default, **10** after an expansion card.

| Aspect | Value |
|--------|-------|
| Type limit | 3, or 10 with the expansion card |
| Amount per type | the cell reports **9,223,372,036,854,775,807** bytes - a long at its top, shown as 9.2E in a terminal - and you will not reach it |
| Needs | MEGA Cells (4.11.0 is the version measured); the chemical variant also needs Mekanism and Applied Mekanistics |

## Partitioning

Partitioning is a cell whitelist: only the types you allow enter the cell, everything else passes by. Edit it in AE2's cell workbench; the fluid and chemical cells partition against their own key types.

## Going to 10 kinds

Put the cell and the **expansion card** together in the cell workbench: out comes the expanded cell, and a MEGA Cells 1M component is returned to you. The card itself is a crafting-table recipe: 4 Green Crystal Matrices + 4 MEGA Cells 1M components + 1 Energized Superconductive Ingot.

## Taking it apart

The fluid and chemical base cells can be disassembled into their housing plus one 1M component. 
> **Warning:** **the item base cell and all three expanded cells have no disassembly recipe.** An expanded cell cannot be taken back apart, so decide before you spend the card.


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

<RecipeFor id="neoecoprototype:simplify_small_bulk_expansion_card" />
