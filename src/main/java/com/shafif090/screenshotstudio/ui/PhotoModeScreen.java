package com.shafif090.screenshotstudio.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import com.shafif090.screenshotstudio.settings.PhotoModeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class PhotoModeScreen extends Screen {
	private static final int PANEL_WIDTH = 276;
	private static final int PANEL_MARGIN = 12;
	private static final DateTimeFormatter PRESET_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private Tab activeTab = Tab.CAMERA;

	public PhotoModeScreen() {
		super(Component.translatable("screen.screenshotstudio.photo_mode"));
	}

	@Override
	protected void init() {
		if (!ScreenshotStudioClient.isPanelVisible()) {
			return;
		}

		int x = panelX();
		int y = 18;
		int contentX = x + 12;
		int contentWidth = PANEL_WIDTH - 24;
		int tabWidth = (contentWidth - 6) / 4;

		addTab(contentX, y, tabWidth, Tab.CAMERA, "screen.screenshotstudio.camera");
		addTab(contentX + tabWidth + 2, y, tabWidth, Tab.COLOR, "screen.screenshotstudio.color");
		addTab(contentX + (tabWidth + 2) * 2, y, tabWidth, Tab.EFFECTS, "screen.screenshotstudio.effects");
		addTab(contentX + (tabWidth + 2) * 3, y, tabWidth, Tab.PRESETS, "screen.screenshotstudio.presets");

		y += 28;
		switch (activeTab) {
			case CAMERA -> initCameraTab(contentX, y, contentWidth);
			case COLOR -> initColorTab(contentX, y, contentWidth);
			case EFFECTS -> initEffectsTab(contentX, y, contentWidth);
			case PRESETS -> initPresetsTab(contentX, y, contentWidth);
		}

		addCommonButtons(contentX, this.height - 76, contentWidth);
	}

	private void initCameraTab(int x, int y, int width) {
		PhotoModeSettings settings = ScreenshotStudioClient.settings();
		addSlider(x, y, width, "FOV", 10.0D, 170.0D, () -> settings.fov, value -> settings.fov = value);
		y += 22;
		addRenderableWidget(Button.builder(dofLabel(settings), button -> {
			settings.depthOfField = !settings.depthOfField;
			if (settings.depthOfField && settings.aperture < 0.1D) {
				settings.aperture = 6.0D;
			}
			rebuildWidgets();
		}).bounds(x, y, width, 18).build());
		y += 22;
		SliderWidget focus = addSlider(x, y, width, "Focus", 0.5D, 100.0D, () -> settings.focusDistance, value -> settings.focusDistance = value);
		focus.active = settings.depthOfField;
		y += 22;
		SliderWidget aperture = addSlider(x, y, width, "Aperture", 0.0D, 20.0D, () -> settings.aperture, value -> settings.aperture = value);
		aperture.active = settings.depthOfField;
		y += 22;
		addSlider(x, y, width, "Roll", -45.0D, 45.0D, () -> settings.roll, value -> settings.roll = value);
		y += 22;
		SliderWidget time = addSlider(x, y, width, "Time Preview", 0.0D, 24000.0D, () -> settings.timeOfDay, value -> settings.timeOfDay = value);
		time.active = false;
	}

	private void initColorTab(int x, int y, int width) {
		PhotoModeSettings settings = ScreenshotStudioClient.settings();
		addSlider(x, y, width, "Brightness", -100.0D, 100.0D, () -> settings.brightness, value -> settings.brightness = value);
		y += 22;
		addSlider(x, y, width, "Contrast", -100.0D, 100.0D, () -> settings.contrast, value -> settings.contrast = value);
		y += 22;
		addSlider(x, y, width, "Saturation", -100.0D, 100.0D, () -> settings.saturation, value -> settings.saturation = value);
		y += 22;
		addSlider(x, y, width, "Temperature", -100.0D, 100.0D, () -> settings.temperature, value -> settings.temperature = value);
		y += 22;
		addSlider(x, y, width, "Tint", -100.0D, 100.0D, () -> settings.tint, value -> settings.tint = value);
		y += 22;
		addSlider(x, y, width, "Vignette", 0.0D, 100.0D, () -> settings.vignette, value -> settings.vignette = value);
	}

	private void initEffectsTab(int x, int y, int width) {
		PhotoModeSettings settings = ScreenshotStudioClient.settings();
		addSlider(x, y, width, "Aberration", 0.0D, 100.0D, () -> settings.chromaticAberration, value -> settings.chromaticAberration = value);
		y += 22;
		addSlider(x, y, width, "Film Grain", 0.0D, 100.0D, () -> settings.filmGrain, value -> settings.filmGrain = value);
		y += 22;
		addSlider(x, y, width, "Sharpness", -50.0D, 50.0D, () -> settings.sharpness, value -> settings.sharpness = value);
	}

	private void initPresetsTab(int x, int y, int width) {
		PresetListWidget list = new PresetListWidget(x, y, width, 112, ScreenshotStudioClient.presets().names(), name -> {
			ScreenshotStudioClient.presets().load(name).ifPresent(ScreenshotStudioClient.settings()::copyFrom);
		});
		addRenderableWidget(list);
		y += 120;
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.save"), button -> {
			String name = "Custom-" + LocalDateTime.now().format(PRESET_TIMESTAMP);
			ScreenshotStudioClient.presets().save(name, ScreenshotStudioClient.settings());
			rebuildWidgets();
		}).bounds(x, y, width, 18).build());
	}

	private void addCommonButtons(int x, int y, int width) {
		int third = (width - 8) / 3;
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.reset"), button -> {
			ScreenshotStudioClient.settings().copyFrom(PhotoModeSettings.defaults());
			rebuildWidgets();
		}).bounds(x, y, third, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.screenshot"), button -> {
			ScreenshotStudioClient.takeScreenshot(Minecraft.getInstance());
		}).bounds(x + third + 4, y, third, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.exit"), button -> {
			ScreenshotStudioClient.exitPhotoMode(Minecraft.getInstance());
		}).bounds(x + (third + 4) * 2, y, width - (third + 4) * 2, 20).build());
	}

	private void addTab(int x, int y, int width, Tab tab, String translationKey) {
		Button button = Button.builder(Component.translatable(translationKey), pressed -> {
			activeTab = tab;
			rebuildWidgets();
		}).bounds(x, y, width, 20).build();
		button.active = activeTab != tab;
		addRenderableWidget(button);
	}

	private SliderWidget addSlider(int x, int y, int width, String label, double min, double max, java.util.function.DoubleSupplier getter, java.util.function.DoubleConsumer setter) {
		SliderWidget slider = new SliderWidget(x, y, width, label, min, max, getter, setter);
		addRenderableWidget(slider);
		return slider;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean suppressUi = ScreenshotStudioClient.shouldSuppressUi();
		ScreenshotStudioClient.postProcess().extractOverlays(graphics, this.width, this.height, ScreenshotStudioClient.settings());

		if (!suppressUi && ScreenshotStudioClient.isPanelVisible()) {
			extractPanel(graphics);
		}

		float flashAlpha = ScreenshotStudioClient.flashAlpha();
		if (flashAlpha > 0.0F) {
			int alpha = (int) (flashAlpha * 150.0F);
			graphics.fill(0, 0, this.width, this.height, (alpha & 255) << 24 | 0xFFFFFF);
		}

		if (!suppressUi) {
			super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isEscape()) {
			ScreenshotStudioClient.exitPhotoMode(Minecraft.getInstance());
			return true;
		}
		if (event.key() == InputConstants.KEY_H) {
			ScreenshotStudioClient.setPanelVisible(!ScreenshotStudioClient.isPanelVisible());
			rebuildWidgets();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (super.mouseDragged(event, dragX, dragY)) {
			return true;
		}
		if (!isInsidePanel(event.x(), event.y())) {
			ScreenshotStudioClient.camera().rotateFromDrag(dragX, dragY);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		}
		if (Minecraft.getInstance().hasAltDown()) {
			ScreenshotStudioClient.camera().changeSpeed(scrollY);
			return true;
		}
		if (!isInsidePanel(mouseX, mouseY)) {
			ScreenshotStudioClient.camera().changeFov(ScreenshotStudioClient.settings(), scrollY);
			return true;
		}
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return ScreenshotStudioClient.shouldPauseWorld();
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void onClose() {
		ScreenshotStudioClient.exitPhotoMode(Minecraft.getInstance());
	}

	private void extractPanel(GuiGraphicsExtractor graphics) {
		int x = panelX();
		graphics.fill(x, PANEL_MARGIN, this.width - PANEL_MARGIN, this.height - PANEL_MARGIN, 0xCC0B0D10);
		graphics.outline(x, PANEL_MARGIN, PANEL_WIDTH, this.height - PANEL_MARGIN * 2, 0x55FFFFFF);
		graphics.text(this.font, Component.translatable("screen.screenshotstudio.photo_mode"), x + 12, this.height - 48, 0xFFECEFF4);
		graphics.text(this.font, "Speed " + String.format(java.util.Locale.ROOT, "%.2f", ScreenshotStudioClient.camera().speed()), x + 12, this.height - 34, 0xFFB8C0CC);
	}

	private boolean isInsidePanel(double mouseX, double mouseY) {
		return ScreenshotStudioClient.isPanelVisible()
				&& mouseX >= panelX()
				&& mouseX <= this.width - PANEL_MARGIN
				&& mouseY >= PANEL_MARGIN
				&& mouseY <= this.height - PANEL_MARGIN;
	}

	private int panelX() {
		return this.width - PANEL_WIDTH - PANEL_MARGIN;
	}

	private static Component dofLabel(PhotoModeSettings settings) {
		return Component.translatable(settings.depthOfField ? "screen.screenshotstudio.dof.on" : "screen.screenshotstudio.dof.off");
	}

	private enum Tab {
		CAMERA,
		COLOR,
		EFFECTS,
		PRESETS
	}
}
