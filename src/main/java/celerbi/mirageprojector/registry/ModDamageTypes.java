package celerbi.mirageprojector.registry;

import celerbi.mirageprojector.MirageProjector;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> OBSIDIAN_SPIKE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "obsidian_spike")
    );

    private ModDamageTypes() {
    }
}
