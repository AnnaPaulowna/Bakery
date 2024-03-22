package satisfy.bakery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import satisfy.bakery.entity.CraftingBowlBlockEntity;
import satisfy.bakery.recipe.CraftingBowlRecipe;
import satisfy.bakery.registry.BlockEntityTypeRegistry;
import satisfy.bakery.registry.ObjectRegistry;
import satisfy.bakery.registry.RecipeTypeRegistry;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("deprecation")
public class CraftingBowlBlock extends BaseEntityBlock {
    public static final int STIRS_NEEDED = 50;
    public static final IntegerProperty STIRRING = IntegerProperty.create("stirring", 0, 32);
    public static final IntegerProperty STIRRED = IntegerProperty.create("stirred", 0, 100);

    public CraftingBowlBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(STIRRING, 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(STIRRED, 0));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(world, pos);
        return (Shapes.or(box(3, 0, 3, 13, 8, 13), box(4, 3, 4, 12, 8, 12))).move(offset.x, offset.y, offset.z);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STIRRING);
        builder.add(STIRRED);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public @NotNull InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        ItemStack itemStack = entity.getItemInHand(hand);
        int stirring = blockState.getValue(STIRRING);
        int stirred = blockState.getValue(STIRRED);

        if(blockEntity instanceof CraftingBowlBlockEntity bowlEntity) {

            if (stirring == 0) {
                if (bowlEntity.canAddItem(itemStack)) {
                    bowlEntity.addItemStack(itemStack.copy());
                    if (!entity.isCreative()) itemStack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
            }

            if (itemStack.isEmpty()) {
                if (stirred >= STIRS_NEEDED) {
                    if (stirring == 0) {
                        entity.getInventory().add(bowlEntity.getItem(4));
                        bowlEntity.setItem(4, ItemStack.EMPTY);
                        world.setBlock(pos, blockState.setValue(STIRRED, 0), 3);
                    }
                } else {
                    if(world instanceof ServerLevel _world) {
                        RandomSource randomsource = _world.random;
                        for (ItemStack stack : bowlEntity.getItems()) {
                            if (stack != ItemStack.EMPTY && bowlEntity.getItem(4) != stack) {
                                ItemParticleOption p = new ItemParticleOption(ParticleTypes.ITEM, stack);
                                _world.sendParticles(p, pos.getCenter().x, pos.getCenter().y + 0.25d, pos.getCenter().z, 1, randomsource.nextGaussian() * 0.15D, randomsource.nextDouble() * 0.15D, randomsource.nextGaussian() * 0.15, randomsource.nextGaussian() * 0.05);
                            }
                        }
                    }

                    if (stirring <= 6) {
                        world.setBlock(pos, blockState.setValue(STIRRING, 10), 3);

                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(blockState, level, pos, random);
    }

    @Override
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
    }


    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraftingBowlBlockEntity(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CraftingBowlBlockEntity be) {
                Containers.dropContents(world, pos, be);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityTypeRegistry.CRAFTING_BOWL_BLOCK_ENTITY.get(), (world1, pos, state1, be) -> be.tick(world1, pos, state1, be));
    }


    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
