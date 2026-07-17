package org.nooberic.dg_toy.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.network.DgToyC2SPawprintRuleStringVarPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleVariable;

import java.util.ArrayList;
import java.util.List;

public class PawprintRuleSettingsScreen extends Screen {
    private static final int HEADER_HEIGHT = 42;
    private static final int FOOTER_HEIGHT = 34;
    private static final int ROW_HEIGHT = 24;
    private static final int VALUE_BOX_WIDTH = 100;
    private static final int VALUE_BOX_HEIGHT = 20;

    private final Screen parent;
    private final BasePawprintRuleHandler<?> handler;
    private final List<PawprintRuleVariable<String>> stringVariables = new ArrayList<>();
    private final List<EditBox> stringValueBoxes = new ArrayList<>();
    private int variableScrollOffset;

    public PawprintRuleSettingsScreen(Screen parent, BasePawprintRuleHandler<?> handler) {
        super(Component.literal(handler.getDisplayName()));
        this.parent = parent;
        this.handler = handler;
    }

    @Override
    protected void init() {
        super.init();
        this.stringVariables.clear();
        this.stringValueBoxes.clear();
        this.variableScrollOffset = 0;
        Player player = Minecraft.getInstance().player;
        for (PawprintRuleVariable<?> variable : this.handler.getVariables()) {
            if (variable.type() != String.class) {
                continue;
            }

            @SuppressWarnings("unchecked")
            PawprintRuleVariable<String> stringVariable = (PawprintRuleVariable<String>) variable;
            this.stringVariables.add(stringVariable);
        }

        if (player != null) {
            ItemStack stack = player.getMainHandItem();
            for (int index = 0; index < this.stringVariables.size(); index++) {
                PawprintRuleVariable<String> variable = this.stringVariables.get(index);
                String currentValue = PawprintRuleNbtHelper.getStringVariable(stack, this.handler.getRuleId(), variable);
                EditBox valueBox = new EditBox(this.font, 0, 0, VALUE_BOX_WIDTH, VALUE_BOX_HEIGHT, Component.literal(variable.displayName()));
                valueBox.setValue(currentValue);
                this.stringValueBoxes.add(valueBox);
                this.addRenderableWidget(valueBox);
            }
        }

        this.addRenderableWidget(Button.builder(Component.literal("保存"), button -> saveAndBack())
            .pos(this.width / 2 - 62, this.height - FOOTER_HEIGHT + 7)
                .size(58, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("取消"), button -> onClose())
            .pos(this.width / 2 + 4, this.height - FOOTER_HEIGHT + 7)
                .size(58, 20)
                .build());
        updateVariablePositions();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, this.handler.getDescription(), this.width / 2, 25, 0xC0C0C0);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int contentTop = HEADER_HEIGHT;
        int contentBottom = this.height - FOOTER_HEIGHT;
        guiGraphics.enableScissor(0, contentTop, this.width, contentBottom);
        for (int index = 0; index < this.stringVariables.size(); index++) {
            int rowY = contentTop + index * ROW_HEIGHT - this.variableScrollOffset;
            guiGraphics.drawString(this.font, this.stringVariables.get(index).displayName(), this.width / 2 - 112, rowY + 6, 0xFFFFFF, false);
        }
        guiGraphics.disableScissor();

        if (getMaxScrollOffset() > 0) {
            guiGraphics.drawString(this.font, "滚动查看变量", 6, contentBottom - 12, 0x808080, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        int contentTop = HEADER_HEIGHT;
        int contentBottom = this.height - FOOTER_HEIGHT;
        if (mouseY < contentTop || mouseY >= contentBottom || getMaxScrollOffset() == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollY);
        }

        this.variableScrollOffset = clamp(this.variableScrollOffset - (int) Math.signum(scrollY) * ROW_HEIGHT, 0, getMaxScrollOffset());
        updateVariablePositions();
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void saveAndBack() {
        for (int index = 0; index < this.stringVariables.size(); index++) {
            PawprintRuleVariable<String> variable = this.stringVariables.get(index);
            EditBox valueBox = this.stringValueBoxes.get(index);
            DgToyNetworking.sendToServer(new DgToyC2SPawprintRuleStringVarPacket(this.handler.getRuleId(), variable.key(), valueBox.getValue()));
        }
        onClose();
    }

    private void updateVariablePositions() {
        int contentTop = HEADER_HEIGHT;
        int contentBottom = this.height - FOOTER_HEIGHT;
        for (int index = 0; index < this.stringValueBoxes.size(); index++) {
            EditBox valueBox = this.stringValueBoxes.get(index);
            int rowY = contentTop + index * ROW_HEIGHT - this.variableScrollOffset;
            valueBox.setX(this.width / 2 + 12);
            valueBox.setY(rowY);
            valueBox.visible = rowY >= contentTop && rowY + VALUE_BOX_HEIGHT <= contentBottom;
        }
    }

    private int getMaxScrollOffset() {
        int contentHeight = this.height - HEADER_HEIGHT - FOOTER_HEIGHT;
        return Math.max(0, this.stringVariables.size() * ROW_HEIGHT - contentHeight);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}