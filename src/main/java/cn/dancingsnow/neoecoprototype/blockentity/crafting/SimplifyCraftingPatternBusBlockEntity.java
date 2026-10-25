package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The F1 bus, with one answer changed: eco builds its pattern-migration source pool out of the grid's
 * {@code PatternContainer} machines and then drops every {@code PatternStorageHost} that reports false
 * (the interface default). eco's own bus inherits that false, so did ours while it reused eco's class -
 * which is what issue #128 reported and what the hook was added for. Only the answer differs here; the
 * block entity type still names eco's parent class, the same shape the three crafting members use.
 */
public final class SimplifyCraftingPatternBusBlockEntity extends ECOCraftingPatternBusBlockEntity {
    public SimplifyCraftingPatternBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean allowsPatternMigrationSource() {
        return true;
    }
}
