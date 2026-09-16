package celerbi.mirageprojector.compat;

import celerbi.mirageprojector.entity.EntityScanData;
import celerbi.mirageprojector.item.ScanCodexItem;
import celerbi.mirageprojector.scan.ScanCodexSavedData;
import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

/** Optional, reflection-only bridge to Easy Mob Farm. Mirage has no hard binary dependency on it. */
public final class EasyMobFarmCompat {
    public static final String MOD_ID = "easy_mob_farm";
    private static final ResourceLocation CAPTURE_CARD_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mob_capture_card");
    private static final ResourceLocation CAPTURE_DATA_ID =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "mob_capture_data");

    private EasyMobFarmCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static boolean isCaptureCard(ItemStack stack) {
        return isLoaded()
                && stack != null
                && !stack.isEmpty()
                && CAPTURE_CARD_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static ImportResult importCaptureCard(ServerPlayer player, ItemStack codex, ItemStack captureCard) {
        if (!isLoaded()) {
            return ImportResult.MOD_NOT_LOADED;
        }
        if (player == null || codex == null || !ScanCodexItem.isCodex(codex) || !isCaptureCard(captureCard)) {
            return ImportResult.INVALID_CARD;
        }

        try {
            DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(CAPTURE_DATA_ID);
            if (componentType == null) {
                return ImportResult.INVALID_CARD;
            }
            Object captureData = getUntypedComponent(captureCard, componentType);
            if (captureData == null) {
                return ImportResult.EMPTY_CARD;
            }

            Method entityTypeMethod = captureData.getClass().getMethod("entityType");
            Method dataMethod = captureData.getClass().getMethod("data");
            Object entityTypeValue = entityTypeMethod.invoke(captureData);
            Object entityDataValue = dataMethod.invoke(captureData);
            if (!(entityTypeValue instanceof EntityType<?> entityType)
                    || !(entityDataValue instanceof CompoundTag entityData)) {
                return ImportResult.INVALID_CARD;
            }

            Entity created = entityType.create(player.serverLevel());
            if (!(created instanceof LivingEntity living)) {
                if (created != null) {
                    created.discard();
                }
                return ImportResult.INVALID_CARD;
            }

            try {
                living.load(entityData.copy());
                EntityScanData.Scan scan = EntityScanData.create(living);
                if (!scan.success()) {
                    return ImportResult.TOO_LARGE;
                }

                UUID codexId = ScanCodexItem.ensureCodexId(codex);
                ScanCodexSavedData saved = ScanCodexSavedData.get(player.getServer());
                if (!saved.canAddType(codexId, scan.entityType())) {
                    return ImportResult.TYPE_LIMIT;
                }
                UUID scanId = saved.addScan(codexId, scan);
                if (ScanCodexItem.isZero(scanId)) {
                    return ImportResult.FAILED;
                }
                ScanCodexItem.setSelectedScanId(codex, scanId);
                return ImportResult.SUCCESS;
            } finally {
                living.discard();
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return ImportResult.FAILED;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object getUntypedComponent(ItemStack stack, DataComponentType<?> componentType) {
        return stack.get((DataComponentType) componentType);
    }

    public enum ImportResult {
        SUCCESS,
        MOD_NOT_LOADED,
        INVALID_CARD,
        EMPTY_CARD,
        TYPE_LIMIT,
        TOO_LARGE,
        FAILED
    }
}
