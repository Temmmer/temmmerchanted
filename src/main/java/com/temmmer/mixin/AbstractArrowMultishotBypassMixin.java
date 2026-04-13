package com.temmmer.mixin;

import com.temmmer.CrossbowArrowDuck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMultishotBypassMixin {

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void temmmer$allowOtherCrossbowArrows(EntityHitResult hit, CallbackInfo ci) {
        AbstractArrow self = (AbstractArrow) (Object) this;
        CrossbowArrowDuck duck = (CrossbowArrowDuck) self;

        if (!duck.temmmer$isFromCrossbowExtra()) {
            return;
        }

        Entity target = hit.getEntity();
        if (!(target instanceof LivingEntity living)) {
            return;
        }

        ((EntityAccessor) living).temmmer$setInvulnerableTime(0);
    }
}