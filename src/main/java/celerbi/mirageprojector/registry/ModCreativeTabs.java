package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
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
                    .displayItems((parameters, output) -> output.accept(ModItems.MIRAGE_PROJECTOR.get()))
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_TABS.register(modEventBus);
    }
}
