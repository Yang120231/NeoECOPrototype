---
navigation:
  title: L1 Powered ME Interface
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 Powered ME Interface

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

The **L1 Powered ME Interface** is a doubled <ItemLink id="ae2:interface" /> that also feeds the network. It carries 18 config slots and 18 stock slots instead of 9 and 9; everything else is vanilla behaviour, so anywhere an ME Interface works, this one works.

Given power and a channel it outputs **200 AE/t**. AE2 keeps only the highest-output passive generator per grid, so it stands down while a stronger source is present.

The block is assembled in the [L1 Processor Assembler](processor_assembler.md), whose recipe table on that page carries the ingredients. Four of them build one [Energized Superconductive Interface](superconductive_interface.md).

<RecipeFor id="neoecoprototype:simplify_powered_me_interface" />
<RecipeFor id="neoecoprototype:cable_powered_me_interface" />
