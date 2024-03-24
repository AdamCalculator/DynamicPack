package com.adamcalculator.dynamicpack.client;

import com.adamcalculator.dynamicpack.DynamicPackMod;
import com.adamcalculator.dynamicpack.pack.BaseContent;
import com.adamcalculator.dynamicpack.pack.DynamicRepoRemote;
import com.adamcalculator.dynamicpack.pack.OverrideType;
import com.adamcalculator.dynamicpack.pack.Pack;
import com.adamcalculator.dynamicpack.util.Out;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class ContentsScreen extends Screen {
    private final Screen parent;
    private final Pack pack;
    private ContentsList contentsList;
    private final Consumer<Pack> onPackReSync = pack -> Compat.runAtUI(this::onClose);
    private boolean syncOnExit = false;
    private Button doneButton;
    private Button resetButton;

    protected final LinkedHashMap<BaseContent, OverrideType> preChangeStates = new LinkedHashMap<>();

    protected ContentsScreen(Screen parent, Pack pack) {
        super(Component.translatable("dynamicpack.screen.pack_contents.title"));
        this.parent = parent;
        this.pack = pack;
        this.minecraft = Minecraft.getInstance();
        this.pack.addDestroyListener(onPackReSync);

        for (BaseContent knownContent : ((DynamicRepoRemote) pack.getRemote()).getKnownContents()) {
            preChangeStates.put(knownContent, knownContent.getOverride());
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

    public void reset() {
        for (BaseContent knownContent : preChangeStates.keySet()) {
            OverrideType overrideType = preChangeStates.get(knownContent);
            try {
                knownContent.setOverrideType(overrideType);
            } catch (Exception e) {
                Out.error("Error while reset changes for content: " + knownContent, e);
            }
        }

        contentsList.refreshAll();
        onAfterChange();
    }


    public void onAfterChange() {
        this.syncOnExit = isChanges();
        updateDoneButton();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
        pack.removeDestroyListener(onPackReSync);
        if (syncOnExit) {
            DynamicPackMod.INSTANCE.startManuallySync();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !syncOnExit;
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        contentsList.render(context, mouseX, mouseY, delta);
        Compat.drawCenteredString(context, this.font, this.title, this.width / 2, 8, 16777215);
    }

    @Override
    protected void init() {
        super.init();
        this.addWidget(this.contentsList = new ContentsList(this, this.minecraft));
        this.addRenderableWidget(doneButton = Compat.createButton(CommonComponents.GUI_DONE, this::onClose, 150, 20, this.width / 2 - 155 + 160, this.height - 29));
        this.addRenderableWidget(resetButton = Compat.createButton(Component.translatable("controls.reset"), this::reset, 150, 20, this.width / 2 - 155, this.height - 29));
        updateDoneButton();
    }

    private void updateDoneButton() {
        if (syncOnExit) {
            doneButton.setMessage(Component.translatable("dynamicpack.screen.pack_contents.apply").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD));
            doneButton.setTooltip(Tooltip.create(Component.translatable("dynamicpack.screen.pack_contents.apply.tooltip")));
        } else {
            doneButton.setMessage(CommonComponents.GUI_DONE);
            doneButton.setTooltip(null);
        }
        resetButton.visible = syncOnExit;
    }
}
