package com.temmmer;

import com.temmmer.enchanting_table_rewrite.CustomEnchantmentMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {

    public static final MenuType<CustomEnchantmentMenu> CUSTOM_ENCHANTMENT =
            Registry.register(
                    BuiltInRegistries.MENU,
                    Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "custom_enchantment"),
                    new MenuType<>(
                            CustomEnchantmentMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    private ModMenuTypes() {
    }

    public static void init() {
    }
}