package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.menu.TerminalMenu;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModItems;
import com.arcanelens.util.StorageNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Placed adjacent to a Storage Block, a Terminal browses that block's whole connected cluster as one
 * merged pool (see StorageNetworkManager/TerminalMenu). Beyond the pool itself, a Terminal owns two real
 * handlers of its own: a fixed 3-slot upgradeHandler (Stack/Slot Upgrade items) and a dynamically-sized
 * bonusSlotHandler backing whatever extra pool slots the installed Slot Upgrades grant - that storage has
 * to live somewhere real, not float across whichever member Storage Blocks happen to exist.
 */
public class TerminalBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int UPGRADE_SLOTS = 3;

    private final ItemStackHandler upgradeHandler = new ItemStackHandler(UPGRADE_SLOTS)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            resizeBonusHandler();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            return stack.is(ModItems.STACK_UPGRADE.get()) || stack.is(ModItems.SLOT_UPGRADE.get());
        }
    };

    private final ItemStackHandler bonusSlotHandler = new ItemStackHandler(0)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
        }
    };

    // Set just around a single synchronous NetworkHooks.openScreen call from openRemotely() below, so the
    // createMenu() callback that call triggers can tell a Warped-Anchor-initiated open apart from a normal
    // in-person right-click on the block - see TerminalMenu's remoteAccess field.
    private boolean pendingRemoteOpen = false;

    public TerminalBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.TERMINAL.get(), pos, state);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("block.arcanelens.terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player)
    {
        return new TerminalMenu(containerId, playerInventory, this, getHomeClusterMembers(level), pendingRemoteOpen);
    }

    /** Opens this Terminal's menu for a player who isn't standing next to it - see WarpedAnchorItem. Same
     * buffer contents TerminalBlock.use() writes (position + ordered member list), so the client-side
     * FriendlyByteBuf constructor doesn't need to know or care which path opened it. */
    public void openRemotely(ServerPlayer player)
    {
        if (level == null)
        {
            return;
        }
        pendingRemoteOpen = true;
        NetworkHooks.openScreen(player, this, buf -> {
            buf.writeBlockPos(worldPosition);
            var members = getHomeClusterMembers(level);
            buf.writeVarInt(members.size());
            for (BlockPos memberPos : members)
            {
                buf.writeBlockPos(memberPos);
            }
        });
        pendingRemoteOpen = false;
    }

    public ItemStackHandler getUpgradeHandler()
    {
        return upgradeHandler;
    }

    public ItemStackHandler getBonusSlotHandler()
    {
        return bonusSlotHandler;
    }

    public int getStackUpgradeCount()
    {
        return countUpgrades(ModItems.STACK_UPGRADE.get());
    }

    public int getSlotUpgradeCount()
    {
        return countUpgrades(ModItems.SLOT_UPGRADE.get());
    }

    private int countUpgrades(Item item)
    {
        int count = 0;
        for (int i = 0; i < upgradeHandler.getSlots(); i++)
        {
            ItemStack stack = upgradeHandler.getStackInSlot(i);
            if (stack.is(item))
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** Every slot in the Terminal's merged pool - both real member-block slots and these bonus slots -
     * must agree on the same effective stack limit, so both the delegating pool wrapper's insert logic
     * and the custom pool Slot's getMaxStackSize() call this single helper rather than computing it twice. */
    public int getEffectiveSlotLimit(ItemStack stack)
    {
        return stack.getMaxStackSize() + getStackUpgradeCount() * Config.stackSizeBonusPerStackUpgrade;
    }

    /** The cluster this Terminal browses: the first adjacent Storage Block (fixed Direction order, see
     * StorageNetworkManager.findAnchor) and everything BFS-reachable from it. Empty if no Storage Block
     * is currently adjacent. */
    public List<BlockPos> getHomeClusterMembers(Level level)
    {
        BlockPos anchor = StorageNetworkManager.findAnchor(level, worldPosition);
        if (anchor == null)
        {
            return List.of();
        }
        return StorageNetworkManager.findClusterFrom(level, anchor).memberStoragePositions();
    }

    /** Resizes bonusSlotHandler to slotUpgradeCount * Config.slotsPerSlotUpgrade, snapshotting existing
     * contents first and dropping anything that no longer fits (an upgrade was removed while its bonus
     * slots were occupied) - same snapshot/resize/restore-with-overflow-drop idiom as
     * StorageBlockEntity.resizeToConfig, generalized here to run on every upgrade change, not just load. */
    private void resizeBonusHandler()
    {
        int target = getSlotUpgradeCount() * Config.slotsPerSlotUpgrade;
        int current = bonusSlotHandler.getSlots();
        if (current == target)
        {
            return;
        }

        ItemStack[] existing = new ItemStack[current];
        for (int i = 0; i < current; i++)
        {
            existing[i] = bonusSlotHandler.getStackInSlot(i);
        }

        bonusSlotHandler.setSize(target);
        for (int i = 0; i < Math.min(current, target); i++)
        {
            bonusSlotHandler.setStackInSlot(i, existing[i]);
        }

        if (target < current && level instanceof ServerLevel serverLevel)
        {
            for (int i = target; i < current; i++)
            {
                if (!existing[i].isEmpty())
                {
                    Containers.dropItemStack(serverLevel, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), existing[i]);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        tag.put("Upgrades", upgradeHandler.serializeNBT());
        tag.put("BonusSlots", bonusSlotHandler.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        upgradeHandler.deserializeNBT(tag.getCompound("Upgrades"));
        bonusSlotHandler.deserializeNBT(tag.getCompound("BonusSlots"));
        resizeBonusHandler();
    }
}
