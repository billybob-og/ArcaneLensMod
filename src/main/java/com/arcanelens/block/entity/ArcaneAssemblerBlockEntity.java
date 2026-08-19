package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.menu.ArcaneAssemblerMenu;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModItemTags;
import com.arcanelens.util.StorageNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Auto-crafts real crafting-table recipes from its 3x3 grid (see AssemblerCraftingContainer), powered by
 * crop/meat "fuel" instead of redstone. Fuel only burns while a valid recipe currently sits in the grid -
 * burning food for nothing would undercut the whole point of the feature (using up crop/meat surplus).
 * Deliberately not part of the storage network's own BFS/claim system (see StorageNetworkManager) - it
 * only ever reads from an adjacent cluster's members' own IItemHandler capabilities (see refillGrid),
 * never registers a claim or appears in BFS traversal itself. Empty grid slots auto-refill from the
 * internal buffer first, falling back to the adjacent cluster (if any) second - see slotMemory.
 */
public class ArcaneAssemblerBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int SLOT_FUEL = 0;
    public static final int BUFFER_START = 1;
    public static final int BUFFER_SLOTS = 8;
    public static final int GRID_START = BUFFER_START + BUFFER_SLOTS;
    public static final int GRID_SLOTS = 9;
    public static final int SLOT_OUTPUT = GRID_START + GRID_SLOTS;
    public static final int TOTAL_SLOTS = SLOT_OUTPUT + 1;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if (slot >= GRID_START && slot < GRID_START + GRID_SLOTS)
            {
                recipeDirty = true;
                // Remember whatever item type just occupied this grid slot (never cleared when the
                // slot goes empty) - this is what lets refillGridFromBuffer keep re-filling the same
                // "pattern" slot-by-slot after a player sets it up once.
                ItemStack current = this.getStackInSlot(slot);
                if (!current.isEmpty())
                {
                    ItemStack template = current.copy();
                    template.setCount(1);
                    slotMemory.set(slot - GRID_START, template);
                }
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            if (slot == SLOT_OUTPUT)
            {
                // Nothing insertable, including hoppers - craft-completion writes here directly,
                // bypassing insertItem.
                return false;
            }
            if (slot == SLOT_FUEL)
            {
                return stack.is(ModItemTags.ASSEMBLER_FUEL);
            }
            return true;
        }
    };

    /** What external blocks (hoppers, pipes, etc.) see via the capability - insertion behaves exactly
     * like the raw handler (still gated per-slot by isItemValid above), but extraction only ever succeeds
     * on SLOT_OUTPUT. Fuel/buffer/grid contents are the machine's own working stock, not up for grabs -
     * a hopper sitting on top shouldn't be able to drain its fuel slot. Internal logic (tick/tryConsumeFuel/
     * refillGrid/performCraft) always goes through the raw itemHandler directly, bypassing this
     * restriction entirely, as does the Menu (constructed with getItemHandler(), not this wrapper). */
    private final IItemHandler externalItemHandler = new IItemHandler()
    {
        @Override
        public int getSlots()
        {
            return itemHandler.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return itemHandler.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
        {
            return itemHandler.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (slot != SLOT_OUTPUT)
            {
                return ItemStack.EMPTY;
            }
            return itemHandler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            return itemHandler.isItemValid(slot, stack);
        }
    };

    private final LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.of(() -> externalItemHandler);
    private final AssemblerCraftingContainer craftingContainer = new AssemblerCraftingContainer(itemHandler, GRID_START);

    private final NonNullList<ItemStack> slotMemory = NonNullList.withSize(GRID_SLOTS, ItemStack.EMPTY);

    private boolean recipeDirty = true;
    @Nullable
    private CraftingRecipe cachedRecipe;
    private int fuelTicksRemaining;
    private int craftProgressTicks;
    private int refillTimer;

    /** Baked in from the placing player's Skill Tree levels at setPlacedBy time (see ArcaneAssemblerBlock)
     * rather than looked up live - unlike a Terminal/Living Chest, this block has no "owning player"
     * concept to consult on every tick (it runs unattended, no player necessarily online), so its speed/
     * fuel-efficiency are fixed to whoever placed it, the same way an enchantment is baked into an item. */
    private int speedLevel;
    private int fuelEfficiencyLevel;

    /** Backs the Menu's data slots (see ArcaneAssemblerMenu.addDataSlots) - the same live-sync mechanism
     * vanilla's furnace uses for its burn-time/cook-time bars. Without this, fuelTicksRemaining/
     * craftProgressTicks are plain server-side fields with no per-tick sync, so an open screen would only
     * ever show whatever was last sent via block-entity chunk sync (effectively frozen). */
    private final ContainerData dataAccess = new ContainerData()
    {
        @Override
        public int get(int index)
        {
            return switch (index)
            {
                case 0 -> fuelTicksRemaining;
                case 1 -> craftProgressTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value)
        {
            switch (index)
            {
                case 0 -> fuelTicksRemaining = value;
                case 1 -> craftProgressTicks = value;
            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }
    };

    public ContainerData getDataAccess()
    {
        return dataAccess;
    }

    public ArcaneAssemblerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ARCANE_ASSEMBLER.get(), pos, state);
    }

    public ItemStackHandler getItemHandler()
    {
        return itemHandler;
    }

    public int getFuelTicksRemaining()
    {
        return fuelTicksRemaining;
    }

    public int getCraftProgressTicks()
    {
        return craftProgressTicks;
    }

    public void setSkillLevels(int speedLevel, int fuelEfficiencyLevel)
    {
        this.speedLevel = Math.max(0, speedLevel);
        this.fuelEfficiencyLevel = Math.max(0, fuelEfficiencyLevel);
        setChanged();
    }

    // Tracks how many ArcaneAssemblerMenu instances currently have this block entity open (see
    // onMenuOpened/onMenuClosed) - a plain counter rather than a boolean since nothing stops two players
    // from viewing the same Assembler at once in multiplayer.
    private int openMenuCount;

    public void onMenuOpened()
    {
        openMenuCount++;
    }

    public void onMenuClosed()
    {
        openMenuCount = Math.max(0, openMenuCount - 1);
    }

    public int getEffectiveCraftTime()
    {
        double reduced = Config.assemblerCraftTimeTicks * (1.0 - speedLevel * Config.assemblerSpeedBonusPerLevel);
        return (int) Math.max(1, Math.round(reduced));
    }

    private int getEffectiveFuelTicks()
    {
        double boosted = Config.assemblerFuelTicksPerItem * (1.0 + fuelEfficiencyLevel * Config.assemblerFuelEfficiencyBonusPerLevel);
        return (int) Math.max(1, Math.round(boosted));
    }

    /** Called from ArcaneAssemblerBlock.getTicker - server-side only, once per tick. */
    public void tick(Level level, BlockPos pos, BlockState state)
    {
        refillTimer++;
        if (refillTimer >= Config.assemblerRefillIntervalTicks)
        {
            refillTimer = 0;
            // Paused while a player has this Assembler's screen open - otherwise the auto-refill loop
            // fights a player trying to redesign the recipe via setSlotTemplate, re-filling slots with
            // old memory before they get to touch them. Crafting/fuel burn on whatever's already in the
            // grid keeps going either way (matches watching a vanilla furnace smelt with its GUI open).
            if (openMenuCount <= 0)
            {
                refillGrid(level, pos);
            }
        }

        if (recipeDirty)
        {
            cachedRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingContainer, level).orElse(null);
            recipeDirty = false;
        }

        boolean canCraft = cachedRecipe != null && canFitOutput(level, cachedRecipe);
        if (canCraft)
        {
            if (fuelTicksRemaining <= 0)
            {
                fuelTicksRemaining = tryConsumeFuel();
            }
            if (fuelTicksRemaining > 0)
            {
                fuelTicksRemaining--;
                craftProgressTicks++;
                if (craftProgressTicks >= getEffectiveCraftTime())
                {
                    performCraft(level, cachedRecipe);
                    craftProgressTicks = 0;
                    recipeDirty = true;
                }
                setChanged();
                return;
            }
        }

        // No valid recipe, or out of fuel and none available to consume - decay progress rather than
        // hard-reset, so a momentary ingredient gap doesn't discard everything built up so far.
        if (craftProgressTicks > 0)
        {
            craftProgressTicks = Math.max(0, craftProgressTicks - 2);
            setChanged();
        }
    }

    /** For each empty grid slot with a remembered "pattern" item type, tries to pull one matching item
     * from the internal buffer first, then (only if the buffer has no match) from whatever storage
     * cluster happens to be adjacent - this is what makes crafting continue unattended once a player
     * sets up a recipe once, rather than requiring the grid to be hand-refilled every craft. Throttled
     * by refillTimer rather than run every tick (see tick()) - a full buffer/cluster scan per empty grid
     * slot every single tick would be wasteful. The cluster lookup itself (findAnchor/findClusterFrom) is
     * computed at most once per refill cycle, not once per empty slot - purely a read, no claim/membership
     * interaction, so this never registers as a network node itself. */
    private void refillGrid(Level level, BlockPos pos)
    {
        List<BlockPos> clusterMembers = null;

        for (int i = 0; i < GRID_SLOTS; i++)
        {
            int gridSlot = GRID_START + i;
            if (!itemHandler.getStackInSlot(gridSlot).isEmpty())
            {
                continue;
            }
            ItemStack template = slotMemory.get(i);
            if (template.isEmpty())
            {
                continue;
            }

            ItemStack pulled = tryPullFromBuffer(template);
            if (pulled.isEmpty())
            {
                if (clusterMembers == null)
                {
                    BlockPos anchor = StorageNetworkManager.findAnchor(level, pos);
                    clusterMembers = anchor != null
                            ? StorageNetworkManager.findClusterFrom(level, anchor).memberStoragePositions()
                            : List.of();
                }
                pulled = tryPullFromCluster(level, clusterMembers, template);
            }

            if (!pulled.isEmpty())
            {
                itemHandler.setStackInSlot(gridSlot, pulled);
            }
        }
    }

    private ItemStack tryPullFromBuffer(ItemStack template)
    {
        for (int b = 0; b < BUFFER_SLOTS; b++)
        {
            int bufferSlot = BUFFER_START + b;
            ItemStack stack = itemHandler.getStackInSlot(bufferSlot);
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, template))
            {
                ItemStack extracted = itemHandler.extractItem(bufferSlot, 1, false);
                if (!extracted.isEmpty())
                {
                    return extracted;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack tryPullFromCluster(Level level, List<BlockPos> members, ItemStack template)
    {
        for (BlockPos memberPos : members)
        {
            BlockEntity be = level.getBlockEntity(memberPos);
            if (be == null)
            {
                continue;
            }
            IItemHandler memberHandler = be.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
            if (memberHandler == null)
            {
                continue;
            }
            for (int slot = 0; slot < memberHandler.getSlots(); slot++)
            {
                ItemStack stack = memberHandler.getStackInSlot(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, template))
                {
                    ItemStack extracted = memberHandler.extractItem(slot, 1, false);
                    if (!extracted.isEmpty())
                    {
                        return extracted;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /** Sets a grid slot's remembered "pattern" directly from a held item, without consuming it - lets a
     * player redesign the recipe by right-clicking each grid slot with the item they want there (see
     * ArcaneAssemblerMenu.clicked), instead of having to physically source and hand-place all 9 real
     * ingredients just to change what's being crafted. If the slot currently holds a different (now stale)
     * item, that gets displaced into the buffer (or dropped if the buffer's full) so the next refill cycle
     * can pull in the newly-requested item instead. */
    public void setSlotTemplate(int gridIndex, ItemStack desired)
    {
        if (desired.isEmpty() || gridIndex < 0 || gridIndex >= GRID_SLOTS)
        {
            return;
        }
        ItemStack template = desired.copy();
        template.setCount(1);

        int gridSlot = GRID_START + gridIndex;
        ItemStack current = itemHandler.getStackInSlot(gridSlot);
        if (!current.isEmpty() && !ItemStack.isSameItemSameTags(current, template))
        {
            itemHandler.setStackInSlot(gridSlot, ItemStack.EMPTY);
            returnToBufferOrDrop(current);
        }

        slotMemory.set(gridIndex, template);
        recipeDirty = true;
        setChanged();
    }

    /** The other half of setSlotTemplate - right-clicking a grid slot with an EMPTY hand instead of a
     * held item (see ArcaneAssemblerMenu.clicked). Without this there'd be no way to tell a slot "you
     * should be empty for this recipe" - a shape like sticks (2 planks, 7 empty slots) needs most slots
     * actively forgotten, not just left alone, or the old sticky memory refills them the moment the
     * screen closes and auto-refill resumes. */
    public void clearSlotTemplate(int gridIndex)
    {
        if (gridIndex < 0 || gridIndex >= GRID_SLOTS)
        {
            return;
        }
        int gridSlot = GRID_START + gridIndex;
        ItemStack current = itemHandler.getStackInSlot(gridSlot);
        if (!current.isEmpty())
        {
            itemHandler.setStackInSlot(gridSlot, ItemStack.EMPTY);
            returnToBufferOrDrop(current);
        }
        slotMemory.set(gridIndex, ItemStack.EMPTY);
        recipeDirty = true;
        setChanged();
    }

    private void returnToBufferOrDrop(ItemStack stack)
    {
        ItemStack remaining = stack;
        for (int b = 0; b < BUFFER_SLOTS && !remaining.isEmpty(); b++)
        {
            remaining = itemHandler.insertItem(BUFFER_START + b, remaining, false);
        }
        if (!remaining.isEmpty() && level instanceof ServerLevel serverLevel)
        {
            Containers.dropItemStack(serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), remaining);
        }
    }

    private int tryConsumeFuel()
    {
        ItemStack fuel = itemHandler.getStackInSlot(SLOT_FUEL);
        if (fuel.isEmpty())
        {
            return 0;
        }
        itemHandler.extractItem(SLOT_FUEL, 1, false);
        return getEffectiveFuelTicks();
    }

    private boolean canFitOutput(Level level, CraftingRecipe recipe)
    {
        ItemStack result = recipe.assemble(craftingContainer, level.registryAccess());
        if (result.isEmpty())
        {
            return false;
        }
        ItemStack currentOutput = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (currentOutput.isEmpty())
        {
            return true;
        }
        return ItemStack.isSameItemSameTags(currentOutput, result)
                && currentOutput.getCount() + result.getCount() <= currentOutput.getMaxStackSize();
    }

    private void performCraft(Level level, CraftingRecipe recipe)
    {
        ItemStack result = recipe.assemble(craftingContainer, level.registryAccess());
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftingContainer);

        for (int i = 0; i < GRID_SLOTS; i++)
        {
            if (!craftingContainer.getItem(i).isEmpty())
            {
                craftingContainer.removeItem(i, 1);
            }
        }

        ItemStack currentOutput = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (currentOutput.isEmpty())
        {
            itemHandler.setStackInSlot(SLOT_OUTPUT, result);
        }
        else
        {
            currentOutput.grow(result.getCount());
        }

        // Real recipes (not a curated whitelist) can legitimately produce container remainders (e.g. an
        // emptied bucket) - route back into the now-empty grid slot if possible, else drop it.
        for (int i = 0; i < remaining.size() && i < GRID_SLOTS; i++)
        {
            ItemStack rem = remaining.get(i);
            if (rem.isEmpty())
            {
                continue;
            }
            ItemStack existing = craftingContainer.getItem(i);
            if (existing.isEmpty())
            {
                craftingContainer.setItem(i, rem);
            }
            else if (ItemStack.isSameItemSameTags(existing, rem))
            {
                existing.grow(rem.getCount());
            }
            else if (level instanceof ServerLevel serverLevel)
            {
                Containers.dropItemStack(serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), rem);
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
        {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("FuelTicksRemaining", fuelTicksRemaining);
        tag.putInt("CraftProgressTicks", craftProgressTicks);
        tag.putInt("SpeedLevel", speedLevel);
        tag.putInt("FuelEfficiencyLevel", fuelEfficiencyLevel);
        ListTag memoryList = new ListTag();
        for (int i = 0; i < GRID_SLOTS; i++)
        {
            memoryList.add(slotMemory.get(i).save(new CompoundTag()));
        }
        tag.put("SlotMemory", memoryList);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        fuelTicksRemaining = tag.getInt("FuelTicksRemaining");
        craftProgressTicks = tag.getInt("CraftProgressTicks");
        speedLevel = tag.getInt("SpeedLevel");
        fuelEfficiencyLevel = tag.getInt("FuelEfficiencyLevel");
        recipeDirty = true;
        if (tag.contains("SlotMemory"))
        {
            ListTag memoryList = tag.getList("SlotMemory", Tag.TAG_COMPOUND);
            for (int i = 0; i < GRID_SLOTS && i < memoryList.size(); i++)
            {
                slotMemory.set(i, ItemStack.of(memoryList.getCompound(i)));
            }
        }
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("block.arcanelens.arcane_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new ArcaneAssemblerMenu(containerId, playerInventory, this);
    }
}
