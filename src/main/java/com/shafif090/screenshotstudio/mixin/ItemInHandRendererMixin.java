package com.shafif090.screenshotstudio.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$hideHandsWithItems(float frameInterp, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer player, int lightCoords, CallbackInfo callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.cancel();
		}
	}
}
