package com.arcanelens.menu;

import com.arcanelens.block.entity.ArcaneAssemblerBlockEntity;
import com.arcanelens.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Fuel slot, an 8-slot ingredient buffer, the live 3x3 crafting grid, and a single output slot - see
 * ArcaneAssemblerBlockEntity for the slot index layout this must stay in sync with. The output slot's
 * mayPlace() mirrors the handler-level isItemValid(SLOT_OUTPUT) rejection as UI-level reinforcement
 * (matching LensCombinerMenu's own SLOT_OUTPUT precedent) - shift-click and hopper insertion are blocked
 * independently at the handler level, this just stops manual GUI placement too.
 */
public class ArcaneAssemblerMenu extends AbstractContainerMenu
{
    private static final int FUEL_X = 8;
    private static final int FUEL_Y = 18;
    private static final int BUFFER_X = 8;
    private static final int BUFFER_Y = 40;
    private static final int GRID_X = 98;
    private static final int GRID_Y = 18;
    private static final int OUTPUT_X = 172;
    private static final int OUTPUT_Y = 36;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 94;
    private static final int HOTBAR_Y = 152;

    // Menu-level slot indices (this.slots), not ArcaneAssemblerBlockEntity's own handler indices - the
    // grid is added right after fuel(1) + buffer(BUFFER_SLOTS).
    private static final int GRID_MENU_START = 1 + ArcaneAssemblerBlockEntity.BUFFER_SLOTS;
    private static final int GRID_MENU_END = GRID_MENU_START + ArcaneAssemblerBlockEntity.GRID_SLOTS;

    private final ArcaneAssemblerBlockEntity blockEntity;

    public ArcaneAssemblerMenu(int containerId, Inventory playerInventory, ArcaneAssemblerBlockEntity blockEntity)
    {
        super(ModMenuTypes.ARCANE_ASSEMBLER.get(), containerId);
        this.blockEntity = blockEntity;
        // Pauses the block entity's auto-refill while this screen is open (see removed() below and
        // ArcaneAssemblerBlockEntity.tick()) - otherwise it fights a player trying to redesign the recipe
        // via right-click (setSlotTemplate), re-filling slots with old memory before they get to them.
        blockEntity.onMenuOpened();
        // Live-syncs fuelTicksRemaining/craftProgressTicks to the client every tick, same mechanism
        // vanilla's furnace uses for its burn-time/cook-time bars - see ArcaneAssemblerBlockEntity.dataAccess.
        this.addDataSlots(blockEntity.getDataAccess());

        IItemHandler handler = blockEntity.getItemHandler();

        this.addSlot(new SlotItemHandler(handler, ArcaneAssemblerBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y));

        for (int i = 0; i < ArcaneAssemblerBlockEntity.BUFFER_SLOTS; i++)
        {
            int x = BUFFER_X + (i % 4) * 18;
            int y = BUFFER_Y + (i / 4) * 18;
            this.addSlot(new SlotItemHandler(handler, ArcaneAssemblerBlockEntity.BUFFER_START + i, x, y));
        }

        for (int i = 0; i < ArcaneAssemblerBlockEntity.GRID_SLOTS; i++)
        {
            int x = GRID_X + (i % 3) * 18;
            int y = GRID_Y + (i / 3) * 18;
            this.addSlot(new SlotItemHandler(handler, ArcaneAssemblerBlockEntity.GRID_START + i, x, y));
        }

        this.addSlot(new SlotItemHandler(handler, ArcaneAssemblerBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y)
        {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack)
            {
                return false;
            }
        });

        for (int row = 0; row < 3; row++)
        {
            for (int col = 0; col < 9; col++)
            {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++)
        {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    public ArcaneAssemblerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData)
    {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    private static ArcaneAssemblerBlockEntity getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData)
    {
        BlockPos pos = extraData.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ArcaneAssemblerBlockEntity assembler)
        {
            return assembler;
        }
        throw new IllegalStateException("No ArcaneAssemblerBlockEntity at " + pos);
    }

    public ArcaneAssemblerBlockEntity getBlockEntity()
    {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player)
    {
        return blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64.0;
    }

    @Override
    public void removed(Player player)
    {
        super.removed(player);
        blockEntity.onMenuClosed();
    }

    /** Right-clicking a grid slot always edits its remembered "wish" instead of the normal vanilla
     * placement/split behavior - holding an item sets that slot's desired item (see
     * ArcaneAssemblerBlockEntity.setSlotTemplate), an empty hand clears it (clearSlotTemplate) so the
     * slot goes back to "intentionally empty" instead of getting auto-refilled from stale memory. Both
     * halves are needed for shapes like sticks, where most of the 9 slots must actively stay empty, not
     * just be left alone. Runs identically on client (prediction) and server (authoritative), matching
     * how every Slot click is normally resolved. */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player)
    {
        if (clickType == ClickType.PICKUP && button == 1 && slotId >= GRID_MENU_START && slotId < GRID_MENU_END)
        {
            ItemStack carried = this.getCarried();
            if (!carried.isEmpty())
            {
                blockEntity.setSlotTemplate(slotId - GRID_MENU_START, carried);
            }
            else
            {
                blockEntity.clearSlotTemplate(slotId - GRID_MENU_START);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem())
        {
            return result;
        }

        ItemStack stackInSlot = slot.getItem();
        result = stackInSlot.copy();
        int machineSlotCount = ArcaneAssemblerBlockEntity.TOTAL_SLOTS;
        int playerInvStart = machineSlotCount;
        int playerInvEnd = playerInvStart + 36;

        if (index < machineSlotCount)
        {
            // From fuel/buffer/grid (output is unreachable here - mayPlace(false) also blocks the
            // reverse direction via moveItemStackTo's own placement check) - into the player inventory.
            if (!this.moveItemStackTo(stackInSlot, playerInvStart, playerInvEnd, true))
            {
                return ItemStack.EMPTY;
            }
        }
        else
        {
            // From the player inventory/hotbar - try fuel first (harmless no-op for non-fuel items,
            // isItemValid rejects them), then the buffer, then the grid. Never targets the output slot.
            int bufferEnd = 1 + ArcaneAssemblerBlockEntity.BUFFER_SLOTS;
            int gridEnd = bufferEnd + ArcaneAssemblerBlockEntity.GRID_SLOTS;
            if (!this.moveItemStackTo(stackInSlot, 0, 1, false)
                    && !this.moveItemStackTo(stackInSlot, 1, bufferEnd, false)
                    && !this.moveItemStackTo(stackInSlot, bufferEnd, gridEnd, false))
            {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty())
        {
            slot.set(ItemStack.EMPTY);
        }
        else
        {
            slot.setChanged();
        }
        return result;
    }
}
