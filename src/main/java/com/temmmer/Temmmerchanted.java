package com.temmmer;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Temmmerchanted implements ModInitializer {
	public static final String MOD_ID = "temmmerchanted";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModLootTableModifiers.register();
		ModMenuTypes.init();

		LOGGER.info("Hello Fabric world!");
	}
}