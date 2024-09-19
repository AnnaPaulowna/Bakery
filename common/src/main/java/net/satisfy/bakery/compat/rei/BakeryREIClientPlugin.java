package net.satisfy.bakery.compat.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.satisfy.bakery.compat.rei.caking.BakerStationCategory;
import net.satisfy.bakery.compat.rei.caking.BakerStationDisplay;
import net.satisfy.bakery.registry.ObjectRegistry;
import net.satisfy.bakery.recipe.BakingStationRecipe;

import java.util.ArrayList;
import java.util.List;


public class BakeryREIClientPlugin {
    public static void registerCategories(CategoryRegistry registry) {
        registry.add(new BakerStationCategory());
        registry.addWorkstations(BakerStationCategory.BAKER_STATION_DISPLAY, EntryStacks.of(ObjectRegistry.BAKER_STATION.get()));
    }

    public static void registerDisplays(DisplayRegistry registry) {
        registry.registerFiller(BakingStationRecipe.class, BakerStationDisplay::new);
    }

    public static List<Ingredient> ingredients(Recipe<RecipeInput> recipe, ItemStack stack) {
        List<Ingredient> l = new ArrayList<>(recipe.getIngredients());
        l.add(0, Ingredient.of(stack.getItem()));
        return l;
    }
}
