package com.shafif090.screenshotstudio.screenshot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.io.File;

public final class OpenScreenshotToast implements Toast {
	private static final Identifier BACKGROUND_SPRITE = Identifier.withDefaultNamespace("toast/system");
	private static final int WIDTH = 220;
	private static final int HEIGHT = 44;
	private static final int BUTTON_X = 156;
	private static final int BUTTON_Y = 21;
	private static final int BUTTON_WIDTH = 52;
	private static final int BUTTON_HEIGHT = 16;
	private static final long DISPLAY_TIME_MS = 10000L;
	private static final Object TOKEN = new Object();
	private static OpenScreenshotToast current;

	private final File file;
	private Visibility wantedVisibility = Visibility.HIDE;
	private boolean forceHide;

	private OpenScreenshotToast(File file) {
		this.file = file;
	}

	public static void show(ToastManager toastManager, File file) {
		OpenScreenshotToast toast = new OpenScreenshotToast(file);
		current = toast;
		toastManager.addToast(toast);
	}

	public static boolean handleClick(Minecraft client, double mouseX, double mouseY) {
		OpenScreenshotToast toast = current;
		if (toast == null || toast.wantedVisibility != Visibility.SHOW) {
			return false;
		}

		int toastX = client.getWindow().getGuiScaledWidth() - WIDTH;
		if (mouseX >= toastX + BUTTON_X
				&& mouseX <= toastX + BUTTON_X + BUTTON_WIDTH
				&& mouseY >= BUTTON_Y
				&& mouseY <= BUTTON_Y + BUTTON_HEIGHT) {
			Util.getPlatform().openFile(toast.file.getAbsoluteFile());
			toast.forceHide = true;
			return true;
		}
		return false;
	}

	@Override
	public Visibility getWantedVisibility() {
		return wantedVisibility;
	}

	@Override
	public void update(ToastManager manager, long fullyVisibleForMs) {
		double displayTime = DISPLAY_TIME_MS * manager.getNotificationDisplayTimeMultiplier();
		wantedVisibility = !forceHide && fullyVisibleForMs < displayTime ? Visibility.SHOW : Visibility.HIDE;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, 0, 0, width(), height());
		graphics.text(font, Component.translatable("toast.screenshotstudio.saved"), 12, 7, 0xFFFFFF00, false);
		graphics.text(font, Component.literal(file.getName()), 12, 22, 0xFFFFFFFF, false);
		graphics.fill(BUTTON_X, BUTTON_Y, BUTTON_X + BUTTON_WIDTH, BUTTON_Y + BUTTON_HEIGHT, 0xFF2F3640);
		graphics.outline(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, 0xFFFFFFFF);
		graphics.centeredText(font, Component.translatable("toast.screenshotstudio.open"), BUTTON_X + BUTTON_WIDTH / 2, BUTTON_Y + 4, 0xFFFFFFFF);
	}

	@Override
	public int width() {
		return WIDTH;
	}

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public Object getToken() {
		return TOKEN;
	}

	@Override
	public void onFinishedRendering() {
		if (current == this) {
			current = null;
		}
	}
}
