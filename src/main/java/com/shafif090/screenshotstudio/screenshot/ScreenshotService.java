package com.shafif090.screenshotstudio.screenshot;

import com.shafif090.screenshotstudio.ScreenshotStudioMod;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.io.File;

public final class ScreenshotService {
	public void capture(Minecraft client) {
		try {
			File directory = new File(client.gameDirectory, Screenshot.SCREENSHOT_DIR);
			directory.mkdir();
			File file = nextScreenshotFile(directory);
			Screenshot.takeScreenshot(client.getMainRenderTarget(), image -> saveScreenshot(client, image, file));
		} catch (Throwable throwable) {
			ScreenshotStudioMod.LOGGER.warn("Version-safe screenshot capture failed; falling back to vanilla keybind", throwable);
			KeyMapping.click(client.options.keyScreenshot.getDefaultKey());
		}
	}

	private void saveScreenshot(Minecraft client, NativeImage image, File file) {
		Util.ioPool().execute(() -> {
			try (image) {
				image.writeToFile(file);
				showSavedToast(client, file);
			} catch (Exception exception) {
				ScreenshotStudioMod.LOGGER.warn("Couldn't save Screenshot Studio screenshot", exception);
				client.execute(() -> SystemToast.add(
						client.getToastManager(),
						SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
						Component.translatable("screenshot.failure", exception.getMessage()),
						null
				));
			}
		});
	}

	private void showSavedToast(Minecraft client, File file) {
		client.execute(() -> OpenScreenshotToast.show(client.getToastManager(), file));
	}

	private File nextScreenshotFile(File directory) {
		String name = Util.getFilenameFormattedDateTime();
		int count = 1;
		while (true) {
			File file = new File(directory, name + (count == 1 ? "" : "_" + count) + ".png");
			if (!file.exists()) {
				return file;
			}
			count++;
		}
	}
}
