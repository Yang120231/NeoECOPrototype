---
navigation:
  title: L1 合成系统
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

# L1 合成系统

**L1 合成系统**是 eco 的 <ItemLink id="neoecoae:crafting_system_l4" /> 的 L1 版本：它把合成样板拆成许多路同时执行，配合计算系统提供的线程与并行数工作，耗电是 eco L4 同级的 **1/8**。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 项目 | 数值 |
|------|------|
| 并行 | **16** 路 |
| 超频后并行 | **32** 路 |
| 与计算侧的关系 | 两边的并行各自独立：这一台管执行，计算系统管能给多少线程 |

## 部件

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

| 部件 | 装什么 | 干什么 | 空闲耗电 | 缺了会怎样 |
|------|--------|--------|----------|------------|
| 合成主机 | — | 决定这一套是 L1；正面是操作面 | 4 AE/t | 结构不成型 |
| F1 工作核心 | — | 真正执行合成的那一格 | 4 AE/t | 少一路执行 |
| 并行核心 | — | 提供并行路数 | 4 AE/t | 少一份并行 |
| 智能样板总线 | 合成样板 | 样板的存放处，网络从这里取样板 | 2 AE/t | 这一列没样板可跑 |
| 合成通风口 | — | 只有外观与一份空闲耗电；**没有过热或停机机制** | 4 AE/t | 少 4 AE/t 耗电，功能不变 |
| 合成流体输入仓 | 流体 | 固定格：给合成供流体 | 2 AE/t | 结构不成型 |
| 合成流体输出仓 | — | 固定格：把合成出的流体送出去 | 2 AE/t | 结构不成型 |
| 合成接口 | — | 固定格：成型后隐进结构，把集群交给外部 ME 电缆 | 2 AE/t | 结构不成型 |
| 合成网络接口 | — | 玩家能打开的那一块，带 eco 的接口界面 | 2 AE/t | 打不开界面，其余不受影响 |
| 合成外壳 | — | 补齐框架 | 2 AE/t | 结构不成型 |

主机往西每加一列，就是**外侧一列**（上下并行核心、中间 F1 工作核心）加**内侧一列**（上下智能样板总线、中间合成通风口）。逐块摆容易出错，用主机界面里的**自动搭建面板**：它给出的就是这套结构的形状权威。
