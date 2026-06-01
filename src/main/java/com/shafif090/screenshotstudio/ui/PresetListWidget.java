package com.shafif090.screenshotstudio.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public final class PresetListWidget extends AbstractWidget {
	private static final int ROW_HEIGHT = 18;
	private final List<String> presets;
	private final Consumer<String> onSelect;
	private int scroll;
	private String selected;

	public PresetListWidget(int x, int y, int width, int height, List<String> presets, Consumer<String> onSelect) {
		super(x, y, width, height, Component.translatable("screen.screenshotstudio.presets"));
		this.presets = presets;
		this.onSelect = onSelect;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(getX(), getY(), getRight(), getBottom(), 0xAA101216);
		graphics.outline(getX(), getY(), getWidth(), getHeight(), 0x66FFFFFF);
		graphics.enableScissor(getX(), getY(), getRight(), getBottom());

		int y = getY() + 4 - scroll;
		for (String preset : presets) {
			boolean hovered = mouseX >= getX() && mouseX <= getRight() && mouseY >= y && mouseY <= y + ROW_HEIGHT;
			boolean active = preset.equals(selected);
			if (active || hovered) {
				graphics.fill(getX() + 3, y, getRight() - 3, y + ROW_HEIGHT - 2, active ? 0x884F8DFF : 0x44FFFFFF);
			}
			graphics.text(font(), preset, getX() + 8, y + 5, 0xFFECEFF4);
			y += ROW_HEIGHT;
		}

		graphics.disableScissor();
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubleClick) {
		int index = (int) ((event.y() - getY() - 4 + scroll) / ROW_HEIGHT);
		if (index >= 0 && index < presets.size()) {
			selected = presets.get(index);
			onSelect.accept(selected);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		int maxScroll = Math.max(0, presets.size() * ROW_HEIGHT - getHeight() + 8);
		scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (scrollY * ROW_HEIGHT)));
		return true;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	private net.minecraft.client.gui.Font font() {
		return net.minecraft.client.Minecraft.getInstance().font;
	}
}
