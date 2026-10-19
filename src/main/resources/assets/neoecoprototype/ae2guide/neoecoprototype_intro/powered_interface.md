---
navigation:
  title: L1 Powered Interface
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 Powered Interface

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

The L1 Powered Interface is a doubled ME Interface that also feeds the network. Everything else is vanilla behaviour, so anything that accepts an ME interface accepts this one.

| Aspect | ME Interface | L1 Powered Interface |
|--------|--------------|----------------------|
| Stock slots | 9 | **18** |
| Config slots | 9 | **18** |
| Units per config slot (items) | 64 | 64 |
| Passive generation | none | **200 AE/t** |
| Crafted at | crafting table | [L1 Processor Assembler](processor_assembler.md) |

## Passive generation

| Aspect | Rule |
|--------|------|
| Needs | power and a channel. |
| Only the strongest counts | AE2 keeps a single passive generator per grid - the one with the highest output - so this machine stands down while a stronger source is present and resumes when it is picked again. |
| Use | sub-network power. Cheaper than a Crystal Resonance Generator, which does 20 AE/t and wants its own connection to the main network. |

## Obtaining

The block is assembled in the [L1 Processor Assembler](processor_assembler.md): **2 Crystal Resonance Generators + 1 ME Interface + 1 Superconducting Processor**. The assembler's recipe cannot be rendered on this page, so the ingredients are in prose - JEI shows the grid.

| Form | Conversion |
|------|------------|
| block → cable part | <RecipeFor id="neoecoprototype:cable_powered_me_interface" /> |
| cable part → block | the same lossless conversion, either way round |

Four of them go into one [Energized Superconductive Interface](superconductive_interface.md).
