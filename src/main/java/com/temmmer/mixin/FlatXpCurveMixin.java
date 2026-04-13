package com.temmmer.mixin;

import com.temmmer.ModConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FlatXpCurveMixin {

    @Unique private static final int COST_LVLS_1_TO_10  = 20;
    @Unique private static final int COST_LVLS_11_TO_20 = 20;
    @Unique private static final int COST_LVLS_21_TO_30 = 20;
    @Unique private static final int COST_30_PLUS       = 20;

    @Unique private static final float HEAL_PER_XP = 0.2f;

    @Inject(method = "getXpNeededForNextLevel()I", at = @At("HEAD"), cancellable = true)
    private void temmmerchanted$flattenXpCurve(CallbackInfoReturnable<Integer> cir) {
        if (ModConfig.INSTANCE.removePlayerXp) {
            return;
        }

        Player self = (Player) (Object) this;
        int lvl = self.experienceLevel;

        if (lvl < 10) {
            cir.setReturnValue(COST_LVLS_1_TO_10);
            return;
        }

        if (lvl < 20) {
            cir.setReturnValue(COST_LVLS_11_TO_20);
            return;
        }

        if (lvl < 30) {
            cir.setReturnValue(COST_LVLS_21_TO_30);
            return;
        }

        cir.setReturnValue(COST_30_PLUS);
    }

    @Inject(method = "giveExperiencePoints(I)V", at = @At("HEAD"), cancellable = true)
    private void temmmerchanted$convertXpToHealing(int points, CallbackInfo ci) {
        if (!ModConfig.INSTANCE.removePlayerXp) {
            return;
        }

        Player self = (Player) (Object) this;

        if (points > 0) {
            self.heal(points * HEAL_PER_XP);
        }

        ci.cancel();
    }
}