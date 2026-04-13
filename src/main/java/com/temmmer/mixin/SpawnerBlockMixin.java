package com.temmmer.mixin;

import com.temmmer.ModConfig;
import net.minecraft.world.level.block.SpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SpawnerBlock.class)
public abstract class SpawnerBlockMixin {

    @ModifyArg(
            method = "spawnAfterBreak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/SpawnerBlock;popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V"
            ),
            index = 2
    )
    private int temmmer$boostSpawnerXp(int originalXp) {
        return Math.max(0, ModConfig.INSTANCE.spawnerXpDropAmount);
    }
}