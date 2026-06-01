package com.shafif090.screenshotstudio.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shafif090.screenshotstudio.ScreenshotStudioMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PresetManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final Path presetDirectory;
	private final Map<String, PhotoModeSettings> builtIns = new LinkedHashMap<>();

	public PresetManager() {
		this.presetDirectory = FabricLoader.getInstance().getConfigDir().resolve("screenshotstudio").resolve("presets");
		this.builtIns.put("Cinematic", PhotoModeSettings.cinematic());
		this.builtIns.put("Vivid", PhotoModeSettings.vivid());
		this.builtIns.put("Noir", PhotoModeSettings.noir());
		ensureDirectory();
	}

	public List<String> names() {
		List<String> names = new ArrayList<>(builtIns.keySet());
		try {
			if (Files.exists(presetDirectory)) {
				try (var stream = Files.list(presetDirectory)) {
					stream.filter(path -> path.getFileName().toString().endsWith(".json"))
							.map(path -> path.getFileName().toString().replaceFirst("\\.json$", ""))
							.sorted(String.CASE_INSENSITIVE_ORDER)
							.filter(name -> !names.contains(name))
							.forEach(names::add);
				}
			}
		} catch (IOException exception) {
			ScreenshotStudioMod.LOGGER.warn("Failed to list Screenshot Studio presets", exception);
		}
		return names;
	}

	public Optional<PhotoModeSettings> load(String name) {
		PhotoModeSettings builtIn = builtIns.get(name);
		if (builtIn != null) {
			return Optional.of(builtIn.copy());
		}

		Path path = presetPath(name);
		if (!Files.exists(path)) {
			return Optional.empty();
		}

		try (Reader reader = Files.newBufferedReader(path)) {
			PhotoModeSettings settings = GSON.fromJson(reader, PhotoModeSettings.class);
			if (settings != null) {
				settings.clamp();
				return Optional.of(settings);
			}
		} catch (IOException exception) {
			ScreenshotStudioMod.LOGGER.warn("Failed to load Screenshot Studio preset {}", name, exception);
		}
		return Optional.empty();
	}

	public void save(String name, PhotoModeSettings settings) {
		ensureDirectory();
		Path path = presetPath(name);
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(settings.copy(), writer);
		} catch (IOException exception) {
			ScreenshotStudioMod.LOGGER.warn("Failed to save Screenshot Studio preset {}", name, exception);
		}
	}

	private void ensureDirectory() {
		try {
			Files.createDirectories(presetDirectory);
		} catch (IOException exception) {
			ScreenshotStudioMod.LOGGER.warn("Failed to create Screenshot Studio preset directory", exception);
		}
	}

	private Path presetPath(String name) {
		String sanitized = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]+", "_");
		if (sanitized.isBlank()) {
			sanitized = "preset";
		}
		return presetDirectory.resolve(sanitized + ".json");
	}
}
