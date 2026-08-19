package com.arcanelens.item;

import com.arcanelens.ArcaneLens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

/**
 * A dedicated, always-obtainable copy of the Arcane Guide. Patchouli's own guide_book item only becomes a
 * specific book via an NBT tag baked in at craft time - a plain /give or a creative-tab pick of the raw
 * patchouli:guide_book item has no tag, so it silently fails to open and shows an "undefined" tooltip. This
 * is a plain Item (not a subclass of Patchouli's own book item class, which lives in an internal, non-
 * exported package) that hardcodes which book to open via the public PatchouliAPI, so a copy works
 * identically no matter how it was obtained - crafted (ModRecipeProvider, a plain shapeless recipe), /give,
 * or the creative tab.
 */
public class ArcaneGuideBookItem extends Item
{
    private static final ResourceLocation BOOK_ID = new ResourceLocation(ArcaneLens.MODID, "arcane_guide");

    public ArcaneGuideBookItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer)
        {
            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
