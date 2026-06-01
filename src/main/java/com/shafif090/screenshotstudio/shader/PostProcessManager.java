package com.shafif090.screenshotstudio.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.shafif090.screenshotstudio.ScreenshotStudioMod;
import com.shafif090.screenshotstudio.mixin.accessor.PostChainAccessor;
import com.shafif090.screenshotstudio.mixin.accessor.PostPassAccessor;
import com.shafif090.screenshotstudio.settings.PhotoModeSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

public final class PostProcessManager {
	private static final Identifier PHOTO_MODE_CHAIN = ScreenshotStudioMod.id("photo_mode");
	private static final String UNIFORM_BLOCK = "ScreenshotStudioConfig";
	private static final int CONFIG_UBO_SIZE = 64;
	private boolean shaderLogged;
	private boolean shaderAvailable = true;
	private int ticks;
	private ByteBuffer uniformScratch;

	public void tick(PhotoModeSettings settings) {
		settings.clamp();
		ticks++;
	}

	public void process(Minecraft client, CrossFrameResourcePool resourcePool, PhotoModeSettings settings) {
		settings.clamp();
		try {
			PostChain postChain = client.getShaderManager().getPostChain(PHOTO_MODE_CHAIN, LevelTargetBundle.MAIN_TARGETS);
			if (postChain == null) {
				shaderAvailable = false;
				if (!shaderLogged) {
					ScreenshotStudioMod.LOGGER.warn("Screenshot Studio post chain missing or failed to compile; using fallback overlays");
					shaderLogged = true;
				}
				return;
			}

			shaderLogged = false;
			shaderAvailable = true;
			updateUniforms(postChain, settings);
			postChain.process(client.getMainRenderTarget(), resourcePool);
		} catch (Throwable throwable) {
			shaderAvailable = false;
			if (!shaderLogged) {
				ScreenshotStudioMod.LOGGER.warn("Screenshot Studio post chain failed; using fallback overlays", throwable);
				shaderLogged = true;
			}
		}
	}

	public void extractOverlays(GuiGraphicsExtractor graphics, int width, int height, PhotoModeSettings settings) {
		if (shaderAvailable) {
			return;
		}

		extractBrightness(graphics, width, height, settings.brightness);
		extractWhiteBalance(graphics, width, height, settings.temperature, settings.tint);
		extractVignette(graphics, width, height, settings.vignette);
		extractFilmGrain(graphics, width, height, settings.filmGrain);
	}

	private void updateUniforms(PostChain postChain, PhotoModeSettings settings) {
		List<PostPass> passes = ((PostChainAccessor) postChain).screenshotstudio$getPasses();
		for (PostPass pass : passes) {
			Map<String, GpuBuffer> uniforms = ((PostPassAccessor) (Object) pass).screenshotstudio$getCustomUniforms();
			GpuBuffer buffer = uniforms.get(UNIFORM_BLOCK);
			if (buffer != null) {
				GpuBuffer writable = ensureWritableUniformBuffer(uniforms, buffer);
				writeUniforms(writable, settings);
			}
		}
	}

