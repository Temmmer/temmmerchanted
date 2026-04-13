package com.temmmer.enchanting_table_rewrite;

import com.temmmer.ModEnchantmentTags;
import com.temmmer.ModConfig;
import com.temmmer.ModMenuTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomEnchantmentMenu extends AbstractContainerMenu {

    public static final int BUTTON_ROW_0 = 0;
    public static final int BUTTON_ROW_1 = 1;
    public static final int BUTTON_ROW_2 = 2;
    public static final int BUTTON_SCROLL_TO_OFFSET_BASE = 100;

    private static final int INPUT_SLOT = 0;
    private static final int LAPIS_SLOT = 1;
    private static final int OUTPUT_SLOT = 0;

    private static final int MENU_INPUT_SLOT = 0;
    private static final int MENU_LAPIS_SLOT = 1;
    private static final int MENU_OUTPUT_SLOT = 2;

    private static final int VISIBLE_ROWS = 3;

    private static final int XP_COST = 20;
    private static final int LAPIS_COST = 30;

    private final Container inputSlots;
    private final ResultContainer resultSlots = new ResultContainer();
    private final ContainerLevelAccess access;
    private final RandomSource random = RandomSource.create();
    private final DataSlot enchantmentSeed = DataSlot.standalone();
    private final DataSlot scrollOffset = DataSlot.standalone();
    private final DataSlot totalOptions = DataSlot.standalone();

    private final DataSlot enchantingPower = DataSlot.standalone();
    private final DataSlot currentXpCost = DataSlot.standalone();
    private final DataSlot currentLapisCost = DataSlot.standalone();

    private final int[] visibleEnchantIds = new int[]{-1, -1, -1};
    private final int[] visibleLevels = new int[]{-1, -1, -1};
    private final int[] visibleCosts = new int[]{0, 0, 0};

    private final List<EnchantOption> options = new ArrayList<>();
    private final DataSlot selectedIndex = DataSlot.standalone();

    public CustomEnchantmentMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public CustomEnchantmentMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.CUSTOM_ENCHANTMENT, containerId);
        this.access = access;
        this.addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
        this.addDataSlot(this.scrollOffset);
        this.addDataSlot(this.totalOptions);
        this.addDataSlot(this.selectedIndex);
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 0));
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 1));
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 2));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 0));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 1));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 2));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 0));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 1));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 2));
        this.addDataSlot(this.enchantingPower);
        this.addDataSlot(this.currentXpCost);
        this.addDataSlot(this.currentLapisCost);

        this.scrollOffset.set(0);
        this.totalOptions.set(0);
        this.selectedIndex.set(-1);
        this.enchantingPower.set(0);
        this.currentXpCost.set(0);
        this.currentLapisCost.set(0);

        this.inputSlots = new SimpleContainer(2) {
            @Override
            public void setChanged() {
                super.setChanged();
                CustomEnchantmentMenu.this.slotsChanged(this);
            }
        };

        this.addSlot(new Slot(this.inputSlots, INPUT_SLOT, 8, 17) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.ENCHANTED_BOOK);
            }
        });

        this.addSlot(new Slot(this.inputSlots, LAPIS_SLOT, 28, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }
        });

        this.addSlot(new Slot(this.resultSlots, OUTPUT_SLOT, 18, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                if (!CustomEnchantmentMenu.this.hasValidSelection()) {
                    return false;
                }

                if (CustomEnchantmentMenu.this.resultSlots.getItem(OUTPUT_SLOT).isEmpty()) {
                    return false;
                }

                return CustomEnchantmentMenu.this.isSelectedOptionAffordable(player);
            }

            @Override
            public void onTake(Player player, ItemStack takenStack) {
                CustomEnchantmentMenu.this.finishEnchant(player, takenStack);
                super.onTake(player, takenStack);
            }
        });

        this.addStandardInventorySlots(inventory, 12, 88);

        this.addDataSlot(this.enchantmentSeed).set(inventory.player.getEnchantmentSeed());
        this.addDataSlot(this.scrollOffset);
        this.addDataSlot(this.totalOptions);
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 0));
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 1));
        this.addDataSlot(DataSlot.shared(this.visibleEnchantIds, 2));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 0));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 1));
        this.addDataSlot(DataSlot.shared(this.visibleLevels, 2));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 0));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 1));
        this.addDataSlot(DataSlot.shared(this.visibleCosts, 2));

        this.scrollOffset.set(0);
        this.totalOptions.set(0);
    }

    public boolean hasRequiredInputs() {
        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        ItemStack lapis = this.inputSlots.getItem(LAPIS_SLOT);

        if (!input.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        int requiredLapis = this.currentLapisCost.get();
        return requiredLapis <= 0 || lapis.getCount() >= requiredLapis;
    }

    private boolean hasEnoughBookshelves(int power) {
        return power >= 5;
    }

    private int resolveXpCost(int power) {
        if (ModConfig.INSTANCE.removePlayerXp) {
            return 0;
        }

        if (power >= 30) {
            return ModConfig.INSTANCE.xpCostTier3;
        }
        if (power >= 15) {
            return ModConfig.INSTANCE.xpCostTier2;
        }
        if (power >= 5) {
            return ModConfig.INSTANCE.xpCostTier1;
        }
        return 0;
    }

    private int resolveLapisCost(int power) {
        if (power >= 18) {
            return ModConfig.INSTANCE.lapisCostTier3;
        }
        if (power >= 10) {
            return ModConfig.INSTANCE.lapisCostTier2;
        }
        if (power >= 5) {
            return ModConfig.INSTANCE.lapisCostTier1;
        }
        return 0;
    }

    public boolean isVisibleOptionSelected(int row) {
        if (!this.hasVisibleOption(row)) {
            return false;
        }

        int absoluteIndex = this.scrollOffset.get() + row;
        return this.selectedIndex.get() == absoluteIndex;
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != this.inputSlots) {
            return;
        }

        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        if (input.isEmpty()) {
            this.clearOptionsAndPreview();
            return;
        }

        this.access.execute((level, pos) -> {
            IdMap<Holder<Enchantment>> idMap =
                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();

            int power = this.countEnchantingPower(level, pos);
            this.enchantingPower.set(power);
            this.currentXpCost.set(this.resolveXpCost(power));
            this.currentLapisCost.set(this.resolveLapisCost(power));

            this.options.clear();

            if (this.hasEnoughBookshelves(power)) {
                this.options.addAll(this.buildOptions(level.registryAccess(), pos, input, power));
            }

            this.totalOptions.set(this.options.size());

            int maxScroll = this.getMaxScrollOffset();
            this.scrollOffset.set(clamp(this.scrollOffset.get(), 0, maxScroll));

            if (!this.hasValidSelection()) {
                this.selectedIndex.set(-1);
            }

            this.refreshVisibleData(idMap);
            this.refreshResultPreview();
            this.broadcastChanges();
        });
    }

    public boolean isVisibleOptionPreviewed(int row) {
        if (!this.hasVisibleOption(row)) {
            return false;
        }

        ItemStack output = this.resultSlots.getItem(OUTPUT_SLOT);
        if (!output.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        ItemEnchantments enchantments =
                output.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (enchantments.size() != 1) {
            return false;
        }

        int absoluteIndex = this.scrollOffset.get() + row;
        if (absoluteIndex < 0 || absoluteIndex >= this.options.size()) {
            return false;
        }

        EnchantOption option = this.options.get(absoluteIndex);

        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            Holder<Enchantment> outputEnchant = entry.getKey();
            int outputLevel = entry.getIntValue();

            return outputEnchant.equals(option.enchantment()) && outputLevel == option.level();
        }

        return false;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId >= BUTTON_ROW_0 && buttonId <= BUTTON_ROW_2) {
            int row = buttonId - BUTTON_ROW_0;
            int absoluteIndex = this.scrollOffset.get() + row;

            if (absoluteIndex < 0 || absoluteIndex >= this.options.size()) {
                return false;
            }

            EnchantOption option = this.options.get(absoluteIndex);

            if (!this.isAffordable(player, option)) {
                this.selectedIndex.set(-1);
                this.resultSlots.setItem(OUTPUT_SLOT, ItemStack.EMPTY);
                this.broadcastChanges();
                return false;
            }

            this.selectedIndex.set(absoluteIndex);
            this.refreshResultPreview();
            this.broadcastChanges();
            return true;
        }

        if (buttonId >= BUTTON_SCROLL_TO_OFFSET_BASE) {
            int requestedOffset = buttonId - BUTTON_SCROLL_TO_OFFSET_BASE;
            int clampedOffset = clamp(requestedOffset, 0, this.getMaxScrollOffset());

            if (clampedOffset != this.scrollOffset.get()) {
                this.scrollOffset.set(clampedOffset);

                this.access.execute((level, pos) -> {
                    IdMap<Holder<Enchantment>> idMap =
                            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).asHolderIdMap();
                    this.refreshVisibleData(idMap);
                    this.broadcastChanges();
                });
            }

            return true;
        }

        return false;
    }

    private List<EnchantOption> buildOptions(RegistryAccess registryAccess, BlockPos tablePos, ItemStack input, int power) {
        if (!input.is(Items.ENCHANTED_BOOK)) {
            return List.of();
        }

        if (!this.hasEnoughBookshelves(power)) {
            return List.of();
        }

        Optional<HolderSet.Named<Enchantment>> optional =
                registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);

        if (optional.isEmpty()) {
            return List.of();
        }

        var enchantRegistry = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        if (optional.isEmpty()) {
            return List.of();
        }

        int tagCount = 0;
        for (Holder<Enchantment> holder : optional.get()) {
            tagCount++;
        }

        List<EnchantOption> result = new ArrayList<>();
        int xpCost = this.resolveXpCost(power);

        for (Holder<Enchantment> holder : optional.get()) {
            if (holder.is(ModEnchantmentTags.BLACKLISTED_ENCHANTMENTS)) {
                continue;
            }

            Enchantment enchantment = holder.value();
            int maxLevel = enchantment.getMaxLevel();

            result.add(new EnchantOption(holder, maxLevel, xpCost));
        }

        result.sort((a, b) -> {
            String an = Enchantment.getFullname(a.enchantment(), a.level()).getString();
            String bn = Enchantment.getFullname(b.enchantment(), b.level()).getString();
            return an.compareToIgnoreCase(bn);
        });

        return result;
    }

    private int countEnchantingPower(net.minecraft.world.level.Level level, BlockPos tablePos) {
        int power = 0;

        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            BlockPos shelfPos = tablePos.offset(offset);
            BlockPos betweenPos = tablePos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);

            boolean provider =
                    level.getBlockState(shelfPos).is(Blocks.BOOKSHELF) ||
                            level.getBlockState(shelfPos).is(Blocks.CHISELED_BOOKSHELF);

            boolean transmitter =
                    level.getBlockState(betweenPos).is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_TRANSMITTER);

            if (provider && transmitter) {
                power++;
            }
        }

        return power;
    }

    private int computeEffectiveCost(ItemStack input, int power) {
        this.random.setSeed((long) this.enchantmentSeed.get());
        int vanillaCost = EnchantmentHelper.getEnchantmentCost(this.random, 2, power, input);
        return Math.max(1, vanillaCost);
    }

    private void refreshVisibleData(IdMap<Holder<Enchantment>> idMap) {
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = this.scrollOffset.get() + row;

            if (index >= 0 && index < this.options.size()) {
                EnchantOption option = this.options.get(index);
                this.visibleEnchantIds[row] = idMap.getId(option.enchantment());
                this.visibleLevels[row] = option.level();
                this.visibleCosts[row] = option.cost();
            } else {
                this.visibleEnchantIds[row] = -1;
                this.visibleLevels[row] = -1;
                this.visibleCosts[row] = 0;
            }
        }
    }

    private void refreshResultPreview() {
        this.resultSlots.setItem(OUTPUT_SLOT, ItemStack.EMPTY);

        if (!this.hasValidSelection()) {
            return;
        }

        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        if (!input.is(Items.ENCHANTED_BOOK)) {
            return;
        }

        EnchantOption option = this.options.get(this.selectedIndex.get());

        ItemStack preview = new ItemStack(Items.ENCHANTED_BOOK);

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(option.enchantment(), option.level());
        preview.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());

        this.resultSlots.setItem(OUTPUT_SLOT, preview);
        this.broadcastChanges();
    }

    private void finishEnchant(Player player, ItemStack takenStack) {
        if (!this.hasValidSelection()) {
            return;
        }

        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        ItemStack lapis = this.inputSlots.getItem(LAPIS_SLOT);

        if (!input.is(Items.ENCHANTED_BOOK) || takenStack.isEmpty()) {
            return;
        }

        EnchantOption option = this.options.get(this.selectedIndex.get());
        if (!this.isAffordable(player, option)) {
            return;
        }

        if (!player.hasInfiniteMaterials()) {
            input.shrink(1);
            lapis.shrink(this.currentLapisCost.get());

            if (input.isEmpty()) {
                this.inputSlots.setItem(INPUT_SLOT, ItemStack.EMPTY);
            }

            if (lapis.isEmpty()) {
                this.inputSlots.setItem(LAPIS_SLOT, ItemStack.EMPTY);
            }

            if (!ModConfig.INSTANCE.removePlayerXp && option.cost() > 0) {
                player.giveExperienceLevels(-option.cost());
            }
        }

        this.resultSlots.setItem(OUTPUT_SLOT, ItemStack.EMPTY);

        player.awardStat(Stats.ENCHANT_ITEM);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, takenStack, option.cost());
        }

        this.enchantmentSeed.set(player.getEnchantmentSeed());
        this.selectedIndex.set(-1);
        this.inputSlots.setChanged();
        this.slotsChanged(this.inputSlots);

        this.access.execute((level, pos) -> {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    level.random.nextFloat() * 0.1F + 0.9F
            );
        });
    }

    private void clearOptionsAndPreview() {
        this.options.clear();
        this.selectedIndex.set(-1);
        this.scrollOffset.set(0);
        this.totalOptions.set(0);

        for (int i = 0; i < VISIBLE_ROWS; i++) {
            this.visibleEnchantIds[i] = -1;
            this.visibleLevels[i] = -1;
            this.visibleCosts[i] = 0;
        }

        this.resultSlots.setItem(0, ItemStack.EMPTY);
        this.broadcastChanges();
    }

    private boolean hasValidSelection() {
        return this.selectedIndex.get() >= 0 && this.selectedIndex.get() < this.options.size();
    }

    private boolean isSelectedOptionAffordable(Player player) {
        if (!this.hasValidSelection()) {
            return false;
        }
        return this.isAffordable(player, this.options.get(this.selectedIndex.get()));
    }

    private boolean isAffordable(Player player, EnchantOption option) {
        if (player.hasInfiniteMaterials()) {
            return true;
        }

        if (!this.hasEnoughBookshelves(this.enchantingPower.get())) {
            return false;
        }

        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        ItemStack lapis = this.inputSlots.getItem(LAPIS_SLOT);
        int requiredLapis = this.currentLapisCost.get();

        if (!input.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        boolean enoughLapis = requiredLapis <= 0 || lapis.getCount() >= requiredLapis;

        if (ModConfig.INSTANCE.removePlayerXp) {
            return enoughLapis;
        }

        boolean enoughXp = player.experienceLevel >= option.cost();
        return enoughLapis && enoughXp;
    }

    private int getStableHolderHash(Holder<Enchantment> holder) {
        return holder.unwrapKey().map(Object::hashCode).orElse(0);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == MENU_OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        } else if (index == MENU_INPUT_SLOT || index == MENU_LAPIS_SLOT) {
            if (!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(stack, MENU_LAPIS_SLOT, MENU_LAPIS_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                Slot inputSlot = this.slots.get(MENU_INPUT_SLOT);
                if (inputSlot.hasItem() || !inputSlot.mayPlace(stack)) {
                    return ItemStack.EMPTY;
                }

                ItemStack single = stack.copyWithCount(1);
                stack.shrink(1);
                inputSlot.setByPlayer(single);
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.inputSlots));
        this.resultSlots.setItem(0, ItemStack.EMPTY);
    }

    public boolean canScroll() {
        return this.totalOptions.get() > VISIBLE_ROWS;
    }

    public int getTotalOptions() {
        return this.totalOptions.get();
    }

    public int getScrollOffset() {
        return this.scrollOffset.get();
    }

    public int getMaxScrollOffset() {
        return Math.max(0, this.totalOptions.get() - VISIBLE_ROWS);
    }

    public boolean hasVisibleOption(int row) {
        return row >= 0 && row < VISIBLE_ROWS && this.visibleEnchantIds[row] >= 0;
    }

    public int getVisibleCost(int row) {
        return row >= 0 && row < VISIBLE_ROWS ? this.visibleCosts[row] : 0;
    }

    public int getVisibleLapisCost(int row) {
        return this.hasVisibleOption(row) ? this.currentLapisCost.get() : 0;
    }

    public int getVisibleLevel(int row) {
        return row >= 0 && row < VISIBLE_ROWS ? this.visibleLevels[row] : -1;
    }

    public int getVisibleEnchantmentId(int row) {
        return row >= 0 && row < VISIBLE_ROWS ? this.visibleEnchantIds[row] : -1;
    }

    public int getEnchantingPower() {
        return this.enchantingPower.get();
    }

    public boolean hasEnoughBookshelves() {
        return this.hasEnoughBookshelves(this.enchantingPower.get());
    }

    public int getCurrentXpCost() {
        return this.currentXpCost.get();
    }

    public int getCurrentLapisCost() {
        return this.currentLapisCost.get();
    }

    public boolean isVisibleOptionAffordable(Player player, int row) {
        if (!this.hasVisibleOption(row)) {
            return false;
        }

        if (!this.hasEnoughBookshelves(this.enchantingPower.get())) {
            return false;
        }

        if (player.hasInfiniteMaterials()) {
            return true;
        }

        ItemStack input = this.inputSlots.getItem(INPUT_SLOT);
        ItemStack lapis = this.inputSlots.getItem(LAPIS_SLOT);
        int lapisCost = this.currentLapisCost.get();

        if (!input.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        boolean enoughLapis = lapisCost <= 0 || lapis.getCount() >= lapisCost;

        if (ModConfig.INSTANCE.removePlayerXp) {
            return enoughLapis;
        }

        int xpCost = this.getVisibleCost(row);
        return enoughLapis && player.experienceLevel >= xpCost;
    }

    public int getGoldCount() {
        ItemStack lapis = this.inputSlots.getItem(LAPIS_SLOT);
        return lapis.isEmpty() ? 0 : lapis.getCount();
    }

    public int getEnchantmentSeedForClient() {
        return this.enchantmentSeed.get();
    }

    private record EnchantOption(Holder<Enchantment> enchantment, int level, int cost) {
    }
}
