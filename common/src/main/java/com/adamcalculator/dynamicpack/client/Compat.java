package com.adamcalculator.dynamicpack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class Compat {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static <T extends GuiEventListener & Widget & NarratableEntry> T createButton(Component text, Runnable press, int w, int h, int x, int y) {
        return (T) new Button(x, y, w, h, text, (jhfdre) -> press.run());
    }

    /**
     * Rewritten from ModMenu (MIT)
     */
    public static void drawWrappedString(PoseStack matrices, String string, int x, int y, int wrapWidth, int lines, int color) {
        while (string != null && string.endsWith("\n")) {
            string = string.substring(0, string.length() - 1);
        }
        List<FormattedText> strings = CLIENT.font.getSplitter().splitLines(new TextComponent(string), wrapWidth, Style.EMPTY);
        for (int i = 0; i < strings.size(); i++) {
            if (i >= lines) {
                break;
            }
            FormattedText renderable = strings.get(i);
            if (i == lines - 1 && strings.size() > lines) {
                renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
            }
            FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
            int x1 = x;

            if (CLIENT.font.isBidirectional()) {
                int width = CLIENT.font.width(line);
                x1 += (float) (wrapWidth - width);
            }
            CLIENT.font.draw(matrices, line, x1, y + i * CLIENT.font.lineHeight, color);
        }
    }


    public static void runAtUI(Runnable o) {
        CLIENT.execute(o);
    }

    public static void drawTexture(PoseStack context, ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(context, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void renderBackground(Screen screen, PoseStack context, int mouseX, int mouseY, float delta) {
        screen.renderBackground(context);
    }

    public static void drawString(PoseStack context, Font font, Component text, int i, int i1, int i2) {
        GuiComponent.drawString(context, font, text, i, i1, i2);
    }

    public static void drawString(PoseStack context, Font font, FormattedCharSequence text, int i, int i1, int i2) {
        GuiComponent.drawString(context, font, text, i, i1, i2);
    }
}
