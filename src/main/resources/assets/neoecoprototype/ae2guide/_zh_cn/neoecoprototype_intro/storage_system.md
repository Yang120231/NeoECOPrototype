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

能挂进这台驱动器的矩阵有哪几族、各自容量与类型上限，集中在 [支持的存储矩阵](matrix_types.md) 一页对照。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 部件

| 部件 | 空闲耗电 |
|------|----------|
| 存储主机 | 32.25 AE/t |
| 存储矩阵驱动器 | 每台 32 AE/t |
| 能量仓 | 0 |
| 存储接口 | 2 AE/t |
| 存储网络接口 | 2 AE/t |
| 存储通风口 | 4 AE/t |
| 存储外壳 | 2 AE/t |

"缺了会怎样"只有两种真实答案：那一格是固定格（不成型），或者它只是可选成员（少它的功能与耗电）。我们没有任何按温度惩罚的机制。 表里没点名的成员一律 2 AE/t——那是我们把 eco 共享的 16 AE/t 元件基线统一压到 L1 的结果。

## 每列的形状

## 搭建步骤

1. 放下存储主机，使其朝外，用外壳搭出周围框架。
2. 在主机**左后方**放存储接口（固定格，成型后隐进结构里）。
3. 从主机**右侧**的外壳再往右开始，水平排列存储矩阵驱动器，一列三格。
4. 在驱动器正后方那一列：下格能量仓、中格存储通风口、上格能量仓。
5. 用外壳补齐剩余位置。

加长就是整列整列地加：三格驱动器配背后那一列仓与风口。不想逐块摆就用主机界面里的**自动搭建面板**，先预览再一键补齐——它给出的就是这套结构的形状权威，上面几步就是它排出来的顺序。

表里主机的 **32.25 AE/t** 不是笔误：eco 给存储主机的基线是 258 AE/t，我们按 L1 的 1/8 缩放后就是这个带小数的值。

## 选哪种矩阵

| 要存什么 | 矩阵系列 | 需要的模组 |
|----------|----------|------------|
| 物品、流体、化学品（1K - 4M） | 物品 / 流体 / 化学品存储矩阵 | 化学品另需 Mekanism 与 Applied Mekanistics |
| 少数几种资源，单种近乎无限 | [小宗存储矩阵](small_bulk_matrices.md) |
| 多种资源混合、类型很多 | 全能 / 量子 / 复合全能矩阵（1K / 1M） | AE2 Omni Cells |
| 指定物品无限囤积 | 无限矩阵（`concrete`）/ KubeJS 无限矩阵 |
| FE 能量 | [FE存储矩阵](flux_storage.md) | AppliedFlux |
| Mekanism 的乏核废料，只这一种 | 乏核废料存储矩阵（1K / 1M） |
| 奇点，不搭物质聚合器 | [L1 奇点元件](singularity_cell.md) | - |

矩阵要设白名单就在 AE2 的元件工作台里分区。奇点元件是例外：它只装自己长出来的那一种，不能分区。

## 别人的元件

L1 驱动器默认只挂 L1 原生元件与小宗盘。eco 更高等级的元件（包括 eco 自家的 MEGA Cells 长桶元件）插得进去但不挂载，除非服务端配置把它们加白（`additional_storage_cells`）。