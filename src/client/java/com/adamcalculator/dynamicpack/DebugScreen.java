package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

import java.io.IOException;

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
        addDrawableChild(ButtonWidget.builder(Text.of("Re-Scan"), button -> {
            DynamicPackMod.rescanPacks();
            MinecraftClient.getInstance().setScreen(new DebugScreen());
        }).size(50, 20).position(this.width-60, 10).build());

        int height = 10;
        for (Pack pack : DynamicPackMod.packs) {
            Out.println("gui pack: " + pack);
            try {
                addDrawableChild(ButtonWidget.builder(Text.of(pack.getLocation().getName() + ":"+pack.checkIsUpdateAvailable()), button -> {

                }).size(160, 20).position(10, height).build());

                addDrawableChild(ButtonWidget.builder(Text.of("Sync!"), button -> {
                    SystemToast toast = new SystemToast(SystemToast.Type.NARRATOR_TOGGLE, Text.literal("T"), Text.literal("d"));
                    MinecraftClient.getInstance().getToastManager().add(toast);

                    try {
                        pack.sync(new SyncProgress() {
                            @Override
                            public void textLog(String s) {
                                toast.setContent(Text.literal("Log"), Text.literal(s));
                            }

                            @Override
                            public void done(boolean b) {
                                if (b) {
                                    toast.setContent(Text.literal("Done. Reload!"), Text.literal("Reload required!!!"));
                                } else {
                                    toast.setContent(Text.literal("Done!"), Text.literal(""));
                                }
                            }
                        }, true);
                    } catch (Exception e) {
                        Out.e(e);
                    }
                }).size(50, 20).position(190, height).build());
            } catch (IOException e) {
                addDrawableChild(ButtonWidget.builder(Text.of(e + ""), button -> {
                }).size(500, 20).position(10, height).build());
                Out.e(e);
            }

            height += 40;
        }
    }
}
