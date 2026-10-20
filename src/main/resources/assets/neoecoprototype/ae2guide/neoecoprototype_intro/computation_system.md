---
navigation:
  title: L1 Computation System
  icon: neoecoprototype:simplify_computation_system
  position: 50
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_computation_system
  - neoecoprototype:simplify_computation_casing
  - neoecoprototype:simplify_computation_parallel_core
  - neoecoprototype:simplify_computation_threading_core
  - neoecoprototype:simplify_computation_cooling_controller
  - neoecoprototype:simplify_computation_drive
  - neoecoprototype:simplify_computation_interface
  - neoecoprototype:simplify_computation_network_interface
  - neoecoprototype:energized_computation_core
  - neoecoprototype:energized_computation_threading_core
  - neoecoprototype:energized_computation_cell_4m
---

# L1 Computation System

The L1 Computation System supplies crafting threads to the ME network. The ideas match eco's computation system, brought forward to L1 early game. The image above is the smallest working shape.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## What each member contributes

| Member | Provides |
|--------|----------|
| Computation host | `cpu_threads` crafting threads (default **2**) |
| Parallel core | **24** parallelism each |
| Threading core | **2** crafting threads each |
| Energized computation core | **+1,024** parallelism each, and **only in the one casing slot directly behind the controller** |
| Energized threading core | **+16** crafting threads each, and **only in the slot nearest the controller** |

## Parts

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_computation_system" />
  <ItemIcon id="neoecoprototype:simplify_computation_casing" />
  <ItemIcon id="neoecoprototype:simplify_computation_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_threading_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_cooling_controller" />
  <ItemIcon id="neoecoprototype:simplify_computation_drive" />
  <ItemIcon id="neoecoprototype:simplify_computation_interface" />
  <ItemIcon id="neoecoprototype:simplify_computation_network_interface" />
</ItemGrid>

## The two energized members

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_energized.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| Item | Crafted at | Ingredients |
|------|------------|-------------|
| Energized computation core | eco's Integrated Working Station | 3 × ECO - C4 Extensible Computation Subsystem Host, 8 × Green Crystal Matrix, 4 × Not So Mysterious Cube, for 66,600 FE |

The other two - the **Energized Reinforced Threading Core** and the **ECO - CE1R Flash Crystal Array** - carry their ingredients on the [L1 Processor Assembler](processor_assembler.md) and are not repeated here.

## Server config

The `l1_computation` section of `neoecoprototype-server.toml` applies to the L1 tier only; higher tiers keep eco's own numbers.

| Key | Default | Meaning |
|-----|---------|---------|
| `cpu_threads` | 2 | crafting threads of one plain L1 subsystem |
| `cpu_accelerators` | 24 | parallelism per plain parallel core |
| `cpu_total_bytes` | 1,572,864 | the tier's nominal crafting storage (1.5 × 1 MiB) |
| `energized_cell_total_bytes` | 5,242,880 | bytes per energized flash enhancement (CE1R), 4 MiB plus 30% |

Re-enter the world after editing. Threads are allocated when the cluster is built, so a structure that is already formed has to be taken down and re-formed to pick up new values.
