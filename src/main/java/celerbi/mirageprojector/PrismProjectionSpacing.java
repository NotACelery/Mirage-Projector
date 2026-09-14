package celerbi.mirageprojector;

import net.minecraft.util.Mth;

/**
 * Collision-safe radial spacing rules for the four lateral faces of Mirage Prism.
 *
 * <p>The persisted distance is an internal absolute radius from the Prism center to the bottom
 * edge of each face. The UI exposes only the <em>extra</em> distance beyond the geometry-derived
 * collision floor. Therefore {@code +0 px} always means the tightest safe carousel for the
 * currently projected faces.</p>
 *
 * <p>Positive Tilt leans every face outward, so the bottom edges remain the closest points and no
 * additional radius is required. Negative Tilt leans the tops inward; in that direction the safe
 * floor grows just enough to stop adjacent faces from crossing. This keeps a +45 degree carousel
 * compact enough for the lower edges to meet while still allowing a downward/inward tilt to widen
 * itself automatically when necessary.</p>
 */
public final class PrismProjectionSpacing {
    /** User-controlled radial expansion beyond the collision-safe floor: about ten blocks. */
    public static final int MAX_EXTRA_DISTANCE_PIXELS = 160;

    /**
     * Wire/NBT safety ceiling retained for v3 compatibility. Runtime spacing is additionally
     * clamped to the current collision floor + {@link #MAX_EXTRA_DISTANCE_PIXELS}.
     */
    public static final int MAX_DISTANCE_PIXELS = 1024;
    public static final int DISTANCE_PIXELS_PER_PU = 64;

    private PrismProjectionSpacing() {
    }

    public static boolean supported(ProjectionChassisProfile chassis, ProjectionSettings settings) {
        if (chassis == null || chassis.geometry() != ProjectionChassisProfile.Geometry.PRISM || settings == null) {
            return false;
        }
        return settings.sourceMode() == ProjectionSettings.SourceMode.IMAGE
                || settings.sourceMode() == ProjectionSettings.SourceMode.BANNER;
    }

    /** Tight collision floor at zero/outward Tilt. */
    public static int baseCollisionDistancePixels(ProjectionSettings settings) {
        FaceFootprint[] faces = faces(settings);
        int floor = 0;
        for (int i = 0; i < faces.length; i++) {
            FaceFootprint a = faces[i];
            FaceFootprint b = faces[(i + 1) % faces.length];
            if (a.empty() || b.empty()) {
                continue;
            }
            // Adjacent bottom edges are perpendicular. They first touch when the shorter half-edge
            // reaches the other face's radial line; anything closer makes the quads intersect.
            floor = Math.max(floor, Mth.ceil(Math.min(a.widthPixels(), b.widthPixels()) * 0.5D));
        }
        return Mth.clamp(floor, 0, MAX_DISTANCE_PIXELS);
    }

    public static int minimumDistancePixels(ProjectionSettings settings) {
        return minimumDistancePixels(settings, settings == null ? 0.0F : settings.tiltDegrees());
    }

    public static int minimumDistancePixels(ProjectionSettings settings, float tiltDegrees) {
        FaceFootprint[] faces = faces(settings);
        if (facesEmpty(faces)) {
            return 0;
        }

        // Positive tilt is outward in the renderer: the bottom edge remains the innermost edge.
        double inwardSin = tiltDegrees < 0.0F
                ? Math.sin(Math.toRadians(Math.min(90.0F, Math.abs(tiltDegrees))))
                : 0.0D;
        int floor = 0;
        for (int i = 0; i < faces.length; i++) {
            FaceFootprint a = faces[i];
            FaceFootprint b = faces[(i + 1) % faces.length];
            if (a.empty() || b.empty()) {
                continue;
            }
            double touchingBottomRadius = Math.min(a.widthPixels(), b.widthPixels()) * 0.5D;
            double sharedInwardReach = Math.min(a.heightPixels(), b.heightPixels()) * inwardSin;
            floor = Math.max(floor, Mth.ceil(touchingBottomRadius + sharedInwardReach));
        }
        return Mth.clamp(floor, 0, MAX_DISTANCE_PIXELS);
    }

    public static int maximumDistancePixels(ProjectionSettings settings) {
        int base = baseCollisionDistancePixels(settings);
        return Mth.clamp(base + MAX_EXTRA_DISTANCE_PIXELS, 0, MAX_DISTANCE_PIXELS);
    }

    public static int effectiveDistancePixels(ProjectionSettings settings) {
        if (settings == null) {
            return 0;
        }
        int minimum = minimumDistancePixels(settings);
        int maximum = Math.max(minimum, maximumDistancePixels(settings));
        int requested = Mth.clamp(settings.distanceOffsetPixels(), 0, MAX_DISTANCE_PIXELS);
        return Mth.clamp(requested, minimum, maximum);
    }