	private GpuBuffer ensureWritableUniformBuffer(Map<String, GpuBuffer> uniforms, GpuBuffer buffer) {
		if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) != 0 && buffer.size() >= CONFIG_UBO_SIZE) {
			return buffer;
		}

		GpuBuffer replacement = RenderSystem.getDevice().createBuffer(
				() -> "Screenshot Studio uniforms",
				GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
				Math.max(CONFIG_UBO_SIZE, buffer.size())
		);
		uniforms.put(UNIFORM_BLOCK, replacement);
		buffer.close();
		return replacement;
	}

	private void writeUniforms(GpuBuffer buffer, PhotoModeSettings settings) {
		if (buffer.size() > Integer.MAX_VALUE) {
			return;
		}

		float brightness = (float) (settings.brightness / 100.0D) * 0.35F;
		float contrast = (float) (settings.contrast / 100.0D);
		float saturation = (float) (settings.saturation / 100.0D);
		float temperature = (float) (settings.temperature / 100.0D) * 0.16F;
		float tint = (float) (settings.tint / 100.0D) * 0.14F;
		float vignette = (float) (settings.vignette / 100.0D);
		float aberration = (float) (settings.chromaticAberration / 100.0D);
		float grain = (float) (settings.filmGrain / 100.0D);
		float sharpness = (float) (settings.sharpness / 10.0D);
		float focusDistance = (float) ((settings.focusDistance - 0.5D) / 99.5D);
		float aperture = (float) (settings.aperture / 20.0D);
		float dofEnabled = settings.depthOfField ? 1.0F : 0.0F;
		float time = ticks / 20.0F;

		int size = (int) buffer.size();
		ByteBuffer data = getUniformScratch(size);
		for (int i = 0; i < size; i++) {
			data.put(i, (byte) 0);
		}
		Std140Builder builder = Std140Builder.intoBuffer(data);
		builder.putVec4(brightness, contrast, saturation, temperature);
		builder.putVec4(tint, vignette, aberration, grain);
		builder.putVec4(sharpness, focusDistance, aperture, dofEnabled);
		builder.putVec4(time, 0.0F, 0.0F, 0.0F);
		ByteBuffer payload = builder.get();
		RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), payload);
		data.clear();
	}

	private ByteBuffer getUniformScratch(int size) {
		if (uniformScratch == null || uniformScratch.capacity() < size) {
			uniformScratch = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		}
		uniformScratch.clear();
		uniformScratch.limit(size);
		return uniformScratch;
	}

	private void extractBrightness(GuiGraphicsExtractor graphics, int width, int height, double brightness) {
		int alpha = (int) (Math.min(1.0D, Math.abs(brightness) / 100.0D) * 80.0D);
		if (alpha <= 0) {
			return;
		}
		int color = brightness > 0.0D ? argb(alpha, 255, 255, 255) : argb(alpha, 0, 0, 0);
		graphics.fill(0, 0, width, height, color);
	}

	private void extractWhiteBalance(GuiGraphicsExtractor graphics, int width, int height, double temperature, double tint) {
		int tempAlpha = (int) (Math.min(1.0D, Math.abs(temperature) / 100.0D) * 38.0D);
		if (tempAlpha > 0) {
			int color = temperature > 0.0D ? argb(tempAlpha, 255, 154, 64) : argb(tempAlpha, 64, 140, 255);
			graphics.fill(0, 0, width, height, color);
		}

		int tintAlpha = (int) (Math.min(1.0D, Math.abs(tint) / 100.0D) * 32.0D);
		if (tintAlpha > 0) {
			int color = tint > 0.0D ? argb(tintAlpha, 255, 80, 220) : argb(tintAlpha, 80, 255, 120);
			graphics.fill(0, 0, width, height, color);
		}
	}

	private void extractVignette(GuiGraphicsExtractor graphics, int width, int height, double strength) {
		int alpha = (int) (Math.min(1.0D, strength / 100.0D) * 150.0D);
		if (alpha <= 0) {
			return;
		}

		int edge = Math.max(12, Math.min(width, height) / 5);
		int dark = argb(alpha, 0, 0, 0);
		int clear = argb(0, 0, 0, 0);
		graphics.fillGradient(0, 0, width, edge, dark, clear);
		graphics.fillGradient(0, height - edge, width, height, clear, dark);
		graphics.fillGradient(0, 0, edge, height, dark, clear);
		graphics.fillGradient(width - edge, 0, width, height, clear, dark);
	}

	private void extractFilmGrain(GuiGraphicsExtractor graphics, int width, int height, double strength) {
		int alpha = (int) (Math.min(1.0D, strength / 100.0D) * 40.0D);
		if (alpha <= 0) {
			return;
		}

		int step = 11;
		for (int y = 0; y < height; y += step) {
			for (int x = (y * 3) % step; x < width; x += step) {
				int shade = ((x * 31 + y * 17) & 1) == 0 ? 255 : 0;
				graphics.fill(x, y, x + 1, y + 1, argb(alpha, shade, shade, shade));
			}
		}
	}

	private static int argb(int alpha, int red, int green, int blue) {
		return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
	}
}
