package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;

/**
 * The four computation cable models, in one place, because a search that starts in
 * {@code models/block/computation_cable} finds no reference to the glowing pair and calls them dead files:
 * they are reached only through eco's per-host cable table (see {@code NeoECOPrototypeClient}).
 *
 * <p>Ted's README item 5 splits them by the drive they sit in, not by the cell: a lower (L1-class) frame
 * wears the plain pair, an upper frame wears the {@code _normal} pair. eco 21.2.10 can express that -
 * its drive renderer hands the lookup the host block alongside the tier - so the split is registration, not
 * an injection.
 */
public final class ComputationCableModels {
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/" + name);
    }

    /** Used by the L1 frame, and registered as the model of both of our tiers. */
    public static final ResourceLocation LOWER_CONNECTED = id("cable_l1");
    public static final ResourceLocation LOWER_DISCONNECTED = id("cable_l1_dis");
    /** Registered against eco's own computation drive, for every tier we own. */
    public static final ResourceLocation UPPER_CONNECTED = id("cable_l1r");
    public static final ResourceLocation UPPER_DISCONNECTED = id("cable_l1r_dis");

    private ComputationCableModels() {
    }
}
