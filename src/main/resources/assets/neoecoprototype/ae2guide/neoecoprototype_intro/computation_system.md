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

The **L1 Computation System** is the L1 version of eco's <ItemLink id="neoecoae:computation_system_l4" />: it stores nothing, it only supplies the network's autocrafting with **crafting threads and parallelism** - more threads and more parallelism means more crafts running at once. The image above is the smallest working shape.

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
| Energized computation core | **+1,024** parallelism each, and **only in the one slot directly behind the controller** (a casing sits there otherwise) |
| Energized threading core | **+16** crafting threads each, and **only in the slot nearest the controller** |

## Parts

Apart from the two fixed slots, the host and the computation interface, every member draws **2 AE/t** idle.

## Building it

1. Place the host facing out, then frame the rest with casings.
2. Put the computation network interface **behind and to the left of the host**.
3. Starting one block right of the casing on the host's right side, lay the superconducting crystal transmitters horizontally.
4. Put a threading core directly behind each transmitter, with a row of computation drives above and below it.
5. Put a row of parallel cores above and below that threading core.
6. Cap the transmitter row with the cooling controller: stand on the structure's right side and place it facing the host.
7. Fill the remaining positions with casings.

Lengthening means adding transmitters, drives, threading cores and parallel cores together. The **auto-build panel** in the host's GUI can preview and fill it in - what it plans is the authoritative shape.

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

| Key | Default | Meaning | When to touch it |
|-----|---------|---------|-----------------|
| `cpu_threads` | 2 | crafting threads of one plain L1 subsystem | when one host cannot feed all the assemblers |
| `cpu_accelerators` | 24 | parallelism per plain parallel core | when the same core count should open more crafts |
| `cpu_total_bytes` | 1,572,864 | the tier's nominal crafting storage (1.5 × 1 MiB) | when a batch runs out of crafting storage |
| `energized_cell_total_bytes` | 5,242,880 | bytes per ECO - CE1R Flash Crystal Array, 4 MiB plus 30% | the same, on the energized tier |

Re-enter the world after editing. Threads are allocated when the cluster is built, so a structure that is already formed has to be taken down and re-formed to pick up new values.
