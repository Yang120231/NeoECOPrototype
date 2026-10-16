package cn.dancingsnow.neoecoprototype.block.dissociation;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorUI;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * The dissociator's block: right-click opens the machine, and a bucket on the outside is how a player who
 * has no pipes gets coolant in and spent solution out.
 *
 * <p>The panel is an LDLib2 block UI, so this block owns the layout entry point and there is no menu type
 * or screen of ours to register - LDLib2's own menu type and screen render it.
 */
public class MeteoriteDissociatorBlock extends AEBaseEntityBlock<MeteoriteDissociatorBlockEntity>
        implements BlockUIMenuType.BlockUI {

    public MeteoriteDissociatorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof MeteoriteDissociatorBlockEntity host) {
            return MeteoriteDissociatorUI.create(holder, host);
        }
        return null;
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                                  Player player, BlockHitResult hit) {
        if (getBlockEntity(level, pos) == null) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useItemOn(held, state, level, pos, player, hand, hit);
    }
}
