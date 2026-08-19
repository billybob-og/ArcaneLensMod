package com.arcanelens.client.screen;

import com.arcanelens.block.entity.CommandTriggerBlockEntity;
import com.arcanelens.block.entity.CommandTriggerMode;
import com.arcanelens.menu.CommandTriggerMenu;
import com.arcanelens.network.NetworkHandler;
import com.arcanelens.network.packet.ServerboundSetCommandTriggerConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class CommandTriggerScreen extends AbstractContainerScreen<CommandTriggerMenu>
{
    private final List<EditBox> commandBoxes = new ArrayList<>();
    private EditBox intervalBox;
    private CommandTriggerMode mode;
    private Button modeButton;

    public CommandTriggerScreen(CommandTriggerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 210;
        this.mode = menu.getBlockEntity().getMode();
    }

    @Override
    protected void init()
    {
        super.init();
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;

        CommandTriggerBlockEntity blockEntity = this.menu.getBlockEntity();
        List<String> existingCommands = blockEntity.getCommands();

        commandBoxes.clear();
        for (int i = 0; i < CommandTriggerBlockEntity.MAX_CYCLE_SLOTS; i++)
        {
            EditBox box = new EditBox(this.font, left + 10, top + 10 + i * 22, 180, 18,
                    Component.literal("Command " + (i + 1)));
            box.setMaxLength(256);
            box.setValue(i < existingCommands.size() ? existingCommands.get(i) : "");
            this.addRenderableWidget(box);
            commandBoxes.add(box);
        }

        intervalBox = new EditBox(this.font, left + 10, top + 100, 60, 18, Component.literal("Interval"));
        intervalBox.setMaxLength(6);
        intervalBox.setValue(Integer.toString(blockEntity.getRepeatIntervalTicks()));
        this.addRenderableWidget(intervalBox);

        modeButton = Button.builder(Component.literal("Mode: " + mode.name()), b -> cycleMode())
                .bounds(left + 10, top + 128, 180, 20).build();
        this.addRenderableWidget(modeButton);

        this.addRenderableWidget(Button.builder(Component.literal("Done (Esc also saves)"), b -> this.onClose())
                .bounds(left + 10, top + 160, 180, 20).build());
    }

    private void cycleMode()
    {
        CommandTriggerMode[] values = CommandTriggerMode.values();
        mode = values[(mode.ordinal() + 1) % values.length];
        modeButton.setMessage(Component.literal("Mode: " + mode.name()));
    }

    // The only way out of this screen is here - Esc (Screen's own default keyPressed handling) and the
    // Done button both funnel into onClose(), so there's no distinct "cancel without saving" path.
    @Override
    public void onClose()
    {
        List<String> commands = new ArrayList<>();
        for (EditBox box : commandBoxes)
        {
            commands.add(box.getValue());
        }
        int interval;
        try
        {
            interval = Math.max(1, Integer.parseInt(intervalBox.getValue().trim()));
        }
        catch (NumberFormatException e)
        {
            interval = 20;
        }
        NetworkHandler.CHANNEL.sendToServer(new ServerboundSetCommandTriggerConfigPacket(commands, mode.name(), interval));
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        // AbstractContainerScreen normally treats the inventory key (default 'E') as "close this screen" -
        // fine for plain item-slot containers, but this screen has no slots and needs 'e' to be typeable in
        // its text fields. Let it fall through to the focused EditBox's own char-typing instead of closing.
        if (this.getFocused() instanceof EditBox editBox && editBox.canConsumeInput()
                && Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode))
        {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // Default AbstractContainerScreen behavior draws a "Inventory" label positioned for a real player
        // inventory grid - this menu has no slots at all, so that label has nowhere sensible to sit and
        // just overlaps the widgets above. Skip it entirely (the screen's title bar isn't needed either,
        // since the GUI itself is self-explanatory).
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF8B7355);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(graphics, mouseX, mouseY, partialTick);

        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.drawString(this.font, "Interval (ticks, REPEATING only):", left + 10, top + 90, 0xFFFFFF, true);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
