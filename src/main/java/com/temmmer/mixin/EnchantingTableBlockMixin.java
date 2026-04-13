package com.temmmer.mixin;

import com.temmmer.enchanting_table_rewrite.CustomEnchantmentMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin {

    @Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
    private void temmmer$replaceEnchantingMenu(
            BlockState state,
            Level level,
            BlockPos pos,
            CallbackInfoReturnable<@Nullable MenuProvider> cir
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnchantingTableBlockEntity table) {
            Component title = table.getDisplayName();
            cir.setReturnValue(new SimpleMenuProvider(
                    (syncId, inventory, player) ->
                            new CustomEnchantmentMenu(syncId, inventory, ContainerLevelAccess.create(level, pos)),
                    title
            ));
        }
    }
}