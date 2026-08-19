package com.arcanelens.item;

import com.arcanelens.ArcaneLens;
import com.arcanelens.Config;
import com.arcanelens.block.entity.TerminalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Binds to one placed Living Terminal (right-click it), then re-opens that exact Terminal's browsing UI
 * from anywhere (right-click in open air) - revives the "portable remote storage access" idea deferred
 * earlier in this project, now funded by Broken Vessel drops and gated behind Warped Attunement (see
 * OverloadRitualUnlockHandler). The bound Terminal's chunk is force-loaded via ForgeChunkManager for as
 * long as the binding lasts, released again on explicit unbind (shift-right-click in open air) or
 * automatically if the Terminal is found gone the next time the Anchor is used - Config.maxWarpedAnchorsPerPlayer
 * bounds the worst case if a bound Anchor is ever lost (lava, void, another player) without going through
 * either cleanup path.
 */
public class WarpedAnchorItem extends Item
{
    private static final String TAG_DIMENSION = "BoundDimension";
    private static final String TAG_POS = "BoundPos";

    public WarpedAnchorItem(Properties properties)
    {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        if (level.isClientSide)
        {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player))
        {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof TerminalBlockEntity))
        {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        if (getBoundPos(stack) != null)
        {
            message(player, "This Warped Anchor is already bound - unbind it first (shift-right-click in open air).");
            return InteractionResult.FAIL;
        }

        if (countOtherBoundAnchors(player, stack) >= Config.maxWarpedAnchorsPerPlayer)
        {
            message(player, "You can't bind more than " + Config.maxWarpedAnchorsPerPlayer
                    + " Warped Anchor" + (Config.maxWarpedAnchorsPerPlayer == 1 ? "" : "s") + " at once.");
            return InteractionResult.FAIL;
        }

        GlobalPos target = GlobalPos.of(level.dimension(), pos.immutable());
        setBoundPos(stack, target);
        requestTicket((ServerLevel) level, pos, true);
        message(player, ChatFormatting.LIGHT_PURPLE, "Bound to this Terminal.");
        return InteractionResult.CONSUME;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player playerEntity, InteractionHand hand)
    {
        ItemStack stack = playerEntity.getItemInHand(hand);
        if (level.isClientSide || !(playerEntity instanceof ServerPlayer player))
        {
            return InteractionResultHolder.pass(stack);
        }

        GlobalPos bound = getBoundPos(stack);
        if (bound == null)
        {
            message(player, "Right-click a placed Living Terminal to bind this Anchor to it.");
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown())
        {
            releaseTicket(player.getServer().getLevel(bound.dimension()), bound.pos());
            clearBoundPos(stack);
            message(player, "Unbound.");
            return InteractionResultHolder.success(stack);
        }

        ServerLevel targetLevel = player.getServer().getLevel(bound.dimension());
        if (targetLevel == null)
        {
            message(player, "The bound dimension no longer exists - unbound.");
            clearBoundPos(stack);
            return InteractionResultHolder.fail(stack);
        }

        targetLevel.getChunk(bound.pos().getX() >> 4, bound.pos().getZ() >> 4);
        if (!(targetLevel.getBlockEntity(bound.pos()) instanceof TerminalBlockEntity terminal))
        {
            // The Terminal was broken/replaced since binding - same "stillValid-style re-check on next
            // use" cleanup the plan calls for, since there's no reliable hook that fires the instant a
            // remote block is removed while nobody's nearby to see it happen.
            releaseTicket(targetLevel, bound.pos());
            clearBoundPos(stack);
            message(player, "The bound Terminal no longer exists - unbound.");
            return InteractionResultHolder.fail(stack);
        }

        terminal.openRemotely(player);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag)
    {
        GlobalPos bound = getBoundPos(stack);
        if (bound == null)
        {
            tooltip.add(Component.literal("Unbound").withStyle(ChatFormatting.GRAY));
            return;
        }
        BlockPos pos = bound.pos();
        tooltip.add(Component.literal("Bound: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                .withStyle(ChatFormatting.GRAY));
    }

    private static void requestTicket(ServerLevel level, BlockPos pos, boolean add)
    {
        ForgeChunkManager.forceChunk(level, ArcaneLens.MODID, pos, pos.getX() >> 4, pos.getZ() >> 4, add, true);
    }

    private static void releaseTicket(@Nullable ServerLevel level, BlockPos pos)
    {
        if (level != null)
        {
            requestTicket(level, pos, false);
        }
    }

    private static int countOtherBoundAnchors(ServerPlayer player, ItemStack excluding)
    {
        int count = 0;
        for (ItemStack stack : player.getInventory().items)
        {
            if (stack != excluding && stack.getItem() instanceof WarpedAnchorItem && getBoundPos(stack) != null)
            {
                count++;
            }
        }
        return count;
    }

    @Nullable
    private static GlobalPos getBoundPos(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_DIMENSION))
        {
            return null;
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString(TAG_DIMENSION)));
        int[] p = tag.getIntArray(TAG_POS);
        if (p.length != 3)
        {
            return null;
        }
        return GlobalPos.of(dimension, new BlockPos(p[0], p[1], p[2]));
    }

    private static void setBoundPos(ItemStack stack, GlobalPos pos)
    {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_DIMENSION, pos.dimension().location().toString());
        tag.putIntArray(TAG_POS, new int[]{pos.pos().getX(), pos.pos().getY(), pos.pos().getZ()});
    }

    private static void clearBoundPos(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag != null)
        {
            tag.remove(TAG_DIMENSION);
            tag.remove(TAG_POS);
        }
    }

    private void message(ServerPlayer player, String text)
    {
        message(player, ChatFormatting.YELLOW, text);
    }

    private void message(ServerPlayer player, ChatFormatting color, String text)
    {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }
}
