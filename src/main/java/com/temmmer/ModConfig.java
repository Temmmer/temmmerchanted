package com.temmmer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Path FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Temmmerchanted.MOD_ID + ".json");

    public static ModConfig INSTANCE = new ModConfig();

    @SerializedName("remove_player_xp")
    public boolean removePlayerXp = false;

    @SerializedName("spawner_on_destroy_xp_amount")
    public int spawnerXpDropAmount = 750;

    @SerializedName("xp_cost_max")
    public int xpCostTier1 = 40;

    @SerializedName("xp_cost_middle")
    public int xpCostTier2 = 30;

    @SerializedName("xp_cost_minimum")
    public int xpCostTier3 = 20;

    @SerializedName("lapis_cost_max")
    public int lapisCostTier1 = 50;

    @SerializedName("lapis_cost_middle")
    public int lapisCostTier2 = 40;

    @SerializedName("lapis_cost_minimum")
    public int lapisCostTier3 = 30;

    @SerializedName("anvil_cost_config")
    public AnvilCostConfig anvilCostConfig = new AnvilCostConfig();

    public static class AnvilCostConfig {
        @SerializedName("enchant_cost")
        public int enchantCost = 2;

        @SerializedName("curse_cost")
        public int curseCost = -3;

        @SerializedName("min_cost")
        public int minCost = 0;

        @SerializedName("max_cost")
        public int maxCost = 10;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
                INSTANCE.validate();
            } catch (IOException e) {
                Temmmerchanted.LOGGER.error("Failed to load config, using defaults", e);
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            Temmmerchanted.LOGGER.error("Failed to save config", e);
        }
    }

    private void validate() {
        if (xpCostTier1 < 0) xpCostTier1 = 0;
        if (xpCostTier2 < 0) xpCostTier2 = 0;
        if (xpCostTier3 < 0) xpCostTier3 = 0;

        if (lapisCostTier1 < 0) lapisCostTier1 = 0;
        if (lapisCostTier2 < 0) lapisCostTier2 = 0;
        if (lapisCostTier3 < 0) lapisCostTier3 = 0;
    }
}