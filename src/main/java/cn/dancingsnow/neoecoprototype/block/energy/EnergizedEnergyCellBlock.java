package cn.dancingsnow.neoecoprototype.block.energy;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.energy.EnergizedEnergyCellBlockEntity;

/**
 * A standalone battery. It is deliberately not a multiblock member: AE2's grid collects every
 * {@code IAEPowerStorage} node into the network's total, so placing one of these adds its buffer without
 * any {@code MultiBlockDefinition} having to know the block exists.
 */
public class EnergizedEnergyCellBlock extends AEBaseEntityBlock<EnergizedEnergyCellBlockEntity> {

    private final double capacity;

    public EnergizedEnergyCellBlock(double capacity) {
        super(AEBaseBlock.metalProps());
        this.capacity = capacity;
    }

    /** AE, not FE: this is the number the grid's energy service adds up. */
    public double getCapacity() {
        return capacity;
    }
}
