package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * The six panel sheets eco resolves per tier, and the L1 copy we ask it to draw instead.
 *
 * <p>eco hands the name, not the path: {@code NETextures} registers each sheet under a constant string and
 * every sprite on it is cut through {@code PanelAtlasSprite}, which now asks {@link IECOTier#getPanelAtlas}
 * first. The six names and their default sheets are measured off that class's own initialiser, so a rename
 * upstream shows up as a null answer here rather than as a silently wrong picture.
 *
 * <p>Answering a name we do not have is null, which is the contract: eco keeps its own sheet for that one.
 */
public enum PanelAtlas {
    COMPUTATION_BACKGROUND("Computation-BACKGROUND_ATLAS", "eco_cpu_controller"),
    COMPUTATION_WIDGETS("Computation-WIDGETS_ATLAS", "cpu_widget"),
    CRAFTING_CONTROLLER("Crafting-CONTROLLER_ATLAS", "crafting_controller"),
    CRAFTING_WIDGETS("Crafting-WIDGETS_ATLAS", "crafting_widgets"),
    STORAGE_CONTROLLER("Storage-CONTROLLER_ATLAS", "storage_controller"),
    STORAGE_WIDGETS("Storage-ATLAS", "storage_widget");

    private final String atlas;
    private final ResourceLocation texture;

    PanelAtlas(String atlas, String file) {
        this.atlas = atlas;
        this.texture = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "textures/gui/panel/atlas/" + file + ".png");
    }

    /** The name eco asks with. */
    public String atlas() {
        return atlas;
    }

    /** Our own sheet, which has to be the same pixel size as the one it replaces. */
    public ResourceLocation texture() {
        return texture;
    }

    @Nullable
    public static ResourceLocation resolve(String atlas) {
        for (var own : values()) {
            if (own.atlas.equals(atlas)) {
                return own.texture;
            }
        }
        return null;
    }
}
