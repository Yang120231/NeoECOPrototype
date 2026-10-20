---
navigation:
  title: Energized Energy Cells
  icon: neoecoprototype:energized_energy_cell_16g
  position: 23
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:energized_energy_cell_16g
  - neoecoprototype:energized_energy_cell_512g
---

# Energized Energy Cells

<Row>
  <BlockImage id="neoecoprototype:energized_energy_cell_16g" scale="3"></BlockImage>
  <BlockImage id="neoecoprototype:energized_energy_cell_512g" scale="3"></BlockImage>
</Row>

The **Energized Energy Cells** are two standalone batteries. Each one is its own grid node the moment it is placed - no multi-block definition has to know the block exists, and AE2 adds its buffer straight into the network's total. They idle at 0, and the network both charges them and draws from them.

| Cell | Capacity (AE) | One craft |
|------|---------------|-----------|
| <ItemLink id="neoecoprototype:energized_energy_cell_16g" /> | 16,000,000,000 | 8 ECO - LT9 High Density Energy Cells + 32 Energized Superconductive Blocks + 64 Green Crystal Matrices |
| <ItemLink id="neoecoprototype:energized_energy_cell_512g" /> | 256,000,000,000 | 8 of the 16G cells + 1 Energized Superconductive Block |

Both are made in eco's <ItemLink id="neoecoae:integrated_working_station" /> and each costs 100,000 FE. Station recipes cannot be drawn on this page, so the ingredients are in the table.

Breaking one carries its charge with it - the "Stored Energy" line on its tooltip is that figure - and placing it again resumes from there.

The second cell's item id still reads `energized_energy_cell_512g` while its capacity is 256G, which matters for any script that looks it up by id.
