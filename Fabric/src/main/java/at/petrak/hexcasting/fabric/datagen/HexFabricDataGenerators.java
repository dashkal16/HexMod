package at.petrak.hexcasting.fabric.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.datagen.HexLootTables;
import at.petrak.hexcasting.datagen.IXplatIngredients;
import at.petrak.hexcasting.datagen.recipe.HexplatRecipes;
import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightToolIngredient;
import at.petrak.hexcasting.datagen.tag.HexActionTagProvider;
import at.petrak.hexcasting.datagen.tag.HexBlockTagProvider;
import at.petrak.hexcasting.datagen.tag.HexItemTagProvider;
import at.petrak.hexcasting.fabric.recipe.FabricModConditionalIngredient;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class HexFabricDataGenerators implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        HexAPI.LOGGER.info("Starting Fabric-specific datagen");

        var pack = gen.createPack();

        pack.addProvider((FabricDataGenerator.Pack.Factory<HexplatRecipes>) x -> new HexplatRecipes(x, INGREDIENTS, HexFabricConditionsBuilder::new));

        var xtags = IXplatAbstractions.INSTANCE.tags();
        var btagProviderWrapper = new BlockTagProviderWrapper(); // CURSED
        pack.addProvider((output, lookup) -> {
            btagProviderWrapper.provider = new HexBlockTagProvider(output, lookup, xtags);
            return btagProviderWrapper.provider;
        });
        pack.addProvider((output, lookup) -> new HexItemTagProvider(output, lookup, btagProviderWrapper.provider, xtags));

        pack.addProvider(HexActionTagProvider::new);

        pack.addProvider((FabricDataGenerator.Pack.Factory<LootTableProvider>) (output) -> new LootTableProvider(
                output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(HexLootTables::new, LootContextParamSets.ALL_PARAMS))
        ));
    }

    private static class BlockTagProviderWrapper {
        HexBlockTagProvider provider;
    }

    private static final IXplatIngredients INGREDIENTS = new IXplatIngredients() {
        @Override
        public Ingredient glowstoneDust() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GLOWSTONE_DUST)),
                new Ingredient.TagValue(tag("glowstone_dusts"))
            ));
        }

        @Override
        public Ingredient leather() {
            // apparently c:leather also includes rabbit hide
            return Ingredient.of(Items.LEATHER);
        }

        @Override
        public Ingredient ironNugget() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.IRON_NUGGET)),
                new Ingredient.TagValue(tag("iron_nuggets"))
            ));
        }

        @Override
        public Ingredient goldNugget() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GOLD_NUGGET)),
                new Ingredient.TagValue(tag("gold_nuggets"))
            ));
        }

        @Override
        public Ingredient copperIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.COPPER_INGOT)),
                new Ingredient.TagValue(tag("copper_ingots"))
            ));
        }

        @Override
        public Ingredient ironIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.IRON_INGOT)),
                new Ingredient.TagValue(tag("iron_ingots"))
            ));
        }

        @Override
        public Ingredient goldIngot() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.GOLD_INGOT)),
                new Ingredient.TagValue(tag("gold_ingots"))
            ));
        }

        @Override
        public EnumMap<DyeColor, Ingredient> dyes() {
            var out = new EnumMap<DyeColor, Ingredient>(DyeColor.class);
            for (var col : DyeColor.values()) {
                out.put(col, new Ingredient(Stream.of(
                    new Ingredient.ItemValue(new ItemStack(DyeItem.byColor(col))),
                    new Ingredient.TagValue(
                        TagKey.create(Registries.ITEM,
                            new ResourceLocation("c", col.getSerializedName() + "_dye"))),
                    new Ingredient.TagValue(
                        TagKey.create(Registries.ITEM,
                            new ResourceLocation("c", col.getSerializedName() + "_dyes"))
                    ))));
            }
            return out;
        }

        @Override
        public Ingredient stick() {
            return new Ingredient(Stream.of(
                new Ingredient.ItemValue(new ItemStack(Items.STICK)),
                new Ingredient.TagValue(tag("wood_sticks"))
            ));
        }

        @Override
        public Ingredient whenModIngredient(Ingredient defaultIngredient, String modid, Ingredient modIngredient) {
            return FabricModConditionalIngredient.of(defaultIngredient, modid, modIngredient);
        }

        private final FarmersDelightToolIngredient AXE_INGREDIENT = () -> {
            JsonObject object = new JsonObject();
            object.addProperty("type", "farmersdelight:tool");
            object.addProperty("tag", "c:tools/axes");
            return object;
        };

        @Override
        public FarmersDelightToolIngredient axeStrip() {
            return AXE_INGREDIENT;
        }

        @Override
        public FarmersDelightToolIngredient axeDig() {
            return AXE_INGREDIENT;
        }
    };

    private static TagKey<Item> tag(String s) {
        return tag("c", s);
    }

    private static TagKey<Item> tag(String namespace, String s) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(namespace, s));
    }
}
