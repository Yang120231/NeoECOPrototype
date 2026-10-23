package cn.dancingsnow.neoecoprototype.item.decoration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;

import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Block item for the four dolls of honoured players. Every one of them can be worn on the head: night
 * vision and the creepers keeping their distance come from
 * {@link cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects}, and each doll carries its own armour
 * and optional extra effect as a {@link DollStats} handed over where it is registered.
 *
 * <p>The face is in the block's own model, so nothing here resolves a player skin and no custom renderer
 * is involved. There used to be a fifth plushie that did exactly that, textured after whoever
 * {@code /prototypefumo} looked up; it is gone, and the four are what ships.
 */
public class FumoItem extends BlockItem implements Equipable {
    /** What the doll grants while worn on the head, plus the effect only some of them carry. */
    public record DollStats(float armor, float toughness, @Nullable Holder<MobEffect> extraEffect) {
    }

    /** The doll's own armour values, given at registration and never null. */
    private final DollStats wornStats;
    private static final ResourceLocation ARMOR_ID = NeoECOPrototype.id("fumo_armor");
    private static final ResourceLocation TOUGHNESS_ID = NeoECOPrototype.id("fumo_armor_toughness");

    public FumoItem(Block block, Properties properties, DollStats wornStats) {
        super(block, properties);
        this.wornStats = java.util.Objects.requireNonNull(wornStats, "a doll needs its worn stats");
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** What a particular doll grants on top of the night vision every doll gives. */
    @Nullable
    public static Holder<MobEffect> extraEffect(ItemStack stack) {
        DollStats stats = statsOf(stack);
        return stats == null ? null : stats.extraEffect();
    }

    /** Null for anything that is not one of our dolls. */
    @Nullable
    public static DollStats statsOf(ItemStack stack) {
        return stack.getItem() instanceof FumoItem item ? item.wornStats : null;
    }

    /** The dolls double as light armour, each with its own values. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        DollStats stats = statsOf(stack);
        if (stats == null) return super.getDefaultAttributeModifiers(stack);
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(ARMOR_ID, stats.armor(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(TOUGHNESS_ID, stats.toughness(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (statsOf(stack) == null) return;
        // What these two lines promise is exactly what FumoEquipmentEffects#onPlayerTick grants.
        tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_hint")
                .withStyle(ChatFormatting.GRAY));
        Holder<MobEffect> extra = extraEffect(stack);
        if (extra != null) {
            // Named after the effect itself, so a future DollStats entry needs no new lang key and the
            // line cannot drift from what FumoEquipmentEffects actually applies.
            tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_extra",
                            extra.value().getDisplayName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
