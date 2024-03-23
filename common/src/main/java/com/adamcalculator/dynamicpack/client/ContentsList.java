package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.pack.BaseContent;
import com.adamcalculator.dynamicpack.pack.DynamicRepoRemote;
import com.adamcalculator.dynamicpack.pack.OverrideType;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.util.Out;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class ContentsList extends ContainerObjectSelectionList<ContentsList.ContentEntry> {
    private final ContentsScreen parent;
    private final Pack pack;
    private final Consumer<Boolean> resyncOnExit;
    private final HashMap<BaseContent, OverrideType> preChangeStates = new HashMap<>();
    private final List<ContentEntry> entries = new ArrayList<>();

    public ContentsList(ContentsScreen parent, Minecraft minecraft, Pack pack, Consumer<Boolean> resyncOnExit) {
        super(minecraft, parent.width, parent.height, 20, parent.height - 32, 40);
        this.parent = parent;
        this.pack = pack;
        this.resyncOnExit = resyncOnExit;


        for (BaseContent knownContent : ((DynamicRepoRemote) pack.getRemote()).getKnownContents()) {
            preChangeStates.put(knownContent, knownContent.getOverride());
            var v = new BaseContentEntry(knownContent);
            entries.add(v);
            this.addEntry(v);
        }
    }

    public boolean isChanges() {
        boolean t = false;

        for (BaseContent knownContent : preChangeStates.keySet()) {
            if (preChangeStates.get(knownContent) != knownContent.getOverride()) {
                t = true;
                break;
            }
        }
        return t;
    }

    public void onAfterChange() {
        boolean isChanges = isChanges();
        resyncOnExit.accept(isChanges);
    }

    public void reset() {
        for (BaseContent knownContent : preChangeStates.keySet()) {
            OverrideType overrideType = preChangeStates.get(knownContent);
            try {
                knownContent.setOverrideType(overrideType);
            } catch (Exception e) {
                Out.error("Error while reset changes", e);
            }
        }


        for (ContentEntry entry : entries) {
            entry.refresh();
        }

        onAfterChange();
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
                onAfterChange();
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

        public void render(PoseStack context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            String txt = content.getId();
            String name = content.getName();
            if (name != null) {
                txt = name;
            }
            Component text = Component.literal(txt);
            Compat.drawString(context, ContentsList.this.minecraft.font, text, (x - 70), y+10, 16777215);
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
