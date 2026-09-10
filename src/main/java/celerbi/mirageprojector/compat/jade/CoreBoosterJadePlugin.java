package celerbi.mirageprojector.compat.jade;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.block.CoreBoosterBlock;
import celerbi.mirageprojector.blockentity.CoreBoosterBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public final class CoreBoosterJadePlugin implements IWailaPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
            MirageProjector.MOD_ID,
            "core_booster_material"
    );

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(CoreBoosterComponent.INSTANCE, CoreBoosterBlock.class);
    }

    private enum CoreBoosterComponent implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof CoreBoosterBlockEntity booster)) {
                return;
            }
            if (booster.material().present()) {
                tooltip.add(Component.translatable(
                        "jade.mirage_projector.core_booster.material",
                        booster.material().displayComponent()
                ));
                tooltip.add(Component.translatable("jade.mirage_projector.core_booster.amplification", "1.50"));
                tooltip.add(Component.translatable(
                        "jade.mirage_projector.core_booster.beacon_effect",
                        booster.material().beaconEffectComponent()
                ));
            } else {
                tooltip.add(Component.translatable("jade.mirage_projector.core_booster.empty"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}
