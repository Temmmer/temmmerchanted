package com.temmmer.client;

import com.temmmer.ModMenuTypes;
import com.temmmer.bottomless.BottomlessBundleTooltip;
import com.temmmer.bottomless.BottomlessBundleTooltipComponent;
import net.fabricmc.api.ClientModInitializer;
import com.temmmer.enchanting_table_rewrite.CustomEnchantmentScreen;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.MenuScreens;

public class TemmmerchantedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.CUSTOM_ENCHANTMENT, CustomEnchantmentScreen::new);
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof BottomlessBundleTooltip tooltip) {
                return new BottomlessBundleTooltipComponent(tooltip);
            }
            return null;
        });
    }
}