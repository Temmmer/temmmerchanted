package com.temmmer.enchanting_table_rewrite;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import com.temmmer.ModConfig;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class CustomEnchantmentScreen extends AbstractContainerScreen<CustomEnchantmentMenu> {

    private static final Identifier BG_TEXTURE =
            Identifier.fromNamespaceAndPath("temmmerchanted", "textures/gui/container/custom_enchanting_table.png");

    private static final Identifier[] ENABLED_LEVEL_SPRITES = new Identifier[]{
            Identifier.withDefaultNamespace("container/enchanting_table/level_1"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_2"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_3")
    };

    private static final Identifier[] DISABLED_LEVEL_SPRITES = new Identifier[]{
            Identifier.withDefaultNamespace("container/enchanting_table/level_1_disabled"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_2_disabled"),
            Identifier.withDefaultNamespace("container/enchanting_table/level_3_disabled")
    };

    private static final Identifier ENCHANTMENT_SLOT_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
    private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    private static final Identifier ENCHANTMENT_SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot");

    private static final Identifier SCROLLER_SPRITE =
            Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE =
            Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    private static final int BG_WIDTH = 256;
    private static final int BG_HEIGHT = 256;

    private static final int BG_Y_OFFSET = 3;

    private static final double SCROLL_UNITS_PER_ROW = 3.0D;
    private double scrollAccumulator = 0.0D;

    private static final int GUI_WIDTH = 184;
    private static final int GUI_HEIGHT = 170;

    private static final int BOOK_SLOT_X = 22;
    private static final int BOOK_SLOT_Y = 21;

    private static final int LAPIS_SLOT_X = 42;
    private static final int LAPIS_SLOT_Y = 21;

    private static final int OUTPUT_SLOT_X = 32;
    private static final int OUTPUT_SLOT_Y = 55;

    private static final int INVENTORY_TOP_LEFT_X = 17;
    private static final int INVENTORY_TOP_LEFT_Y = 84;

    private static final int XP_BOX_CENTER_X = 160;

    private static final int XP_BOX_CENTER_Y_0 = 23 + BG_Y_OFFSET;
    private static final int XP_BOX_CENTER_Y_1 = 42 + BG_Y_OFFSET;
    private static final int XP_BOX_CENTER_Y_2 = 61 + BG_Y_OFFSET;

    private static final int SCROLLBAR_LEFT_X = 164;
    private static final int SCROLLBAR_TOP_Y = 14 + BG_Y_OFFSET;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 57;
    private static final int SCROLLER_HEIGHT = 15;

    private static final int ROW_X = 50;
    private static final int ROW_Y_0 = 14 + BG_Y_OFFSET;
    private static final int ROW_SPACING = 19;
    private static final int ROW_WIDTH = 108;
    private static final int ROW_HEIGHT = 19;

    private static final int COST_TEXT_X = 68;
    private static final int COST_TEXT_Y_0 = 22 + BG_Y_OFFSET;
    private static final int COST_TEXT_Y_1 = 41 + BG_Y_OFFSET;
    private static final int COST_TEXT_Y_2 = 60 + BG_Y_OFFSET;

    private boolean scrolling = false;

    public CustomEnchantmentScreen(CustomEnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.titleLabelX = 8;
        this.titleLabelY = 3 + BG_Y_OFFSET;
        this.inventoryLabelY = 72 + BG_Y_OFFSET + 1;
        this.inventoryLabelX = 12;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BG_TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                BG_WIDTH,
                BG_HEIGHT
        );

        this.renderScrollbar(guiGraphics, mouseX, mouseY);
        this.renderEnchantRows(guiGraphics, mouseX, mouseY);
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos + SCROLLBAR_LEFT_X;
        int y = this.topPos + SCROLLBAR_TOP_Y;

        boolean canScroll = this.menu.canScroll();
        if (this.insideScrollbar(mouseX, mouseY) && canScroll) {
            guiGraphics.requestCursor(this.scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        }

        Identifier sprite = canScroll ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        int thumbY = y + this.getScrollerPixelOffset();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, thumbY, SCROLLBAR_WIDTH, SCROLLER_HEIGHT);
    }

    private static int getXpBoxCenterY(int row) {
        return switch (row) {
            case 0 -> XP_BOX_CENTER_Y_0;
            case 1 -> XP_BOX_CENTER_Y_1;
            default -> XP_BOX_CENTER_Y_2;
        };
    }

    private int getScrollerPixelOffset() {
        int maxOffset = this.menu.getMaxScrollOffset();
        if (maxOffset <= 0) {
            return 0;
        }

        int movable = SCROLLBAR_HEIGHT - SCROLLER_HEIGHT;
        float progress = (float) this.menu.getScrollOffset() / (float) maxOffset;
        return Mth.floor(progress * movable);
    }

    private void renderEnchantRows(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (int row = 0; row < 3; row++) {
            int rowX = this.leftPos + ROW_X;
            int rowY = this.topPos + getRowY(row);

            if (!this.menu.hasVisibleOption(row)) {
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ENCHANTMENT_SLOT_DISABLED_SPRITE,
                        rowX,
                        rowY,
                        ROW_WIDTH,
                        ROW_HEIGHT
                );
                continue;
            }

            boolean hovered = this.isHovering(ROW_X, getRowY(row), ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY);
            boolean selected = this.menu.isVisibleOptionSelected(row);
            boolean affordable = this.menu.isVisibleOptionAffordable(this.minecraft.player, row);

            int textColor = -9937334;

            if (selected) {
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ENCHANTMENT_SLOT_DISABLED_SPRITE,
                        rowX,
                        rowY,
                        ROW_WIDTH,
                        ROW_HEIGHT
                );
                textColor = ARGB.opaque((textColor & 16711422) >> 1);
            } else if (!affordable) {
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ENCHANTMENT_SLOT_DISABLED_SPRITE,
                        rowX,
                        rowY,
                        ROW_WIDTH,
                        ROW_HEIGHT
                );
                textColor = ARGB.opaque((textColor & 16711422) >> 1);
            } else if (hovered) {
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ENCHANTMENT_SLOT_HIGHLIGHTED_SPRITE,
                        rowX,
                        rowY,
                        ROW_WIDTH,
                        ROW_HEIGHT
                );
                guiGraphics.requestCursor(CursorTypes.POINTING_HAND);
                textColor = -128;
            } else {
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        ENCHANTMENT_SLOT_SPRITE,
                        rowX,
                        rowY,
                        ROW_WIDTH,
                        ROW_HEIGHT
                );
                textColor = -1;
            }

            Optional<Holder.Reference<Enchantment>> optional =
                    this.minecraft.level.registryAccess()
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .get(this.menu.getVisibleEnchantmentId(row));

            String enchantText;
            if (optional.isPresent()) {
                enchantText = Enchantment.getFullname(optional.get(), this.menu.getVisibleLevel(row)).getString();
            } else {
                enchantText = "Unknown";
            }

            String displayedText = this.getDisplayedEnchantText(row, enchantText, affordable);
            int textOffsetX = ModConfig.INSTANCE.removePlayerXp ? 4 : 20;

            guiGraphics.drawString(
                    this.font,
                    displayedText,
                    rowX + textOffsetX,
                    rowY + 6,
                    textColor,
                    false
            );

            if (!ModConfig.INSTANCE.removePlayerXp) {
                int iconIndex = this.getPowerTierIconIndex();
                Identifier levelSprite;

                if (iconIndex < 0) {
                    levelSprite = DISABLED_LEVEL_SPRITES[2];
                } else {
                    levelSprite = affordable ? ENABLED_LEVEL_SPRITES[iconIndex] : DISABLED_LEVEL_SPRITES[iconIndex];
                }

                int badgeX = this.leftPos + COST_TEXT_X - 16;
                int badgeY = rowY + 2;

                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        levelSprite,
                        badgeX,
                        badgeY,
                        16,
                        16
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean repeated) {
        if (event.button() == 0) {
            if (this.insideScrollbar(event.x(), event.y()) && this.menu.canScroll()) {
                this.scrolling = true;
                this.applyScrollFromMouse(event.y());
                return true;
            }

            for (int row = 0; row < 3; row++) {
                if (this.isHovering(ROW_X, getRowY(row), ROW_WIDTH, ROW_HEIGHT, event.x(), event.y())) {
                    if (this.menu.hasVisibleOption(row)) {
                        int button = CustomEnchantmentMenu.BUTTON_ROW_0 + row;
                        this.menu.clickMenuButton(this.minecraft.player, button);
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(event, repeated);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.scrolling && this.menu.canScroll()) {
            this.applyScrollFromMouse(event.y());
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            this.scrolling = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        if (!this.menu.canScroll()) {
            return false;
        }

        this.scrollAccumulator += verticalAmount;

        int steps = 0;

        while (this.scrollAccumulator >= SCROLL_UNITS_PER_ROW) {
            steps++;
            this.scrollAccumulator -= SCROLL_UNITS_PER_ROW;
        }

        while (this.scrollAccumulator <= -SCROLL_UNITS_PER_ROW) {
            steps--;
            this.scrollAccumulator += SCROLL_UNITS_PER_ROW;
        }

        if (steps == 0) {
            return true;
        }

        int current = this.menu.getScrollOffset();
        int next = current - steps;
        next = Mth.clamp(next, 0, this.menu.getMaxScrollOffset());

        if (next != current) {
            int button = CustomEnchantmentMenu.BUTTON_SCROLL_TO_OFFSET_BASE + next;

            this.menu.clickMenuButton(this.minecraft.player, button);
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }

        return true;
    }

    private void applyScrollFromMouse(double mouseY) {
        int maxOffset = this.menu.getMaxScrollOffset();
        if (maxOffset <= 0) {
            return;
        }

        float localY = (float) mouseY - (float) (this.topPos + SCROLLBAR_TOP_Y);
        float clamped = Mth.clamp(localY - (SCROLLER_HEIGHT / 2.0F), 0.0F, (float) (SCROLLBAR_HEIGHT - SCROLLER_HEIGHT));
        float progress = clamped / (float) (SCROLLBAR_HEIGHT - SCROLLER_HEIGHT);

        int offset = Math.round(progress * maxOffset);
        offset = Mth.clamp(offset, 0, maxOffset);

        int button = CustomEnchantmentMenu.BUTTON_SCROLL_TO_OFFSET_BASE + offset;
        if (this.menu.clickMenuButton(this.minecraft.player, button)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderEnchantmentTooltips(guiGraphics, mouseX, mouseY);
    }

    private void renderEnchantmentTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Player player = this.minecraft.player;

        for (int row = 0; row < 3; row++) {
            if (!this.menu.hasVisibleOption(row)) {
                continue;
            }

            if (!this.isHovering(ROW_X, getRowY(row), ROW_WIDTH, ROW_HEIGHT, mouseX, mouseY)) {
                continue;
            }

            if (!this.menu.hasEnoughBookshelves()) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal("Not enough bookshelf power").withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal("Need at least 5 bookshelves").withStyle(ChatFormatting.GRAY));
                guiGraphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
                break;
            }

            int enchantmentId = this.menu.getVisibleEnchantmentId(row);
            int level = this.menu.getVisibleLevel(row);
            int xpCost = this.menu.getVisibleCost(row);
            int lapisCost = this.menu.getVisibleLapisCost(row);

            Optional<Holder.Reference<Enchantment>> optional =
                    this.minecraft.level.registryAccess()
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .get(enchantmentId);

            if (optional.isEmpty()) {
                continue;
            }

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(
                    "container.enchant.clue",
                    Enchantment.getFullname(optional.get(), level)
            ).withStyle(ChatFormatting.WHITE));

            tooltip.add(Component.empty());

            if (!ModConfig.INSTANCE.removePlayerXp && player.experienceLevel < xpCost && !player.hasInfiniteMaterials()) {
                tooltip.add(Component.translatable(
                        "container.enchant.level.requirement",
                        xpCost
                ).withStyle(ChatFormatting.RED));
            } else {
                if (lapisCost > 0) {
                    MutableComponent lapisLine = lapisCost == 1
                            ? Component.translatable("container.enchant.lapis.one")
                            : Component.translatable("container.enchant.lapis.many", lapisCost);

                    tooltip.add(lapisLine.withStyle(
                            this.menu.getGoldCount() >= lapisCost || player.hasInfiniteMaterials()
                                    ? ChatFormatting.GRAY
                                    : ChatFormatting.RED
                    ));
                }

                if (!ModConfig.INSTANCE.removePlayerXp && xpCost > 0) {
                    MutableComponent levelLine = xpCost == 1
                            ? Component.translatable("container.enchant.level.one")
                            : Component.translatable("container.enchant.level.many", xpCost);

                    tooltip.add(levelLine.withStyle(ChatFormatting.GRAY));
                }
            }

            guiGraphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
            break;
        }
    }

    private boolean insideScrollbar(double mouseX, double mouseY) {
        int left = this.leftPos + SCROLLBAR_LEFT_X;
        int top = this.topPos + SCROLLBAR_TOP_Y;
        int right = left + SCROLLBAR_WIDTH;
        int bottom = top + SCROLLBAR_HEIGHT;

        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }

    private static int getRowY(int row) {
        return ROW_Y_0 + ROW_SPACING * row;
    }

    private int getPowerTierIconIndex() {
        int power = this.menu.getEnchantingPower();

        if (power >= 18) {
            return 0;
        }
        if (power >= 10) {
            return 1;
        }
        if (power >= 5) {
            return 2;
        }

        return -1;
    }

    private static final String[] RUNES = {
            "ᔑ", "ʖ", "ᓵ", "↸", "ᒷ", "⎓", "⊣", "⍑", "╎", "⋮",
            "ꖌ", "ꖎ", "ᒲ", "リ", "𝙹", "¡", "ᑑ", "∷", "ᓭ", "ℸ",
            "⚍", "⍊", "∴", "̇/", "||", "⨅"
    };

    private String buildRuneText(int row) {
        int seed = this.menu.getEnchantmentSeedForClient() + row * 31;
        java.util.Random random = new java.util.Random(seed);

        int words = 3 + random.nextInt(2);
        StringBuilder sb = new StringBuilder();

        for (int w = 0; w < words; w++) {
            int len = 3 + random.nextInt(5);
            for (int i = 0; i < len; i++) {
                sb.append(RUNES[random.nextInt(RUNES.length)]);
            }
            if (w + 1 < words) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    private String getDisplayedEnchantText(int row, String enchantText, boolean affordable) {
        int maxNameLength = ModConfig.INSTANCE.removePlayerXp ? 20 : 13;
        String dots = " . .";

        if (enchantText.length() <= maxNameLength) {
            return enchantText;
        }

        String suffix = dots;
        while (!suffix.isEmpty() && maxNameLength + suffix.length() > 19) {
            suffix = suffix.substring(0, suffix.length() - 1);
        }

        return enchantText.substring(0, maxNameLength) + suffix;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }
}
