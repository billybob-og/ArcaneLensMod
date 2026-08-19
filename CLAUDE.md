# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Arcane Lens — a Forge 1.20.1 (47.4.21) Minecraft mod. Mod id `arcanelens`, Java package `com.arcanelens`, MIT license, targeting Modrinth distribution. It centers on an inscribable Magic Lens item (spells assigned via an Inscription Workbench + Diamond Engraver), soul-fire mana pedestals, a standalone scroll-wheel spellcasting system, a castle+dungeon structure for learning spells from books, a diamond-tier-gated custom ore, and a lens-combining mechanic.

The full design spec and phase-by-phase plan lives at `C:\Users\alfon\.claude\plans\heres-the-idea-for-graceful-aurora.md` — read it for resolved mechanics (merge rules, mana formulas, pedestal linking, achievements) before re-deriving them from scratch. Two features exist beyond that plan: the Diamond Engraver tool (`item/DiamondEngraverItem.java`) and the Soul Feeder block (`block/SoulFeederBlock.java`). Treat the plan as historical intent, not as a substitute for reading current source — it can drift from what's actually implemented.

The user is a modding beginner working iteratively with an AI assistant; keep explanations accessible and build/test in-game after non-trivial changes rather than assuming correctness from a clean compile.

## Commands

- Build: `.\gradlew build`
- Run client (in-game manual testing): `.\gradlew runClient`
- Run data generators (regenerate `src/generated/resources/`): `.\gradlew runData`
- Run dedicated server: `.\gradlew runServer`

`gradle.properties` pins `org.gradle.vfs.watch=false` — this is a required workaround for recurring Windows file-lock errors in `build/` during compiles. Do not remove it.

After adding, renaming, or removing anything datagen touches (registry entries, recipes, loot tables, tags, lang keys, worldgen features), run `runData` before `runClient` — the generated JSON under `src/generated/resources/` is what actually ships, and stale generated files will not reflect source changes until regenerated.

## Architecture

### Registration

Each registrable type gets its own `DeferredRegister` in a dedicated class under `registry/` (`ModBlocks`, `ModItems`, `ModBlockEntities`, `ModMenuTypes`, `ModCreativeTabs`). All are `.register(modEventBus)`'d together in `ArcaneLens`'s constructor. Follow this pattern for any new registrable type rather than ad-hoc registration elsewhere.

Spells are a genuine custom Forge registry, not a bare enum/map: `api/spell/SpellRegistry.java` defines `SPELL_REGISTRY_KEY` (`arcanelens:spells`) and its `DeferredRegister<Spell>`; actual spell instances are registered in `spell/ModSpells.java`. Adding a new spell means implementing `api/spell/Spell.java`, registering it in `ModSpells`, and calling `ModSpells.register()`'s static-init trigger (already wired in `ArcaneLens`'s constructor) — no separate bootstrap step needed.

### Datagen-only assets

Blockstates, item/block models, recipes, loot tables, block tags, language keys, and worldgen datapack entries (configured/placed features, biome modifiers) are **generated**, not hand-authored — see `datagen/DataGenerators.java`'s `gatherData` for the full provider list, all wired through the single `GatherDataEvent` handler. Output lands in `src/generated/resources/`; never hand-edit files there, edit the corresponding `datagen/Mod*Provider.java` and rerun `runData`.

Exceptions: raw texture PNGs (`src/main/resources/assets/arcanelens/textures/...`) and a handful of hand-authored Blockbench block models (`assets/arcanelens/models/block/soul_pedestal.json`, `lens_pedestal.json`) are real files under `src/main/resources/` — `ModBlockStateProvider` references these via `models().getExistingFile(...)` rather than generating them, since Blockbench output isn't reproducible from the simple cube/cube_bottom_top helpers used for the mod's other blocks.

Custom art workflow: the user drops Blockbench-authored `.json`/texture files into `Modles/` (source files awaiting wiring) and raw sprite PNGs into `Sprites/` at the repo root. Check both folders before assuming a block/item still needs placeholder art — wire found assets into `assets/arcanelens/models|textures/...` and the relevant datagen provider.

### Capabilities

Player capabilities (e.g. `IKnownSpells`) follow: interface (`capability/IKnownSpells.java`) + implementation (`KnownSpellsImpl.java`, holds the real NBT-serializable state) + `ICapabilitySerializable` provider (`KnownSpellsProvider.java`, exposes the `Capability<T>` token and delegates `serializeNBT`/`deserializeNBT`) + a central `CapabilityHandler.java` that wires `RegisterCapabilitiesEvent`, `AttachCapabilitiesEvent<Entity>` (attach only to `Player`), and the death/sync lifecycle. All hooked into `MinecraftForge.EVENT_BUS`/`modEventBus` from `ArcaneLens`'s constructor.

**Death-persistence gotcha**: Forge invalidates an entity's capabilities during removal, which happens *before* `PlayerEvent.Clone` fires on death — so reading `event.getOriginal().getCapability(...)` in `Clone` silently no-ops. The working pattern (see `CapabilityHandler.onPlayerDeath`/`onPlayerClone`): snapshot the capability's data into `player.getPersistentData()` (a plain `CompoundTag`, unaffected by cap invalidation) during `LivingDeathEvent`, while the entity is still valid, then read that snapshot back out of `event.getOriginal().getPersistentData()` in `Clone` and apply it to the new entity's capability. Separately, client sync packets for capability data must be re-sent on **both** `PlayerEvent.PlayerLoggedInEvent` (initial login) and `PlayerEvent.PlayerRespawnEvent` (respawn doesn't refire login) — see `CapabilityHandler.onPlayerLoggedIn`/`onPlayerRespawn` calling `KnownSpellsSync.syncToClient`.

