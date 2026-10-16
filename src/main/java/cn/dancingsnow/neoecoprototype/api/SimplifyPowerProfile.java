package cn.dancingsnow.neoecoprototype.api;

/**
 * Power tuning for addon-owned machine hosts.
 *
 * <p>The base values are our own numbers, scaled down by {@code idlePowerMultiplier} for every L1 host.
 * They are not a copy of eco's L4 table. Measured against the 21.2.2 jar, eco's crafting controller sets
 * its node to 64.0 AE/t ({@code ECOCraftingSystemBlockEntity}, a literal); ours is the seventh argument
 * below, 32 x 1/8 = <b>4.0 AE/t</b> - a sixteenth of eco's L4, not half of it. The 258 in the fifth slot
 * is the <i>storage</i> controller, and reading the two columns in the wrong order is what once made this
 * comment claim 32.25. The runtime figure agrees with 4.0: a host with a face, mood 0.5 and the default
 * 10% saving reports 3.8 AE/t.</p>
 */
public enum SimplifyPowerProfile {
    L1(1.0D / 8.0D, 16.0D, 32.0D, 256.0D, 258.0D, 16.0D, 32.0D);

    private final double idlePowerMultiplier;
    private final double baseComponentIdlePower;
    private final double highIdleComponentPower;
    private final double storageDriveBaseIdlePower;
    private final double storageControllerBaseIdlePower;
    private final double computationControllerBaseIdlePower;
    private final double craftingControllerBaseIdlePower;

    SimplifyPowerProfile(
            double idlePowerMultiplier,
            double baseComponentIdlePower,
            double highIdleComponentPower,
            double storageDriveBaseIdlePower,
            double storageControllerBaseIdlePower,
            double computationControllerBaseIdlePower,
            double craftingControllerBaseIdlePower) {
        this.idlePowerMultiplier = idlePowerMultiplier;
        this.baseComponentIdlePower = baseComponentIdlePower;
        this.highIdleComponentPower = highIdleComponentPower;
        this.storageDriveBaseIdlePower = storageDriveBaseIdlePower;
        this.storageControllerBaseIdlePower = storageControllerBaseIdlePower;
        this.computationControllerBaseIdlePower = computationControllerBaseIdlePower;
        this.craftingControllerBaseIdlePower = craftingControllerBaseIdlePower;
    }

    public double idlePowerMultiplier() {
        return idlePowerMultiplier;
    }

    public double baseComponentIdlePower() {
        return scaled(baseComponentIdlePower);
    }

    public double highIdleComponentPower() {
        return scaled(highIdleComponentPower);
    }

    public double storageDriveIdlePower() {
        return scaled(storageDriveBaseIdlePower);
    }

    public double scale(double basePower) {
        return scaled(basePower);
    }

    public double storageControllerIdlePower() {
        return scaled(storageControllerBaseIdlePower);
    }

    public double computationControllerIdlePower() {
        return scaled(computationControllerBaseIdlePower);
    }

    public double craftingControllerIdlePower() {
        return scaled(craftingControllerBaseIdlePower);
    }

    private double scaled(double basePower) {
        return basePower * idlePowerMultiplier;
    }
}
