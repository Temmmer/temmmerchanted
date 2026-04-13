package com.temmmer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Unique
    private static final int temmmer$BASE_DURATION_TICKS = 40;

    @Unique
    private static final int temmmer$EXTRA_DURATION_PER_LEVEL = 0;

    @Unique
    private static final int temmmer$MAX_AMPLIFIER = 2;

    @Unique
    private static final float temmmer$VAMPIRTOOTH_HEAL_AMOUNT = 4.0F;

    @WrapOperation(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;attackVisualEffects(Lnet/minecraft/world/entity/Entity;ZZZZF)V"
            )
    )
    private void temmmer$applyCritEffects(
            Player instance,
            Entity target,
            boolean arg1,
            boolean arg2,
            boolean arg3,
            boolean arg4,
            float attackStrengthScale,
            Operation<Void> original
    ) {
        original.call(instance, target, arg1, arg2, arg3, arg4, attackStrengthScale);

        boolean criticalHit = arg2;

        if (!criticalHit) {
            return;
        }

        if (!(target instanceof LivingEntity livingTarget)) {
            return;
        }

        ItemStack weapon = instance.getWeaponItem();
        if (weapon.isEmpty()) {
            return;
        }

        Holder<net.minecraft.world.item.enchantment.Enchantment> baneHolder = instance.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.BANE_OF_ARTHROPODS);

        int baneLevel = EnchantmentHelper.getItemEnchantmentLevel(baneHolder, weapon);
        if (baneLevel > 0) {
            temmmer$applyOrUpgradeSlowness(livingTarget, baneLevel);
        }

        Holder<net.minecraft.world.item.enchantment.Enchantment> vampirtoothHolder = instance.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(com.temmmer.TemmmerchantedKeys.VAMPIRTOOTH);

        int vampirtoothLevel = EnchantmentHelper.getItemEnchantmentLevel(vampirtoothHolder, weapon);
        if (vampirtoothLevel > 0) {
            instance.heal(temmmer$VAMPIRTOOTH_HEAL_AMOUNT);
        }
    }

    @Unique
    private static void temmmer$applyOrUpgradeSlowness(LivingEntity target, int baneLevel) {
        MobEffectInstance current = target.getEffect(MobEffects.SLOWNESS);

        int amplifier = current == null
                ? 0
                : Math.min(current.getAmplifier() + 1, temmmer$MAX_AMPLIFIER);

        int duration = temmmer$BASE_DURATION_TICKS
                + ((baneLevel - 1) * temmmer$EXTRA_DURATION_PER_LEVEL);

        if (current != null
                && current.getAmplifier() >= amplifier
                && current.getDuration() >= duration) {
            return;
        }

        target.addEffect(new MobEffectInstance(
                MobEffects.SLOWNESS,
                duration,
                amplifier,
                false,
                true,
                true
        ));
    }
}