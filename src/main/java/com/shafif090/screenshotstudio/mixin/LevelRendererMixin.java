package com.shafif090.screenshotstudio.mixin;

import com.shafif090.screenshotstudio.ScreenshotStudioClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelRendererMixin {
	@Shadow
	private EntityRenderState extractEntity(Entity entity, float partialTickTime) {
		throw new AssertionError();
	}

	@Inject(method = "extractVisibleEntities", at = @At("RETURN"), require = 0)
	private void screenshotstudio$extractRealPlayer(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState output, CallbackInfo callbackInfo) {
		Minecraft client = Minecraft.getInstance();
		if (!ScreenshotStudioClient.isPhotoModeActive()
				|| client.level == null
				|| client.player == null
				|| camera.entity() == client.player) {
			return;
		}

		TickRateManager tickRateManager = client.level.tickRateManager();
		float partialTick = deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(client.player));
		EntityRenderState state = extractEntity(client.player, partialTick);
		state.isInvisible = false;
		if (state instanceof LivingEntityRenderState livingState) {
			livingState.isInvisibleToPlayer = false;
		}
		if (!ScreenshotStudioClient.shouldShowNameTags()) {
			state.nameTag = null;
			state.scoreText = null;
		}
		output.entityRenderStates.add(state);
		output.lastEntityRenderStateCount = output.entityRenderStates.size();
	}
}
