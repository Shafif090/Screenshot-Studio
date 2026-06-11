package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"), require = 0)
	private void screenshotstudio$exitPhotoModeOnDisconnect(Screen screen, boolean keepResourcePacks, CallbackInfo callbackInfo) {
		ScreenshotStudioClient.forceExitPhotoMode();
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"), require = 0)
	private void screenshotstudio$exitPhotoModeOnDisconnect(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo callbackInfo) {
		ScreenshotStudioClient.forceExitPhotoMode();
	}
}
