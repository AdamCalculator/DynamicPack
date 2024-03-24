package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.pack.BaseContent;
import com.adamcalculator.dynamicpack.pack.OverrideType;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class ContentsList extends ContainerObjectSelectionList<ContentsList.ContentEntry> {
    private final ContentsScreen parent;

    public ContentsList(ContentsScreen parent, Minecraft minecraft) {
        super(minecraft, parent.width, parent.height, 20, parent.height - 32, 40);
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
                // TODO: 1.18.2 backport: not work tooltips
                //this.stateButton.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack_contents.state.tooltip_disabled")));
            }
        }

        private Button createStateButton() {
            return new Button(0, 0, 140, 20, new TranslatableComponent("dynamicpack.screen.pack_contents.state", currentState()), (button) -> {
                try {
                    content.nextOverride();
                } catch (Exception e) {
                    Out.error("Error while switch content override", e);
                }
                parent.onAfterChange();
                refresh();
            });
        }


        @Override
        public void refresh() {
            stateButton.setMessage(new TranslatableComponent("dynamicpack.screen.pack_contents.state", currentState()));
        }

        private Component currentState() {
            OverrideType ov = content.getOverride();
            String s = "hmm";
            if (ov == OverrideType.TRUE) {
                s = "dynamicpack.screen.pack_contents.state.true";
            } else if (ov == OverrideType.FALSE) {
                s = "dynamicpack.screen.pack_contents.state.false";
            } else if (ov == OverrideType.NOT_SET) {
                if (content.getWithDefaultState()) {
                    s = "dynamicpack.screen.pack_contents.state.default.true";
                } else {
                    s = "dynamicpack.screen.pack_contents.state.default.false";
                }
            }
            return new TranslatableComponent(s);
        }

        public void render(PoseStack context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String txt = content.getId();
            String name = content.getName();
            if (name != null) {
                txt = name;
            }
            Component text = new TextComponent(txt);
            Compat.drawString(context, ContentsList.this.minecraft.font, text, (x - 50), y+10, 16777215);
            this.stateButton.x = (x+entryWidth-140);
            this.stateButton.y = (y);
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
