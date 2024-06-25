package net.satisfy.bakery.block.crops;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.satisfy.bakery.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class OatCropBlock extends CropBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 5);

    public OatCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull ItemLike getBaseSeedId() {
        return ObjectRegistry.OAT_SEEDS.get();
    }

    @Override
    public int getMaxAge() {
        return 5;
    }

    @Override
    public @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
