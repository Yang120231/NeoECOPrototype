package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AE2 pattern provider behavior under the addon's green visual variant.
 *
 * <p>Staying a subclass of AE2's provider is what keeps the pattern-disk mod's linkage working: the
 * integration in {@code neoecoae} collects hosts by {@code instanceof PatternProviderLogicHost} and hands
 * each one its own pattern inventory, so this machine shows up in the disk terminals without our code
 * mentioning either mod. Re-parenting this block entity to an eco class, or AE2 moving that interface,
 * silently drops it - and {@code provider_eject}'s cast of a placed instance to
 * {@code PatternProviderLogicHost} is the assertion that goes red when that happens.
 */
public class SimplifyPatternProviderBlockEntity extends PatternProviderBlockEntity {
    /**
     * Three rows of nine: {@code l1_pattern_provider.png} draws exactly this many pattern slots, and the
     * shipped GUI style declares the same grid. {@code GuiGrooveAlignmentTest} reads this number, so
     * raising it without drawing another row now goes red instead of spilling slots onto the panel.
     */
    public static final int PATTERN_SLOTS = 27;

    public SimplifyPatternProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void openMenu(net.minecraft.world.entity.player.Player player, MenuHostLocator locator) {
        MenuOpener.open(ModRegistration.L1_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(net.minecraft.world.entity.player.Player player, appeng.menu.ISubMenu subMenu) {
        MenuOpener.returnTo(ModRegistration.L1_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this, PATTERN_SLOTS);
    }

}
