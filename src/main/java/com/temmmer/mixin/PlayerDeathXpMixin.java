package com.temmmer.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDeathXpMixin {

    @Unique private static final int MIN_LEVEL_AFTER_DEATH = 20;
    @Unique private static final int MAX_LEVELS_LOST_ON_DEATH = 10;

    @Inject(method = "getBaseExperienceReward", at = @At("HEAD"), cancellable = true)
    private void vw$capDeathXpDrop(ServerLevel level, CallbackInfoReturnable<Integer> cir) {
        Player self = (Player) (Object) this;
        int currentLevel = self.experienceLevel;

        if (currentLevel <= MIN_LEVEL_AFTER_DEATH) {
            cir.setReturnValue(0);
            return;
        }

        int targetLevel = Math.max(MIN_LEVEL_AFTER_DEATH, currentLevel - MAX_LEVELS_LOST_ON_DEATH);

        int droppedXp = 0;
        for (int lvl = targetLevel; lvl < currentLevel; lvl++) {
            droppedXp += vw$getFlatXpCostForLevel(lvl);
        }

        cir.setReturnValue(droppedXp);
    }

    @Unique
    private static int vw$getFlatXpCostForLevel(int lvl) {
        if (lvl < 10) {
            return 12;
        }

        if (lvl < 20) {
            return 28;
        }

        if (lvl < 30) {
            return 60;
        }

        return 60;
    }
}
