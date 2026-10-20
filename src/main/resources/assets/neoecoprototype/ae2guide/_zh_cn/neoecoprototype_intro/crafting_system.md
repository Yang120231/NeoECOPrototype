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

**L1 合成系统**是 eco 的 <ItemLink id="neoecoae:crafting_system_l4" /> 的 L1 版本：把合成样板装进总线，工作核心就并行执行网络交来的合成任务，耗电是 eco L4 同级的 **1/8**。它管执行，计算系统管能给多少线程——**两边的数字各算各的，谁也不给对面加数**。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 项目 | 数值 |
|------|------|
| 并行 | **16** |
| 开超频后 | **32** |
| 与计算侧的关系 | 各算各的：这一台给 16 / 32，计算系统给线程数与它的加速器数 |

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
| 合成流体输入仓 | 冷却液 | 固定格：**主动冷却的冷却液从这里进**（不是配方流体） | 2 AE/t | 结构不成型 |
| 合成流体输出仓 | — | 固定格：把用过的冷却液排出去 | 2 AE/t | 结构不成型 |
| 合成接口 | — | 固定格：成型后隐进结构，把集群交给外部 ME 电缆 | 2 AE/t | 结构不成型 |
| 合成网络接口 | — | 玩家能打开的那一块，带 eco 的接口界面 | 2 AE/t | 打不开界面，其余不受影响 |
| 合成外壳 | — | 补齐框架 | 2 AE/t | 结构不成型 |

## 搭建步骤

1. 放下主机，使其朝外，用外壳搭出周围框架。
2. 在主机**左后方**放合成网络接口（玩家能打开的那一块），它上方放流体输入仓、下方放流体输出仓。
3. 从主机**右侧**的外壳再往右开始，水平排列 F1 工作核心。
4. 在工作核心的上下各放一排并行核心，正后方放合成通风口。
5. 在通风口的上下各放一排智能样板总线。
6. 用外壳补齐剩余结构。

加长时，工作核心、并行核心、样板总线与通风口要一起加。不想逐块摆就用主机界面里的**自动搭建面板**：它给出的就是这套结构的形状权威，上面六步就是它排出来的顺序。

## 超频与冷却

超频与主动冷却的开关、冷却液等级要求都沿用 eco 那一套，规则看 eco 自己的合成系统页；这一档的两个数是 **16**（不开超频）与 **32**（开超频）。冷却液位在我们这里还有一层用处：合成主机的**机魂**读它来决定空闲耗电省多少（详见机魂那一节），所以冷却液满不满会改变待机数字，但不改变能不能超频——后者归 eco 的规则管。
