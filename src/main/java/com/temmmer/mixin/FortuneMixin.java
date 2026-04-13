package com.temmmer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public class FortuneMixin {

    @ModifyReturnValue(
            method = "getItemEnchantmentLevel",
            at = @At("RETURN")
    )
    private static int temmmer$treatFortuneAsThree(int original,
                                                   Holder<Enchantment> enchantment,
                                                   ItemStack stack) {

        if (original > 0 && enchantment.is(Enchantments.FORTUNE)) {
            return 3;
        }

        return original;
    }
}