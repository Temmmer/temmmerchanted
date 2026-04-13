package com.temmmer.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
    @Unique
    private static final float temmmer$THRASHER_RATIO = 0.75F;

    @Unique
    private static final double temmmer$SHOCKWAVE_RADIUS = 3.5D;

    @Shadow
    public abstract float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource);

    @Shadow
    private static Predicate<LivingEntity> knockbackPredicate(Entity entity, Entity entity2) {
        throw new AssertionError();
    }

    @Inject(method = "knockback", at = @At("TAIL"))
    private static void temmmer$applyThrasher(Level level, Entity entity, Entity entity2, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (!(entity2 instanceof LivingEntity mainTarget)) {
            return;
        }

        if (!(player.getWeaponItem().getItem() instanceof MaceItem maceItem)) {
            return;
        }

        Holder<Enchantment> thrasherHolder = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.DENSITY);

        int thrasherLevel = EnchantmentHelper.getItemEnchantmentLevel(thrasherHolder, player.getWeaponItem());
        if (thrasherLevel <= 0) {
            return;
        }

        DamageSource damageSource = player.damageSources().playerAttack(player);

        float smashBonus = maceItem.getAttackDamageBonus(mainTarget, 0.0F, damageSource);
        if (smashBonus <= 0.0F) {
            return;
        }

        float shockwaveDamage = smashBonus * temmmer$THRASHER_RATIO;
        if (shockwaveDamage <= 0.0F) {
            return;
        }

        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                entity2.getBoundingBox().inflate(temmmer$SHOCKWAVE_RADIUS),
                knockbackPredicate(entity, entity2)
        )) {
            nearby.hurt(damageSource, shockwaveDamage);
        }
    }
}