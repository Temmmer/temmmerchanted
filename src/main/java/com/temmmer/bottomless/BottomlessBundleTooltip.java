package com.temmmer.bottomless;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record BottomlessBundleTooltip(ItemStack storedItem, int count) implements TooltipComponent {
}
