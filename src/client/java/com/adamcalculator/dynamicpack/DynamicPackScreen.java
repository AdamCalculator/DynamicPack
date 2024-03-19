package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DynamicPackScreen extends Screen {
    private final Screen parent;

    public DynamicPackScreen(Screen parent, Pack pack) {
        super(Text.literal(pack.getName()));
        this.client = MinecraftClient.getInstance();
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.of("Manually sync"), button -> DynamicPackModBase.INSTANCE.startSyncThread()).size(120, 20).position(this.width / 2, this.height / 2).build());
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
