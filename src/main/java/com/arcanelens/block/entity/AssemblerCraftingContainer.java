package com.arcanelens.block.entity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A real 3x3 CraftingContainer view over the block entity's grid slot range, so
 * RecipeManager.getRecipeFor(RecipeType.CRAFTING, ...) matches real shaped recipes correctly. Deliberately
 * hand-rolled rather than using Forge's net.minecraftforge.items.wrapper.RecipeWrapper, which reports
 * getWidth()=slotCount/getHeight()=1 (built for single-row recipe types like smelting) and would silently
 * break ShapedRecipe row/column matching for anything with real 2D shape.
 */
public class AssemblerCraftingContainer implements CraftingContainer
{
    private final IItemHandlerModifiable handler;
    private final int slotOffset;

    public AssemblerCraftingContainer(IItemHandlerModifiable handler, int slotOffset)
    {
        this.handler = handler;
        this.slotOffset = slotOffset;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public int getHeight()
    {
        return 3;
    }

    @NotNull
    @Override
    public List<ItemStack> getItems()
    {
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = 0; i < 9; i++)
        {
            items.add(handler.getStackInSlot(slotOffset + i));
        }
        return items;
    }

    @Override
    public int getContainerSize()
    {
        return 9;
    }

    @Override
    public boolean isEmpty()
    {
        for (int i = 0; i < 9; i++)
        {
            if (!handler.getStackInSlot(slotOffset + i).isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem(int slot)
    {
        return handler.getStackInSlot(slotOffset + slot);
    }

    @NotNull
    @Override
    public ItemStack removeItem(int slot, int amount)
    {
        return handler.extractItem(slotOffset + slot, amount, false);
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int slot)
    {
        ItemStack stack = handler.getStackInSlot(slotOffset + slot);
        handler.setStackInSlot(slotOffset + slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack)
    {
        handler.setStackInSlot(slotOffset + slot, stack);
    }

    @Override
    public void setChanged()
    {
        // No-op: the backing ItemStackHandler already tracks its own dirty state via
        // onContentsChanged on set/extract - this container is just a view over it, not the source of truth.
    }

    @Override
    public boolean stillValid(Player player)
    {
        return true;
    }

    @Override
    public void clearContent()
    {
        for (int i = 0; i < 9; i++)
        {
            handler.setStackInSlot(slotOffset + i, ItemStack.EMPTY);
        }
    }

    @Override
    public void fillStackedContents(StackedContents contents)
    {
        for (int i = 0; i < 9; i++)
        {
            contents.accountStack(handler.getStackInSlot(slotOffset + i));
        }
    }
}
