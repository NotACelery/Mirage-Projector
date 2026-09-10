package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.CoreBoosterMaterial;
import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MirageProjector.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRAGE_PROJECTOR_TAB =
            CREATIVE_TABS.register("mirage_projector", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mirage_projector"))
                    .icon(() -> ModItems.MIRAGE_PROJECTOR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MIRAGE_PROJECTOR.get());
                        output.accept(ModItems.MIRAGE_DISPLAY.get());
                        output.accept(ModItems.WIDE_MIRAGE_PROJECTOR.get());
                        output.accept(ModItems.TALL_MIRAGE_PROJECTOR.get());
                        output.accept(ModItems.MIRAGE_FIELD_PROJECTOR.get());
                        output.accept(ModItems.MIRAGE_PRISM.get());
                        output.accept(ModItems.CORE_BOOSTER.get());
                        output.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.GLASS));
                        output.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.QUARTZ));
                        output.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.AMETHYST));
                        output.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.DIAMOND));
                        output.accept(CoreBoosterBlockEntity.stackForMaterial(CoreBoosterMaterial.NETHERITE));
                        output.accept(ModItems.CRYING_OBSIDIAN_SHARD.get());
                        output.accept(ModItems.SMALL_CRYING_OBSIDIAN_BUD.get());
                        output.accept(ModItems.MEDIUM_CRYING_OBSIDIAN_BUD.get());
                        output.accept(ModItems.LARGE_CRYING_OBSIDIAN_BUD.get());
                        output.accept(ModItems.CRYING_OBSIDIAN_CLUSTER.get());
                        output.accept(ModItems.OBSIDIAN_SPIKE.get());
                        output.accept(ModItems.ENTITY_SCAN_CARD.get());
                        output.accept(ModItems.DEBUG_HANDBOOK.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
