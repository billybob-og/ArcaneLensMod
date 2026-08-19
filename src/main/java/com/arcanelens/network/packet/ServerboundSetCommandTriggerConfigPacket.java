package com.arcanelens.network.packet;

import com.arcanelens.block.entity.CommandTriggerBlockEntity;
import com.arcanelens.block.entity.CommandTriggerMode;
import com.arcanelens.menu.CommandTriggerMenu;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ServerboundSetCommandTriggerConfigPacket
{
    private final List<String> commands;
    private final String mode;
    private final int repeatIntervalTicks;

    public ServerboundSetCommandTriggerConfigPacket(List<String> commands, String mode, int repeatIntervalTicks)
    {
        this.commands = commands;
        this.mode = mode;
        this.repeatIntervalTicks = repeatIntervalTicks;
    }

    public static void encode(ServerboundSetCommandTriggerConfigPacket packet, FriendlyByteBuf buf)
    {
        buf.writeVarInt(packet.commands.size());
        for (String command : packet.commands)
        {
            buf.writeUtf(command);
        }
        buf.writeUtf(packet.mode);
        buf.writeVarInt(packet.repeatIntervalTicks);
    }

    public static ServerboundSetCommandTriggerConfigPacket decode(FriendlyByteBuf buf)
    {
        int size = buf.readVarInt();
        if (size < 0 || size > CommandTriggerBlockEntity.MAX_CYCLE_SLOTS)
        {
            throw new DecoderException("Invalid command list size: " + size);
        }
        List<String> commands = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            commands.add(buf.readUtf());
        }
        String mode = buf.readUtf();
        int repeatIntervalTicks = buf.readVarInt();
        return new ServerboundSetCommandTriggerConfigPacket(commands, mode, repeatIntervalTicks);
    }

    public static void handle(ServerboundSetCommandTriggerConfigPacket packet, Supplier<NetworkEvent.Context> ctxSupplier)
    {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || !player.canUseGameMasterBlocks())
            {
                return;
            }
            if (player.containerMenu instanceof CommandTriggerMenu menu)
            {
                var blockEntity = menu.getBlockEntity();
                blockEntity.setCommands(packet.commands);
                try
                {
                    blockEntity.setMode(CommandTriggerMode.valueOf(packet.mode));
                }
                catch (IllegalArgumentException ignored)
                {
                }
                blockEntity.setRepeatIntervalTicks(packet.repeatIntervalTicks);
                menu.broadcastChanges();
            }
        });
        ctx.setPacketHandled(true);
    }
}
