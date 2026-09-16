package com.arcanelens.endlessdungeon.dungeon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a room template's jigsaw blocks once and caches the result for the server's lifetime (templates are
 * static assets - the same room's connectors never change between reads). Uses filterBlocks(pos, settings,
 * Blocks.JIGSAW) rather than IncrementalStructurePlacer's full palette read, since that call needs a specific
 * Block to match against and jigsaw connectors are exactly that: one specific block type, not "every block."
 *
 * <p>ORIENTATION decodes to a FrontAndTop, not a plain 6-way Direction - front() is the face the connector
 * opens toward, top() is needed for ALIGNED joints (ROLLABLE joints can freely rotate around front, ALIGNED
 * ones can't). Getting only front() right still visually "attaches" two rooms but can leave an ALIGNED seam
 * rolled/upside-down - this needs a dedicated two-piece placement test, not just a glance, before trusting it
 * against the shared persistent world (see the plan's open risks).</p>
 */
public class ConnectorScanner
{
    public record ConnectorInfo(BlockPos localPos, Direction front, Direction top, ResourceLocation name,
                                 ResourceLocation target, ResourceLocation pool, String joint, String finalState)
    {
    }

    public record TemplateConnectors(ResourceLocation templateId, List<ConnectorInfo> connectors)
    {
    }

    private static final Map<ResourceLocation, TemplateConnectors> CACHE = new HashMap<>();

    public static TemplateConnectors scan(ServerLevel level, ResourceLocation templateId)
    {
        TemplateConnectors cached = CACHE.get(templateId);
        if (cached != null)
        {
            return cached;
        }

        StructureTemplateManager manager = level.getStructureManager();
        StructureTemplate template = manager.getOrCreate(templateId);

        List<StructureTemplate.StructureBlockInfo> jigsaws =
                template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.JIGSAW);

        List<ConnectorInfo> connectors = new ArrayList<>(jigsaws.size());
        for (StructureTemplate.StructureBlockInfo info : jigsaws)
        {
            CompoundTag nbt = info.nbt();
            if (nbt == null)
            {
                continue;
            }

            FrontAndTop orientation = info.state().getValue(JigsawBlock.ORIENTATION);
            connectors.add(new ConnectorInfo(
                    info.pos(),
                    orientation.front(),
                    orientation.top(),
                    new ResourceLocation(nbt.getString("name")),
                    new ResourceLocation(nbt.getString("target")),
                    new ResourceLocation(nbt.getString("pool")),
                    nbt.getString("joint"),
                    nbt.getString("final_state")));
        }

        TemplateConnectors result = new TemplateConnectors(templateId, List.copyOf(connectors));
        CACHE.put(templateId, result);
        return result;
    }
}
