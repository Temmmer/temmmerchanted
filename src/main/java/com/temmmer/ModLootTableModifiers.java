package com.temmmer;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class ModLootTableModifiers {
    private ModLootTableModifiers() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            if (!key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
                return;
            }

            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> bottomlessKey =
                    ResourceKey.create(
                            Registries.ENCHANTMENT,
                            Identifier.fromNamespaceAndPath(Temmmerchanted.MOD_ID, "bottomless")
                    );

            var bottomless = registries.lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(bottomlessKey);

            ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchants.set(bottomless, 1);

            LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator.binomial(1, 0.33F))
                    .add(
                            LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                    .apply(
                                            net.minecraft.world.level.storage.loot.functions.SetComponentsFunction
                                                    .setComponent(DataComponents.STORED_ENCHANTMENTS, enchants.toImmutable())
                                    )
                    );

            tableBuilder.withPool(pool);

            tableBuilder.withPool(pool);
        });
    }
}