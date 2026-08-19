package com.arcanelens.command;

import com.arcanelens.api.spell.Spell;
import com.arcanelens.api.spell.SpellRegistry;
import com.arcanelens.capability.FaithProvider;
import com.arcanelens.capability.FaithSync;
import com.arcanelens.capability.KnownSpellsProvider;
import com.arcanelens.capability.KnownSpellsSync;
import com.arcanelens.item.MagicLensItem;
import com.arcanelens.network.packet.ServerboundCycleSpellPacket;
import com.arcanelens.worldgen.HungerTowerPlacer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ArcaneLensCommand
{
    private static final SimpleCommandExceptionType UNKNOWN_SPELL =
            new SimpleCommandExceptionType(Component.literal("Unknown spell id."));
    private static final SimpleCommandExceptionType NO_LENS_HELD =
            new SimpleCommandExceptionType(Component.literal("Target isn't holding a Magic Lens."));

    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("arcanelens")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("learn")
                        .then(Commands.argument("spell", ResourceLocationArgument.id())
                                .suggests(ArcaneLensCommand::suggestSpells)
                                .executes(ctx -> learn(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> learn(ctx, EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("forget")
                        .then(Commands.argument("spell", ResourceLocationArgument.id())
                                .suggests(ArcaneLensCommand::suggestSpells)
                                .executes(ctx -> forget(ctx, ctx.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> forget(ctx, EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("learnall")
                        .executes(ctx -> learnAll(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> learnAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("forgetall")
                        .executes(ctx -> forgetAll(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> forgetAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("listknown")
                        .executes(ctx -> listKnown(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> listKnown(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("mana")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(ctx -> setMana(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setMana(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("maxmana")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(ctx -> setMaxMana(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> setMaxMana(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                .then(Commands.literal("faith")
                        .executes(ctx -> viewFaith(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> viewFaith(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> setFaith(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> setFaith(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                        .then(Commands.literal("defeats")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setDefeats(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                                .then(Commands.argument("player", EntityArgument.player())
                                                        .executes(ctx -> setDefeats(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")))))))
                        .then(Commands.literal("boons")
                                .then(Commands.literal("set")
                                        .then(Commands.literal("blessing")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> setBlessingBoon(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                                        .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(ctx -> setBlessingBoon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount"))))))
                                        .then(Commands.literal("burdenresist")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> setBurdenResistBoon(ctx.getSource(), ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "amount")))
                                                        .then(Commands.argument("player", EntityArgument.player())
                                                                .executes(ctx -> setBurdenResistBoon(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")))))))))
                .then(Commands.literal("hungertower")
                        .then(Commands.literal("place")
                                .executes(ctx -> placeHungerTower(ctx.getSource())))));
    }

    private static CompletableFuture<Suggestions> suggestSpells(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder)
    {
        for (var entry : SpellRegistry.REGISTRY.get().getEntries())
        {
            builder.suggest(entry.getKey().location().toString());
        }
        return builder.buildFuture();
    }

    private static ResourceLocation resolveSpellId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException
    {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "spell");
        if (SpellRegistry.REGISTRY.get().getValue(id) == null)
        {
            throw UNKNOWN_SPELL.create();
        }
        return id;
    }

    private static int learn(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException
    {
        ResourceLocation spellId = resolveSpellId(ctx);
        Spell spell = SpellRegistry.REGISTRY.get().getValue(spellId);
        target.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> cap.learn(spellId));
        KnownSpellsSync.syncToClient(target);

        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " learned ")
                .append(spell.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return 1;
    }

    private static int forget(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException
    {
        ResourceLocation spellId = resolveSpellId(ctx);
        target.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> cap.getKnownSpells().remove(spellId));
        KnownSpellsSync.syncToClient(target);

        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " forgot " + spellId), true);
        return 1;
    }

    private static int learnAll(CommandSourceStack source, ServerPlayer target)
    {
        target.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap ->
                SpellRegistry.REGISTRY.get().getEntries().forEach(entry -> cap.learn(entry.getKey().location())));
        KnownSpellsSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " learned all spells"), true);
        return 1;
    }

    private static int forgetAll(CommandSourceStack source, ServerPlayer target)
    {
        target.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> cap.getKnownSpells().clear());
        KnownSpellsSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " forgot all spells"), true);
        return 1;
    }

    private static int listKnown(CommandSourceStack source, ServerPlayer target)
    {
        target.getCapability(KnownSpellsProvider.CAPABILITY).ifPresent(cap -> {
            String list = cap.getKnownSpells().isEmpty() ? "(none)" : cap.getKnownSpells().stream()
                    .map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "));
            source.sendSuccess(() -> Component.literal(target.getName().getString() + " knows: " + list), false);
        });
        return 1;
    }

    private static int setMana(CommandSourceStack source, ServerPlayer target, int amount) throws CommandSyntaxException
    {
        ItemStack lens = ServerboundCycleSpellPacket.findHeldLens(target);
        if (lens.isEmpty())
        {
            throw NO_LENS_HELD.create();
        }
        MagicLensItem.setMana(lens, amount);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s lens mana set to "
                + MagicLensItem.getMana(lens) + "/" + MagicLensItem.getMaxMana(lens)), true);
        return 1;
    }

    private static int setMaxMana(CommandSourceStack source, ServerPlayer target, int amount) throws CommandSyntaxException
    {
        ItemStack lens = ServerboundCycleSpellPacket.findHeldLens(target);
        if (lens.isEmpty())
        {
            throw NO_LENS_HELD.create();
        }
        MagicLensItem.setMaxMana(lens, amount);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s lens max mana set to "
                + MagicLensItem.getMaxMana(lens) + " (current mana: " + MagicLensItem.getMana(lens) + ")"), true);
        return 1;
    }

    private static int viewFaith(CommandSourceStack source, ServerPlayer target)
    {
        target.getCapability(FaithProvider.CAPABILITY).ifPresent(cap ->
                source.sendSuccess(() -> Component.literal(target.getName().getString() + " has " + cap.getFaith() + " faith, has defeated "
                        + "the sleeping god " + cap.getSleepingGodDefeats() + " time(s), eyes revealed: " + cap.isHungerEyesRevealed()
                        + ", blessing boon: " + cap.getBlessingBoonLevel() + ", burden-resist boon: " + cap.getBurdenResistBoonLevel()), false));
        return 1;
    }

    private static int setFaith(CommandSourceStack source, ServerPlayer target, int amount)
    {
        target.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.setFaith(amount));
        FaithSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s faith set to " + amount), true);
        return 1;
    }

    private static int setDefeats(CommandSourceStack source, ServerPlayer target, int amount)
    {
        target.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.setSleepingGodDefeats(amount));
        FaithSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s sleeping god defeats set to " + amount), true);
        return 1;
    }

    private static int setBlessingBoon(CommandSourceStack source, ServerPlayer target, int amount)
    {
        target.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.setBlessingBoonLevel(amount));
        FaithSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s blessing boon level set to " + amount), true);
        return 1;
    }

    private static int setBurdenResistBoon(CommandSourceStack source, ServerPlayer target, int amount)
    {
        target.getCapability(FaithProvider.CAPABILITY).ifPresent(cap -> cap.setBurdenResistBoonLevel(amount));
        FaithSync.syncToClient(target);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s burden-resist boon level set to " + amount), true);
        return 1;
    }

    private static int placeHungerTower(CommandSourceStack source)
    {
        ServerLevel level = source.getLevel();
        HungerTowerPlacer.forcePlace(level);
        source.sendSuccess(() -> Component.literal("The Hunger Tower has been (re)placed."), true);
        return 1;
    }
}
