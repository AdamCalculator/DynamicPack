package com.adamcalculator.dynamicpack;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class Compat {
    public static <T extends Element & Drawable & Selectable> T createButton(Text text, Runnable press, int w, int h, int x, int y) {
        return (T) ButtonWidget.builder(text, button -> press.run()).size(w, h).position(x, y).build();
    }
}
