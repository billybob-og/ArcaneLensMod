package com.arcanelens.client.renderer;

import com.arcanelens.ArcaneLens;
import com.arcanelens.registry.ModItems;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Draws the Vessel-Bound Elytra chestplate's wings with vanilla's own elytra model and our texture.
 * Vanilla's ElytraLayer only accepts the actual Elytra item, so this is a second instance of it that
 * accepts ours instead - added to the player renderers in ClientModEvents. */
public class VesselBoundElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M>
{
    private static final ResourceLocation WINGS =
            new ResourceLocation(ArcaneLens.MODID, "textures/entity/vessel_bound_elytra.png");

    public VesselBoundElytraLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet)
    {
        super(parent, modelSet);
    }

    @Override
    public boolean shouldRender(ItemStack stack, T entity)
    {
        return stack.is(ModItems.VESSEL_BOUND_ELYTRA_CHESTPLATE.get());
    }

    @Override
    public ResourceLocation getElytraTexture(ItemStack stack, T entity)
    {
        return WINGS;
    }
}
