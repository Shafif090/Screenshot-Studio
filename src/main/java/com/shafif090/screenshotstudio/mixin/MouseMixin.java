package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import com.shafif090.screenshotstudio.screenshot.OpenScreenshotToast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	public abstract double getScaledXPos(Window window);

	@Shadow
	public abstract double getScaledYPos(Window window);

	@Inject(method = "onButton", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$openScreenshotToast(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo callbackInfo) {
		if (action != 1 || rawButtonInfo.button() != 0 || handle != this.minecraft.getWindow().handle()) {
			return;
		}

		Window window = this.minecraft.getWindow();
		if (OpenScreenshotToast.handleClick(this.minecraft, this.getScaledXPos(window), this.getScaledYPos(window))) {
			callbackInfo.cancel();
		}
	}

	@Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$stopPlayerLook(double elapsedTime, CallbackInfo callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.cancel();
		}
	}
}
