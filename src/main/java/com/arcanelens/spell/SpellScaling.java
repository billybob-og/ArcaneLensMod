package com.arcanelens.spell;

/** Duplicate-spell buffing formula: +25% effect / -10% cost per extra copy, capped at 3 copies. */
public class SpellScaling
{
    private static final int MAX_STACKS_COUNTED = 3;

    public static float effectMultiplier(int stackCount)
    {
        int capped = Math.min(Math.max(stackCount, 1), MAX_STACKS_COUNTED);
        return 1.0f + 0.25f * (capped - 1);
    }

    public static float costMultiplier(int stackCount)
    {
        int capped = Math.min(Math.max(stackCount, 1), MAX_STACKS_COUNTED);
        return 1.0f - 0.10f * (capped - 1);
    }

    /** Every {@code interval} duplicate copies beyond the first grants one additional cast (e.g. an extra projectile/bolt), capped at {@code max}. */
    public static int scaledCastCount(int stackCount, int interval, int max)
    {
        int extra = (Math.max(stackCount, 1) - 1) / interval;
        return Math.min(1 + extra, max);
    }
}
