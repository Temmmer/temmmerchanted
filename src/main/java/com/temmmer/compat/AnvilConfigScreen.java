package com.temmmer.compat;

import com.temmmer.ModConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AnvilConfigScreen extends Screen {

    private final Screen parent;

    private EditBox anvilEnchantCostBox;
    private EditBox anvilCurseCostBox;
    private EditBox anvilMinCostBox;
    private EditBox anvilMaxCostBox;

    public AnvilConfigScreen(Screen parent) {
        super(Component.literal("Anvil Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ensureAnvilConfig();

        int centerX = this.width / 2;
        int leftX = centerX - 140;
        int rightX = centerX + 25;
        int fieldWidth = 115;
        int fieldHeight = 18;
        int y = 20;

        addFieldLabel("Enchant Cost Increase", leftX, y - 10, fieldWidth);
        addFieldLabel("Curse Cost Discount", rightX, y - 10, fieldWidth);

        this.anvilEnchantCostBox = addNumberBox(leftX, y, fieldWidth, fieldHeight,
                String.valueOf(ModConfig.INSTANCE.anvilCostConfig.enchantCost));
        this.anvilCurseCostBox = addNumberBox(rightX, y, fieldWidth, fieldHeight,
                String.valueOf(ModConfig.INSTANCE.anvilCostConfig.curseCost));

        y += 33;

        addFieldLabel("Anvil Min Repair Cost", leftX, y - 10, fieldWidth);
        addFieldLabel("Anvil Max Repair Cost", rightX, y - 10, fieldWidth);

        this.anvilMinCostBox = addNumberBox(leftX, y, fieldWidth, fieldHeight,
                String.valueOf(ModConfig.INSTANCE.anvilCostConfig.minCost));
        this.anvilMaxCostBox = addNumberBox(rightX, y, fieldWidth, fieldHeight,
                String.valueOf(ModConfig.INSTANCE.anvilCostConfig.maxCost));

        y += 40;

        this.addRenderableWidget(
                Button.builder(Component.literal("Save"), button -> {
                    saveValues();
                    ModConfig.save();
                    assert this.minecraft != null;
                    this.minecraft.setScreen(this.parent);
                }).bounds(centerX - 102, y, 100, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Back"), button -> {
                    assert this.minecraft != null;
                    this.minecraft.setScreen(this.parent);
                }).bounds(centerX + 2, y, 100, 20).build()
        );
    }

    private void addFieldLabel(String text, int x, int y, int width) {
        this.addRenderableWidget(new StringWidget(x, y, width, 9, Component.literal(text), this.font));
    }

    private EditBox addNumberBox(int x, int y, int width, int height, String initialValue) {
        EditBox box = new EditBox(this.font, x, y, width, height, Component.empty());
        box.setValue(initialValue);
        box.setMaxLength(12);
        box.setFilter(value -> value.isEmpty() || value.matches("-?\\d*"));
        this.addRenderableWidget(box);
        return box;
    }

    private static void ensureAnvilConfig() {
        if (ModConfig.INSTANCE.anvilCostConfig == null) {
            ModConfig.INSTANCE.anvilCostConfig = new ModConfig.AnvilCostConfig();
        }
    }

    private void saveValues() {
        ensureAnvilConfig();

        ModConfig.INSTANCE.anvilCostConfig.enchantCost = parseIntOrFallback(
                this.anvilEnchantCostBox.getValue(),
                ModConfig.INSTANCE.anvilCostConfig.enchantCost
        );
        ModConfig.INSTANCE.anvilCostConfig.curseCost = parseIntOrFallback(
                this.anvilCurseCostBox.getValue(),
                ModConfig.INSTANCE.anvilCostConfig.curseCost
        );
        ModConfig.INSTANCE.anvilCostConfig.minCost = parseIntOrFallback(
                this.anvilMinCostBox.getValue(),
                ModConfig.INSTANCE.anvilCostConfig.minCost
        );
        ModConfig.INSTANCE.anvilCostConfig.maxCost = parseIntOrFallback(
                this.anvilMaxCostBox.getValue(),
                ModConfig.INSTANCE.anvilCostConfig.maxCost
        );

        if (ModConfig.INSTANCE.anvilCostConfig.minCost < 0) {
            ModConfig.INSTANCE.anvilCostConfig.minCost = 0;
        }

        if (ModConfig.INSTANCE.anvilCostConfig.maxCost < ModConfig.INSTANCE.anvilCostConfig.minCost) {
            ModConfig.INSTANCE.anvilCostConfig.maxCost = ModConfig.INSTANCE.anvilCostConfig.minCost;
        }
    }

    private static int parseIntOrFallback(String value, int fallback) {
        if (value == null || value.isBlank() || value.equals("-")) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}