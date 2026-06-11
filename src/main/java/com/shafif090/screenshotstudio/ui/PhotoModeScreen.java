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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class PhotoModeScreen extends Screen {
	private static final int PANEL_WIDTH = 276;
	private static final int PANEL_MARGIN = 12;
	private static final int HELP_WIDTH = PANEL_WIDTH - 24;
	private static final int HELP_HEIGHT = 166;
	private static final DateTimeFormatter PRESET_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
	private static final String[] HELP_LINES = {
			"screen.screenshotstudio.help.move",
			"screen.screenshotstudio.help.vertical",
			"screen.screenshotstudio.help.look",
			"screen.screenshotstudio.help.fov",
			"screen.screenshotstudio.help.speed",
			"screen.screenshotstudio.help.boost",
			"screen.screenshotstudio.help.roll",
			"screen.screenshotstudio.help.focus",
			"screen.screenshotstudio.help.mode",
			"screen.screenshotstudio.help.hide",
			"screen.screenshotstudio.help.exit"
	};

	private Tab activeTab = Tab.CAMERA;
	private final List<SliderWidget> sliders = new ArrayList<>();
	private boolean helpVisible;

	public PhotoModeScreen() {
		super(Component.translatable("screen.screenshotstudio.photo_mode"));
	}

	@Override
	protected void init() {
		sliders.clear();
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
		addHelpButton(contentX + contentWidth - 20, this.height - 52);
	}

	private void initCameraTab(int x, int y, int width) {
		PhotoModeSettings settings = ScreenshotStudioClient.settings();
		addSlider(x, y, width, "FOV", 10.0D, 170.0D, () -> settings.fov, value -> settings.fov = value);
		y += 22;
		addRenderableWidget(Button.builder(movementModeLabel(), button -> {
			ScreenshotStudioClient.camera().cycleMovementMode();
			rebuildWidgets();
		}).bounds(x, y, width, 18).build());
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
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.focus_pick"), button -> {
			if (ScreenshotStudioClient.pickFocusDistance(Minecraft.getInstance())) {
				rebuildWidgets();
			}
		}).bounds(x, y, width, 18).build());
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
			ScreenshotStudioClient.presets().load(name).ifPresent(settings -> {
				ScreenshotStudioClient.settings().copyFrom(settings);
				rebuildWidgets();
			});
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

	private void addHelpButton(int x, int y) {
		addRenderableWidget(Button.builder(Component.translatable("screen.screenshotstudio.help"), button -> {
			helpVisible = !helpVisible;
		}).bounds(x, y, 20, 20).build());
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
		sliders.add(slider);
		return slider;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean suppressUi = ScreenshotStudioClient.shouldSuppressUi();
		ScreenshotStudioClient.postProcess().extractOverlays(graphics, this.width, this.height, ScreenshotStudioClient.settings());
		refreshSliders();

		if (!suppressUi && ScreenshotStudioClient.isPanelVisible()) {
			extractPanel(graphics);
		}

		float flashAlpha = ScreenshotStudioClient.flashAlpha();
		if (flashAlpha > 0.0F) {
			int alpha = (int) (flashAlpha * 150.0F);
			graphics.fill(0, 0, this.width, this.height, (alpha & 255) << 24 | 0xFFFFFF);
		}

		if (!suppressUi) {
			extractStatus(graphics);
			super.extractRenderState(graphics, mouseX, mouseY, partialTick);
			if (helpVisible && ScreenshotStudioClient.isPanelVisible()) {
				extractHelp(graphics);
			}
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
			if (!ScreenshotStudioClient.isPanelVisible()) {
				helpVisible = false;
			}
			rebuildWidgets();
			return true;
		}
		if (event.key() == InputConstants.KEY_F) {
			if (ScreenshotStudioClient.pickFocusDistance(Minecraft.getInstance())) {
				rebuildWidgets();
			}
			return true;
		}
		if (event.key() == InputConstants.KEY_M) {
			ScreenshotStudioClient.camera().cycleMovementMode();
			rebuildWidgets();
			return true;
		}
		if (isFreecamMovementKey(event.key())) {
			return true;
		}
		return super.keyPressed(event);
	}

	private static boolean isFreecamMovementKey(int key) {
		return switch (key) {
			case InputConstants.KEY_W,
					InputConstants.KEY_A,
					InputConstants.KEY_S,
					InputConstants.KEY_D,
					InputConstants.KEY_SPACE,
					InputConstants.KEY_LSHIFT,
					InputConstants.KEY_RSHIFT,
					InputConstants.KEY_LCONTROL,
					InputConstants.KEY_RCONTROL,
					InputConstants.KEY_Q,
					InputConstants.KEY_E -> true;
			default -> false;
		};
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (helpVisible) {
			if (isInsideHelp(event.x(), event.y())) {
				return true;
			}
			helpVisible = false;
			return true;
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() == 2 && !isInsidePanel(event.x(), event.y())) {
			if (ScreenshotStudioClient.pickFocusDistance(Minecraft.getInstance())) {
				rebuildWidgets();
			}
			return true;
		}
		return false;
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
			refreshSliders();
			return true;
		}
		if (!isInsidePanel(mouseX, mouseY)) {
			ScreenshotStudioClient.camera().changeFov(ScreenshotStudioClient.settings(), scrollY);
			refreshSliders();
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
		graphics.text(this.font, statusSpeed(), x + 12, this.height - 34, 0xFFB8C0CC);
	}

	private void extractHelp(GuiGraphicsExtractor graphics) {
		int x = helpX();
		int y = helpY();
		graphics.fill(x, y, x + HELP_WIDTH, y + HELP_HEIGHT, 0xEE101216);
		graphics.outline(x, y, HELP_WIDTH, HELP_HEIGHT, 0x884F8DFF);
		graphics.text(this.font, Component.translatable("screen.screenshotstudio.help.title"), x + 10, y + 8, 0xFFECEFF4);

		int lineY = y + 24;
		for (String key : HELP_LINES) {
			graphics.text(this.font, Component.translatable(key), x + 10, lineY, 0xFFB8C0CC);
			lineY += 12;
		}
	}

	private void extractStatus(GuiGraphicsExtractor graphics) {
		PhotoModeSettings settings = ScreenshotStudioClient.settings();
		int x = 10;
		int y = this.height - 50;
		graphics.fill(x - 4, y - 4, x + 222, y + 36, 0x99000000);
		graphics.text(this.font, String.format(Locale.ROOT, "FOV %.0f  Roll %.1f  %s", settings.fov, settings.roll, statusSpeed()), x, y, 0xFFECEFF4);
		graphics.text(this.font, String.format(Locale.ROOT, "%s  Focus %.1f  Aperture %.1f", settings.depthOfField ? "DoF On" : "DoF Off", settings.focusDistance, settings.aperture), x, y + 12, 0xFFB8C0CC);
		graphics.text(this.font, "Shot UI: Hidden", x, y + 24, 0xFF8EA0B8);
	}

	private boolean isInsidePanel(double mouseX, double mouseY) {
		return ScreenshotStudioClient.isPanelVisible()
				&& mouseX >= panelX()
				&& mouseX <= this.width - PANEL_MARGIN
				&& mouseY >= PANEL_MARGIN
				&& mouseY <= this.height - PANEL_MARGIN;
	}

	private boolean isInsideHelp(double mouseX, double mouseY) {
		int x = helpX();
		int y = helpY();
		return ScreenshotStudioClient.isPanelVisible()
				&& mouseX >= x
				&& mouseX <= x + HELP_WIDTH
				&& mouseY >= y
				&& mouseY <= y + HELP_HEIGHT;
	}

	private int helpX() {
		return panelX() + 12;
	}

	private int helpY() {
		return Math.max(PANEL_MARGIN + 48, this.height - 260);
	}

	private int panelX() {
		return this.width - PANEL_WIDTH - PANEL_MARGIN;
	}

	private static Component dofLabel(PhotoModeSettings settings) {
		return Component.translatable(settings.depthOfField ? "screen.screenshotstudio.dof.on" : "screen.screenshotstudio.dof.off");
	}

	private static Component movementModeLabel() {
		return Component.translatable("screen.screenshotstudio.move_mode", ScreenshotStudioClient.camera().movementMode().label());
	}

	private static String statusSpeed() {
		return String.format(Locale.ROOT, "%s %.2f", ScreenshotStudioClient.camera().movementMode().label(), ScreenshotStudioClient.camera().speed());
	}

	private void refreshSliders() {
		for (SliderWidget slider : sliders) {
			slider.refresh();
		}
	}

	private enum Tab {
		CAMERA,
		COLOR,
		EFFECTS,
		PRESETS
	}
}
