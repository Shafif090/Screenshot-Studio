package com.shafif090.screenshotstudio.ui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public final class SliderWidget extends AbstractSliderButton {
	private final String label;
	private final double min;
	private final double max;
	private final DoubleSupplier getter;
	private final DoubleConsumer setter;

	public SliderWidget(int x, int y, int width, String label, double min, double max, DoubleSupplier getter, DoubleConsumer setter) {
		super(x, y, width, 18, Component.empty(), normalize(getter.getAsDouble(), min, max));
		this.label = label;
		this.min = min;
		this.max = max;
		this.getter = getter;
		this.setter = setter;
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		double realValue = min + (max - min) * this.value;
		String formatted = Math.abs(realValue) >= 10.0D
				? String.format(Locale.ROOT, "%.0f", realValue)
				: String.format(Locale.ROOT, "%.1f", realValue);
		setMessage(Component.literal(label + ": " + formatted));
	}

	@Override
	protected void applyValue() {
		setter.accept(min + (max - min) * this.value);
	}

	public void refresh() {
		this.value = normalize(getter.getAsDouble(), min, max);
		updateMessage();
	}

	private static double normalize(double value, double min, double max) {
		if (max <= min) {
			return 0.0D;
		}
		double normalized = (value - min) / (max - min);
		return Math.max(0.0D, Math.min(1.0D, normalized));
	}
}
