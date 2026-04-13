package com.temmmer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantmentTags {
    public static final TagKey<Enchantment> CURSES =
            TagKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath("temmmerchanted", "curses")
            );

    public static final TagKey<Enchantment> BLACKLISTED_ENCHANTMENTS =
            TagKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "blacklisted_enchantments")
            );

    public static final TagKey<Enchantment> BOTTOMLESS =
            TagKey.create(
                    Registries.ENCHANTMENT,
                    Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "bottomless")
            );

    private ModEnchantmentTags() {
    }
}
