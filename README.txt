Arcane Lens
===========

A Minecraft Forge 1.20.1 mod centered on the Magic Lens: an inscribable item
that stores up to 5 spells (expandable through combining), powered by mana
you charge at soul-fire pedestals.

Features
--------
- Magic Lens item: assign up to 5 spells at the Inscription Workbench,
  cycle between them with the scroll wheel, and cast with a keybind.
- Soul Pedestal + Lens Pedestal: burn soul sand to convert items into mana,
  which links (chunk-scan based, no redstone/wiring needed) to nearby Lens
  Pedestals to charge a held lens.
- 5 starter spells: Fireball, Heal, Frost Bolt, Blink, and Lightning Strike.
  Learn them permanently from Ancient Spell Books.
- Arcane Ore (diamond-tier tools only) and the Lens Combiner: merge two
  lenses into one, accumulating locked spells and scaling mana capacity,
  up to 5 total combines per lens lineage.
- A hand-built surface library connected via jigsaw to a generated
  underground dungeon (2-12 rooms), with loot chests holding Ancient
  Spell Books and other rewards.
- 8 advancements tracking progress through the above systems.

Requirements
------------
- Minecraft 1.20.1
- Minecraft Forge 47.4.21 or later

Building from source
---------------------
This is a standard Forge MDK-based project. See the Forge documentation at
https://docs.minecraftforge.net/en/1.20.1/gettingstarted/ for IDE setup.
Common Gradle tasks:
    ./gradlew build      - build the mod jar
    ./gradlew runClient  - launch a client with the mod loaded
    ./gradlew runData    - regenerate datagen output (recipes, loot tables,
                            models, worldgen registries, language files)

License
-------
See LICENSE (MIT) for the mod's own source code. LICENSE.txt covers the
bundled Minecraft Forge/FML code, licensed separately under LGPL 2.1.
