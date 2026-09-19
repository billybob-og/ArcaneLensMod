A spellcasting mod built around a Magic Lens: learn spells, explore an old library's dungeon, kill a sleeping god for its power, and earn the favor (or attention) of something that's begun to watch you.

<details>
<summary>Spoiler</summary>

<details>
  <summary>V1-4</summary>

Core spellcasting

- Magic Lens — the central item; equip up to 5 spells at once, cast/cycle with keybinds
- Mana system with a capacity that grows through merging lenses (up to 5x merge tier)
- 17 unique spells across offense, utility, and support: Fireball, Frost Bolt, Lightning Strike, Chain Lightning, Meteor Shower, Poison Bolt, Life Drain, Shockwave, Arrow Volley, Heal, Blink, Arcane Shield, Haste Surge, Gravity Pull, Summon Ally, Invisibility, Pocket Dimension
- Ancient Spell Books (dungeon loot) to learn new spells
- Diamond Engraver + Inscription Workbench for duplicating/customizing a lens's spell loadout
- Lens Combiner to merge two lenses into one, raising max mana and slot count

Mana infrastructure

- Soul Pedestal — burns soul sand + items to generate mana
- Lens Pedestal — charges a placed lens automatically, links to nearby Soul Pedestals
- Soul Feeder — auto-feeds items into a linked Soul Pedestal, no manual restocking

Exploration

- A library with an entrance to a multi-room dungeon with tunnels, treasure rooms, and dangers
- A Faith Shrine structure, separate from the main dungeon
- A sunken boss chamber holding The One Who Sleeps

The boss fight

- The One Who Sleeps — a boss with its own arena
- Sleeping God armor set (fire-immune) that grants temporary flight, scaled by accumulated Faith

The Faith economy

- Faith Altars — burn items in soul fire for Faith, with diminishing returns to discourage bulk-farming one item
- Weird Amulet — private Faith readout on right-click

The Hunger (post-boss meta-progression)

- A watching presence that starts after defeating the boss, scaling with Faith and repeat kills
- Random blessing/burden moments with visible apparitions and status effects
- A semi-permanent one-way "eyes revealed" milestone that changes the sun and moon's appearance (per-player, not server-wide)
- Sun and Moon Ward — reversible toggle to hide the changed sky
- Tokens of the Hunger, spent on a choice between The Hunger's Boon (extends blessing duration) or The Hunger's Bindings (burn to shift blessing/burden odds, with bonus effects at high levels)
- The Hunger Tower — a landmark that rises once the eyes are revealed at 1.1k faith after killing the one who sleeps, with a permanent idol on top offering cryptic teaser dialogue

Skill Tree & Arcane Ink

