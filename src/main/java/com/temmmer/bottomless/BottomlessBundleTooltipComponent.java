package com.temmmer.bottomless;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class BottomlessBundleTooltipComponent implements ClientTooltipComponent {
    private static final Identifier SLOT_BACKGROUND =
            Identifier.withDefaultNamespace("container/bundle/slot_background");

    private final ItemStack stack;
    private final int count;

    public BottomlessBundleTooltipComponent(BottomlessBundleTooltip tooltip) {
        this.stack = tooltip.storedItem();
        this.count = tooltip.count();
    }

    @Override
    public int getHeight(Font font) {
        return 24;
    }

    @Override
    public int getWidth(Font font) {
        return 24;
    }

    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        int renderY = y - 2;

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SLOT_BACKGROUND,
                x,
                renderY,
                24,
                24
        );

        if (!this.stack.isEmpty()) {
            guiGraphics.renderItem(this.stack, x + 4, renderY + 4);
            guiGraphics.renderItemDecorations(font, this.stack, x + 4, renderY + 4, String.valueOf(this.count));
        }
    }
}