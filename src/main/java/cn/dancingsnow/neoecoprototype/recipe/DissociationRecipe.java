package cn.dancingsnow.neoecoprototype.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One dissociation table: what goes in, what can come out, and how the roll is shaped.
 *
 * <p>Products are a JSON <em>object</em> - {@code "item:id": weight} - rather than a positional array,
 * because a position list makes every reader memorise which index is which item, and one reorder in the
 * product list silently retunes every table that points at it. Keys carry the meaning here.
 *
 * <p>Weights are <em>shares</em>, not probabilities: they are normalised to sum to one at load, so a table
 * written as {@code 1/1/1} means a third each. A table whose shares cannot be turned into a distribution at
 * all - empty, all zero, or not a number - is refused, and the recipe manager names the offending file.
 */
public record DissociationRecipe(ItemStack input, Map<Item, Double> weights, int draws, int perHit,
                                 FluidStack coolant, FluidStack solution,
                                 double powerPerRun, int ticksPerRun)
        implements Recipe<DissociationRecipe.Input> {

    /** Anything further from one than this is worth telling the pack author about. */
    public static final double SUM_TOLERANCE = 0.001D;
    public static final int MAX_PRODUCTS = 12;

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger("neoecoprototype/dissociation");

    public static final Codec<Map<Item, Double>> WEIGHTS_CODEC = Codec.unboundedMap(
            BuiltInRegistries.ITEM.byNameCodec(), Codec.DOUBLE)
            .xmap(map -> {
                // LinkedHashMap keeps the file's own order for display, which is the one thing a positional
                // array used to offer.
                Map<Item, Double> ordered = new LinkedHashMap<>();
                map.forEach((item, weight) -> ordered.put(item, weight));
                return Collections.unmodifiableMap(ordered);
            }, Map::copyOf);

    public static final RecipeType<DissociationRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "neoecoprototype:dissociation";
        }
    };

    public DissociationRecipe {
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("a dissociation table needs at least one product");
        }
        if (weights.size() > MAX_PRODUCTS) {
            throw new IllegalArgumentException("a dissociation table has " + weights.size()
                    + " products but the machine has " + MAX_PRODUCTS + " output slots");
        }
        double total = 0.0D;
        for (double weight : weights.values()) {
            if (Double.isNaN(weight)) {
                throw new IllegalArgumentException("a dissociation weight is not a number");
            }
            total += weight;
        }
        if (total <= 0.0D) {
            throw new IllegalArgumentException("a dissociation table's weights sum to " + total
                    + ", which cannot be turned into shares");
        }
        if (Math.abs(total - 1.0D) > SUM_TOLERANCE) {
            LOGGER.warn("Dissociation table for {} sums to {}, not 1.0; normalising to shares. "
                    + "Weights are shares, so a table written as 1/1/1 means a third each.",
            BuiltInRegistries.ITEM.getKey(input.getItem()), total);
        }
        Map<Item, Double> shares = new LinkedHashMap<>();
        double running = 0.0D;
        List<Item> keys = List.copyOf(weights.keySet());
        for (int i = 0; i < keys.size(); i++) {
            if (i == keys.size() - 1) {
                // Hand the rounding remainder to the last product so the shares add up to exactly one and
                // the roll's cumulative walk can never fall off the end.
                shares.put(keys.get(i), Math.max(0.0D, 1.0D - running));
            } else {
                double share = weights.get(keys.get(i)) / total;
                shares.put(keys.get(i), share);
                running += share;
            }
        }
        weights = Collections.unmodifiableMap(shares);
        if (draws <= 0 || perHit <= 0) {
            throw new IllegalArgumentException("draws and perHit must both be positive, got "
                    + draws + " and " + perHit);
        }
    }

    /** How many of each product a roll produced; absent keys were not rolled at all. */
    public Map<Item, Integer> roll(RandomSource source) {
        Map<Item, Integer> counted = new LinkedHashMap<>();
        for (int draw = 0; draw < draws; draw++) {
            double pick = source.nextDouble();
            double walked = 0.0D;
            Item chosen = null;
            for (Map.Entry<Item, Double> entry : weights.entrySet()) {
                walked += entry.getValue();
                if (pick < walked) {
                    chosen = entry.getKey();
                    break;
                }
            }
            if (chosen == null) {
                // Only reachable when floating point leaves the walk just short of one; the last product
                // owns that remainder by construction, so fall back to it rather than dropping the draw.
                chosen = lastProduct();
            }
            counted.merge(chosen, perHit, Integer::sum);
        }
        return counted;
    }

    public Item lastProduct() {
        Item last = null;
        for (Item item : weights.keySet()) {
            last = item;
        }
        return last;
    }

    /** The largest a single product can get in one run, which is what sizes an output slot. */
    public int maxPerProduct() {
        return draws * perHit;
    }

    @Override
    public boolean matches(Input input, Level level) {
        return this.input.isEmpty() || ItemStack.isSameItemSameComponents(this.input, input.stack());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return new ItemStack(lastProduct(), perHit);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return input;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return cn.dancingsnow.neoecoprototype.registration.ModRegistration.DISSOCIATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return cn.dancingsnow.neoecoprototype.registration.ModRegistration.DISSOCIATION_TYPE.get();
    }

    /** The one thing a player can act on from the table alone: what goes in. */
    public ItemStack input() {
        return input;
    }

    public static class Serializer implements RecipeSerializer<DissociationRecipe> {
        private static final MapCodec<DissociationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("input").forGetter(DissociationRecipe::input),
                WEIGHTS_CODEC.fieldOf("products").forGetter(DissociationRecipe::weights),
                Codec.intRange(1, 64).optionalFieldOf("draws", 12).forGetter(DissociationRecipe::draws),
                Codec.intRange(1, 640).optionalFieldOf("per_hit", 10).forGetter(DissociationRecipe::perHit),
                FluidStack.CODEC.fieldOf("coolant").forGetter(DissociationRecipe::coolant),
                FluidStack.CODEC.fieldOf("solution").forGetter(DissociationRecipe::solution),
                Codec.doubleRange(0.0D, 1.0E9D).optionalFieldOf("power", 100000.0D)
                        .forGetter(DissociationRecipe::powerPerRun),
                Codec.intRange(1, 72000).optionalFieldOf("ticks", 200).forGetter(DissociationRecipe::ticksPerRun)
        ).apply(instance, DissociationRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, DissociationRecipe> STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

        @Override
        public MapCodec<DissociationRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DissociationRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record Input(ItemStack stack) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? stack : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    /** Only used to read a fluid out of a table without dragging a level into a pure function. */
    public static Fluid fluidOf(FluidStack stack) {
        return stack.getFluid();
    }
}
