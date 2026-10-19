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

一台 <ItemLink id="neoecoprototype:simplify_storage_controller" /> 主机，加任意数量的 <ItemLink id="neoecoprototype:simplify_drive" /> 驱动器与 <ItemLink id="neoecoprototype:simplify_energy_cell" /> 能量仓。耗电是 eco L4 同级的 **1/8**。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 部件

| 部件 | 作用 |
|------|------|
| 存储主机 | 决定结构等级与操作面；**正面是操作面** |
| 存储矩阵驱动器 | 放存储矩阵，并把它挂进 ME 网络 |
| 能量仓 | 能量缓冲 |
| 存储接口 | 盘内资源对外供给 |
| 存储网络接口 | 接入 ME 网络 |
| 存储通风口 | 热量管理 |
| 存储外壳 | 补齐结构框架 |

逐块摆放容易出错，直接用主机界面里的**自动搭建面板**：先预览结构，再一键补齐。

## 选哪种矩阵

| 要存什么 | 矩阵系列 | 需要的模组 |
|----------|----------|------------|
| 物品、流体、化学品（1K - 4M） | 物品 / 流体 / 化学品存储矩阵 | 化学品另需 Mekanism 与 Applied Mekanistics |
| 少数几种资源，单种近乎无限 | [小宗存储矩阵](small_bulk_matrices.md) | MEGA Cells |
| 多种资源混合、类型很多 | 全能 / 量子 / 复合全能矩阵（1K / 1M） | AE2 Omni Cells |
| 指定物品无限囤积 | 无限矩阵（`concrete`）/ KubeJS 无限矩阵 | - |
| FE 能量 | [FE存储矩阵](flux_storage.md) | AppliedFlux |
| Mekanism 的乏核废料，只这一种 | 乏核废料存储矩阵（1K / 1M） | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| 奇点，不建凝聚器 | [L1 奇点元件](singularity_cell.md) | - |

所有矩阵都能分区：放进 AE2 的元件工作台标记即可。

## 别人的元件

L1 驱动器默认只挂 L1 原生元件与小宗盘。eco 更高等级的元件（包括 eco 自家的 MEGA Cells 长桶元件）插得进去但不挂载，除非服务端配置把它们加白（`additional_storage_cells`）。
