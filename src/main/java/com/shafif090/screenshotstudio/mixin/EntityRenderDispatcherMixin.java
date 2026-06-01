package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$hidePhotoModeCamera(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeCamera(entity)) {
			callbackInfo.setReturnValue(false);
		}
	}
}