### Networking

A single `SimpleChannel` (`network/NetworkHandler.CHANNEL`) registered once in `ArcaneLens`'s constructor via `NetworkHandler.register()`. Each packet is its own class under `network/packet/` with static `encode`/`decode`/`handle` methods, registered against a sequential `packetId` counter in `NetworkHandler.register()`. `handle` does `ctx.enqueueWork(() -> { ... })` then `ctx.setPacketHandled(true)`. Server-side handlers re-validate everything the client claims (known-spell ownership, mana/cooldown, engraver presence/durability) before mutating state — see `ServerboundSetSpellSlotPacket` and `ServerboundCastSpellPacket` as the templates.

### Block entities with inventories

Blocks with slots (`InscriptionWorkbenchBlockEntity`, `SoulFeederBlockEntity`, `LensCombinerBlockEntity`) hold an `ItemStackHandler` field (override `onContentsChanged` → `setChanged()`, optionally `isItemValid`), expose it through `ForgeCapabilities.ITEM_HANDLER` via a `LazyOptional` that's invalidated in an overridden `invalidateCaps()`, and persist it in `saveAdditional`/`load` via `serializeNBT`/`deserializeNBT`.

**Slot-count-migration gotcha**: `ItemStackHandler.deserializeNBT` restores whatever slot count was persisted in an old save, silently shrinking the handler back down if you've since added a slot — this crashes `AbstractContainerMenu#broadcastChanges` on any pre-existing placed block. When adding a slot to an existing block entity's handler, force a resize back to the new slot count after `deserializeNBT` in `load()`, preserving existing contents first. See `InscriptionWorkbenchBlockEntity.load()` (migrated from 1 slot to 2 when the engraver slot was added) as the template to copy for future slot additions.

Menu/screen wiring: the block's `use()` calls `NetworkHooks.openScreen(serverPlayer, blockEntity, pos)` (block entity implements `MenuProvider`); the corresponding `Menu` class has a server-side constructor (real block entity reference) and a client-side constructor reading a `BlockPos` from `FriendlyByteBuf`; screens are registered in `client/ClientModEvents.onClientSetup` via `MenuScreens.register`.

### Magic Lens NBT schema

`item/MagicLensItem.java` exposes static get/set helpers directly over `ItemStack` NBT (`Mana`, `MaxMana`, `MergeCount`, `Spells` — a fixed-length list of `MAX_CONFIGURABLE_SLOTS`, `LockedSpells` — an unbounded accumulating list, `SelectedIndex`) rather than a capability, since this data must travel with the stack itself (dropped, traded, stored in chests, combined). Follow this static-helpers-over-NBT pattern for any future per-stack state rather than introducing an item capability.

`util/LensSpellView.build(ItemStack)` is the single source of truth for a lens's actual castable spell list: it collapses locked ∪ configurable by spell id into `(spellId, stackCount)` entries. Both the HUD/cycle logic and `ServerboundCastSpellPacket`'s cast handling read through this — don't reimplement the collapsing logic elsewhere. `spell/SpellScaling.java` holds the paired effect/cost multiplier formulas (+25% effect, −10% cost per extra copy, capped at 3 copies) that consume its `stackCount`.

`util/LensMerger.java` implements the merge rules (locked-list accumulation, fresh configurable slots seeded from the top lens, mana formula, merge-count cap) as pure functions over `ItemStack` — `canMerge`/`computeNewMergeCount`/`computePrice`/`merge` — called from `LensCombinerMenu`/`LensCombinerBlockEntity`.

### Pedestal/feeder linking

`util/PedestalLinker.findLinked(level, origin, radius, blockEntityClass)` is the shared chunk-scan link-discovery routine (iterates loaded chunks' block-entity maps within a chunk-aligned bounding box, not brute-force block-by-block scanning) — reused by both Soul Pedestal → Lens Pedestal linking and Soul Feeder → Soul Pedestal linking via the two convenience wrappers `findLinkedLensPedestals`/`findLinkedSoulPedestals`. Any future "find nearby linked block entities of type X" feature should go through this, not a new scan implementation.

### Config

`Config.java` follows the standard `ForgeConfigSpec` pattern: define each value on `BUILDER` with `.comment(...).defineInRange(...)`/`.defineList(...)`, mirror it into a plain `public static` field, and refresh those fields from the spec inside `onLoad(ModConfigEvent)`. Read config through the static fields (`Config.linkRadius`, etc.) elsewhere in the codebase, never through the `ForgeConfigSpec.*Value` objects directly.

### Worldgen

Configured features, placed features, and biome modifiers are defined as `bootstrap(BootstapContext<T>)` static methods (`worldgen/ModConfiguredFeatures.java`, `ModPlacedFeatures.java`, `ModBiomeModifiers.java`) registered into a `RegistrySetBuilder` inside `DataGenerators.gatherData`, output via `DatapackBuiltinEntriesProvider`. This is the same datagen pass as everything else in "Datagen-only assets" above — there's no separate worldgen codegen step.

## Current state

Phases 0–6 of the design plan are complete and tested in-game. Phase 7 (castle + jigsaw dungeon structure) is just beginning: room/corridor pieces are being hand-built in-world (structure blocks) under a "Build" area, not yet exported as `.nbt` or wired into template pools/jigsaw JSON.
