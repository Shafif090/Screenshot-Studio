package com.shafif090.screenshotstudio.camera;

import com.mojang.blaze3d.platform.InputConstants;
import com.shafif090.screenshotstudio.settings.PhotoModeSettings;
import com.shafif090.screenshotstudio.settings.ScreenshotStudioConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FreeCameraController {
	private final CameraState state = new CameraState();
	private double speed = 0.35D;
	private MovementMode movementMode = MovementMode.CINEMATIC;

	public void captureFromPlayer(LocalPlayer player, PhotoModeSettings settings) {
		state.snap(player.getEyePosition(1.0F), player.getYRot(), player.getXRot(), (float) settings.roll);
	}

	public void tick(Minecraft client, PhotoModeSettings settings, ScreenshotStudioConfig config) {
		if (client.getWindow() == null) {
			return;
		}

		double speedScale = speed * movementMode.speedMultiplier * Math.max(0.05D, config.movementSpeedMultiplier);
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_LCONTROL)
				|| InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_RCONTROL)) {
			speedScale *= movementMode.boostMultiplier;
		}

		Vec3 movement = Vec3.ZERO;
		double yawRadians = Math.toRadians(state.targetYaw());
		Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
		Vec3 right = new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));

		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_W)) {
			movement = movement.add(forward);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_S)) {
			movement = movement.subtract(forward);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_D)) {
			movement = movement.add(right);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_A)) {
			movement = movement.subtract(right);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_SPACE)) {
			movement = movement.add(0.0D, 1.0D, 0.0D);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_LSHIFT)
				|| InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_RSHIFT)) {
			movement = movement.add(0.0D, -1.0D, 0.0D);
		}

		if (movement.lengthSqr() > 0.0001D) {
			state.setTargetPosition(state.targetPosition().add(movement.normalize().scale(speedScale)));
		}

		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_Q)) {
			settings.roll = Mth.clamp(settings.roll - 1.5D, -45.0D, 45.0D);
		}
		if (InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_E)) {
			settings.roll = Mth.clamp(settings.roll + 1.5D, -45.0D, 45.0D);
		}

		state.setTargetRoll((float) settings.roll);
		state.interpolate(movementMode.interpolationAlpha);
	}

	public void rotateFromDrag(double deltaX, double deltaY) {
		state.setTargetYaw(state.targetYaw() + (float) deltaX * 0.15F);
		state.setTargetPitch(Mth.clamp(state.targetPitch() + (float) deltaY * 0.15F, -89.0F, 89.0F));
	}

	public void changeSpeed(double scrollSteps) {
		speed = Mth.clamp(speed + scrollSteps * 0.06D, 0.03D, 3.0D);
	}

	public void cycleMovementMode() {
		MovementMode[] values = MovementMode.values();
		movementMode = values[(movementMode.ordinal() + 1) % values.length];
	}

	public void changeRoll(PhotoModeSettings settings, double scrollSteps) {
		settings.roll = Mth.clamp(settings.roll + scrollSteps * 2.5D, -45.0D, 45.0D);
		state.setTargetRoll((float) settings.roll);
	}

	public void changeFov(PhotoModeSettings settings, double scrollSteps) {
		settings.fov = Mth.clamp(settings.fov + scrollSteps * 2.5D, 10.0D, 170.0D);
	}

	public CameraState state() {
		return state;
	}

	public double speed() {
		return speed;
	}

	public MovementMode movementMode() {
		return movementMode;
	}

	public enum MovementMode {
		PRECISION("Precision", 0.35D, 2.0D, 0.20F),
		CINEMATIC("Cinematic", 0.80D, 3.0D, 0.12F),
		SCOUTING("Scouting", 2.25D, 5.0D, 0.35F);

		private final String label;
		private final double speedMultiplier;
		private final double boostMultiplier;
		private final float interpolationAlpha;

		MovementMode(String label, double speedMultiplier, double boostMultiplier, float interpolationAlpha) {
			this.label = label;
			this.speedMultiplier = speedMultiplier;
			this.boostMultiplier = boostMultiplier;
			this.interpolationAlpha = interpolationAlpha;
		}

		public String label() {
			return label;
		}
	}
}
