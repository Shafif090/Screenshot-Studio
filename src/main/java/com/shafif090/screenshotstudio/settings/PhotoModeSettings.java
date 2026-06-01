package com.shafif090.screenshotstudio.settings;

import net.minecraft.util.Mth;

public final class PhotoModeSettings {
	public double fov = 70.0D;
	public boolean depthOfField;
	public double focusDistance = 12.0D;
	public double aperture = 0.0D;
	public double roll = 0.0D;
	public double brightness = 0.0D;
	public double contrast = 0.0D;
	public double saturation = 0.0D;
	public double temperature = 0.0D;
	public double tint = 0.0D;
	public double vignette = 0.0D;
	public double chromaticAberration = 0.0D;
	public double filmGrain = 0.0D;
	public double sharpness = 0.0D;
	public double timeOfDay = 6000.0D;

	public static PhotoModeSettings defaults() {
		return new PhotoModeSettings();
	}

	public static PhotoModeSettings cinematic() {
		PhotoModeSettings settings = defaults();
		settings.fov = 55.0D;
		settings.contrast = 18.0D;
		settings.saturation = -8.0D;
		settings.temperature = 12.0D;
		settings.vignette = 35.0D;
		settings.filmGrain = 8.0D;
		return settings;
	}

	public static PhotoModeSettings vivid() {
		PhotoModeSettings settings = defaults();
		settings.fov = 65.0D;
		settings.brightness = 6.0D;
		settings.contrast = 12.0D;
		settings.saturation = 32.0D;
		settings.temperature = 4.0D;
		settings.vignette = 12.0D;
		return settings;
	}

	public static PhotoModeSettings noir() {
		PhotoModeSettings settings = defaults();
		settings.fov = 48.0D;
		settings.brightness = -8.0D;
		settings.contrast = 46.0D;
		settings.saturation = -100.0D;
		settings.vignette = 58.0D;
		settings.filmGrain = 18.0D;
		return settings;
	}

	public PhotoModeSettings copy() {
		PhotoModeSettings copy = new PhotoModeSettings();
		copy.copyFrom(this);
		return copy;
	}

	public void copyFrom(PhotoModeSettings other) {
		this.fov = other.fov;
		this.depthOfField = other.depthOfField;
		this.focusDistance = other.focusDistance;
		this.aperture = other.aperture;
		this.roll = other.roll;
		this.brightness = other.brightness;
		this.contrast = other.contrast;
		this.saturation = other.saturation;
		this.temperature = other.temperature;
		this.tint = other.tint;
		this.vignette = other.vignette;
		this.chromaticAberration = other.chromaticAberration;
		this.filmGrain = other.filmGrain;
		this.sharpness = other.sharpness;
		this.timeOfDay = other.timeOfDay;
		clamp();
	}

	public void clamp() {
		this.fov = Mth.clamp(this.fov, 10.0D, 170.0D);
		this.focusDistance = Mth.clamp(this.focusDistance, 0.5D, 100.0D);
		this.aperture = Mth.clamp(this.aperture, 0.0D, 20.0D);
		this.roll = Mth.clamp(this.roll, -45.0D, 45.0D);
		this.brightness = Mth.clamp(this.brightness, -100.0D, 100.0D);
		this.contrast = Mth.clamp(this.contrast, -100.0D, 100.0D);
		this.saturation = Mth.clamp(this.saturation, -100.0D, 100.0D);
		this.temperature = Mth.clamp(this.temperature, -100.0D, 100.0D);
		this.tint = Mth.clamp(this.tint, -100.0D, 100.0D);
		this.vignette = Mth.clamp(this.vignette, 0.0D, 100.0D);
		this.chromaticAberration = Mth.clamp(this.chromaticAberration, 0.0D, 100.0D);
		this.filmGrain = Mth.clamp(this.filmGrain, 0.0D, 100.0D);
		this.sharpness = Mth.clamp(this.sharpness, -50.0D, 50.0D);
		this.timeOfDay = Mth.clamp(this.timeOfDay, 0.0D, 24000.0D);
	}
}
