package cn.dancingsnow.neoecoprototype.integration.kubejs;

import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoprototype.integration.mekanism.SimplifyChemicalStorageCellItem;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * Every Mekanism and Applied Mekanistics name this addon can reach for through a script-made matrix lives
 * here, behind calls the builder makes only after checking both mods are installed.
 *
 * <p>Not a style preference. Registering the KubeJS builder type resolves the builder class itself, so a
 * single reference left in {@code StorageMatrixBuilder} takes down every pack that has KubeJS but not
 * Mekanism - measured as 552 copies of {@code NoClassDefFoundError: me/ramidzkh/mekae2/ae2/MekanismKey}
 * during the registry event, with the server never starting and no test running.
 */
final class ChemicalMatrixSupport {
    private ChemicalMatrixSupport() {
    }

    /**
     * The bound chemical's key. Nothing here runs until a matrix actually asks for it, so a pack that
     * never writes {@code chemicalType(...)} never loads this class at all.
     */
    static AEKey resolve(ResourceLocation id, ResourceLocation matrix) {
        var chemical = MekanismAPI.CHEMICAL_REGISTRY.get(id);
        if (chemical == null || chemical.isEmptyType()) {
            throw new IllegalStateException("Unknown Mekanism chemical '" + id + "' for matrix " + matrix);
        }
        return MekanismKey.of(new ChemicalStack(chemical, 1));
    }

    /** The chemical family's type count. Read only past the guard, so loading it is safe there. */
    static int totalTypes() {
        return SimplifyChemicalStorageCellItem.TOTAL_TYPES;
    }

    /** Called from behind the guard in {@code createObject}, which is what makes loading it safe. */
    static Item create(Item.Properties properties, long bytes, int bytesPerType, ResourceLocation bound) {
        return new SimplifyChemicalStorageCellItem(properties, bytes, bytesPerType, bound);
    }
}
