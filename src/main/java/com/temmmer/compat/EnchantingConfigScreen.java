package com.temmmer.compat;

import com.temmmer.ModConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EnchantingConfigScreen extends Screen {

    private final Screen parent;

    private EditBox xpTier1Box;
    private EditBox xpTier2Box;
    private EditBox xpTier3Box;

    private EditBox lapisTier1Box;
    private EditBox lapisTier2Box;
    private EditBox lapisTier3Box;

    public EnchantingConfigScreen(Screen parent) {
        super(Component.literal("Enchanting Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int leftX = centerX - 140;
        int rightX = centerX + 25;
        int fieldWidth = 115;
        int fieldHeight = 18;
        int y = 25;

        addFieldLabel("XP Cost Max", leftX, y - 10, fieldWidth);
        addFieldLabel("Lapis Cost Max", rightX, y - 10, fieldWidth);

        this.xpTier1Box = addNumberBox(leftX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.xpCostTier1));
        this.lapisTier1Box = addNumberBox(rightX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.lapisCostTier1));

        y += 33;

        addFieldLabel("XP Cost Middle", leftX, y - 10, fieldWidth);
        addFieldLabel("Lapis Cost Middle", rightX, y - 10, fieldWidth);

        this.xpTier2Box = addNumberBox(leftX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.xpCostTier2));
        this.lapisTier2Box = addNumberBox(rightX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.lapisCostTier2));

        y += 33;

        addFieldLabel("XP Cost Min", leftX, y - 10, fieldWidth);
        addFieldLabel("Lapis Cost Min", rightX, y - 10, fieldWidth);

        this.xpTier3Box = addNumberBox(leftX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.xpCostTier3));
        this.lapisTier3Box = addNumberBox(rightX, y, fieldWidth, fieldHeight, String.valueOf(ModConfig.INSTANCE.lapisCostTier3));

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

    private void saveValues() {
        ModConfig.INSTANCE.xpCostTier1 = parseIntOrFallback(this.xpTier1Box.getValue(), ModConfig.INSTANCE.xpCostTier1);
        ModConfig.INSTANCE.xpCostTier2 = parseIntOrFallback(this.xpTier2Box.getValue(), ModConfig.INSTANCE.xpCostTier2);
        ModConfig.INSTANCE.xpCostTier3 = parseIntOrFallback(this.xpTier3Box.getValue(), ModConfig.INSTANCE.xpCostTier3);

        ModConfig.INSTANCE.lapisCostTier1 = parseIntOrFallback(this.lapisTier1Box.getValue(), ModConfig.INSTANCE.lapisCostTier1);
        ModConfig.INSTANCE.lapisCostTier2 = parseIntOrFallback(this.lapisTier2Box.getValue(), ModConfig.INSTANCE.lapisCostTier2);
        ModConfig.INSTANCE.lapisCostTier3 = parseIntOrFallback(this.lapisTier3Box.getValue(), ModConfig.INSTANCE.lapisCostTier3);

        if (ModConfig.INSTANCE.xpCostTier1 < 0) ModConfig.INSTANCE.xpCostTier1 = 0;
        if (ModConfig.INSTANCE.xpCostTier2 < 0) ModConfig.INSTANCE.xpCostTier2 = 0;
        if (ModConfig.INSTANCE.xpCostTier3 < 0) ModConfig.INSTANCE.xpCostTier3 = 0;

        if (ModConfig.INSTANCE.lapisCostTier1 < 0) ModConfig.INSTANCE.lapisCostTier1 = 0;
        if (ModConfig.INSTANCE.lapisCostTier2 < 0) ModConfig.INSTANCE.lapisCostTier2 = 0;
        if (ModConfig.INSTANCE.lapisCostTier3 < 0) ModConfig.INSTANCE.lapisCostTier3 = 0;
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