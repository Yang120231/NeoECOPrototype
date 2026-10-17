package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.lowdragmc.lowdraglib2.editor.resource.BuiltinPath;
import com.lowdragmc.lowdraglib2.editor.resource.TexturesResource;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.List;
import java.util.Map;

/**
 * Puts our artist's artwork where eco's three host panels read their own.
 *
 * <p>eco's answer to issue #122 (commits {@code 1949d763} and {@code 2249c671}, both after the 21.2.6
 * release) registers six complete atlases into LDLib2's {@code ui-eco} provider and routes every sprite
 * lookup in the controller panels through them, so an addon can replace an atlas by name instead of
 * reaching for a package-private class it cannot see. That is the whole cost of this route: a replaced
 * atlas is shared, so eco's own L4-L9 hosts show our art too. Per-machine skinning would need the node
 * level, and eco builds these widgets inside {@code createCustom} with an unmodifiable child list.</p>
 *
 * <p>We replace only a name that is <em>already</em> registered. Adding one ourselves would sit in the
 * provider untouched by any drawing code and read, from here, exactly like a feature that works - on
 * 21.2.6 nothing does.</p>
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, value = Dist.CLIENT)
public final class HostPanelSkins {

    private static final String PROVIDER = "ui-eco";

    /**
     * eco's resource name to our file. {@code ui-eco:Computation-BACKGROUND_ATLAS} is deliberately
     * absent: her {@code eco_cpu_controller.png} is byte-identical to eco's (0.0% differing pixels),
     * so replacing it would move nothing and only add a file to the jar.
     */
    private static final Map<String, String> ATLASES = Map.of(
            "ui-eco:Storage-CONTROLLER_ATLAS", "storage_controller",
            "ui-eco:Storage-ATLAS", "storage_widget",
            "ui-eco:Computation-WIDGETS_ATLAS", "cpu_widget",
            "ui-eco:Crafting-CONTROLLER_ATLAS", "crafting_controller",
            "ui-eco:Crafting-WIDGETS_ATLAS", "crafting_widgets");

    private HostPanelSkins() {
    }

    /**
     * Re-applied on every screen open rather than once at startup: LDLib2 rebuilds the builtin entries
     * on a resource reload (F3+T), and the whole pass is five map puts.
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        apply();
    }

    private static void apply() {
        var instance = TexturesResource.INSTANCE.getResourceInstance();
        if (instance == null) {
            return;
        }
        for (var provider : instance.getBuiltinProviders().values().stream().flatMap(List::stream).toList()) {
            if (!PROVIDER.equals(provider.getName())) {
                continue;
            }
            for (var entry : ATLASES.entrySet()) {
                var path = new BuiltinPath(entry.getKey());
                if (!provider.hasResource(path)) {
                    continue;
                }
                provider.addResource(path, SpriteTexture.of(ResourceLocation.fromNamespaceAndPath(
                        NeoECOPrototype.MOD_ID, "textures/gui/host_panel/" + entry.getValue() + ".png")));
            }
        }
    }
}
