package celerbi.mirageprojector.client;

import celerbi.mirageprojector.ProjectionSettings;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class ProjectionRenderContext {
    private static final ThreadLocal<State> ACTIVE = new ThreadLocal<>();

    private ProjectionRenderContext() {
    }

    public static Scope push(LivingEntity entity, ProjectionSettings settings) {
        return push(entity, settings, false);
    }

    public static Scope push(LivingEntity entity, ProjectionSettings settings, boolean lateDepthStableGhost) {
        State previous = ACTIVE.get();
        ACTIVE.set(new State(
                entity,
                settings == null ? ProjectionSettings.DEFAULT : settings.sanitized(),
                lateDepthStableGhost
        ));
        return new Scope(previous);
    }

    @Nullable
    public static ProjectionSettings settingsFor(LivingEntity entity) {
        State state = ACTIVE.get();
        return state != null && state.entity() == entity ? state.settings() : null;
    }

    @Nullable
    public static ProjectionSettings activeSettings() {
        State state = ACTIVE.get();
        return state == null ? null : state.settings();
    }

    @Nullable
    public static LivingEntity activeEntity() {
        State state = ACTIVE.get();
        return state == null ? null : state.entity();
    }

    public static boolean ghostActive() {
        ProjectionSettings settings = activeSettings();
        return settings != null && settings.opacityPercent() < 100;
    }

    public static boolean ghostActiveFor(LivingEntity entity) {
        ProjectionSettings settings = settingsFor(entity);
        return settings != null && settings.opacityPercent() < 100;
    }

    public static boolean lateDepthStableGhost() {
        State state = ACTIVE.get();
        return state != null && state.lateDepthStableGhost() && state.settings().opacityPercent() < 100;
    }

    private record State(
            LivingEntity entity,
            ProjectionSettings settings,
            boolean lateDepthStableGhost
    ) {
    }

    public static final class Scope implements AutoCloseable {
        @Nullable
        private final State previous;
        private boolean closed;

        private Scope(@Nullable State previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previous == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(previous);
            }
        }
    }
}
