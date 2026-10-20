---
navigation:
  title: L1 Crafting System
  icon: neoecoprototype:simplify_crafting_system
  position: 60
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_crafting_system
  - neoecoprototype:simplify_crafting_casing
  - neoecoprototype:simplify_crafting_parallel_core
  - neoecoprototype:simplify_crafting_pattern_bus
  - neoecoprototype:simplify_crafting_worker
  - neoecoprototype:simplify_crafting_vent
  - neoecoprototype:simplify_crafting_interface
  - neoecoprototype:simplify_crafting_network_interface
  - neoecoprototype:simplify_fluid_input_hatch
  - neoecoprototype:simplify_fluid_output_hatch
---

# L1 Crafting System

The **L1 Crafting System** is the L1 version of eco's <ItemLink id="neoecoae:crafting_system_l4" />: it runs crafting patterns in many lanes at once, and it leans on the computation system for how many threads and lanes it may open. It idles at **1/8** of eco's L4 equivalent. The image above is the smallest working shape.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| Aspect | Value |
|--------|-------|
| Parallelism | **16** lanes |
| Overclocked | **32** lanes |
| Against the computation side | the two are independent: this one executes, the computation system decides how many threads it may use |

## Parts

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype:simplify_crafting_network_interface" />
  <ItemIcon id="neoecoprototype:simplify_fluid_input_hatch" />
  <ItemIcon id="neoecoprototype:simplify_fluid_output_hatch" />
</ItemGrid>

| Part | Holds | What it does | Idle draw | What its absence costs |
|------|-------|--------------|-----------|------------------------|
| Crafting host | - | Fixes the tier at L1; its front face is the control face | 4 AE/t | The structure never forms |
| F1 Worker Core | - | The slot that actually performs a craft | 4 AE/t | One lane less |
| Parallel core | - | Supplies parallel lanes | 4 AE/t | Fewer lanes |
| F1 smart pattern bus | crafting patterns | Where patterns live; the network reads patterns from here | 2 AE/t | That column has nothing to run |
| Crafting vent | - | Appearance and an idle draw, that is all - **there is no heat or shutdown mechanic** | 4 AE/t | 4 AE/t less, no function lost |
| F1 crafting fluid input hatch | fluid | Fixed slot: feeds fluids into the crafts | 2 AE/t | The structure never forms |
| F1 crafting fluid output hatch | - | Fixed slot: pushes crafted fluids out | 2 AE/t | The structure never forms |
| Crafting interface | - | Fixed slot: invisible once formed, hands the cluster to outside ME cables | 2 AE/t | The structure never forms |
| Crafting network interface | - | The one the player opens, with eco's interface UI | 2 AE/t | No GUI; nothing else changes |
| Crafting casing | - | Fills out the frame | 2 AE/t | The structure never forms |

Each column added west of the host is an **outer column** (parallel cores top and bottom, an F1 Worker Core between them) plus an **inner column** (pattern buses top and bottom, the crafting vent between them). Placing blocks one by one is error-prone, so use the **auto-build panel** in the host's GUI - what it plans is the authoritative shape.