- The Hunger Idol atop the Hunger Tower trades Tokens of the Hunger, then Arcane Ink, for The Hunger's Pact — a reusable key that opens the Skill Tree screen
- Arcane Ink — a new rare currency (dungeon loot, or an expensive Nether Star recipe as a backup) spent on every node in the tree
- Mana Boost — raises max mana on every Magic Lens you own, the tree's root node
- Cost Reduction / Cooldown Reduction — cut spell mana costs and cooldowns by up to 25% each at max level
- Pocket Dimension / Pocket Dimension Expansion — unlocks the Pocket Dimension spell, then grows your personal room from 9x9 up to 15x15
- Storage System / Storage Network Expansion — unlocks crafting Living Chests and Living Terminals, then raises the per-network storage cap above the base 15
- Arcane Assembler / Assembler Speed / Assembler Fuel Efficiency — unlocks crafting the Arcane Assembler, then speeds up its crafting and stretches its fuel further
- Overload Ritual / Warped Attunement — unlocks the ritual that tears a portal to the Warped Hollow open, then (after Broken Vessel's first defeat) lowers how full a storage network needs to be to open further portals

Pocket Dimension

- A skill-tree-only 17th spell (never found in Ancient Spell Books) that teleports you into your own personal room in a private dimension
- Casting it again while inside teleports you back to exactly where, and which way, you left from
- Each player gets an isolated room in the shared Pocket Dimension level, upgradeable via the Skill Tree from 9x9 up to 15x15

Networked Storage

- Living Chest — a 5-slot storage cube that auto-merges into one shared pool with any other Living Chest it's placed touching
- Storage Connector — bridges two separate Living Chest clusters into a single network without adding storage of its own
- Living Terminal — browses an entire connected cluster as one scrollable grid, with a search box and a sort button (Default / Name / Count)
- Compactor Upgrade / Slot Upgrade — Terminal upgrade-slot items that raise per-item stack size or add bonus pool slots
- Requires the Storage System skill unlock to craft Living Chests and Living Terminals

Arcane Assembler

- Auto-crafts real crafting-table recipes from its own 3x3 grid — anything craftable by hand works, not a fixed list
- Powered by crop and meat "fuel" instead of redstone, so it only burns food while a valid recipe is actually sitting in the grid
- Sticky recipe memory: right-click a grid slot with an item to set what belongs there, or with an empty hand to clear it
- Auto-refills its grid from its own internal buffer first, then from an adjacent Living Chest cluster if it's built touching one
- Requires its own Skill Tree unlock to craft

The Overload Ritual & the Warped Hollow

- Push a Living Chest network to its full storage capacity, then use an Overload Core + Warped Catalyst to overload it — tearing open a portal to a new dimension, the Warped Hollow
- The Warped Hollow is a real, explorable dimension, not a private room — open as many portals as you like, each landing in its own private area of the same shared space
- Ruined chambers are scattered throughout the Warped Hollow, each a small hand-built site worth the walk to find
- Portals are two-way and permanent, so getting back is as easy as walking through again
- Requires its own Skill Tree unlock (Overload Ritual)

Broken Vessel

- A second full boss fight, waiting inside a tower deep in the Warped Hollow
- Four HP-driven phases as the warped fungus consuming it takes over
- Completing the Overload Ritual for the first time hands you a compass pointing straight to its lair
- Re-fightable as many times as you want via the Vessel Summoning Charm, once you've found the lair yourself

The Pantheon Revealed

- After defeating the Sleeping God and earning the Hunger's attention, the Hunger Idol will eventually point you toward a hidden shrine buried in the Warped Hollow — find it for a real Faith reward and the fullest picture yet of what you've actually been dealing with
- New lore ties the Sleeping God, the Hunger, and Broken Vessel together through a shared, unnamed infection: the Warping
- Two new advancements track this discovery, following on from defeating the Sleeping God

Vessel-Bound rewards

- Smith your Sleeping God armor into Vessel-Bound Sleeping God using Broken Vessel drops — same stats, but its flight passive no longer has a hard cooldown (the budget recovers passively while grounded instead) and the full set grants Poison/Wither immunity
- Warped Anchor — bind it to a placed Living Terminal, then open that Terminal's storage from anywhere; gated behind the Warped Attunement skill
- Vessel Summoning Charm — re-summon Broken Vessel inside its own lair whenever you're ready for another fight

Accessories

- Charm slot (via Curios): Magnet Charm, Faith Charm, Mana Orb Charm, Burning Charcoal Charm, Antivenom Charm, Preserved Wither Rose Charm
- Bracelet slot (via Curios): Iron/Gold/Netherite Bracelets, each granting bonus Charm slots while worn

Guide book

- A full in-game Arcane Guide (Patchouli-based) covering every system above, including dedicated chapters for Networked Storage, the Arcane Assembler, and the Overload Ritual/Warped Hollow/Broken Vessel
- Always obtainable via crafting, the creative inventory, or /give — not locked to any single method

Advancements

- A progression tree from crafting your first lens through defeating both bosses and mastering all 16 spells, extending on through the Skill Tree, Pocket Dimension, Networked Storage, Arcane Assembler, and accessories

</details>

<details>
<summary>V5</summary>

### The Pantheon

- Pledge your faith to one of five gods at their own dedicated Faith Altar — Hunger, Florian (Fertility), Aurelia (War), Quetzera (Sun), or Janus (Theater) — each unlocking a permanent, god-specific perk track for as long as you stay devoted
- Change your mind later: pledging to a different god swaps your active perk track, though your devotion progress with the new god starts fresh
- **The Hunger** — immunity to food-based ailments plus lifesteal in combat
- **Florian (Fertility)** — bonus max hearts that scale with your devotion, plus passive regeneration near nature once fully pledged
- **Aurelia (War)** — permanently scaling Strength, plus a Totem-of-Undying-style extra life once fully devoted
- **Quetzera (Sun)** — daytime buffs (Night Vision, Glowing, Haste, Speed, scaling Armor) that flip into nighttime debuffs after dark
- **Janus (Theater)** — a Theater Helmet grants three Faith-fueled active abilities (Stage Clone, Crowd Control, Vanishing Act), plus passive fall-damage immunity, dodge chance, and reduced aggro. Stage Clone's decoy wears your skin and armor, and leaves nothing behind when it falls
- Six new advancements track making your pledge and committing to each of the five gods

### The Trials

- Craft a **God Hub Medallion** (a Warped Vessel Shard ringed by gold) to open a door to the God Challenge Hub — a dark, flat dimension ringed with one arena per god. Right-click it again inside to return to exactly where you left
- Use a Challenge Altar to be taken into that god's arena and face their champion. Fall, and the fight ends; win, and you're carried back to the hub
- **Aurelia** fights with a wound-up spear thrust and a straight-line charge, growing faster and deadlier across three stages. Beat her for **Aurelia's Spear** — a blade stronger than diamond with extra reach
- **Florian** takes the shape of a great stag that gores and tosses you skyward, or charges head-down. Beat him for **Florian's Antler** — right-click it to call a lesser Florian to fight beside you for a short while
- Every victory after your first pays out Faith instead, and a god's relic is fire-proof and can't be lost to the void
- More gods' trials are still to come

### Winged Chestplate

- Smith a Vessel-Bound Chestplate with an Elytra and a Warped Attunement Template to make the **Vessel-Bound Winged Chestplate** — it keeps the set's recovering flight and adds a real glide on top

### Guide

- A new Pantheon chapter in the Arcane Guide covers the full pledge system, every god's perks, and the Trials

</details>

</details>

<details>
<summary>Credits</summary>

</details>
