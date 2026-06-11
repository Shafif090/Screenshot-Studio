package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import com.shafif090.screenshotstudio.camera.CameraState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.Projection;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Unique
	private static final Vector3f SCREENSHOTSTUDIO_FORWARDS = new Vector3f(0.0F, 0.0F, 1.0F);
	@Unique
	private static final Vector3f SCREENSHOTSTUDIO_UP = new Vector3f(0.0F, 1.0F, 0.0F);
	@Unique
	private static final Vector3f SCREENSHOTSTUDIO_LEFT = new Vector3f(1.0F, 0.0F, 0.0F);

	@Shadow
	protected abstract void setPosition(Vec3 position);

	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	@Final
	private Quaternionf rotation;

	@Shadow
	@Final
	private Vector3f forwards;

	@Shadow
	@Final
	private Vector3f panoramicForwards;

	@Shadow
	@Final
	private Vector3f up;

	@Shadow
	@Final
	private Vector3f left;

	@Shadow
	private boolean detached;

	@Shadow
	private int matrixPropertiesDirty;

	@Shadow
	@Final
	private Projection projection;

	@Shadow
	private float fov;

	@Shadow
	private float hudFov;

	@Shadow
	private float depthFar;

	@Inject(method = "update", at = @At("TAIL"), require = 0)
	private void screenshotstudio$overrideCamera(DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		if (!ScreenshotStudioClient.isPhotoModeActive()) {
			return;
		}

		CameraState state = ScreenshotStudioClient.camera().state();
		setPosition(state.renderPosition());
		setRotation(state.renderYaw(), state.renderPitch());
		applyRoll(state.renderRoll());
		applyFov();
		this.detached = true;
	}

	@Inject(method = "getFov", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$overrideFov(CallbackInfoReturnable<Float> callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.setReturnValue((float) ScreenshotStudioClient.settings().fov);
		}
	}

	@Inject(method = "calculateFov", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$overrideCalculatedFov(float partialTicks, CallbackInfoReturnable<Float> callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.setReturnValue((float) ScreenshotStudioClient.settings().fov);
		}
	}

	@Inject(method = "calculateHudFov", at = @At("HEAD"), cancellable = true, require = 0)
	private void screenshotstudio$overrideCalculatedHudFov(float partialTicks, CallbackInfoReturnable<Float> callbackInfo) {
		if (ScreenshotStudioClient.isPhotoModeActive()) {
			callbackInfo.setReturnValue((float) ScreenshotStudioClient.settings().fov);
		}
	}

	@Unique
	private void applyRoll(float rollDegrees) {
		if (Math.abs(rollDegrees) < 0.001F) {
			return;
		}

		this.rotation.rotateZ((float) Math.toRadians(rollDegrees));
		SCREENSHOTSTUDIO_FORWARDS.rotate(this.rotation, this.forwards);
		SCREENSHOTSTUDIO_FORWARDS.rotate(this.rotation, this.panoramicForwards);
		SCREENSHOTSTUDIO_UP.rotate(this.rotation, this.up);
		SCREENSHOTSTUDIO_LEFT.rotate(this.rotation, this.left);
		this.matrixPropertiesDirty |= 3;
	}

	@Unique
	private void applyFov() {
		Minecraft client = Minecraft.getInstance();
		if (client.getWindow() == null) {
			return;
		}

		this.fov = (float) ScreenshotStudioClient.settings().fov;
		this.hudFov = this.fov;
		this.projection.setupPerspective(
				0.05F,
				this.depthFar,
				this.fov,
				(float) client.getWindow().getWidth(),
				(float) client.getWindow().getHeight()
		);
		this.matrixPropertiesDirty |= 2;
	}
}
