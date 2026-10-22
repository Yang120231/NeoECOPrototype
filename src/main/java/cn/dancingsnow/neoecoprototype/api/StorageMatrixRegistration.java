package cn.dancingsnow.neoecoprototype.api;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Factory entry point for new finite matrices; legacy registrations intentionally do not use it.
 *
 * <p>It lives in {@code api/} and names {@link SimplifyStorageCellItem} on purpose: this is the layer
 * rule's one documented exception (AGENTS.md §3), because the method is advertised to script authors in
 * {@code docs/kubejs.md} and a factory has to name the concrete item it builds. The CI carve-out is
 * scoped to this file and to the items layer only - depending on {@code blockentity/}, {@code block/} or
 * {@code integration/} from here would still fail the build gate.
 */
public final class StorageMatrixRegistration {
    private StorageMatrixRegistration() {
    }

    public static Supplier<SimplifyStorageCellItem> registerFinite(
            DeferredRegister<Item> items,
            String registryName,
            StorageMatrixDefinition definition,
            AEKeyType keyType,
            Supplier<ECOCellType> cellType) {
        if (definition.infinite()) {
            throw new IllegalArgumentException("Use registerInfinite for an infinite matrix");
        }
        // getNamespace() is the DeferredRegister's own mod namespace. getRegistryKey().location() would be
        // the registry's - minecraft:item - which no matrix id can ever match.
        if (!definition.id().getNamespace().equals(items.getNamespace())
                || !definition.id().getPath().equals(registryName)) {
            throw new IllegalArgumentException("Matrix definition id does not match item registration: "
                    + definition.id() + " vs " + registryName);
        }
        return items.register(registryName, () -> {
            var item = new SimplifyStorageCellItem(new Item.Properties().stacksTo(1), keyType, cellType,
                    definition.bytes(), definition.bytesPerType(), definition.totalTypes());
            StorageMatrixRegistry.register(definition, item);
            return item;
        });
    }
}
