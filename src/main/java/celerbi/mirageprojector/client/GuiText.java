package celerbi.mirageprojector.client;

import net.minecraft.client.gui.Font;

final class GuiText {
    private static final String ELLIPSIS = "…";

    private GuiText() {
    }

    static String fit(Font font, String value, int maxWidth) {
        if (value == null || value.isEmpty() || font.width(value) <= maxWidth) {
            return value == null ? "" : value;
        }
        int target = Math.max(0, maxWidth - font.width(ELLIPSIS));
        int end = value.length();
        while (end > 0 && font.width(value.substring(0, end)) > target) {
            end--;
        }
        return value.substring(0, end) + ELLIPSIS;
    }
}
