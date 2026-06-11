package com.shafif090.screenshotstudio.camera;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PhotoModeCameraEntity extends AbstractClientPlayer {
	private static final int ENTITY_ID = -909090;
	private static final GameProfile PROFILE = new GameProfile(
			UUID.nameUUIDFromBytes("screenshotstudio:photo_mode_camera".getBytes(StandardCharsets.UTF_8)),
			"ScreenshotStudioCamera"
	);

	public PhotoModeCameraEntity(ClientLevel level) {
		super(level, PROFILE);
		setId(ENTITY_ID);
		setPose(Pose.SWIMMING);
		setNoGravity(true);
		this.noPhysics = true;
		getAbilities().flying = true;
	}

	public void syncTo(CameraState state) {
		Vec3 position = state.targetPosition();
		snapTo(position.x, position.y - getEyeHeight(), position.z, state.targetYaw(), state.targetPitch());
		setYHeadRot(state.targetYaw());
		setYBodyRot(state.targetYaw());
		setDeltaMovement(Vec3.ZERO);
		setOnGround(false);
		this.noPhysics = true;
		getAbilities().flying = true;
	}

	public void spawn() {
		((ClientLevel) level()).addEntity(this);
	}

	public void despawn() {
		if (!isRemoved()) {
			((ClientLevel) level()).removeEntity(getId(), RemovalReason.DISCARDED);
		}
	}

	@Override
	public void tick() {
		setDeltaMovement(Vec3.ZERO);
		setOnGround(false);
	}

	@Override
	public boolean canCollideWith(Entity other) {
		return false;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public void setPose(Pose pose) {
		super.setPose(Pose.SWIMMING);
	}

	@Override
	public float getViewXRot(float partialTick) {
		return getXRot();
	}

	@Override
	public float getViewYRot(float partialTick) {
		return getYRot();
	}
}
