package com.arcanelens.entity.boss;

/** What the Florian boss and his temporary Antler summon (FlorianCompanionEntity) share - both run the same
 * gore/charge moveset (FlorianGoreGoal/FlorianChargeGoal) and the same model animation, just with the
 * companion's numbers turned down, so those read this instead of a concrete entity class. */
public interface FlorianFighter
{
    enum Action
    {
        NONE, GORE, CHARGE
    }

    Action getFlorianAction();

    void setFlorianAction(Action action, int durationTicks);

    int getActionEndTick();

    int goreCooldownTicks();

    int chargeCooldownTicks();

    double approachSpeed();
}