    /**
     * User-facing Prism Distance is the extra radial separation beyond the no-tilt collision floor.
     * Zero therefore means "as tight as safely possible", rather than "at the projector center".
     */
    public static int minimumExtraDistancePixels(ProjectionSettings settings) {
        return Math.max(0, minimumDistancePixels(settings) - baseCollisionDistancePixels(settings));
    }

    public static int effectiveExtraDistancePixels(ProjectionSettings settings) {
        int base = baseCollisionDistancePixels(settings);
        return Mth.clamp(effectiveDistancePixels(settings) - base, 0, MAX_EXTRA_DISTANCE_PIXELS);
    }

    public static int absoluteDistancePixelsFromExtra(ProjectionSettings settings, int extraPixels) {
        int base = baseCollisionDistancePixels(settings);
        int minimumExtra = minimumExtraDistancePixels(settings);
        int safeExtra = Mth.clamp(Math.max(extraPixels, minimumExtra), 0, MAX_EXTRA_DISTANCE_PIXELS);
        return Mth.clamp(base + safeExtra, 0, MAX_DISTANCE_PIXELS);
    }

    public static int extraDistancePixels(ProjectionSettings settings) {
        return effectiveExtraDistancePixels(settings);
    }

    /**
     * Keeps Tilt in the requested direction, but refuses an inward angle whose collision floor
     * would require more than the ten-block Prism Distance budget.
     */
    public static int clampTiltDegreesToSafeRange(ProjectionSettings settings, int requestedTiltDegrees) {
        int requested = Mth.clamp(requestedTiltDegrees,
                -ProjectionSettings.MAX_TILT_DEGREES, ProjectionSettings.MAX_TILT_DEGREES);
        if (requested >= 0 || minimumExtraDistancePixels(settings, requested) <= MAX_EXTRA_DISTANCE_PIXELS) {
            return requested;
        }
        for (int tilt = requested + 1; tilt <= 0; tilt++) {
            if (minimumExtraDistancePixels(settings, tilt) <= MAX_EXTRA_DISTANCE_PIXELS) {
                return tilt;
            }
        }
        return 0;
    }

    public static int minimumAllowedTiltDegrees(ProjectionSettings settings) {
        return clampTiltDegreesToSafeRange(settings, -ProjectionSettings.MAX_TILT_DEGREES);
    }

    public static int minimumExtraDistancePixels(ProjectionSettings settings, float tiltDegrees) {
        return Math.max(0, minimumDistancePixels(settings, tiltDegrees) - baseCollisionDistancePixels(settings));
    }

    public static int powerCost(ProjectionSettings settings) {
        int extra = effectiveExtraDistancePixels(settings);
        return extra <= 0 ? 0 : Mth.ceil(extra / (double) DISTANCE_PIXELS_PER_PU);
    }

    private static FaceFootprint[] faces(ProjectionSettings settings) {
        ProjectionSettings safe = settings == null ? ProjectionSettings.DEFAULT : settings.sanitized();
        if (safe.sourceMode() == ProjectionSettings.SourceMode.BANNER) {
            int height = Math.max(1, safe.scalePixels());
            int width = Math.max(1, Math.round(height * 0.5F));
            FaceFootprint banner = new FaceFootprint(width, height);
            return new FaceFootprint[]{banner, banner, banner, banner};
        }
        if (safe.sourceMode() != ProjectionSettings.SourceMode.IMAGE) {
            return emptyFaces();
        }

        return new FaceFootprint[]{
                face(safe.hasImage(), safe.imageWidth(), safe.imageHeight(), safe.scalePixels()),
                face(safe.hasEastImage(), safe.eastImageWidth(), safe.eastImageHeight(), safe.scalePixels()),
                face(safe.hasBackImage(), safe.backImageWidth(), safe.backImageHeight(), safe.scalePixels()),
                face(safe.hasWestImage(), safe.westImageWidth(), safe.westImageHeight(), safe.scalePixels())
        };
    }

    private static FaceFootprint face(boolean present, int sourceWidth, int sourceHeight, int scalePixels) {
        if (!present) {
            return FaceFootprint.EMPTY;
        }
        ProjectionImageSizing.Size size = ProjectionImageSizing.size(sourceWidth, sourceHeight, scalePixels);
        return new FaceFootprint(Math.max(1, size.widthPixels()), Math.max(1, size.heightPixels()));
    }

    private static FaceFootprint[] emptyFaces() {
        return new FaceFootprint[]{FaceFootprint.EMPTY, FaceFootprint.EMPTY, FaceFootprint.EMPTY, FaceFootprint.EMPTY};
    }

    private static boolean facesEmpty(FaceFootprint[] faces) {
        for (FaceFootprint face : faces) {
            if (!face.empty()) {
                return false;
            }
        }
        return true;
    }

    private record FaceFootprint(int widthPixels, int heightPixels) {
        private static final FaceFootprint EMPTY = new FaceFootprint(0, 0);

        private boolean empty() {
            return widthPixels <= 0 || heightPixels <= 0;
        }
    }
}
