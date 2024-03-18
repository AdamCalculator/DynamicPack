package com.adamcalculator.dynamicpack;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DebugScreen extends Screen {
    protected DebugScreen() {
        super(Text.literal("DebugScreen"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.of("Re-Scan & Re-sync normally"), button -> DynamicPackModBase.INSTANCE.startSyncThread()).size(120, 20).position(this.width / 2, this.height / 2).build());
    }
}
