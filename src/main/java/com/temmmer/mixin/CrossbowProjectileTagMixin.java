package com.temmmer.mixin;

import com.temmmer.Temmmerchanted;
import com.temmmer.CrossbowArrowDuck;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowProjectileTagMixin {

    @Unique
    private static final ResourceKey<Enchantment> DOUBLECHARGE_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "doublecharge")
            );

    @Inject(method = "createProjectile", at = @At("RETURN"))
    private void temmmer$tagCreatedProjectile(
            Level level,
            LivingEntity shooter,
            ItemStack weapon,
            ItemStack ammo,
            boolean isCreative,
            CallbackInfoReturnable<Projectile> cir
    ) {
        if (level.isClientSide()) {
            return;
        }

        Projectile projectile = cir.getReturnValue();
        if (!(projectile instanceof AbstractArrow arrow)) {
            return;
        }

        boolean multishot = temmmer$hasEnchantment(level, weapon, Enchantments.MULTISHOT);
        boolean doublecharge = temmmer$hasEnchantment(level, weapon, DOUBLECHARGE_KEY);

        if (!multishot && !doublecharge) {
            return;
        }

        CrossbowArrowDuck duck = (CrossbowArrowDuck) arrow;
        duck.temmmer$setFromCrossbowExtra(true);
        duck.temmmer$setFromMultishot(multishot);
        duck.temmmer$setFromDoublecharge(doublecharge);
    }

    @Unique
    private static boolean temmmer$hasEnchantment(Level level, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return false;
        }

        Holder.Reference<Enchantment> enchantment =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);

        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) > 0;
    }
}