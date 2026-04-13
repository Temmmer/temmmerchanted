package com.temmmer.compat;

import com.temmmer.ModConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen extends Screen {

    private final Screen parent;

    private Button removeXpButton;
    private EditBox spawnerXpDropBox;

    public ModConfigScreen(Screen parent) {
        super(Component.literal("Temmmerchanted Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = 140;
        int fieldHeight = 18;
        int y = this.height / 2 - 70;

        addFieldLabel("Spawner XP Drop", centerX - (fieldWidth / 2), y - 10, fieldWidth);
        this.spawnerXpDropBox = addNumberBox(
                centerX - (fieldWidth / 2),
                y,
                fieldWidth,
                fieldHeight,
                String.valueOf(ModConfig.INSTANCE.spawnerXpDropAmount)
        );

        y += 28;

        this.removeXpButton = this.addRenderableWidget(
                Button.builder(getRemoveXpText(), button -> {
                    ModConfig.INSTANCE.removePlayerXp = !ModConfig.INSTANCE.removePlayerXp;
                    refreshButtonTexts();
                }).bounds(centerX - 100, y, 200, 20).build()
        );

        y += 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Enchanting Settings"), button -> {
                    saveValues();
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new EnchantingConfigScreen(this));
                }).bounds(centerX - 100, y, 200, 20).build()
        );

        y += 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Anvil Settings"), button -> {
                    saveValues();
                    assert this.minecraft != null;
                    this.minecraft.setScreen(new AnvilConfigScreen(this));
                }).bounds(centerX - 100, y, 200, 20).build()
        );

        y += 28;

        this.addRenderableWidget(
                Button.builder(Component.literal("Done"), button -> {
                    saveValues();
                    ModConfig.save();
                    assert this.minecraft != null;
                    this.minecraft.setScreen(this.parent);
                }).bounds(centerX - 100, y, 200, 20).build()
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

    private void refreshButtonTexts() {
        if (this.removeXpButton != null) {
            this.removeXpButton.setMessage(getRemoveXpText());
        }
    }

    private static Component getRemoveXpText() {
        return Component.literal("Remove Player XP: " + (ModConfig.INSTANCE.removePlayerXp ? "ON" : "OFF"));
    }

    private void saveValues() {
        ModConfig.INSTANCE.spawnerXpDropAmount = parseIntOrFallback(
                this.spawnerXpDropBox.getValue(),
                ModConfig.INSTANCE.spawnerXpDropAmount
        );

        if (ModConfig.INSTANCE.spawnerXpDropAmount < 0) {
            ModConfig.INSTANCE.spawnerXpDropAmount = 0;
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
        saveValues();
        ModConfig.save();
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}