package com.temmmer.mixin;

import com.temmmer.ModEnchantmentTags;
import com.temmmer.ModConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Shadow @Final
    private DataSlot cost;

    @Shadow
    private int repairItemCountCost;

    @Unique
    private boolean temmmer$allowZeroCostPickup;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void temmmer$applyCustomRepairRules(CallbackInfo ci) {
        this.temmmer$allowZeroCostPickup = false;

        if (ModConfig.INSTANCE.removePlayerXp) {
            ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) (Object) this;
            Container inputSlots = accessor.temmmer$getInputSlots();
            ResultContainer resultSlots = accessor.temmmer$getResultSlots();

            ItemStack left = inputSlots.getItem(0);
            ItemStack right = inputSlots.getItem(1);
            ItemStack output = resultSlots.getItem(0);

            if (!left.isEmpty() && !right.isEmpty() && !output.isEmpty() && left.isDamageableItem()) {
                boolean repairWithMaterial = left.isValidRepairItem(right);
                boolean combineSameTools = right.isDamageableItem() && left.getItem() == right.getItem();

                if (repairWithMaterial) {
                    temmmer$applyCustomMaterialRepair(left, right, output);
                }

                if (repairWithMaterial || combineSameTools) {
                    this.cost.set(0);
                    this.temmmer$allowZeroCostPickup = true;
                }
            } else if (!output.isEmpty()) {
                this.cost.set(0);
                this.temmmer$allowZeroCostPickup = true;
            }

            return;
        }

        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) (Object) this;
        Container inputSlots = accessor.temmmer$getInputSlots();
        ResultContainer resultSlots = accessor.temmmer$getResultSlots();

        ItemStack left = inputSlots.getItem(0);
        ItemStack right = inputSlots.getItem(1);
        ItemStack output = resultSlots.getItem(0);

        if (left.isEmpty() || right.isEmpty() || output.isEmpty()) {
            return;
        }

        if (!left.isDamageableItem()) {
            return;
        }

        boolean repairWithMaterial = left.isValidRepairItem(right);
        boolean combineSameTools = right.isDamageableItem() && left.getItem() == right.getItem();

        if (!repairWithMaterial && !combineSameTools) {
            return;
        }

        if (repairWithMaterial) {
            temmmer$applyCustomMaterialRepair(left, right, output);
        }

        var outputEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(output);

        if (outputEnchantments.isEmpty()) {
            this.cost.set(0);
            this.temmmer$allowZeroCostPickup = true;
            return;
        }

        int customCost = 0;

        for (var entry : outputEnchantments.entrySet()) {
            Holder<Enchantment> enchantmentHolder = entry.getKey();

            if (enchantmentHolder.is(ModEnchantmentTags.CURSES)) {
                customCost += ModConfig.INSTANCE.anvilCostConfig.curseCost;
            } else {
                customCost += ModConfig.INSTANCE.anvilCostConfig.enchantCost;
            }
        }

        if (customCost < ModConfig.INSTANCE.anvilCostConfig.minCost) {
            customCost = ModConfig.INSTANCE.anvilCostConfig.minCost;
        } else if (customCost > ModConfig.INSTANCE.anvilCostConfig.maxCost) {
            customCost = ModConfig.INSTANCE.anvilCostConfig.maxCost;
        }

        this.cost.set(customCost);

        if (customCost == 0) {
            this.temmmer$allowZeroCostPickup = true;
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void temmmer$allowZeroCostRepairPickup(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
        if (!this.temmmer$allowZeroCostPickup) {
            return;
        }

        ItemCombinerMenuAccessor accessor = (ItemCombinerMenuAccessor) (Object) this;
        ItemStack output = accessor.temmmer$getResultSlots().getItem(0);

        if (!output.isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private void temmmer$applyCustomMaterialRepair(ItemStack left, ItemStack right, ItemStack output) {
        if (!left.isValidRepairItem(right)) {
            return;
        }

        int maxDamage = left.getMaxDamage();
        if (maxDamage <= 0) {
            return;
        }

        int currentDamage = left.getDamageValue();
        if (currentDamage <= 0) {
            return;
        }

        boolean hasMending = temmmer$hasMending(left) || temmmer$hasMending(output);

        if (right.getItem() == Items.NETHERITE_INGOT) {
            int repairAmount = hasMending ? maxDamage : (maxDamage * 3) / 4;
            if (repairAmount <= 0) {
                return;
            }

            int newDamage = currentDamage - repairAmount;
            if (newDamage < 0) {
                newDamage = 0;
            }

            output.setDamageValue(newDamage);
            this.repairItemCountCost = 1;
            return;
        }

        int perItemRepair = Math.max(maxDamage / 4, 1);

        if (hasMending) {
            perItemRepair *= 2;
        }

        int materialCount = this.repairItemCountCost;
        if (materialCount <= 0) {
            return;
        }

        int totalRepair = perItemRepair * materialCount;
        int newDamage = currentDamage - totalRepair;
        if (newDamage < 0) {
            newDamage = 0;
        }

        output.setDamageValue(newDamage);
    }

    @Unique
    private static boolean temmmer$hasMending(ItemStack stack) {
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantmentHolder = entry.getKey();
            if (enchantmentHolder.is(Enchantments.MENDING)) {
                return true;
            }
        }

        return false;
    }
}