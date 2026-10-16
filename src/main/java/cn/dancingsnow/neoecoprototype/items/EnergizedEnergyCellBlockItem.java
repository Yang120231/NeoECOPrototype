package cn.dancingsnow.neoecoprototype.items;

import appeng.api.ids.AEComponents;
import appeng.core.localization.Tooltips;
import cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Shows the same "Stored Energy" line AE2's own energy cell shows, so a player can tell the two tiers apart
 * and see what a dismantled one is still holding. The amount lives in {@link AEComponents#STORED_ENERGY},
 * which our block entity writes when the battery is dismantled; the capacity comes off the block, because
 * both tiers share one item class and one block entity type.
 *
 * <p>This is a plain {@link BlockItem} rather than AE2's {@code AEBaseBlockItem}: that class declares
 * {@code appendHoverText} final and routes it through {@code addCheckedInformation}, and measured in the
 * game test server nothing ever reaches the latter for an addon item.</p>
 */
public class EnergizedEnergyCellBlockItem extends BlockItem {

    public EnergizedEnergyCellBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (getBlock() instanceof EnergizedEnergyCellBlock cell) {
            tooltip.add(Tooltips.energyStorageComponent(
                    stack.getOrDefault(AEComponents.STORED_ENERGY, 0.0), cell.getCapacity()));
        }
    }
}
