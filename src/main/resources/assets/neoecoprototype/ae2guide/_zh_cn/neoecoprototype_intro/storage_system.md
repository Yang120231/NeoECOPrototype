---
navigation:
  title: L1 存储系统
  icon: neoecoprototype:simplify_storage_controller
  position: 10
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_storage_controller
  - neoecoprototype:simplify_drive
  - neoecoprototype:simplify_energy_cell
  - neoecoprototype:simplify_item_storage_cell_1k
  - neoecoprototype:simplify_item_storage_cell_16k
  - neoecoprototype:simplify_item_storage_cell_64k
  - neoecoprototype:simplify_item_storage_cell_1m
  - neoecoprototype:simplify_item_storage_cell_4m
  - neoecoprototype:simplify_fluid_storage_cell_1k
  - neoecoprototype:simplify_fluid_storage_cell_16k
  - neoecoprototype:simplify_fluid_storage_cell_64k
  - neoecoprototype:simplify_fluid_storage_cell_1m
  - neoecoprototype:simplify_fluid_storage_cell_4m
  - neoecoprototype:simplify_chemical_storage_cell_1k
  - neoecoprototype:simplify_chemical_storage_cell_16k
  - neoecoprototype:simplify_chemical_storage_cell_1m
  - neoecoprototype:simplify_chemical_storage_cell_4m
---

# L1 存储系统

L1 存储系统是一套可扩展的多方块，作用与 AE2 的<ItemLink id="ae2:drive" />相同——把存储矩阵挂进 ME 网络——但它自带能量缓冲，并且把 eco 的 L4 那套结构提前到 L1 这一级。待机总耗电是 eco L4 同级结构的 **1/8**。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 部件

| 部件 | 装什么 | 干什么 | 空闲耗电 | 缺了会怎样 |
|------|--------|--------|----------|------------|
| 存储主机 | — | 决定这一套是 L1；**正面是操作面**，自动搭建面板也在这面 | 32.25 AE/t | 结构不成型 |
| 存储矩阵驱动器 | 一只存储矩阵 | 把矩阵挂进网络；手持矩阵右键插入、空手潜行右键取出 | 每台 32 AE/t | 少一格容量，也少这一台的耗电 |
| 能量仓 | — | 给结构存 **100,000 AE** 缓冲，并对网络公开 | 0 | 少一份缓冲 |
| 存储接口 | — | 成型后隐进结构，把集群的网格节点交给外部 ME 电缆 | 2 AE/t | 结构不成型 |
| 存储网络接口 | — | 玩家能打开的那一块，带 eco 的接口界面 | 2 AE/t | 打不开界面，其余不受影响 |
| 存储通风口 | — | 只有外观与一份空闲耗电；**没有过热或停机这回事** | 4 AE/t | 少 4 AE/t 耗电，功能不变 |
| 存储外壳 | — | 补齐框架 | 2 AE/t | 结构不成型 |

"缺了会怎样"只有两种真实答案：那一格是固定格（不成型），或者它只是可选成员（少它的功能与耗电）。我们没有任何按温度惩罚的机制。表里没点名的成员一律 **2 AE/t**——那是我们把 eco 共享的 16 AE/t 元件基线统一压到 L1 的结果。

## 每列的形状

主机往西每加一列，就是**三格驱动器 + 背面一列（上能量仓、中通风口、下能量仓）**，列数由主机的构建长度决定。逐块摆容易出错，直接用主机界面里的**自动搭建面板**：先预览，再一键补齐——面板给出的就是这套结构的形状权威。

## 选哪种矩阵

| 要存什么 | 矩阵系列 | 需要的模组 |
|----------|----------|------------|
| 物品、流体、化学品（1K - 4M） | 物品 / 流体 / 化学品存储矩阵 | 化学品另需 Mekanism 与 Applied Mekanistics |
| 少数几种资源，单种近乎无限 | [小宗存储矩阵](small_bulk_matrices.md) | MEGA Cells |
| 多种资源混合、类型很多 | 全能 / 量子 / 复合全能矩阵（1K / 1M） | AE2 Omni Cells |
| 指定物品无限囤积 | 无限矩阵（`concrete`）/ KubeJS 无限矩阵 | - |
| FE 能量 | [FE存储矩阵](flux_storage.md) | AppliedFlux |
| Mekanism 的乏核废料，只这一种 | 乏核废料存储矩阵（1K / 1M） | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| 奇点，不搭物质聚合器 | [L1 奇点元件](singularity_cell.md) | - |

矩阵要设白名单就在 AE2 的元件工作台里分区。奇点元件是例外：它只装自己长出来的那一种，不能分区。

## 别人的元件

L1 驱动器默认只挂 L1 原生元件与小宗盘。eco 更高等级的元件（包括 eco 自家的 MEGA Cells 长桶元件）插得进去但不挂载，除非服务端配置把它们加白（`additional_storage_cells`）。
