package com.arcanelens.block;

import com.arcanelens.registry.ModBlocks;
import com.arcanelens.worldgen.ModDimensions;
import com.arcanelens.worldgen.OverloadPortalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The walkable interior tile of a completed Overload Ritual portal frame (see
 * OverloadCoreBlockEntity.completeRitual) - non-collidable so entities can walk into it, and unbreakable by
 * normal means (see registration - matches vanilla portal blocks not being minable), so a pair can't be
 * accidentally half-destroyed. Any entity walking into either leg teleports to the other.
 *
 * <p>Entity.setPortalCooldown()/isOnPortalCooldown() alone isn't enough here: the destination tile is
 * always exactly the far side's own portal block, dead-centered with zero displacement (no relative-offset
 * placement like vanilla Nether portals do), so an entity that doesn't move away lands right back inside a
 * portal block. Vanilla's own cooldown is time-based, not position-based - it expires whether or not the
 * entity ever left, which re-armed entityInside while the entity was still standing in the destination
 * portal and bounced it straight back (confirmed in testing: rapid back-and-forth teleporting). IN_TRANSIT
 * (a persistent-data flag, not the vanilla cooldown) fixes this properly: it blocks re-triggering
 * unconditionally until onPlayerTick confirms the player has actually left every portal block, regardless
 * of how long that takes.</p>
 *
 * <p>The 2-tall frame interior is 2 instances of this same block, distinguished by UPPER - the author's
 * portal art is a single asymmetric image per animation frame (16 wide x 32 tall, top half distinct from
 * bottom half, not a repeating tile), so each half needs its own texture/model - see
 * ModBlockStateProvider and OverloadCoreBlockEntity.buildPortalFrame, which sets this property.</p>
 */
public class OverloadPortalBlock extends Block
{
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");

    private static final String IN_TRANSIT_KEY = "arcanelens_overload_portal_transit";

    public OverloadPortalBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(UPPER);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity)
    {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)
                || entity.isOnPortalCooldown() || entity.getPersistentData().getBoolean(IN_TRANSIT_KEY))
        {
            return;
        }
        if (!entity.canChangeDimensions())
        {
            return;
        }

        ServerLevel brokenVesselLevel = serverLevel.getServer().getLevel(ModDimensions.BROKEN_VESSEL_KEY);
        if (brokenVesselLevel == null)
        {
            // Same defensive shape as PocketDimensionSpell - a malformed/missing dimension registration
            // should never crash the game, just silently no-op the teleport.
            return;
        }

        GlobalPos here = GlobalPos.of(serverLevel.dimension(), pos);
        GlobalPos destination = OverloadPortalSavedData.get(brokenVesselLevel).getPairedDestination(here);
        if (destination == null)
        {
            // Pair desynced (a leg was removed by an explosion/another mod despite being nominally
            // unbreakable) - no-op rather than crash on a missing destination.
            return;
        }

        ServerLevel destLevel = serverLevel.getServer().getLevel(destination.dimension());
        if (destLevel == null)
        {
            return;
        }

        BlockPos destPos = destination.pos();
        entity.teleportTo(destLevel, destPos.getX() + 0.5, destPos.getY(), destPos.getZ() + 0.5,
                Set.<RelativeMovement>of(), entity.getYRot(), entity.getXRot());
        entity.setPortalCooldown();
        entity.getPersistentData().putBoolean(IN_TRANSIT_KEY, true);

        // teleportTo doesn't reset fall state on its own - an entity crossing while already falling (e.g.
        // jumped into the origin leg mid-fall) otherwise lands on the other side still carrying that fall
        // distance/downward velocity and can take damage or die on arrival, confirmed in testing. Vanilla
        // Nether portals avoid this because the entity is already grounded/stationary by the time the
        // multi-tick portal-entering delay finishes; this portal teleports instantly on contact, so it has
        // to clear fall state explicitly instead.
        entity.resetFallDistance();
        entity.setDeltaMovement(entity.getDeltaMovement().x, 0.0, entity.getDeltaMovement().z);
    }

    /** Clears IN_TRANSIT once a player has genuinely stepped out of both portal tiles (checked at foot and
     * head level, since the frame is 2 tall) - the only thing that re-arms entityInside after a teleport.
     * Players only; other entities (items pushed through, etc.) have nothing ticking their persistent data
     * back to false, but nothing else in this dimension currently needs a non-player to re-use a portal
     * repeatedly, so that's an acceptable gap rather than a real one. */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !(event.player instanceof ServerPlayer player))
        {
            return;
        }

        if (!player.getPersistentData().getBoolean(IN_TRANSIT_KEY))
        {
            return;
        }

        Level level = player.level();
        BlockPos feet = player.blockPosition();
        boolean stillInPortal = level.getBlockState(feet).is(ModBlocks.OVERLOAD_PORTAL.get())
                || level.getBlockState(feet.above()).is(ModBlocks.OVERLOAD_PORTAL.get());
        if (!stillInPortal)
        {
            player.getPersistentData().putBoolean(IN_TRANSIT_KEY, false);
        }
    }
}
