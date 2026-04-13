package com.temmmer.mixin;

import com.temmmer.Temmmerchanted;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class MultiChargeCrossbowMixin {

    private static final ResourceKey<Enchantment> DOUBLECHARGE_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "doublecharge")
            );

    @Inject(method = "tryLoadProjectiles", at = @At("RETURN"))
    private static void temmmer$appendSecondProjectileIfDoublecharge(
            LivingEntity shooter,
            ItemStack weapon,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        if (!(weapon.getItem() instanceof CrossbowItem crossbowItem)) {
            return;
        }

        Level level = shooter.level();
        if (level.isClientSide()) {
            return;
        }

        if (!temmmer$hasDoublecharge(level, weapon)) {
            return;
        }

        ChargedProjectiles charged = weapon.get(DataComponents.CHARGED_PROJECTILES);
        if (charged == null || charged.isEmpty()) {
            return;
        }

        List<ItemStack> currentProjectiles = charged.getItems();
        if (currentProjectiles.size() != 1) {
            return;
        }

        ItemStack secondAmmoSource = temmmer$findSecondProjectile(shooter, crossbowItem);
        if (secondAmmoSource.isEmpty()) {
            return;
        }

        ItemStack secondLoadedProjectile = secondAmmoSource.copyWithCount(1);

        if (shooter instanceof Player player && !player.getAbilities().instabuild) {
            secondAmmoSource.shrink(1);
        }

        List<ItemStack> updatedProjectiles = new ArrayList<>(2);
        updatedProjectiles.add(currentProjectiles.get(0));
        updatedProjectiles.add(secondLoadedProjectile);

        weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(updatedProjectiles));
    }

    private static ItemStack temmmer$findSecondProjectile(LivingEntity shooter, CrossbowItem crossbowItem) {
        if (!(shooter instanceof Player player)) {
            return ItemStack.EMPTY;
        }

        Predicate<ItemStack> supportedProjectiles = crossbowItem.getSupportedHeldProjectiles();

        ItemStack offhand = player.getOffhandItem();
        if (temmmer$isUsableProjectile(offhand, supportedProjectiles)) {
            return offhand;
        }

        ItemStack mainhand = player.getMainHandItem();
        if (temmmer$isUsableProjectile(mainhand, supportedProjectiles)) {
            return mainhand;
        }

        for (int i = 0, size = player.getInventory().getContainerSize(); i < size; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (temmmer$isUsableProjectile(stack, supportedProjectiles)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean temmmer$isUsableProjectile(ItemStack candidate, Predicate<ItemStack> supportedProjectiles) {
        return !candidate.isEmpty()
                && candidate.getCount() > 0
                && supportedProjectiles.test(candidate);
    }

    private static boolean temmmer$hasDoublecharge(Level level, ItemStack stack) {
        var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> enchantment = lookup.getOrThrow(DOUBLECHARGE_KEY);
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) > 0;
    }
}