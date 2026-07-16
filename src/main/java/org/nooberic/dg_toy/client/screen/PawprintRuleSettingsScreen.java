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

public class PawprintRuleSettingsScreen extends Screen {
    private final Screen parent;
    private final BasePawprintRuleHandler<?> handler;
    private EditBox stringValueBox;

    public PawprintRuleSettingsScreen(Screen parent, BasePawprintRuleHandler<?> handler) {
        super(Component.literal(handler.getDisplayName()));
        this.parent = parent;
        this.handler = handler;
    }

    @Override
    protected void init() {
        super.init();
        Player player = Minecraft.getInstance().player;
        PawprintRuleVariable<?> stringVariable = firstStringVariable();
        if (player != null && stringVariable != null) {
            @SuppressWarnings("unchecked")
            PawprintRuleVariable<String> variable = (PawprintRuleVariable<String>) stringVariable;
            ItemStack stack = player.getMainHandItem();
            String currentValue = PawprintRuleNbtHelper.getStringVariable(stack, this.handler.getRuleId(), variable);
            this.stringValueBox = new EditBox(this.font, this.width / 2 - 60, this.height / 2 - 10, 120, 20, Component.literal(variable.displayName()));
            this.stringValueBox.setValue(currentValue);
            this.addRenderableWidget(this.stringValueBox);
        }

        this.addRenderableWidget(Button.builder(Component.literal("保存"), button -> saveAndBack())
                .pos(this.width / 2 - 60, this.height / 2 + 20)
                .size(58, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("返回"), button -> onClose())
                .pos(this.width / 2 + 2, this.height / 2 + 20)
                .size(58, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 36, 0xFFFFFF);
        PawprintRuleVariable<?> stringVariable = firstStringVariable();
        if (stringVariable != null) {
            guiGraphics.drawString(this.font, stringVariable.displayName(), this.width / 2 - 60, this.height / 2 - 24, 0xFFFFFF, false);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void saveAndBack() {
        PawprintRuleVariable<?> stringVariable = firstStringVariable();
        if (stringVariable != null && this.stringValueBox != null) {
            DgToyNetworking.sendToServer(new DgToyC2SPawprintRuleStringVarPacket(this.handler.getRuleId(), stringVariable.key(), this.stringValueBox.getValue()));
        }
        onClose();
    }

    private PawprintRuleVariable<?> firstStringVariable() {
        for (PawprintRuleVariable<?> variable : this.handler.getVariables()) {
            if (variable.type() == String.class) {
                return variable;
            }
        }
        return null;
    }
}