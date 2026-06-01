package com.shafif090.screenshotstudio;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScreenshotStudioMod implements ModInitializer {
	public static final String MOD_ID = "screenshotstudio";
	public static final Logger LOGGER = LoggerFactory.getLogger("Screenshot Studio");

	@Override
	public void onInitialize() {
		LOGGER.info("Screenshot Studio core initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
