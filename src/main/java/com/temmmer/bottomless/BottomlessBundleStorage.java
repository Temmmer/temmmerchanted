package com.temmmer.bottomless;

import com.temmmer.ModEnchantmentTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class BottomlessBundleStorage {
    public static final int MAX_COUNT = 960;

    private static final String ROOT = "TemmmerBottomless";
    private static final String STORED_ITEM = "StoredItem";
    private static final String STORED_COUNT = "StoredCount";

    private BottomlessBundleStorage() {
    }

    public static boolean isBottomlessBundle(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (var entry : enchantments.entrySet()) {
            if (entry.getKey().is(ModEnchantmentTags.BOTTOMLESS)) {
                return true;
            }
        }

        return false;
    }

    public static int getStoredCount(ItemStack bundle) {
        CompoundTag tag = getCustomTag(bundle);
        CompoundTag root = tag.getCompound(ROOT).orElse(null);

        if (root == null) {
            return 0;
        }

        int count = root.getInt(STORED_COUNT).orElse(0);
        return Math.max(count, 0);
    }

    public static ItemStack getStoredItem(ItemStack bundle) {
        CompoundTag tag = getCustomTag(bundle);
        CompoundTag root = tag.getCompound(ROOT).orElse(null);

        if (root == null) {
            return ItemStack.EMPTY;
        }

        int rawId = root.getInt(STORED_ITEM).orElse(-1);
        if (rawId < 0) {
            return ItemStack.EMPTY;
        }

        Item item = Item.byId(rawId);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, 1);
    }

    public static boolean isEmpty(ItemStack bundle) {
        return getStoredCount(bundle) <= 0 || getStoredItem(bundle).isEmpty();
    }

    public static int tryInsert(ItemStack bundle, ItemStack incoming) {
        if (!isBottomlessBundle(bundle)) {
            return 0;
        }

        if (incoming.isEmpty()) {
            return 0;
        }

        if (!incoming.isStackable()) {
            return 0;
        }

        if (!incoming.getItem().canFitInsideContainerItems()) {
            return 0;
        }

        ItemStack stored = getStoredItem(bundle);
        int currentCount = getStoredCount(bundle);
        int remainingSpace = MAX_COUNT - currentCount;

        if (remainingSpace <= 0) {
            return 0;
        }

        if (stored.isEmpty()) {
            int moved = Math.min(incoming.getCount(), remainingSpace);
            setStored(bundle, incoming, moved);
            incoming.shrink(moved);
            return moved;
        }

        if (stored.getItem() != incoming.getItem()) {
            return 0;
        }

        int moved = Math.min(incoming.getCount(), remainingSpace);
        setStored(bundle, stored, currentCount + moved);
        incoming.shrink(moved);
        return moved;
    }

    public static ItemStack removeStack(ItemStack bundle) {
        if (!isBottomlessBundle(bundle)) {
            return ItemStack.EMPTY;
        }

        ItemStack stored = getStoredItem(bundle);
        int currentCount = getStoredCount(bundle);

        if (stored.isEmpty() || currentCount <= 0) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(currentCount, stored.getMaxStackSize());
        ItemStack out = stored.copy();
        out.setCount(extracted);

        int newCount = currentCount - extracted;
        if (newCount <= 0) {
            clear(bundle);
        } else {
            setStored(bundle, stored, newCount);
        }

        return out;
    }

    public static float getFullness(ItemStack bundle) {
        return Math.min((float) getStoredCount(bundle) / (float) MAX_COUNT, 1.0f);
    }

    public static void clear(ItemStack bundle) {
        CompoundTag tag = getCustomTag(bundle);
        tag.remove(ROOT);
        bundle.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setStored(ItemStack bundle, ItemStack storedItem, int count) {
        CompoundTag tag = getCustomTag(bundle);
        CompoundTag root = new CompoundTag();

        root.putInt(STORED_ITEM, Item.getId(storedItem.getItem()));
        root.putInt(STORED_COUNT, Math.max(0, Math.min(count, MAX_COUNT)));
        tag.put(ROOT, root);

        bundle.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void removeAmount(ItemStack bundle, int amount) {
        if (amount <= 0) {
            return;
        }

        ItemStack stored = getStoredItem(bundle);
        int count = getStoredCount(bundle);

        if (stored.isEmpty() || count <= 0) {
            return;
        }

        int remaining = count - amount;

        if (remaining <= 0) {
            clear(bundle);
        } else {
            setStored(bundle, stored, remaining);
        }
    }

    private static CompoundTag getCustomTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }
}