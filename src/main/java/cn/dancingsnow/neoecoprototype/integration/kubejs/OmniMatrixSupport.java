package cn.dancingsnow.neoecoprototype.integration.kubejs;

import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * The Omni Cells half of the same arrangement as {@link ChemicalMatrixSupport}: eco's universal cell
 * extends an Omni Cells class, so naming it in the builder is enough to break a pack that never installed
 * the mod - the reference is resolved when the builder type is registered, not when a script uses it.
 */
final class OmniMatrixSupport {
    /** eco's OmniCells marker for "no type limit". */
    private static final int UNLIMITED_TYPES = -1;
    private static final double IDLE_DRAIN = 8.0D;

    private OmniMatrixSupport() {
    }

    /**
     * @param quantum true for the quantum family, which carries no type limit at all
     * @param types   the type count the table was written with, used as-is for universal
     */
    static Item create(Item.Properties properties, boolean quantum, int types, long bytes) {
        Supplier<ECOCellType> cellType = quantum
                ? NEOmniCellTypes.QUANTUM_OMNI::get
                : NEOmniCellTypes.OMNI::get;
        return new ECOUniversalStorageCellItem(properties, SimplifyTier.L1, cellType, IDLE_DRAIN,
                quantum ? UNLIMITED_TYPES : types, bytes);
    }

    /** The quantum matrix reports no ceiling, so the definition has to be written with one. */
    static int typesForDefinition(boolean quantum, int types) {
        return quantum ? Integer.MAX_VALUE : types;
    }
}
