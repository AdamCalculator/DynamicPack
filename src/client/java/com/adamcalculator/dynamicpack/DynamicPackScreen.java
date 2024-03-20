package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
        addDrawableChild(Compat.createButton(
                Text.of("Manually sync"),
                        () -> DynamicPackModBase.INSTANCE.startManuallySync(),
                100, 20, width - 120, 10
        ));
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
