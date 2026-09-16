package com.arcanelens.registry;

import com.arcanelens.ArcaneLens;
import com.arcanelens.menu.ArcaneAssemblerMenu;
import com.arcanelens.menu.CommandTriggerMenu;
import com.arcanelens.menu.GodChallengeMenu;
import com.arcanelens.menu.GodPledgeMenu;
import com.arcanelens.menu.InscriptionWorkbenchMenu;
import com.arcanelens.menu.LensCombinerMenu;
import com.arcanelens.menu.SkillTreeMenu;
import com.arcanelens.menu.TerminalMenu;
import com.arcanelens.menu.TokenOfTheHungerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes
{
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ArcaneLens.MODID);

    public static final RegistryObject<MenuType<InscriptionWorkbenchMenu>> INSCRIPTION_WORKBENCH = MENUS.register(
            "inscription_workbench",
            () -> IForgeMenuType.create(InscriptionWorkbenchMenu::new));

    public static final RegistryObject<MenuType<LensCombinerMenu>> LENS_COMBINER = MENUS.register(
            "lens_combiner",
            () -> IForgeMenuType.create(LensCombinerMenu::new));

    public static final RegistryObject<MenuType<CommandTriggerMenu>> COMMAND_TRIGGER = MENUS.register(
            "command_trigger",
            () -> IForgeMenuType.create(CommandTriggerMenu::new));

    public static final RegistryObject<MenuType<TokenOfTheHungerMenu>> TOKEN_OF_THE_HUNGER = MENUS.register(
            "token_of_the_hunger",
            () -> IForgeMenuType.create(TokenOfTheHungerMenu::new));

    public static final RegistryObject<MenuType<SkillTreeMenu>> SKILL_TREE = MENUS.register(
            "skill_tree",
            () -> IForgeMenuType.create(SkillTreeMenu::new));

    public static final RegistryObject<MenuType<TerminalMenu>> TERMINAL = MENUS.register(
            "terminal",
            () -> IForgeMenuType.create(TerminalMenu::new));

    public static final RegistryObject<MenuType<ArcaneAssemblerMenu>> ARCANE_ASSEMBLER = MENUS.register(
            "arcane_assembler",
            () -> IForgeMenuType.create(ArcaneAssemblerMenu::new));

    public static final RegistryObject<MenuType<GodPledgeMenu>> GOD_PLEDGE = MENUS.register(
            "god_pledge",
            () -> IForgeMenuType.create(GodPledgeMenu::new));

    public static final RegistryObject<MenuType<GodChallengeMenu>> GOD_CHALLENGE = MENUS.register(
            "god_challenge",
            () -> IForgeMenuType.create(GodChallengeMenu::new));
}
