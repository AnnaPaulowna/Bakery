package satisfy.bakery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import satisfy.bakery.block.CraftingBowlBlock;
import satisfy.bakery.recipe.CraftingBowlRecipe;
import satisfy.bakery.registry.BlockEntityTypeRegistry;
import satisfy.bakery.registry.ObjectRegistry;
import satisfy.bakery.registry.RecipeTypeRegistry;

import java.util.Set;
import java.util.stream.IntStream;

public class CraftingBowlBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer, BlockEntityTicker<CraftingBowlBlockEntity> {

    private NonNullList<ItemStack> stacks = NonNullList.withSize(5, ItemStack.EMPTY);


    public CraftingBowlBlockEntity(BlockPos position, BlockState state) {
        super(BlockEntityTypeRegistry.CRAFTING_BOWL_BLOCK_ENTITY.get(), position, state);
    }


    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound))
            ContainerHelper.saveAllItems(compound, this.stacks);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public @NotNull Component getDefaultName() {
        return Component.literal("crafting_bowl");
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }


    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        Item item = stack.getItem();
        return  (item == Items.WHEAT || item == Items.SUGAR || item == Items.MILK_BUCKET || item == Items.EGG || item == ObjectRegistry.YEAST.get()) &&
                !this.hasAnyOf(Set.of(stack.getItem()));
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    public int filledSlots() {
        int i = 4;

        for(int j = 0; j < this.getContainerSize(); ++j) {
            if (this.getItem(j) == ItemStack.EMPTY) {
                i--;
            }
        }

        return i;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public boolean canAddItem(ItemStack stack) {
        return this.canPlaceItem(0, stack) && filledSlots() < this.getContainerSize() - 1;
    }

    public void addItemStack(ItemStack stack) {
        for(int j = 0; j < this.getContainerSize(); ++j) {
            if (this.getItem(j) == ItemStack.EMPTY) {
                this.setItem(j, stack);
                setChanged();
                return;
            }
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState, CraftingBowlBlockEntity blockEntity) {
        if (!(level.getBlockState(blockPos).getBlock() instanceof CraftingBowlBlock)) {
            return;
        }

        int stirring = blockState.getValue(CraftingBowlBlock.STIRRING);
        int stirred = blockState.getValue(CraftingBowlBlock.STIRRED);

        if (stirring > 0) {
            if (stirred < CraftingBowlBlock.STIRS_NEEDED) {
                stirred++;
                CraftingBowlRecipe recipe = level.getRecipeManager().getRecipeFor(RecipeTypeRegistry.CRAFTING_BOWL_RECIPE_TYPE.get(), blockEntity, level).orElse(null);
                if (stirred == CraftingBowlBlock.STIRS_NEEDED && recipe != null) {
                    recipe.getIngredients().forEach(ingredient -> {
                        int size = blockEntity.getItems().size();
                        for (int slot = 0; slot < size; slot++) {
                            ItemStack stack = blockEntity.getItem(slot);
                            if (ingredient.test(stack)) {
                                blockEntity.setItem(slot, ItemStack.EMPTY);
                                break;
                            }
                        }
                    });
                    blockEntity.setItem(4, recipe.getResultItem(level.registryAccess()));
                }
            }

            stirring -= 1;

            level.setBlock(blockPos, blockState.setValue(CraftingBowlBlock.STIRRING, stirring).setValue(CraftingBowlBlock.STIRRED, stirred), 3);
        } else if (stirred > 0 && stirred < CraftingBowlBlock.STIRS_NEEDED) {
            level.setBlock(blockPos, blockState.setValue(CraftingBowlBlock.STIRRED, 0), 3);
        }
    }
}
