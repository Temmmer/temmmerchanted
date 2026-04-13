package com.temmmer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class TemmmerchantedKeys {

    public static final ResourceKey<Enchantment> VAMPIRTOOTH = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "vampirtooth")
    );

    private TemmmerchantedKeys() {}
}