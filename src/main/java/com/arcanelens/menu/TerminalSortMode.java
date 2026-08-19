package com.arcanelens.menu;

/**
 * How TerminalMenu.getVisiblePoolIndices() orders the occupied portion of the pool view. DEFAULT is the
 * pre-existing "occupied slots in underlying pool order" behavior (see TerminalMenu javadoc on why that
 * ordering exists at all - hopper-fed items surfacing at the front regardless of which physical Storage
 * Block they land in). NAME/COUNT are additional orderings layered on top of that same occupied/empty
 * split; empty slots are never sorted, they always trail last exactly as before.
 */
public enum TerminalSortMode
{
    DEFAULT("Default"),
    NAME("Name"),
    COUNT("Count");

    private final String label;

    TerminalSortMode(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    public TerminalSortMode next()
    {
        TerminalSortMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
