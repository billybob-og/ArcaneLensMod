package com.arcanelens.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings
{
    public static final String CATEGORY = "key.categories.arcanelens";

    public static final KeyMapping CAST_SPELL = new KeyMapping(
            "key.arcanelens.cast_spell", KeyConflictContext.IN_GAME, InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE, CATEGORY);

    public static final KeyMapping CYCLE_SPELL = new KeyMapping(
            "key.arcanelens.cycle_spell", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R, CATEGORY);
}
