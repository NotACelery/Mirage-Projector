package celerbi.mirageprojector.network;

import celerbi.mirageprojector.MirageProjector;
import celerbi.mirageprojector.client.ClientMirageLightSync;
import celerbi.mirageprojector.light.LightDecayMode;
import celerbi.mirageprojector.light.engine.MirageLightProfile;
import celerbi.mirageprojector.light.engine.MirageLightRuntimeMode;
import celerbi.mirageprojector.light.engine.MirageLightShape;
import celerbi.mirageprojector.light.engine.MirageLightSource;
import celerbi.mirageprojector.light.engine.MirageLightSourceId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Source-descriptor synchronization for the virtual Mirage light backend.
 *
 * We synchronize sources, not solved voxels. Both sides run the same deterministic
 * solver against their local chunk geometry, keeping packets tiny even for large fields.
 */
public record MirageLightSourceSyncPayload(
        Action action,
        String kind,
        long key,
        BlockPos origin,
        int conceptualLight,
        int substepsPerLightLevel,
        int airStepCostUnits,
        int detourExtraCostUnits,
        int maxRadius,
        int decayModeOrdinal,
        int shapeOrdinal,
        double directionX,
        double directionY,
        double directionZ,
        float coneAngleDegrees,
        int rgb,
        int runtimeModeOrdinal
) implements CustomPacketPayload {
    public static final Type<MirageLightSourceSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MirageProjector.MOD_ID, "mirage_light_source_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MirageLightSourceSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MirageLightSourceSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                    Action action = safeEnum(Action.values(), buffer.readVarInt(), Action.CLEAR);
                    String kind = buffer.readUtf(64);
                    long key = buffer.readLong();
                    BlockPos origin = buffer.readBlockPos();
                    int conceptualLight = buffer.readVarInt();
                    int substeps = buffer.readVarInt();
                    int airCost = buffer.readVarInt();
                    int detourExtraCost = buffer.readVarInt();
                    int maxRadius = buffer.readVarInt();
                    int decay = buffer.readVarInt();
                    int shape = buffer.readVarInt();
                    double directionX = buffer.readDouble();
                    double directionY = buffer.readDouble();
                    double directionZ = buffer.readDouble();
                    float cone = buffer.readFloat();
                    int rgb = buffer.readInt();
                    int runtime = buffer.readVarInt();
                    return new MirageLightSourceSyncPayload(
                            action,
                            kind,
                            key,
                            origin,
                            conceptualLight,
                            substeps,
                            airCost,
                            detourExtraCost,
                            maxRadius,
                            decay,
                            shape,
                            directionX,
                            directionY,
                            directionZ,
                            cone,
                            rgb,
                            runtime
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, MirageLightSourceSyncPayload payload) {
                    buffer.writeVarInt(payload.action().ordinal());
                    buffer.writeUtf(payload.kind(), 64);
                    buffer.writeLong(payload.key());
                    buffer.writeBlockPos(payload.origin());
                    buffer.writeVarInt(payload.conceptualLight());
                    buffer.writeVarInt(payload.substepsPerLightLevel());
                    buffer.writeVarInt(payload.airStepCostUnits());
                    buffer.writeVarInt(payload.detourExtraCostUnits());
                    buffer.writeVarInt(payload.maxRadius());
                    buffer.writeVarInt(payload.decayModeOrdinal());
                    buffer.writeVarInt(payload.shapeOrdinal());
                    buffer.writeDouble(payload.directionX());
                    buffer.writeDouble(payload.directionY());
                    buffer.writeDouble(payload.directionZ());
                    buffer.writeFloat(payload.coneAngleDegrees());
                    buffer.writeInt(payload.rgb());
                    buffer.writeVarInt(payload.runtimeModeOrdinal());
                }
            };

    public static MirageLightSourceSyncPayload upsert(MirageLightSource source) {
        MirageLightProfile profile = source.profile();
        return new MirageLightSourceSyncPayload(
                Action.UPSERT,
                source.id().kind(),
                source.id().key(),
                source.origin(),
                profile.conceptualLight(),
                profile.substepsPerLightLevel(),
                profile.airStepCostUnits(),
                profile.detourExtraCostUnits(),
                profile.maxRadius(),
                profile.decayMode().ordinal(),
                profile.shape().ordinal(),
                profile.direction().x,
                profile.direction().y,
                profile.direction().z,
                profile.coneAngleDegrees(),
                profile.rgb(),
                source.runtimeMode().ordinal()
        );
    }

    public static MirageLightSourceSyncPayload remove(MirageLightSourceId id, BlockPos origin) {
        return new MirageLightSourceSyncPayload(
                Action.REMOVE,
                id.kind(),
                id.key(),
                origin,
                0,
                1,
                1,
                0,
                0,
                LightDecayMode.VANILLA.ordinal(),
                MirageLightShape.OMNIDIRECTIONAL.ordinal(),
                0.0D,
                0.0D,
                0.0D,
                360.0F,
                0xFFFFFF,
                MirageLightRuntimeMode.STATIC_WORLD.ordinal()
        );
    }

    public static MirageLightSourceSyncPayload clear() {
        return remove(new MirageLightSourceId("clear", 0L), BlockPos.ZERO).withAction(Action.CLEAR);
    }

    private MirageLightSourceSyncPayload withAction(Action newAction) {
        return new MirageLightSourceSyncPayload(
                newAction, kind, key, origin, conceptualLight, substepsPerLightLevel,
                airStepCostUnits, detourExtraCostUnits, maxRadius, decayModeOrdinal, shapeOrdinal,
                directionX, directionY, directionZ, coneAngleDegrees, rgb, runtimeModeOrdinal
        );
    }

    public MirageLightSourceId sourceId() {
        return new MirageLightSourceId(kind, key);
    }

    public MirageLightSource source() {
        MirageLightProfile profile = new MirageLightProfile(
                conceptualLight,
                substepsPerLightLevel,
                airStepCostUnits,
                detourExtraCostUnits,
                maxRadius,
                safeEnum(LightDecayMode.values(), decayModeOrdinal, LightDecayMode.VANILLA),
                safeEnum(MirageLightShape.values(), shapeOrdinal, MirageLightShape.OMNIDIRECTIONAL),
                new Vec3(directionX, directionY, directionZ),
                coneAngleDegrees,
                rgb
        );
        return new MirageLightSource(
                sourceId(),
                origin,
                profile,
                safeEnum(MirageLightRuntimeMode.values(), runtimeModeOrdinal, MirageLightRuntimeMode.STATIC_WORLD)
        );
    }

    @Override
    public Type<MirageLightSourceSyncPayload> type() {
        return TYPE;
    }

    public static void handle(MirageLightSourceSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientMirageLightSync.apply(payload));
    }

    private static <E> E safeEnum(E[] values, int ordinal, E fallback) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : fallback;
    }

    public enum Action {
        UPSERT,
        REMOVE,
        CLEAR
    }
}
