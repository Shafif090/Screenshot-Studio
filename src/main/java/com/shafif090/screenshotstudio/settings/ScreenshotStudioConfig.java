package com.shafif090.screenshotstudio.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import com.shafif090.screenshotstudio.ScreenshotStudioMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScreenshotStudioConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public int activationKey = InputConstants.KEY_F9;
	public int screenshotKey = InputConstants.UNKNOWN.getValue();
	public boolean screenshotKeyEnabled = false;
	public String defaultPreset = "Cinematic";
	public double movementSpeedMultiplier = 1.0D;
	public PhotoModeSettings lastSettings;

	public static ScreenshotStudioConfig load() {
		Path path = configPath();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				ScreenshotStudioConfig config = GSON.fromJson(reader, ScreenshotStudioConfig.class);
				if (config != null) {
					config.clamp();
					return config;
				}
			} catch (IOException exception) {
				ScreenshotStudioMod.LOGGER.warn("Failed to load Screenshot Studio config", exception);
			}
		}

		ScreenshotStudioConfig config = new ScreenshotStudioConfig();
		config.save();
		return config;
	}

	public void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException exception) {
			ScreenshotStudioMod.LOGGER.warn("Failed to save Screenshot Studio config", exception);
		}
	}

	private void clamp() {
		if (movementSpeedMultiplier < 0.05D) {
			movementSpeedMultiplier = 0.05D;
		}
		if (movementSpeedMultiplier > 10.0D) {
			movementSpeedMultiplier = 10.0D;
		}
		if (defaultPreset == null || defaultPreset.isBlank()) {
			defaultPreset = "Cinematic";
		}
		if (lastSettings != null) {
			lastSettings.clamp();
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("screenshotstudio.json");
	}
}
