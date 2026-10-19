---
navigation:
  title: Energized Superconductive Interface
  icon: neoecoprototype:superconductive_interface
  position: 90
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:superconductive_interface
  - neoecoprototype:cable_superconductive_interface
---

# Energized Superconductive Interface

<Row>
  <BlockImage id="neoecoprototype:superconductive_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_superconductive_interface" />
</ItemGrid>

The high end of our two interfaces: the same doubled layout as the [L1 Powered Interface](powered_interface.md), far more room per config slot, and twenty times the output.

| Aspect | ME Interface | Energized Superconductive Interface |
|--------|--------------|--------------------------------------|
| Stock slots | 9 | **18** |
| Config slots | 9 | **18** |
| Units per config slot (items) | 64 | **8,192** |
| Units per config slot (fluids, chemicals) | 4,000 mB | **512,000 mB** |
| Passive generation | none | **4,000 AE/t** |
| Crafted at | crafting table | eco's Integrated Working Station |

Setting it up is the same as any ME interface.

## Passive generation

4,000 AE/t is usually the strongest source in the network, so one of these takes over the network's passive generation and suppresses every [L1 Powered Interface](powered_interface.md) - AE2 keeps only the highest-output passive generator per grid.

## Obtaining

The block is assembled in eco's **Integrated Working Station**: **1 Sky Cold Ice + 4 [L1 Powered Interfaces](powered_interface.md) + 9 Energized Superconductive Ingots + 4 Superconducting Processors**, for 10,000 AE. Station recipes cannot be rendered on this page, so the ingredients are in prose.

| Form | Conversion |
|------|------------|
| block → cable part | <RecipeFor id="neoecoprototype:cable_superconductive_interface" /> |
| cable part → block | the same lossless conversion, either way round |
