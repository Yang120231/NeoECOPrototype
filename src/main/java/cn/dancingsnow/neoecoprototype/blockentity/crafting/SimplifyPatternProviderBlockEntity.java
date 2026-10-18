package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** AE2 pattern provider behavior under the addon's green visual variant. */
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

    /**
     * AE2 names a provider's terminal group after the machine it faces, which is right for a part mounted
     * on a cable feeding one machine and wrong here: this is a standalone 27-slot block, and facing a
     * drive or an assembler made every terminal - AE2's own and any addon's - list it as "ME驱动器" or
     * "分子装配室" instead of what it is.
     */
    @Override
    public appeng.api.implementations.blockentities.PatternContainerGroup getTerminalGroup() {
        return new appeng.api.implementations.blockentities.PatternContainerGroup(
                appeng.api.stacks.AEItemKey.of(getBlockState().getBlock()),
                getDisplayName(),
                java.util.List.of());
    }
}
