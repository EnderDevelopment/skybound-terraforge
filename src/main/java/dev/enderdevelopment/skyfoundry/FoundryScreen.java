package dev.enderdevelopment.skyfoundry;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class FoundryScreen extends HandledScreen<FoundryScreenHandler> {
    public FoundryScreen(FoundryScreenHandler handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); }

    @Override protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Change radius"), button -> client.interactionManager.clickButton(handler.syncId, 0)).dimensions(x + 8, y + 58, 110, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Lift terrain"), button -> client.interactionManager.clickButton(handler.syncId, 1)).dimensions(x + 124, y + 58, 100, 20).build());
    }

    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = x, top = y;
        context.fill(left, top, left + backgroundWidth, top + backgroundHeight, 0xE0161B29);
        context.fill(left + 5, top + 5, left + backgroundWidth - 5, top + 28, 0xFF273C5A);
        context.drawBorder(left, top, backgroundWidth, backgroundHeight, 0xFF8BC6FF);
        context.drawText(textRenderer, Text.literal("Radius: " + radius()), left + 8, top + 36, 0xFFBBDFFF, false);
    }

    private int radius() {
        if (client == null || client.world == null) return 8;
        return client.world.getBlockState(handler.position()).get(FoundryBlock.RADIUS);
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { renderBackground(context, mouseX, mouseY, delta); super.render(context, mouseX, mouseY, delta); drawMouseoverTooltip(context, mouseX, mouseY); }
}
