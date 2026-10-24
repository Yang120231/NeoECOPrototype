package cn.dancingsnow.neoecoprototype.block.crafting;

import appeng.block.crafting.ICraftingUnitType;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * What one plushie gives a crafting CPU by itself: the bytes, the threads, the item it is seen as, and
 * the name it takes in the CPU list. {@link #item()} is resolved late because the block is registered
 * before the item is.
 */
public record DollCraftingUnitType(long storageBytes, int acceleratorThreads, Supplier<Item> item,
                                   String cpuName) implements ICraftingUnitType {
    @Override
    public long getStorageBytes() {
        return storageBytes;
    }

    @Override
    public int getAcceleratorThreads() {
        return acceleratorThreads;
    }

    @Override
    public Item getItemFromType() {
        return item.get();
    }
}
