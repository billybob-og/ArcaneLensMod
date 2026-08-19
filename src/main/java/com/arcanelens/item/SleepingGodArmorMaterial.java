package com.arcanelens.item;

import com.arcanelens.ArcaneLens;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.Map;

/** Durability is a flat 2000 per piece for every slot - well above Netherite's (max 592, on the chestplate) -
 * and the set also grants creative-style flight (see the Faith flight system). Defense/toughness are dialed
 * back below Diamond to compensate for those two advantages: total defense sits between Gold and Iron, and
 * toughness is dropped entirely, so big hits still sting despite the armor being nearly unbreakable. The
 * repair ingredient (Warped Fungus) and texture namespace also differ from vanilla materials. Name is
 * "arcanelens:sleeping_god" so Forge's getArmorResource hook resolves the texture to
 * assets/arcanelens/textures/models/armor/sleeping_god_layer_1.png / _layer_2.png. */
public enum SleepingGodArmorMaterial implements ArmorMaterial
{
    INSTANCE;

    private static final int DURABILITY = 2000;

    private static final Map<ArmorItem.Type, Integer> DEFENSE_FOR_TYPE = new EnumMap<>(ArmorItem.Type.class);

    static
    {
        DEFENSE_FOR_TYPE.put(ArmorItem.Type.BOOTS, 2);
        DEFENSE_FOR_TYPE.put(ArmorItem.Type.LEGGINGS, 4);
        DEFENSE_FOR_TYPE.put(ArmorItem.Type.CHESTPLATE, 5);
        DEFENSE_FOR_TYPE.put(ArmorItem.Type.HELMET, 2);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type)
    {
        return DURABILITY;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type)
    {
        return DEFENSE_FOR_TYPE.get(type);
    }

    @Override
    public int getEnchantmentValue()
    {
        return 10;
    }

    @Override
    public SoundEvent getEquipSound()
    {
        return SoundEvents.ARMOR_EQUIP_DIAMOND;
    }

    @Override
    public Ingredient getRepairIngredient()
    {
        return Ingredient.of(Items.WARPED_FUNGUS);
    }

    @Override
    public String getName()
    {
        return ArcaneLens.MODID + ":sleeping_god";
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
