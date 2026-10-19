---
navigation:
  title: L1 Processor Assembler
  icon: neoecoprototype:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:simplify_pattern_provider
---

# L1 Processor Assembler

The L1 Processor Assembler does what AE2's Molecular Assembler does, but takes processing patterns only: a green shell, used with the L1 Pattern Provider.

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>

## Built-in recipes

| Output | Ingredients |
|--------|-------------|
| Logic Processor | silicon + redstone + gold ingot |
| Calculation Processor | silicon + redstone + certus quartz crystal |
| Engineering Processor | silicon + redstone + diamond |
| [L1 Powered Interface](powered_interface.md) | 2 Crystal Resonance Generators + 1 ME Interface + 1 Superconducting Processor |
| Energized Threading Core | 2 eco threading cores + 1 L1 computation casing |
| Energized Computation Cell 4M | 2 L1 computation cells + 1 4M storage component |
| Structure Expansion Card | AE2 memory card + aluminium ingot + green crystal matrix |
| Snowball / powder snow bucket | 4 snow blocks → 4 snowballs; 3 snowballs + 1 bucket → powder snow bucket |
| Three named dolls | see the decoration group in the creative tab |

Processor recipes beyond those three are derived live from AE2's inscriber recipes, so a recipe another mod adds to the inscriber works with no data file. The server config can switch the derivation off (`derive_processor_recipes_from_inscriber`) or exclude outputs (`disabled_processor_recipes`).

## Speed

5 card slots taking **Acceleration Cards**, on AE2's usual rule: full slots mean one craft every 2 ticks.

## Batch crafting

| Aspect | Rule |
|--------|------|
| One cycle | However many complete sets a push carries, they finish in a **single** crafting cycle: output scales with the multiplier, **time does not**. |
| Want it faster | More acceleration cards - a bigger multiplier never shortens the cycle. |
| Multiplier ceiling | Each ingredient is stacked by its own stack size, and a pattern that needs **more than nine slots** is rejected: the materials go back to the network and the log names the offending line. For a processor (three ingredients, stacks of 64) the ceiling is **192×**. |
| Output that will not fit | One stack reaches the machine per round; the rest is inserted into the network rather than lost. |

## Pattern provider

The L1 Pattern Provider holds **27 patterns** (AE2 holds 9) and pushes work the same way AE2's provider feeds a Molecular Assembler. Its recipe gives **2** at a time.
