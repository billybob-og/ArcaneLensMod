package com.arcanelens.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Built on the *same* SleepingGodArmorMaterial as the original (same defense/toughness/durability - this is
 * an attunement, not a stat upgrade), so the equipped-body texture needs a per-item override rather than
 * relying on the material's own getName() (which would otherwise always resolve back to
 * sleeping_god_layer_1/2.png, since that lookup is material-based, not item-based). Forge's
 * IForgeItem.getArmorTexture hook is exactly the intended escape hatch for this - reuse the material/stats,
 * override just the rendered texture.
 */
public class VesselBoundArmorItem extends FireResistantArmorItem
{
    public VesselBoundArmorItem(Type type, Properties properties)
    {
        super(SleepingGodArmorMaterial.INSTANCE, type, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
    {
        String layer = slot == EquipmentSlot.LEGS ? "2" : "1";
        String suffix = type == null ? "" : "_" + type;
        return "arcanelens:textures/models/armor/vessel_bound_sleeping_god_layer_" + layer + suffix + ".png";
    }
}
