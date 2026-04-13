package com.temmmer.mixin;

import com.temmmer.bottomless.BottomlessBundleStorage;
import com.temmmer.bottomless.BottomlessBundleTooltip;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static com.temmmer.bottomless.BottomlessBundleStorage.getStoredCount;
import static com.temmmer.bottomless.BottomlessBundleStorage.getStoredItem;

@Mixin(BundleItem.class)
public abstract class BundleItemMixin {
    @Unique
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F);

    @Unique
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0F, 0.44F, 0.53F, 1.0F);

    @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessOverrideOtherStackedOnMe(
            ItemStack bundle,
            ItemStack other,
            Slot slot,
            ClickAction clickAction,
            Player player,
            SlotAccess slotAccess,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!BottomlessBundleStorage.isBottomlessBundle(bundle)) {
            return;
        }

        if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
            if (slot.allowModification(player)) {
                int moved = BottomlessBundleStorage.tryInsert(bundle, other);
                if (moved > 0) {
                    temmmer$broadcast(player);
                    player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
                } else {
                    player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
                }
            }
            cir.setReturnValue(true);
            return;
        }

        if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            if (slot.allowModification(player)) {
                ItemStack extracted = BottomlessBundleStorage.removeStack(bundle);
                if (!extracted.isEmpty()) {
                    slotAccess.set(extracted);
                    temmmer$broadcast(player);
                    player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
                }
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessOverrideStackedOnOther(
            ItemStack bundle,
            Slot slot,
            ClickAction clickAction,
            Player player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!BottomlessBundleStorage.isBottomlessBundle(bundle)) {
            return;
        }

        ItemStack other = slot.getItem();

        if (clickAction == ClickAction.PRIMARY && !other.isEmpty()) {
            int moved = BottomlessBundleStorage.tryInsert(bundle, other);
            if (moved > 0) {
                temmmer$broadcast(player);
                player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_INSERT, 0.8F, 1.0F);
            } else {
                player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_INSERT_FAIL, 1.0F, 1.0F);
            }
            cir.setReturnValue(true);
            return;
        }

        if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
            ItemStack extracted = BottomlessBundleStorage.removeStack(bundle);
            if (!extracted.isEmpty()) {
                ItemStack rest = slot.safeInsert(extracted);
                if (!rest.isEmpty()) {
                    BottomlessBundleStorage.tryInsert(bundle, rest);
                } else {
                    temmmer$broadcast(player);
                    player.playSound(net.minecraft.sounds.SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 1.0F);
                }
            }
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessTooltip(ItemStack stack, CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        ItemStack stored = getStoredItem(stack);
        int count = getStoredCount(stack);

        if (stored.isEmpty() || count <= 0) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        ItemStack display = stored.copy();
        display.setCount(Math.min(count, display.getMaxStackSize()));
        cir.setReturnValue(Optional.of(new BottomlessBundleTooltip(display, count)));
    }

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        cir.setReturnValue(getStoredCount(stack) > 0);
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        float fullness = BottomlessBundleStorage.getFullness(stack);
        cir.setReturnValue(Math.min(1 + Mth.floor(fullness * 12.0f), 13));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        cir.setReturnValue(getStoredCount(stack) >= BottomlessBundleStorage.MAX_COUNT
                ? FULL_BAR_COLOR
                : BAR_COLOR);
    }

    @Inject(method = "dropContent", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessDropContent(
            Level level,
            Player player,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        ItemStack stored = BottomlessBundleStorage.getStoredItem(stack);
        int count = BottomlessBundleStorage.getStoredCount(stack);

        if (stored.isEmpty() || count <= 0) {
            ci.cancel();
            return;
        }

        int amount = Math.max(1, Math.min(count, stored.getMaxStackSize()));

        ItemStack extracted = stored.copy();
        extracted.setCount(amount);

        BottomlessBundleStorage.removeAmount(stack, amount);

        player.drop(extracted, true);

        level.playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.BUNDLE_DROP_CONTENTS,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.8F,
                0.8F + level.getRandom().nextFloat() * 0.4F
        );

        ci.cancel();
    }

    @Inject(method = "removeOneItemFromBundle", at = @At("HEAD"), cancellable = true)
    private static void temmmer$bottomlessRemoveOneItemFromBundle(
            ItemStack stack,
            Player player,
            BundleContents bundleContents,
            CallbackInfoReturnable<Optional<ItemStack>> cir
    ) {
        if (!BottomlessBundleStorage.isBottomlessBundle(stack)) {
            return;
        }

        ItemStack stored = BottomlessBundleStorage.getStoredItem(stack);
        int count = BottomlessBundleStorage.getStoredCount(stack);

        if (stored.isEmpty() || count <= 0) {
            cir.setReturnValue(Optional.empty());
            return;
        }

        int amount = Math.max(1, Math.min(count, stored.getMaxStackSize()));

        ItemStack extracted = stored.copy();
        extracted.setCount(amount);

        BottomlessBundleStorage.removeAmount(stack, amount);

        player.playSound(
                net.minecraft.sounds.SoundEvents.BUNDLE_REMOVE_ONE,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F
        );

        cir.setReturnValue(Optional.of(extracted));
    }

    @Inject(method = "onDestroyed", at = @At("HEAD"), cancellable = true)
    private void temmmer$bottomlessOnDestroyed(ItemEntity itemEntity, CallbackInfo ci) {
    }

    @Unique
    private static void temmmer$broadcast(Player player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != null) {
            menu.slotsChanged(player.getInventory());
        }
    }
}