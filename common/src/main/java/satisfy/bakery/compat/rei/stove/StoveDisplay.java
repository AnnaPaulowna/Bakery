package satisfy.bakery.compat.rei.stove;


import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.registry.RecipeManagerContext;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.Recipe;
import satisfy.bakery.Bakery;
import satisfy.bakery.recipe.StoveRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StoveDisplay extends BasicDisplay implements SimpleGridMenuDisplay {

    public static final CategoryIdentifier<StoveDisplay> STOVE_DISPLAY = CategoryIdentifier.of(Bakery.MOD_ID, "stove_display");


    private final float xp;

    public StoveDisplay(StoveRecipe recipe) {
        this(EntryIngredients.ofIngredients(recipe.getIngredients()), Collections.singletonList(EntryIngredients.of(recipe.getResultItem(BasicDisplay.registryAccess()))), recipe, recipe.getExperience());
    }

    public StoveDisplay(List<EntryIngredient> input, List<EntryIngredient> output, Recipe<?> recipe, float xp) {
        super(input, output, Optional.ofNullable(null));
        this.xp = xp;
    }

    public float getXp() {
        return xp;
    }


    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    public static <R extends StoveDisplay> BasicDisplay.Serializer<R> serializer(BasicDisplay.Serializer.RecipeLessConstructor<R> constructor) {
        return BasicDisplay.Serializer.ofRecipeLess(constructor, (display, tag) -> {
            tag.putFloat("experience", display.getXp());
        });
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return STOVE_DISPLAY;
    }
}