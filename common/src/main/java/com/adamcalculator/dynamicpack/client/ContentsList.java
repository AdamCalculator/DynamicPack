package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.pack.BaseContent;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ContentsList extends ContainerObjectSelectionList<ContentsList.ContentEntry> {
    private final ContentsScreen parent;

    public ContentsList(ContentsScreen parent, Minecraft minecraft) {
        super(minecraft, parent.width, parent.height - 52, 20, 40);
        this.parent = parent;

        for (BaseContent knownContent : parent.preChangeStates.keySet()) {
            var v = new ContentsList.BaseContentEntry(knownContent);
            this.addEntry(v);
        }
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public void refreshAll() {
        children().forEach(ContentEntry::refresh);
    }

    public class BaseContentEntry extends ContentEntry {
        private final BaseContent content;
        private final Button stateButton;

        BaseContentEntry(BaseContent knownContent) {
            this.content = knownContent;

            this.stateButton = createStateButton();
            stateButton.active = !content.isRequired();
            if (!stateButton.active) {
                this.stateButton.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack_contents.state.tooltip_disabled")));
            }
        }

        private Button createStateButton() {
            return Button.builder(Component.translatable("dynamicpack.screen.pack_contents.state", currentState()), (button) -> {
                try {
                    content.nextOverride();
                } catch (Exception e) {
                    Out.error("Error while switch content override", e);
                }
                parent.onAfterChange();
                refresh();
            }).bounds(0, 0, 140, 20).build();
        }


        @Override
        public void refresh() {
            stateButton.setMessage(Component.translatable("dynamicpack.screen.pack_contents.state", currentState()));
        }

        private Component currentState() {
            String s = switch (content.getOverride()) {
                case TRUE -> "dynamicpack.screen.pack_contents.state.true";
                case FALSE -> "dynamicpack.screen.pack_contents.state.false";
                case NOT_SET -> {
                    if (content.getWithDefaultState()) {
                        yield "dynamicpack.screen.pack_contents.state.default.true";
                    } else {
                        yield "dynamicpack.screen.pack_contents.state.default.false";
                    }
                }
            };
            return Component.translatable(s);
        }

        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String txt = content.getId();
            String name = content.getName();
            if (name != null) {
                txt = name;
            }
            Component text = Component.literal(txt);
            context.drawString(ContentsList.this.minecraft.font, text, (x - 50), y+10, 16777215, false);
            this.stateButton.setX(x+entryWidth-140);
            this.stateButton.setY(y);
            this.stateButton.render(context, mouseX, mouseY, tickDelta);
        }

        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.stateButton);
        }

        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.stateButton);
        }

    }

    public abstract static class ContentEntry extends Entry<ContentEntry> {
        public abstract void refresh();
    }
}
