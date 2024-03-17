package com.adamcalculator.dynamicpack;

import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.util.Out;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class DebugScreen extends Screen {
    protected DebugScreen() {
        super(Text.literal("DebugScreen"));
    }

    @Override
    public void render(DrawContext matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.of("Re-Scan & Re-sync normally"), button -> {
            DynamicPackModBase.INSTANCE.rescanPacks();
            DynamicPackModBase.INSTANCE.startSyncThread();
            MinecraftClient.getInstance().setScreen(new DebugScreen());
        }).size(120, 20).position(this.width-130, 10).build());

        int height = 10;
        for (Pack pack : DynamicPackModBase.packs.toArray(new Pack[0])) {
            Out.println("gui pack: " + pack);
            try {
                addDrawableChild(ButtonWidget.builder(Text.of(pack.getLocation().getName() + ":"+pack.getCachedUpdateAvailableStatus()), button -> {

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

                            @Override
                            public void downloading(String name, float percentage) {

                            }

                            @Override
                            public void start() {

                            }
                        }, true);
                    } catch (Exception e) {
                        Out.e(e);
                    }
                }).size(50, 20).position(190, height).build());
            } catch (Exception e) {
                addDrawableChild(ButtonWidget.builder(Text.of(e + ""), button -> {
                }).size(500, 20).position(10, height).build());
                Out.e(e);
            }

            height += 40;
        }
    }
}
