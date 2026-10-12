package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoae.all.NEMultiBlocks;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A consumable that climbs exactly one rung of the tier ladder: {@code L4 -> L6} or {@code L6 -> L9}.
 *
 * <p>Which family the machine belongs to is read off the host block standing there, so the two cards
 * cover all three families between them and no card is stamped with a family id. Unlike the expansion
 * card this one is the price: the tier is already paid for in parts, and the card is what says the
 * player is allowed to move a finished machine up a rung at all. It is eaten only after the new tier is
 * standing - a machine that is too short, or is not on the rung this card covers, costs nothing.</p>
 *
 * <p>Both cards demand a machine at its ceiling, and that ceiling is eco's config value
 * ({@code storageSystemMaxLength} and friends), not a constant we could bake into the item.</p>
 */
public class SimplifyTierUpgradeCardItem extends SimplifyStructureExpansionCardItem {
    /** The one rung this card covers, and nothing else. */
    public enum Rung {
        FOUR_TO_SIX,
        SIX_TO_NINE
    }

    private final Rung rung;

    public SimplifyTierUpgradeCardItem(Properties properties, Rung rung) {
        super(properties);
        this.rung = rung;
    }

    public Rung rung() {
        return rung;
    }

    @Override
    protected boolean canGrow() {
        return false;
    }

    @Override
    protected boolean consumedOnSuccess() {
        return true;
    }

    /** The card is the price: no parts are taken from the inventory and none are handed back to it. */
    @Override
    protected boolean chargesParts() {
        return false;
    }

    @Override
    protected String upgradedKey() {
        return "card.neoecoprototype.upgrade.upgraded_free";
    }

    @Override
    protected @Nullable MultiBlockDefinition targetOf(MultiBlockDefinition current) {
        return rungTarget(rung, current);
    }

    /**
     * The one rung this card covers, for every family, or null when the machine standing there is not
     * on it. Read off the definitions themselves rather than a tier number, because eco registers its
     * hosts per tier and there is no {@code getTier()} to ask.
     */
    public static @Nullable MultiBlockDefinition rungTarget(Rung rung, MultiBlockDefinition current) {
        if (current == null) {
            return null;
        }
        if (rung == Rung.FOUR_TO_SIX) {
            if (current == NEMultiBlocks.STORAGE_SYSTEM_L4) {
                return NEMultiBlocks.STORAGE_SYSTEM_L6;
            }
            if (current == NEMultiBlocks.COMPUTATION_SYSTEM_L4) {
                return NEMultiBlocks.COMPUTATION_SYSTEM_L6;
            }
            if (current == NEMultiBlocks.CRAFTING_SYSTEM_L4) {
                return NEMultiBlocks.CRAFTING_SYSTEM_L6;
            }
            return null;
        }
        if (current == NEMultiBlocks.STORAGE_SYSTEM_L6) {
            return NEMultiBlocks.STORAGE_SYSTEM_L9;
        }
        if (current == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
            return NEMultiBlocks.COMPUTATION_SYSTEM_L9;
        }
        if (current == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return NEMultiBlocks.CRAFTING_SYSTEM_L9;
        }
        return null;
    }

    @Override
    protected Component refusal(MultiBlockDefinition current, int length) {
        if (current != null && (current == NEMultiBlocks.STORAGE_SYSTEM_L9
                || current == NEMultiBlocks.COMPUTATION_SYSTEM_L9
                || current == NEMultiBlocks.CRAFTING_SYSTEM_L9)) {
            return Component.translatable("card.neoecoprototype.expansion.at_max", length);
        }
        return Component.translatable(rung == Rung.FOUR_TO_SIX
                ? "card.neoecoprototype.upgrade.wrong_rung_4_6"
                : "card.neoecoprototype.upgrade.wrong_rung_6_9");
    }
}
