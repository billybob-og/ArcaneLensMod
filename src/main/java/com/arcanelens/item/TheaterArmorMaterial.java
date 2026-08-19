package com.arcanelens.item;

import com.arcanelens.ArcaneLens;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Deliberately zero defense on every slot - the mask's 10% dodge chance (see
 * TheaterHelmetPerkHandler) compensates for the lost armor instead, per the design brief. Only the
 * HELMET type is actually registered as an item (see ModItems.THEATER_HELMET), but ArmorMaterial
 * still requires a value for every type. */
public enum TheaterArmorMaterial implements ArmorMaterial
{
    INSTANCE;

    private static final int DURABILITY = 200;

    @Override
    public int getDurabilityForType(ArmorItem.Type type)
    {
        return DURABILITY;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type)
    {
        return 0;
    }

    @Override
    public int getEnchantmentValue()
    {
        return 10;
    }

    @Override
    public SoundEvent getEquipSound()
    {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return Ingredient.of(Items.PHANTOM_MEMBRANE);
    }

    @Override
    public String getName()
    {
        return ArcaneLens.MODID + ":theater";
    }

    @Override
    public float getToughness()
    {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance()
    {
        return 0.0F;
    }
}
