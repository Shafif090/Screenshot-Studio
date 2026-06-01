package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private CrossFrameResourcePool resourcePool;

	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$hideHand(CameraRenderState cameraRenderState, float tickDelta, Matrix4fc matrix, CallbackInfo callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.cancel();
		}
	}

	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V",
					shift = At.Shift.BEFORE
			),
			require = 0
	)
	private void screenshotstudio$processPhotoModeBeforeGui(net.minecraft.client.DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo callbackInfo) {
		if (!ScreenshotStudioClient.isPhotoModeActive()) {
			return;
		}

		ScreenshotStudioClient.postProcess().process(this.minecraft, this.resourcePool, ScreenshotStudioClient.settings());
		ScreenshotStudioClient.captureQueuedScreenshot(this.minecraft);
	}
}
