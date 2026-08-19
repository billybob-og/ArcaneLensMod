package com.arcanelens.block.entity;

import com.arcanelens.menu.CommandTriggerMenu;
import com.arcanelens.registry.ModBlockEntities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An invisible stand-in for a Command Block that fires when a player touches it (directly, or relayed
 * through an adjacent TriggerPlateBlockEntity), instead of needing redstone. Reuses vanilla's own
 * BaseCommandBlock dispatch (same permission level 2 as a real Command Block) rather than reinventing
 * command parsing. See CommandTriggerMode for the five behaviors; all edge/interval decision-making lives
 * here so REPEATING mode works identically whether touched directly or relayed via a plate.
 */
public class CommandTriggerBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int MAX_CYCLE_SLOTS = 4;

    private final List<String> commands = new ArrayList<>(List.of("", "", "", ""));
    private CommandTriggerMode mode = CommandTriggerMode.NORMAL;
    private int repeatIntervalTicks = 20;
    private boolean toggleArmed = true;
    private int cycleIndex;
    private boolean hasFired;

    private boolean touchedLastTick;
    private int continuousTouchTicks;
    private boolean relayedTouchThisTick;
    private long lastFireGameTime = -1;

    private final BaseCommandBlock commandBlockDelegate = new BaseCommandBlock()
    {
        @Override
        public ServerLevel getLevel()
        {
            return (ServerLevel) CommandTriggerBlockEntity.this.level;
        }

        @Override
        public void onUpdated()
        {
        }

        @Override
        public Vec3 getPosition()
        {
            return Vec3.atCenterOf(worldPosition);
        }

        @Override
        public CommandSourceStack createCommandSourceStack()
        {
            return new CommandSourceStack(this, Vec3.atCenterOf(worldPosition), new Vec2(0.0F, 0.0F),
                    getLevel(), 2, getName().getString(), getName(), getLevel().getServer(), null);
        }

        @Override
        public boolean isValid()
        {
            return !isRemoved();
        }
    };

    public CommandTriggerBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.COMMAND_TRIGGER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        boolean touchedNow = !serverLevel.getEntitiesOfClass(Player.class, new AABB(pos)).isEmpty() || relayedTouchThisTick;
        relayedTouchThisTick = false;

        boolean risingEdge = touchedNow && !touchedLastTick;
        continuousTouchTicks = touchedNow ? continuousTouchTicks + 1 : 0;
        touchedLastTick = touchedNow;

        switch (mode)
        {
            case NORMAL, TOGGLE, CYCLE ->
            {
                if (risingEdge)
                {
                    fire(serverLevel);
                }
            }
            case DELETE_AFTER_FIRING ->
            {
                if (risingEdge && !hasFired)
                {
                    fire(serverLevel);
                }
            }
            case REPEATING ->
            {
                int interval = Math.max(1, repeatIntervalTicks);
                if (touchedNow && (continuousTouchTicks - 1) % interval == 0)
                {
                    fire(serverLevel);
                }
            }
        }
    }

    /** Called by an adjacent TriggerPlateBlockEntity during its own tick when it detects a player touching
     * itself - relayed presence is folded into this block's own touch state for exactly one tick. */
    public void markRelayedTouch()
    {
        relayedTouchThisTick = true;
    }

    private void fire(ServerLevel level)
    {
        if (level.getGameTime() == lastFireGameTime)
        {
            return;
        }
        lastFireGameTime = level.getGameTime();

        String command;
        switch (mode)
        {
            case TOGGLE ->
            {
                toggleArmed = !toggleArmed;
                if (!toggleArmed)
                {
                    setChanged();
                    return;
                }
                command = commands.get(0);
            }
            case CYCLE -> command = nextNonBlankCommand();
            default -> command = commands.get(0);
        }

        runVanillaCommand(level, command);

        if (mode == CommandTriggerMode.DELETE_AFTER_FIRING)
        {
            hasFired = true;
            deleteConnectedPlates(level);
            level.removeBlock(getBlockPos(), false);
            return;
        }
        setChanged();
    }

    /** Removes the whole web of Trigger Plates feeding into this trigger (same flood-fill shape as
     * TriggerPlateBlockEntity's own touch-relay, just deleting instead of marking touched, and stopping at
     * any other Command Trigger it runs into rather than deleting plates that still serve a different one). */
    private void deleteConnectedPlates(ServerLevel level)
    {
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(getBlockPos());
        queue.add(getBlockPos());

        while (!queue.isEmpty())
        {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values())
            {
                BlockPos neighborPos = current.relative(direction);
                if (!visited.add(neighborPos))
                {
                    continue;
                }
                if (level.getBlockEntity(neighborPos) instanceof TriggerPlateBlockEntity)
                {
                    level.removeBlock(neighborPos, false);
                    queue.add(neighborPos);
                }
            }
        }
    }

    private String nextNonBlankCommand()
    {
        for (int i = 0; i < commands.size(); i++)
        {
            int index = (cycleIndex + i) % commands.size();
            if (!commands.get(index).isBlank())
            {
                cycleIndex = (index + 1) % commands.size();
                return commands.get(index);
            }
        }
        return "";
    }

    private void runVanillaCommand(ServerLevel level, String command)
    {
        if (command == null || command.isBlank())
        {
            return;
        }
        if (level.getServer() == null || !level.getServer().isCommandBlockEnabled())
        {
            return;
        }
        commandBlockDelegate.setCommand(command);
        commandBlockDelegate.performCommand(level);
    }

    public List<String> getCommands()
    {
        return commands;
    }

    public void setCommands(List<String> newCommands)
    {
        for (int i = 0; i < MAX_CYCLE_SLOTS; i++)
        {
            commands.set(i, i < newCommands.size() ? newCommands.get(i) : "");
        }
        setChanged();
    }

    public CommandTriggerMode getMode()
    {
        return mode;
    }

    public void setMode(CommandTriggerMode mode)
    {
        this.mode = mode;
        setChanged();
    }

    public int getRepeatIntervalTicks()
    {
        return repeatIntervalTicks;
    }

    public void setRepeatIntervalTicks(int repeatIntervalTicks)
    {
        this.repeatIntervalTicks = Math.max(1, repeatIntervalTicks);
        setChanged();
    }

    @Override
    public boolean onlyOpCanSetNbt()
    {
        return true;
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        // BlockEntity data is only sent to clients on initial chunk load by default - without this, the
        // client's own copy of this block entity never learns about later changes (e.g. commands typed
        // into the GUI), so reopening the screen would keep showing stale/empty fields even though the
        // server's copy saved correctly.
        if (level instanceof ServerLevel serverLevel)
        {
            BlockState state = getBlockState();
            serverLevel.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName()
    {
        return Component.literal("Command Trigger");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player)
    {
        return new CommandTriggerMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        ListTag commandsTag = new ListTag();
        for (String command : commands)
        {
            commandsTag.add(StringTag.valueOf(command));
        }
        tag.put("Commands", commandsTag);
        tag.putString("Mode", mode.name());
        tag.putInt("RepeatIntervalTicks", repeatIntervalTicks);
        tag.putBoolean("ToggleArmed", toggleArmed);
        tag.putInt("CycleIndex", cycleIndex);
        tag.putBoolean("HasFired", hasFired);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        ListTag commandsTag = tag.getList("Commands", Tag.TAG_STRING);
        for (int i = 0; i < MAX_CYCLE_SLOTS; i++)
        {
            commands.set(i, i < commandsTag.size() ? commandsTag.getString(i) : "");
        }
        try
        {
            mode = CommandTriggerMode.valueOf(tag.getString("Mode"));
        }
        catch (IllegalArgumentException ignored)
        {
            mode = CommandTriggerMode.NORMAL;
        }
        repeatIntervalTicks = Math.max(1, tag.contains("RepeatIntervalTicks") ? tag.getInt("RepeatIntervalTicks") : 20);
        toggleArmed = !tag.contains("ToggleArmed") || tag.getBoolean("ToggleArmed");
        cycleIndex = tag.getInt("CycleIndex");
        hasFired = tag.getBoolean("HasFired");
    }
}
