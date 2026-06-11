package com.shafif090.screenshotstudio;

import com.mojang.blaze3d.platform.InputConstants;
import com.shafif090.screenshotstudio.camera.FreeCameraController;
import com.shafif090.screenshotstudio.camera.PhotoModeCameraEntity;
import com.shafif090.screenshotstudio.screenshot.ScreenshotService;
import com.shafif090.screenshotstudio.settings.PhotoModeSettings;
import com.shafif090.screenshotstudio.settings.PresetManager;
import com.shafif090.screenshotstudio.settings.ScreenshotStudioConfig;
import com.shafif090.screenshotstudio.shader.PostProcessManager;
import com.shafif090.screenshotstudio.ui.PhotoModeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class ScreenshotStudioClient implements ClientModInitializer {
	private static final FreeCameraController CAMERA = new FreeCameraController();
	private static final PostProcessManager POST_PROCESS = new PostProcessManager();
	private static final ScreenshotService SCREENSHOTS = new ScreenshotService();

	private static ScreenshotStudioConfig config;
	private static PresetManager presets;
	private static PhotoModeSettings settings;
	private static KeyMapping toggleKey;
	private static KeyMapping screenshotKey;
	private static boolean active;
	private static boolean panelVisible = true;
	private static boolean previousHideGui;
	private static boolean previousSmartCull;
	private static CameraType previousCameraType;
	private static PhotoModeCameraEntity cameraEntity;
	private static ClientLevel activeLevel;
	private static LocalPlayer activePlayer;
	private static int flashTicks;
	private static boolean screenshotQueued;
	private static int screenshotDelayTicks;
	private static int uiSuppressTicks;
	private static int settingsAutosaveTicks;

	@Override
	public void onInitializeClient() {
		config = ScreenshotStudioConfig.load();
		presets = new PresetManager();
		settings = config.lastSettings != null
				? config.lastSettings.copy()
				: presets.load(config.defaultPreset).orElseGet(PhotoModeSettings::defaults);
		settings.clamp();

		KeyMapping.Category category = KeyMapping.Category.register(ScreenshotStudioMod.id("category"));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.screenshotstudio.toggle",
				InputConstants.Type.KEYSYM,
				config.activationKey,
				category
		));
		screenshotKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.screenshotstudio.screenshot",
				InputConstants.Type.KEYSYM,
				config.screenshotKey,
				category
		));

		ClientTickEvents.START_CLIENT_TICK.register(ScreenshotStudioClient::onStartClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(ScreenshotStudioClient::onEndClientTick);
		ScreenshotStudioMod.LOGGER.info("Screenshot Studio client initialized");
	}

	private static void onStartClientTick(Minecraft client) {
		if (!active || client.player == null || client.player != activePlayer || !(client.player.input instanceof KeyboardInput)) {
			return;
		}

		Input keyPresses = client.player.input.keyPresses;
		ClientInput input = new ClientInput();
		input.keyPresses = new Input(false, false, false, false, false, keyPresses.shift(), false);
		client.player.input = input;
	}

	private static void onEndClientTick(Minecraft client) {
		while (toggleKey.consumeClick()) {
			togglePhotoMode(client);
		}

		if (uiSuppressTicks > 0) {
			uiSuppressTicks--;
		}
		if (screenshotDelayTicks > 0) {
			screenshotDelayTicks--;
		}

		if (!active) {
			return;
		}

		if (client.player == null || client.level == null || client.player != activePlayer || client.level != activeLevel) {
			exitPhotoMode(client);
			return;
		}

		if (!(client.screen instanceof PhotoModeScreen)) {
			client.setScreen(new PhotoModeScreen());
		}

		CAMERA.tick(client, settings, config);
		settings.clamp();
		syncCameraEntity(client);
		POST_PROCESS.tick(settings);
		if (++settingsAutosaveTicks >= 200) {
			saveCurrentSettings();
			settingsAutosaveTicks = 0;
		}

		if (flashTicks > 0) {
			flashTicks--;
		}

		if (config.screenshotKeyEnabled) {
			while (screenshotKey.consumeClick()) {
				takeScreenshot(client);
			}
		}
	}

	public static void togglePhotoMode(Minecraft client) {
		if (active) {
			exitPhotoMode(client);
		} else {
			enterPhotoMode(client);
		}
	}

	public static void enterPhotoMode(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		active = true;
		panelVisible = true;
		previousHideGui = client.options.hideGui;
		previousSmartCull = client.smartCull;
		previousCameraType = client.options.getCameraType();
		activeLevel = client.level;
		activePlayer = client.player;
		settingsAutosaveTicks = 0;
		client.smartCull = false;
		client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
		CAMERA.captureFromPlayer(client.player, settings);
		syncCameraEntity(client);
		client.setScreen(new PhotoModeScreen());
	}

	public static void exitPhotoMode(Minecraft client) {
		if (!active) {
			return;
		}

		active = false;
		saveCurrentSettings();
		restoreCameraEntity(client);
		activeLevel = null;
		activePlayer = null;
		client.options.hideGui = previousHideGui;
		client.smartCull = previousSmartCull;
		if (previousCameraType != null) {
			client.options.setCameraType(previousCameraType);
			previousCameraType = null;
		}
		if (client.player != null) {
			client.player.input = new KeyboardInput(client.options);
		}
		flashTicks = 0;
		screenshotQueued = false;
		screenshotDelayTicks = 0;
		uiSuppressTicks = 0;
		KeyMapping.releaseAll();

		if (client.screen instanceof PhotoModeScreen) {
			client.setScreen(null);
		}
	}

	public static void forceExitPhotoMode() {
		if (active) {
			exitPhotoMode(Minecraft.getInstance());
		}
	}

	public static void takeScreenshot(Minecraft client) {
		if (!active || screenshotQueued) {
			return;
		}

		screenshotQueued = true;
		screenshotDelayTicks = 1;
		uiSuppressTicks = Math.max(uiSuppressTicks, 2);
	}

	public static void captureQueuedScreenshot(Minecraft client) {
		if (!screenshotQueued || screenshotDelayTicks > 0) {
			return;
		}

		screenshotQueued = false;
		SCREENSHOTS.capture(client);
		flashTicks = 6;
	}

	public static boolean isPhotoModeActive() {
		return active;
	}

	public static boolean shouldPauseWorld() {
		return false;
	}

	public static boolean isPhotoModeCamera(Object entity) {
		return entity instanceof PhotoModeCameraEntity;
	}

	public static boolean isPanelVisible() {
		return panelVisible;
	}

	public static boolean shouldSuppressUi() {
		return uiSuppressTicks > 0;
	}

	public static void setPanelVisible(boolean visible) {
		panelVisible = visible;
	}

	public static PhotoModeSettings settings() {
		return settings;
	}

	public static void saveCurrentSettings() {
		if (config != null && settings != null) {
			config.lastSettings = settings.copy();
			config.save();
		}
	}

	public static boolean pickFocusDistance(Minecraft client) {
		if (!active || client.level == null) {
			return false;
		}

		Vec3 from = CAMERA.state().renderPosition();
		HitResult hitResult = client.hitResult;
		if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
			return false;
		}

		settings.depthOfField = true;
		if (settings.aperture < 0.1D) {
			settings.aperture = 6.0D;
		}
		settings.focusDistance = Mth.clamp(hitResult.getLocation().distanceTo(from), 0.5D, 100.0D);
		return true;
	}

	public static PresetManager presets() {
		return presets;
	}

	public static FreeCameraController camera() {
		return CAMERA;
	}

	public static PostProcessManager postProcess() {
		return POST_PROCESS;
	}

	public static ScreenshotStudioConfig config() {
		return config;
	}

	public static float flashAlpha() {
		return flashTicks <= 0 ? 0.0F : flashTicks / 6.0F;
	}

	public static Component savedToastTitle() {
		return Component.translatable("toast.screenshotstudio.saved");
	}

	private static void syncCameraEntity(Minecraft client) {
		if (client.level == null) {
			return;
		}

		if (cameraEntity == null || cameraEntity.level() != client.level || cameraEntity.isRemoved()) {
			if (cameraEntity != null && !cameraEntity.isRemoved()) {
				cameraEntity.despawn();
			}
			cameraEntity = new PhotoModeCameraEntity(client.level);
			cameraEntity.spawn();
		}

		cameraEntity.syncTo(CAMERA.state());
		client.smartCull = false;
		if (client.getCameraEntity() != cameraEntity) {
			client.setCameraEntity(cameraEntity);
		}
	}

	private static void restoreCameraEntity(Minecraft client) {
		PhotoModeCameraEntity entity = cameraEntity;
		cameraEntity = null;
		if (client.player != null && (client.getCameraEntity() == entity || isPhotoModeCamera(client.getCameraEntity()))) {
			client.setCameraEntity(client.player);
		}
		if (entity != null) {
			entity.despawn();
		}
	}
}
