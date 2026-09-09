package celerbi.mirageprojector;

/**
 * Single-source aspect-ratio contract shared by renderer, Power and UI.
 * Scale always controls the source image's dominant pixel axis; Mirage never crops or stretches.
 */
public final class ProjectionImageSizing {
    private ProjectionImageSizing() {}

    public static Size size(int sourceWidth, int sourceHeight, int scalePixels) {
        int width = Math.max(1, sourceWidth);
        int height = Math.max(1, sourceHeight);
        int scale = Math.max(1, scalePixels);
        if (width >= height) {
            return new Size(scale, Math.max(1, Math.round(scale * height / (float) width)), Axis.WIDTH);
        }
        return new Size(Math.max(1, Math.round(scale * width / (float) height)), scale, Axis.HEIGHT);
    }

    /**
     * Prism faces have adaptive nominal geometry instead of being trapped at 48x48.
     * Clearly horizontal content borrows Wide's 80x32 envelope, clearly vertical content borrows
     * Tall's 32x80 envelope, and near-square content keeps Prism's native 48x48 envelope.
     */
    public static NominalEnvelope prismNominal(int sourceWidth, int sourceHeight) {
        int w = Math.max(1, sourceWidth);
        int h = Math.max(1, sourceHeight);
        double ratio = w / (double) h;
        if (ratio >= 1.20D) return new NominalEnvelope(80, 32, Orientation.WIDE_LIKE);
        if (ratio <= (1.0D / 1.20D)) return new NominalEnvelope(32, 80, Orientation.TALL_LIKE);
        return new NominalEnvelope(48, 48, Orientation.SQUARE_LIKE);
    }

    public static Orientation orientation(int sourceWidth, int sourceHeight) {
        if (sourceWidth > sourceHeight) return Orientation.WIDE_LIKE;
        if (sourceHeight > sourceWidth) return Orientation.TALL_LIKE;
        return Orientation.SQUARE_LIKE;
    }

    public static boolean contraryToNaturalChassis(ProjectionChassisProfile chassis, int sourceWidth, int sourceHeight) {
        if (chassis == ProjectionChassisProfile.WIDE) return sourceHeight > sourceWidth;
        if (chassis == ProjectionChassisProfile.TALL) return sourceWidth > sourceHeight;
        return false;
    }

    public enum Axis { WIDTH, HEIGHT }
    public enum Orientation { WIDE_LIKE, TALL_LIKE, SQUARE_LIKE }
    public record Size(int widthPixels, int heightPixels, Axis dominantAxis) {}
    public record NominalEnvelope(int widthPixels, int heightPixels, Orientation orientation) {}
}
