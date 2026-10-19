---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:simplify_pattern_provider
---

# L1 处理器装配室

L1 处理器装配室做 AE2 分子装配室做的事，但只接受处理样板：绿色外壳，配 L1 样板供应器使用。

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>

## 出厂配方

| 产物 | 原料 |
|------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 计算处理器 | 硅 + 红石 + 石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |
| [L1 供能接口](powered_interface.md) | 2 水晶谐振发电机 + 1 ME接口 + 1 SA 超导处理器 |
| 盈能强化线程核心 | 2 eco 线程核心 + 1 L1 计算机壳 |
| 盈能强化运算单元 4M | 2 L1 运算单元 + 1 4M 存储成分 |
| 结构扩展卡 | AE2 记忆卡 + 铝锭 + 绿晶矩阵 |
| 雪球 / 细雪桶 | 4 雪块 → 4 雪球；3 雪球 + 1 桶 → 细雪桶 |
| 三只具名玩偶 | 见创造栏的装饰一组 |

三种处理器之外的其余处理器配方，从 AE2 压印器的配方现场推导——别的模组给压印器加的配方不需要任何数据文件就能用。服务端配置可以关掉推导（`derive_processor_recipes_from_inscriber`），或按输出排除（`disabled_processor_recipes`）。

## 加速

5 个卡槽，装**加速卡**，规则与原版分子装配室一致：满卡即每 2 刻合成一次。

## 批量合成

| 项目 | 规则 |
|------|------|
| 一次做完 | 一次推送里带了几份完整材料，就在**一个**合成周期里做完，产出按份数翻倍，**耗时不变**。 |
| 想更快 | 只能继续加速卡——多倍材料不会让单个周期更短。 |
| 倍数上限 | 每种原料各按自己一组的数量分摞，**摞过九格这张样板就被拒收**，原料退回网络，日志写明是哪一条卡的。以处理器为例（三种原料、一组 64 个）上限是 **192 倍**。 |
| 装不下的产出 | 一轮只有一组送到机器前面，交不下的那部分直接进网络，不会消失。 |

## 样板供应器

L1 样板供应器提供 **27 个样板槽位**（AE2 是 9 个），推送样板的方式与 AE2 样板供应器驱动分子装配室相同。它的配方一次给出 **2 台**。
