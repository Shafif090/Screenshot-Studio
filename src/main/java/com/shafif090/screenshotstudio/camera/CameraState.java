package com.shafif090.screenshotstudio.camera;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class CameraState {
	private Vec3 targetPosition = Vec3.ZERO;
	private Vec3 renderPosition = Vec3.ZERO;
	private float targetYaw;
	private float targetPitch;
	private float targetRoll;
	private float renderYaw;
	private float renderPitch;
	private float renderRoll;

	public void snap(Vec3 position, float yaw, float pitch, float roll) {
		this.targetPosition = position;
		this.renderPosition = position;
		this.targetYaw = yaw;
		this.targetPitch = pitch;
		this.targetRoll = roll;
		this.renderYaw = yaw;
		this.renderPitch = pitch;
		this.renderRoll = roll;
	}

	public void interpolate(float alpha) {
		this.renderPosition = new Vec3(
				Mth.lerp(alpha, this.renderPosition.x, this.targetPosition.x),
				Mth.lerp(alpha, this.renderPosition.y, this.targetPosition.y),
				Mth.lerp(alpha, this.renderPosition.z, this.targetPosition.z)
		);
		this.renderYaw = Mth.rotLerp(alpha, this.renderYaw, this.targetYaw);
		this.renderPitch = Mth.lerp(alpha, this.renderPitch, this.targetPitch);
		this.renderRoll = Mth.lerp(alpha, this.renderRoll, this.targetRoll);
	}

	public Vec3 targetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(Vec3 targetPosition) {
		this.targetPosition = targetPosition;
	}

	public Vec3 renderPosition() {
		return renderPosition;
	}

	public float targetYaw() {
		return targetYaw;
	}

	public void setTargetYaw(float targetYaw) {
		this.targetYaw = targetYaw;
	}

	public float targetPitch() {
		return targetPitch;
	}

	public void setTargetPitch(float targetPitch) {
		this.targetPitch = targetPitch;
	}

	public float targetRoll() {
		return targetRoll;
	}

	public void setTargetRoll(float targetRoll) {
		this.targetRoll = targetRoll;
	}

	public float renderYaw() {
		return renderYaw;
	}

	public float renderPitch() {
		return renderPitch;
	}

	public float renderRoll() {
		return renderRoll;
	}
}
