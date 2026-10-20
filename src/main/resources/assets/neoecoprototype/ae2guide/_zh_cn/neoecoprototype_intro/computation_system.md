---
navigation:
  title: L1 计算系统
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

# L1 计算系统

L1 计算系统给 ME 网络提供合成线程，概念与 eco 计算系统一致，档位带到 L1 前期。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 每台贡献多少

| 成员 | 提供 |
|------|------|
| 计算主机 | `cpu_threads` 条合成线程（默认 **2**） |
| 并行核心 | 每块 **24** 并行 |
| 线程核心 | 每块 **2** 条合成线程 |
| 盈能强化计算机核心 | 每块 **+1,024** 并行，**只能装在控制器正后方那一格外壳站位** |
| 盈能强化线程核心 | 每块 **+16** 条合成线程，**只接受离控制器最近的那一格** |

## 部件

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

## 两件盈能强化成员

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_energized.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 物品 | 在哪合成 | 原料 |
|------|----------|------|
| 盈能强化计算机核心 | eco 的集成工作站 | 3 × ECO - C4 可拓展计算子系统主机、8 × 绿晶晶格、4 × 没那么神秘的方块，耗能 66,600 FE |

另外两件（**盈能强化线程核心**、**ECO - CE1R 闪存晶阵**）的原料写在 [L1 处理器装配室](processor_assembler.md) 的出厂配方表里，这里不再重复。

## 服务端配置

`neoecoprototype-server.toml` 的 `l1_computation` 段只作用于 L1 一档，更高档沿用 eco 自己的数值。

| 配置项 | 默认 | 含义 |
|--------|------|------|
| `cpu_threads` | 2 | 一台普通 L1 子系统的合成线程数 |
| `cpu_accelerators` | 24 | 每块普通并行核心提供的并行数 |
| `cpu_total_bytes` | 1,572,864 | 该档标称的合成存储字节数（1 MiB 的 1.5 倍） |
| `energized_cell_total_bytes` | 5,242,880 | 每只盈能闪存增强（CE1R）的字节数（4 MiB 加 30%） |

改完要重进世界。线程在集群构造时分配，所以已经成型的结构要拆掉重新成型才吃到新值。
