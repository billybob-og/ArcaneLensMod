package com.arcanelens.block.entity;

/** How a CommandTriggerBlockEntity responds to a player touching it. */
public enum CommandTriggerMode
{
    NORMAL,
    REPEATING,
    TOGGLE,
    CYCLE,
    DELETE_AFTER_FIRING
}
