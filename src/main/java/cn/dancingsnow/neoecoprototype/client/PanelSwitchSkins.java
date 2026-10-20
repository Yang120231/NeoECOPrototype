package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.BindableValue;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slf4j.LoggerFactory;

/**
 * The host panels' switches wear the artwork's green tiles while they are on - the three computation
 * switches and the two crafting mode buttons.
 *
 * <p>eco picks those tiles itself, per state change, from its own widget sheet: the computation switches go
 * through {@code switchSprite(on, hover)} and the crafting mode buttons through
 * {@code updateModeButtonStyle(button, on)}, and both then push the result through
 * {@code Button.buttonStyle}. Neither family has a provider cell - {@code IHostPanelTextureProvider$Part}
 * only covers the tier badge, the host marker and the three computation member rows - and both factories are
 * private, so the only door left is LDLib's public one: eco gives every one of these widgets an id,
 * {@code ModularUI.getElementById} is public, and {@code ButtonStyle.baseTexture/hoverTexture} are public.
 *
 * <p>What that costs us: eco re-applies its own two tiles whenever the value flips, so applying once would
 * last until the first click. Our listener registers after its {@code BindableValue} already has one, and
 * LDLib walks listeners in registration order, so ours runs last and wins for the "on" case. The "off" case
 * is left alone on purpose - eco's off tile is what the artwork sheet also has there, byte for byte.
 *
 * <p>Gated on the placed block, so eco's own L4/L6/L9 panels keep their tiles. If a widget is ever renamed,
 * that shows up in the log instead of silently doing nothing.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, value = Dist.CLIENT)
public final class PanelSwitchSkins {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PanelSwitchSkins.class);

    private static final String PANEL = "textures/gui/panel/";
    private static final String[] COMPUTATION_IDS = {
            "computation_switch_substitutions",
            "computation_switch_cycle_planning",
            "computation_switch_fast_planner",
    };
    private static final String[] CRAFTING_IDS = {"overclock_button", "active_cooling_button"};

    @SubscribeEvent
    public static void onPanelBuilt(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ModularUIContainerScreen screen)) {
            return;
        }
        ModularUIContainerMenu menu = screen.getMenu();
        if (!(menu.uiHolder instanceof BlockUIMenuType.BlockUIHolder holder)) {
            return;
        }
        Block host = holder.blockState.getBlock();
        String[] ids;
        String folder;
        if (host == ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get()) {
            ids = COMPUTATION_IDS;
            folder = "computation_switch_";
        } else if (host == ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()) {
            ids = CRAFTING_IDS;
            folder = "crafting_mode_button_";
        } else {
            return;
        }
        SpriteTexture on = tile(folder + "on.png");
        SpriteTexture onHover = tile(folder + "on_hover.png");
        for (String id : ids) {
            UIElement element = menu.modularUI.getElementById(id);
            if (!(element instanceof Button button)) {
                LOGGER.warn("the L1 {} panel has no switch under id {}, so it keeps eco's tile",
                        folder, id);
                continue;
            }
            boolean driven = false;
            for (UIElement child : button.getChildren()) {
                if (child instanceof BindableValue<?> value) {
                    driven = true;
                    value.registerValueListener(changed -> paint(button, on, onHover,
                            Boolean.TRUE.equals(changed)));
                    paint(button, on, onHover, Boolean.TRUE.equals(value.getValue()));
                }
            }
            if (!driven) {
                LOGGER.warn("the L1 {} switch {} has no bound value child, so we cannot tell whether it is"
                        + " on and left eco's tile alone", folder, id);
            }
        }
    }

    private static SpriteTexture tile(String name) {
        return SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, PANEL + name)).setSprite(0, 0, 15, 26);
    }

    private static void paint(Button button, SpriteTexture on, SpriteTexture onHover, boolean lit) {
        if (!lit) {
            return;
        }
        button.buttonStyle(style -> style.baseTexture(on).hoverTexture(onHover));
    }

    private PanelSwitchSkins() {
    }
}
