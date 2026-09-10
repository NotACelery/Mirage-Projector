# Mirage Projector dev.43 — Crystal / Beacon QA

Do not mark dev.43 build-clean/stable until this list is exercised after a successful Windows `build.bat`.

## Build / load

- [ ] `build.bat` succeeds on Java 21.
- [ ] game reaches title screen with no Mixin apply errors.
- [ ] dedicated server starts with no client-class loading from common crystal Mixins.
- [ ] no datapack/loot modifier errors in log.

## Crystal registration / art

- [ ] all four stages exist in creative inventory.
- [ ] all four place on all Amethyst-like faces for decoration.
- [ ] waterlogging behaves.
- [ ] normal textures read as dark Crying Obsidian, not purple Amethyst recolors.
- [ ] energized variants are visibly stronger but retain dark volcanic-glass identity.

## Renewable growth

Construct exactly:

```text
Lava SOURCE
Crying Obsidian
Air
```

- [ ] Small Bud eventually nucleates directly below.
- [ ] flowing Lava does not work.
- [ ] occupied target does not get overwritten.
- [ ] natural Bud faces DOWN.
- [ ] removing Lava stops future growth but preserves current stage.
- [ ] removing Crying Obsidian stops future growth.
- [ ] Silk-placed decorative crystals away from generator do not age.
- [ ] after nucleation, later stages subjectively advance faster than initial nucleation.

For fast QA, temporarily increasing randomTickSpeed is acceptable; restore normal gamerule afterward.

## Harvest

Without Silk:

- [ ] Small -> 1 shard.
- [ ] Medium -> 2.
- [ ] Large -> 3.
- [ ] Mature -> 4.

With Silk:

- [ ] each stage drops itself.

Fortune:

- [ ] does not increase shard count.

## Structure loot

Generate/open representative fresh structures/chests or use loot commands where practical:

- [ ] Ruined Portal can yield 1–3 shards.
- [ ] Abandoned Mineshaft can yield 1–2.
- [ ] Armorer/Toolsmith/Weaponsmith village chests can yield 1–2.
- [ ] vanilla loot still exists; Mirage adds rather than replaces.

## Beacon excitation / block light

With an active Beacon directly below the crystal column:

- [ ] Small becomes energized and emits level-14 fallback light.
- [ ] Medium/Large/Mature reach vanilla fallback 15.
- [ ] removing/deactivating Beacon clears energized state within roughly one second.
- [ ] reactivation restores it.

Stacking:

- [ ] lower Small lets reduced energy reach upper crystal.
- [ ] lower Medium/Large further reduce upper excitation visually.
- [ ] lower Mature prevents upper crystal from energizing from that Beacon column.

## Vertical Beam rendering

- [ ] Small continues ~75% visually above midpoint.
- [ ] Medium ~50%.
- [ ] Large ~25%.
- [ ] Mature 0% above midpoint.
- [ ] beam terminates visually at crystal center around Y+0.5, not at block top/bottom.
- [ ] Beacon potion effects remain active even with Mature in beam.
- [ ] stained-glass beam colors remain visible below/through applicable stages.
- [ ] multiple stained-glass sections plus multiple crystals do not reset hue.

## Residual leak rendering

- [ ] only one leak per crystal at once.
- [ ] width looks ~half vanilla Beacon inner beam.
- [ ] Crying-Obsidian lavender/purple palette.
- [ ] starts short and strong.
- [ ] extends over ~1 second.
- [ ] stays at max length ~1 second.
- [ ] retracts while fading over ~1 second.
- [ ] Mature chosen max falls within ~3–4 blocks before collision.
- [ ] wall collision clips the beam; it does not pass through solid blocks.
- [ ] directions change over successive events and are not vertical.
- [ ] two clients observe the same deterministic direction/event timing for the same crystal.
- [ ] no half-block X/Z offset error from double centering.

## Graphics compatibility

Repeat a representative Mature test under:

- [ ] Fast.
- [ ] Fancy.
- [ ] Fabulous.
- [ ] without shaders.
- [ ] with commonly used shader stack if available.

Watch for depth holes, beam sorting, z-fighting and unexpected full-screen translucency.

## dev.42 regression

- [ ] six chassis still render correctly.
- [ ] Core Chamber still shows actual Core item from all camera angles.
- [ ] no return of broad ghost Glass sheets.
- [ ] Power/UI/GIF/Entity basics still open and function.
