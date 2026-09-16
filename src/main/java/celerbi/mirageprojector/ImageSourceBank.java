package celerbi.mirageprojector;

import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

public final class ImageSourceBank {

    public static final int ACTIVE_MULTI_SLOTS = 4;

    public static final int PERSISTED_COMPAT_SLOTS = 9;

    private final Asset[] slots = new Asset[PERSISTED_COMPAT_SLOTS];

    public ImageSourceBank() {
        Arrays.fill(slots, Asset.EMPTY);
    }

    public ImageSourceBank copy() {
        ImageSourceBank copy = new ImageSourceBank();
        System.arraycopy(slots, 0, copy.slots, 0, slots.length);
        return copy;
    }

    public Asset get(int slot) {
        return validSlot(slot) ? slots[slot] : Asset.EMPTY;
    }

    public void set(int slot, String id, int width, int height) {
        if (!validSlot(slot)) {
            return;
        }
        slots[slot] = sanitize(id, width, height);
    }

    public void set(int slot, Asset asset) {
        if (!validSlot(slot)) {
            return;
        }
        slots[slot] = asset == null ? Asset.EMPTY : sanitize(asset.id(), asset.width(), asset.height());
    }

    public void clear(int slot) {
        if (validSlot(slot)) {
            slots[slot] = Asset.EMPTY;
        }
    }

    public void clearAll() {
        Arrays.fill(slots, Asset.EMPTY);
    }

    public void swap(int first, int second) {
        if (!validSlot(first) || !validSlot(second) || first == second) {
            return;
        }
        Asset tmp = slots[first];
        slots[first] = slots[second];
        slots[second] = tmp;
    }

    public void move(int from, int to) {
        if (!validSlot(from) || !validSlot(to) || from == to) {
            return;
        }
        Asset moving = slots[from];
        if (from < to) {
            System.arraycopy(slots, from + 1, slots, from, to - from);
        } else {
            System.arraycopy(slots, to, slots, to + 1, from - to);
        }
        slots[to] = moving;
    }

    public int firstPresentIndex() {
        for (int i = 0; i < PERSISTED_COMPAT_SLOTS; i++) {
            if (slots[i].present()) {
                return i;
            }
        }
        return -1;
    }

    public int normalizePresentIndex(int preferred) {
        if (validSlot(preferred) && slots[preferred].present()) {
            return preferred;
        }
        return firstPresentIndex();
    }

    public int nextPresentIndex(int current, int direction) {
        int first = firstPresentIndex();
        if (first < 0) {
            return -1;
        }
        int step = direction < 0 ? -1 : 1;
        int start = validSlot(current) ? current : first;
        for (int offset = 1; offset <= PERSISTED_COMPAT_SLOTS; offset++) {
            int candidate = Math.floorMod(start + step * offset, PERSISTED_COMPAT_SLOTS);
            if (slots[candidate].present()) {
                return candidate;
            }
        }
        return first;
    }

    public boolean hasAny(int limit) {
        return countPresent(limit) > 0;
    }

    public int countPresent(int limit) {
        int safeLimit = Math.max(0, Math.min(limit, PERSISTED_COMPAT_SLOTS));
        int count = 0;
        for (int i = 0; i < safeLimit; i++) {
            if (slots[i].present()) {
                count++;
            }
        }
        return count;
    }

    public void copySlotToEmpty(int source, int limit) {
        Asset asset = get(source);
        if (!asset.present()) {
            return;
        }
        int safeLimit = Math.max(0, Math.min(limit, PERSISTED_COMPAT_SLOTS));
        for (int i = 0; i < safeLimit; i++) {
            if (!slots[i].present()) {
                slots[i] = asset;
            }
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < PERSISTED_COMPAT_SLOTS; i++) {
            Asset asset = slots[i];
            if (!asset.present()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putString("Id", asset.id());
            entry.putInt("Width", asset.width());
            entry.putInt("Height", asset.height());
            tag.put("Slot" + i, entry);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        clearAll();
        if (tag == null) {
            return;
        }
        for (int i = 0; i < PERSISTED_COMPAT_SLOTS; i++) {
            String key = "Slot" + i;
            if (!tag.contains(key)) {
                continue;
            }
            CompoundTag entry = tag.getCompound(key);
            set(i, entry.getString("Id"), entry.getInt("Width"), entry.getInt("Height"));
        }
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        for (int i = 0; i < PERSISTED_COMPAT_SLOTS; i++) {
            Asset asset = slots[i];
            buffer.writeUtf(asset.id(), 128);
            buffer.writeVarInt(asset.width());
            buffer.writeVarInt(asset.height());
        }
    }

    public static ImageSourceBank read(RegistryFriendlyByteBuf buffer) {
        ImageSourceBank bank = new ImageSourceBank();
        for (int i = 0; i < PERSISTED_COMPAT_SLOTS; i++) {
            bank.set(i, buffer.readUtf(128), buffer.readVarInt(), buffer.readVarInt());
        }
        return bank;
    }

    private static Asset sanitize(String id, int width, int height) {
        String safe = id == null ? "" : id.trim();
        if (safe.isBlank() || !ProjectionAssetRules.isValidAssetId(safe) || width <= 0 || height <= 0) {
            return Asset.EMPTY;
        }
        return new Asset(safe, Math.min(width, 8192), Math.min(height, 8192));
    }

    private static boolean validSlot(int slot) {
        return slot >= 0 && slot < PERSISTED_COMPAT_SLOTS;
    }

    public record Asset(String id, int width, int height) {
        public static final Asset EMPTY = new Asset("", 0, 0);

        public boolean present() {
            return id != null && !id.isBlank() && width > 0 && height > 0;
        }
    }
}
