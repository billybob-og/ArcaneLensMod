package com.arcanelens.block.entity;

import com.arcanelens.Config;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.registry.ModBlockEntities;
import com.arcanelens.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class FaithAltarBlockEntity extends BlockEntity
{
    // Per-item cumulative burned count, kept per-altar (not per-player) so a single shrine can't be
    // exploited by spreading a bulk item across multiple accounts - see Config.faithDiminishStacks.
    private final Map<Item, Integer> burnedCounts = new HashMap<>();
    private int scanTimer;

    public FaithAltarBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FAITH_ALTAR.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        scanTimer++;
        if (scanTimer < Config.faithScanIntervalTicks)
        {
            return;
        }
        scanTimer = 0;

        AABB scanBox = new AABB(pos).inflate(Config.faithScanRadius);

        // Whether there's any soul fire/lit soul campfire anywhere near this altar at all - checked once per
        // scan rather than in a tight box around each individual item. A tight per-item box (the original
        // approach) routinely missed items that vanilla itself had already set on fire: several items tossed
        // in together scatter on impact and can land a couple of blocks from the flame while still catching
        // fire (vanilla's own contact detection is more generous than a fixed per-item box), so an item could
        // visibly burn up without ever registering as "touching" soul fire by our own narrower math. Trusting
        // vanilla's own itemEntity.isOnFire() flag - true exactly when it's actually receiving fire damage -
        // combined with "is there soul fire anywhere in this altar's play area" is both simpler and far more
        // forgiving of scatter, without misattributing a fire from somewhere else entirely (the whole point of
        // the original position check).
        boolean soulFireNearAltar = hasSoulFireNearby(serverLevel, scanBox);

        for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, scanBox))
        {
            if (!itemEntity.isAlive() || !itemEntity.isOnFire() || !soulFireNearAltar)
            {
                continue;
            }

            if (!(itemEntity.getOwner() instanceof ServerPlayer player))
            {
                // No known player to credit (e.g. dropped by a non-player source) - let it burn out normally.
                continue;
            }

            ItemStack burning = itemEntity.getItem();

            // The Hunger's Bindings is handled by HungersBindingsHandler instead - it just needs to be burned
            // by any fire anywhere, not tied to a Faith Altar or soul fire specifically, so it's deliberately
            // not part of this altar's own burn detection.
            if (burning.is(ModItems.HUNGERS_BINDINGS.get()))
            {
                continue;
            }

            serverLevel.sendParticles(ParticleTypes.SOUL,
                    itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(),
                    12, 0.25, 0.35, 0.25, 0.02);
            serverLevel.playSound(null, itemEntity.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 0.6f, 1.0f);

            int faith = computeFaith(burning);
            itemEntity.discard();

            if (faith > 0)
            {
                player.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.addFaith(faith));
                FaithSync.syncToClient(player);
            }
        }
    }

    /** Scans the altar's whole play area once per tick (rather than a tight box per item) for any soul fire
     * or lit soul campfire - see the tick() comment for why this replaced a narrower per-item position check. */
    private boolean hasSoulFireNearby(ServerLevel level, AABB scanBox)
    {
        for (BlockPos check : BlockPos.betweenClosed(
                new BlockPos((int) Math.floor(scanBox.minX), (int) Math.floor(scanBox.minY), (int) Math.floor(scanBox.minZ)),
                new BlockPos((int) Math.floor(scanBox.maxX), (int) Math.floor(scanBox.maxY), (int) Math.floor(scanBox.maxZ))))
        {
            BlockState state = level.getBlockState(check);
            if (state.is(Blocks.SOUL_FIRE))
            {
                return true;
            }
            if (state.is(Blocks.SOUL_CAMPFIRE) && state.getValue(CampfireBlock.LIT))
            {
                return true;
            }
        }
        return false;
    }

    /** Faith per rarity, scaled by count, halving every Config.getDiminishStacksForRarity(rarity) stacks of
     * that exact item already burned through this altar - discourages bulk-farming a single cheap item for
     * Faith, while letting rarer (naturally scarcer) items tolerate more stacks before decaying. */
    private int computeFaith(ItemStack stack)
    {
        Item item = stack.getItem();
        int count = stack.getCount();
        int stackSize = Math.max(1, stack.getMaxStackSize());
        int priorBurned = burnedCounts.getOrDefault(item, 0);

        int diminishStacks = Config.getDiminishStacksForRarity(stack.getRarity());
        int tier = priorBurned / (diminishStacks * stackSize);
        double multiplier = Math.pow(0.5, tier);

        int faithPerItem = Config.getFaithForRarity(stack.getRarity());
        int faith = (int) Math.floor(faithPerItem * count * multiplier);

        burnedCounts.put(item, priorBurned + count);
        setChanged();
        return faith;
    }

    @Override
    protected void saveAdditional(CompoundTag tag)
    {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        burnedCounts.forEach((item, count) -> {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null)
            {
                CompoundTag entry = new CompoundTag();
                entry.putString("Item", id.toString());
                entry.putInt("Count", count);
                list.add(entry);
            }
        });
        tag.put("BurnedCounts", list);
    }

    @Override
    public void load(CompoundTag tag)
    {
        super.load(tag);
        burnedCounts.clear();
        ListTag list = tag.getList("BurnedCounts", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            CompoundTag entry = list.getCompound(i);
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getString("Item")));
            if (item != null)
            {
                burnedCounts.put(item, entry.getInt("Count"));
            }
        }
    }
}
