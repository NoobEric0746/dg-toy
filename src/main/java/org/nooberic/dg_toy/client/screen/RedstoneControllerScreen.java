package org.nooberic.dg_toy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.entity.RedstoneControllerBlockEntity;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.network.DgToyC2SRedstoneControllerSettingsSyncPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class RedstoneControllerScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/gui/gui_redstone_controller.png");
    private static final int UI_WIDTH = 115;
    private static final int UI_HEIGHT = 133;
    private static final int BUTTON_SIZE = 12;
    private static final int CHANNEL_WIDTH = 4;
    private static final int CHANNEL_HEIGHT = 17;
    private static final int BAR_WIDTH = 55;
    private static final int BAR_HEIGHT = 3;
    private static final int SLIDER_WIDTH = 7;
    private static final int SLIDER_HEIGHT = 5;
    private static final int TIME_WIDTH = 8;
    private static final int TIME_HEIGHT = 4;

    private static final int SOFT_U = 117;
    private static final int HARD_U = 130;
    private static final int BUTTON_PRESSED_V = 5;
    private static final int BUTTON_HOVER_V = 18;
    private static final int BUTTON_NORMAL_V = 31;
    private static final int BAR_U = 116;
    private static final int BAR_LIT_V = 45;
    private static final int BAR_DARK_V = 49;
    private static final int SLIDER_NORMAL_U = 116;
    private static final int SLIDER_HOVER_U = 124;
    private static final int SLIDER_PRESSED_U = 132;
    private static final int SLIDER_V = 53;
    private static final int TIME_NORMAL_U = 116;
    private static final int TIME_HOVER_U = 125;
    private static final int TIME_PRESSED_U = 134;
    private static final int TIME_PLUS_V = 59;
    private static final int TIME_MINUS_V = 64;
    private static final int CHANNEL_A_U = 157;
    private static final int CHANNEL_AB_U = 162;
    private static final int CHANNEL_B_U = 167;
    private static final int CHANNEL_NORMAL_V = 6;
    private static final int CHANNEL_HOVER_V = 26;

    private static final int BAR_X = 20;
    private static final int BAR_Y = 61;
    private static final int MODE_X = 91;
    private static final int MODE_Y = 57;
    private static final int CHANNEL_X = 12;
    private static final int CHANNEL_Y = 81;
    private static final int TIME_PLUS_X = 59;
    private static final int TIME_PLUS_Y = 82;
    private static final int TIME_MINUS_X = 59;
    private static final int TIME_MINUS_Y = 96;
    private static final int TITLE_CENTER_X = 56;
    private static final int TITLE_Y = 9;
    private static final int FREQUENCY_LABEL_X = 11;
    private static final int FREQUENCY_LABEL_Y = 22;
    private static final int FREQUENCY_INPUT_X = 60;
    private static final int FREQUENCY_INPUT_Y = 21;
    private static final int FREQUENCY_INPUT_WIDTH = 40;
    private static final int FREQUENCY_INPUT_HEIGHT = 9;
    private static final int PULSE_TEXT_X = 13;
    private static final int PULSE_TEXT_Y = 41;
    private static final int PULSE_TEXT_WIDTH = 89;
    private static final int PULSE_TEXT_HEIGHT = 9;
    private static final int DURATION_TEXT_X = 39;
    private static final int DURATION_TEXT_Y = 87;
    private static final int DURATION_TEXT_WIDTH = 13;
    private static final int DURATION_TEXT_HEIGHT = 11;
    private static final int TRACK_MIN_X = 18;
    private static final int TRACK_MAX_X = 70;
    private static final int TRACK_Y = 60;

    private boolean hardMode;
    private int selectedChannel = 1;
    private int sliderX = TRACK_MIN_X;
    private int duration = 1;
    private int frequency;
    private final BlockPos controllerPos;
    private List<String> pulseIds = List.of();
    private int selectedPulseIndex;
    private boolean draggingSlider;
    private boolean leftMouseDown;
    private EditBox frequencyInput;

    public RedstoneControllerScreen(RedstoneControllerBlockEntity controller) {
        super(Component.translatable("screen.dg_toy.redstone_controller"));
        this.controllerPos = controller.getBlockPos();
        this.hardMode = controller.isHardMode();
        this.selectedChannel = toUiChannel(controller.getChannel());
        this.sliderX = strengthToSliderX(controller.getStrength());
        this.duration = controller.getDuration();
        this.frequency = controller.getFrequency();
        this.pulseIds = RemoteControlItem.getAvailablePulseIds();
        this.selectedPulseIndex = this.pulseIds.indexOf(controller.getPulseId());
        if (this.selectedPulseIndex < 0) {
            this.selectedPulseIndex = 0;
        }
    }

    @Override
    protected void init() {
        this.draggingSlider = false;
        this.leftMouseDown = false;
        this.frequencyInput = new EditBox(this.font, leftPos() + FREQUENCY_INPUT_X, topPos() + FREQUENCY_INPUT_Y,
            FREQUENCY_INPUT_WIDTH, FREQUENCY_INPUT_HEIGHT, Component.literal("频率"));
        this.frequencyInput.setBordered(false);
        this.frequencyInput.setTextColor(0xFFFFFF);
        this.frequencyInput.setMaxLength(16);
        this.frequencyInput.setValue(Integer.toString(this.frequency));
        this.addRenderableWidget(this.frequencyInput);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int left = leftPos();
        int top = topPos();

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, UI_WIDTH, UI_HEIGHT);
        renderStrengthBar(guiGraphics, left, top);
        renderModeButton(guiGraphics, left, top, mouseX, mouseY);
        renderChannelButton(guiGraphics, left, top, mouseX, mouseY);
        renderTimeButton(guiGraphics, left + TIME_PLUS_X, top + TIME_PLUS_Y, mouseX, mouseY, TIME_PLUS_V);
        renderTimeButton(guiGraphics, left + TIME_MINUS_X, top + TIME_MINUS_Y, mouseX, mouseY, TIME_MINUS_V);
        renderSlider(guiGraphics, left, top, mouseX, mouseY);
        RenderSystem.disableBlend();
        renderLabels(guiGraphics, left, top);
        renderPulseText(guiGraphics, left, top);
        renderDurationText(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    private void renderStrengthBar(GuiGraphics guiGraphics, int left, int top) {
        int litWidth = clamp(this.sliderX - TRACK_MIN_X + 1, 0, BAR_WIDTH);
        if (litWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X, top + BAR_Y, BAR_U, BAR_LIT_V, litWidth, BAR_HEIGHT);
        }
        int darkWidth = BAR_WIDTH - litWidth;
        if (darkWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X + litWidth, top + BAR_Y, BAR_U + litWidth, BAR_DARK_V, darkWidth, BAR_HEIGHT);
        }
    }

    private void renderPulseText(GuiGraphics guiGraphics, int left, int top) {
        if (!this.pulseIds.isEmpty()) {
            guiGraphics.drawString(this.font, this.pulseIds.get(this.selectedPulseIndex), left + PULSE_TEXT_X, top + PULSE_TEXT_Y, 0xFFFFFF, false);
        }
    }

    private void renderLabels(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.drawCenteredString(this.font, "红石遥控器", left + TITLE_CENTER_X, top + TITLE_Y, 0xFFFFFF);
        guiGraphics.drawString(this.font, "遥控器频率", left + FREQUENCY_LABEL_X, top + FREQUENCY_LABEL_Y, 0xFFFFFF, false);
    }

    private void renderDurationText(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.drawString(this.font, Integer.toString(this.duration), left + DURATION_TEXT_X, top + DURATION_TEXT_Y, 0xFFFFFF, false);
    }

    private void renderModeButton(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + MODE_X;
        int y = top + MODE_Y;
        guiGraphics.blit(TEXTURE, x, y, this.hardMode ? HARD_U : SOFT_U,
                textureVForState(mouseX, mouseY, x, y, BUTTON_SIZE, BUTTON_SIZE, BUTTON_NORMAL_V, BUTTON_HOVER_V, BUTTON_PRESSED_V), BUTTON_SIZE, BUTTON_SIZE);
    }

    private void renderChannelButton(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + CHANNEL_X;
        int y = top + CHANNEL_Y;
        int u = switch (this.selectedChannel) {
            case 1 -> CHANNEL_AB_U;
            case 2 -> CHANNEL_B_U;
            default -> CHANNEL_A_U;
        };
        int v = isInside(mouseX, mouseY, x, y, CHANNEL_WIDTH, CHANNEL_HEIGHT) ? CHANNEL_HOVER_V : CHANNEL_NORMAL_V;
        guiGraphics.blit(TEXTURE, x, y, u, v, CHANNEL_WIDTH, CHANNEL_HEIGHT);
    }

    private void renderTimeButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, int v) {
        guiGraphics.blit(TEXTURE, x, y,
                textureUForState(mouseX, mouseY, x, y, TIME_WIDTH, TIME_HEIGHT, TIME_NORMAL_U, TIME_HOVER_U, TIME_PRESSED_U), v, TIME_WIDTH, TIME_HEIGHT);
    }

    private void renderSlider(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + this.sliderX;
        int y = top + TRACK_Y;
        int u = this.draggingSlider ? SLIDER_PRESSED_U : isInside(mouseX, mouseY, x, y, SLIDER_WIDTH, SLIDER_HEIGHT) ? SLIDER_HOVER_U : SLIDER_NORMAL_U;
        guiGraphics.blit(TEXTURE, x, y, u, SLIDER_V, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!isInside(mouseX, mouseY, leftPos() + FREQUENCY_INPUT_X, topPos() + FREQUENCY_INPUT_Y, FREQUENCY_INPUT_WIDTH, FREQUENCY_INPUT_HEIGHT)) {
            commitFrequency();
        }
        this.leftMouseDown = true;
        int left = leftPos();
        int top = topPos();
        if (isInside(mouseX, mouseY, left + this.sliderX, top + TRACK_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.draggingSlider = true;
            updateSlider(mouseX, left);
            return true;
        }
        if (isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            DgToyScreenSounds.playButtonClick();
            this.hardMode = !this.hardMode;
            saveSettings();
            return true;
        }
        if (isInside(mouseX, mouseY, left + CHANNEL_X, top + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.selectedChannel = this.selectedChannel == 3 ? 1 : this.selectedChannel + 1;
            saveSettings();
            return true;
        }
        if (isInside(mouseX, mouseY, left + TIME_PLUS_X, top + TIME_PLUS_Y, TIME_WIDTH, TIME_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.duration = Math.min(10, this.duration + 1);
            saveSettings();
            return true;
        }
        if (isInside(mouseX, mouseY, left + TIME_MINUS_X, top + TIME_MINUS_Y, TIME_WIDTH, TIME_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.duration = Math.max(1, this.duration - 1);
            saveSettings();
            return true;
        }
        if (isInside(mouseX, mouseY, left + PULSE_TEXT_X, top + PULSE_TEXT_Y, PULSE_TEXT_WIDTH, PULSE_TEXT_HEIGHT)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            commitFrequency();
            this.setFocused(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingSlider) {
            updateSlider(mouseX, leftPos());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.leftMouseDown = false;
            if (this.draggingSlider) {
                this.draggingSlider = false;
                saveSettings();
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        commitFrequency();
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int left = leftPos();
        int top = topPos();
        if (!isInside(mouseX, mouseY, left + PULSE_TEXT_X, top + PULSE_TEXT_Y, PULSE_TEXT_WIDTH, PULSE_TEXT_HEIGHT)) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        if (this.pulseIds.isEmpty() || delta == 0) {
            return true;
        }
        int direction = delta > 0 ? -1 : 1;
        this.selectedPulseIndex = (this.selectedPulseIndex + direction + this.pulseIds.size()) % this.pulseIds.size();
        saveSettings();
        return true;
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top) {
        int sliderX = left + this.sliderX;
        int sliderY = top + TRACK_Y;
        if (isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(sliderXToStrength(this.sliderX))), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            List<Component> tooltip = new ArrayList<>();
            if (this.hardMode) {
                tooltip.add(Component.literal("数值强度模式"));
                tooltip.add(Component.literal("产生的强度等于设置的数值"));
            } else {
                tooltip.add(Component.literal("动态强度模式"));
                tooltip.add(Component.literal("0-33按比例对应感受阈值以下"));
                tooltip.add(Component.literal("34-66对应感受阈值到疼痛阈值,适合用于奖励"));
                tooltip.add(Component.literal("67-100对应疼痛阈值到强度上限,适合用于惩罚"));
                tooltip.add(Component.literal("需要对方提前设置相应数值"));
            }
            guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + CHANNEL_X, top + CHANNEL_Y, CHANNEL_WIDTH, CHANNEL_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.literal("通道: " + channelLabel(this.selectedChannel)), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + PULSE_TEXT_X, top + PULSE_TEXT_Y, PULSE_TEXT_WIDTH, PULSE_TEXT_HEIGHT)) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("当前波形:"));
            tooltip.add(Component.literal(selectedPulseId()));
            tooltip.add(Component.literal("使用鼠标滚轮切换"));
            guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + TIME_PLUS_X, top + TIME_PLUS_Y, TIME_WIDTH, TIME_HEIGHT)
                || isInside(mouseX, mouseY, left + TIME_MINUS_X, top + TIME_MINUS_Y, TIME_WIDTH, TIME_HEIGHT)
                || isInside(mouseX, mouseY, left + DURATION_TEXT_X, top + DURATION_TEXT_Y, DURATION_TEXT_WIDTH, DURATION_TEXT_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.literal("时长: " + this.duration + "秒"), mouseX, mouseY);
        }
    }

    private void updateSlider(double mouseX, int left) {
        this.sliderX = clamp((int) Math.round(mouseX - left) - SLIDER_WIDTH / 2, TRACK_MIN_X, TRACK_MAX_X);
    }

    private void saveSettings() {
        DgToyNetworking.sendToServer(new DgToyC2SRedstoneControllerSettingsSyncPacket(
                this.controllerPos,
                sliderXToStrength(this.sliderX),
                this.hardMode,
                selectedPulseId(),
                this.duration,
                toSavedChannel(this.selectedChannel),
                this.frequency));
    }

    private void commitFrequency() {
        if (this.frequencyInput == null) {
            return;
        }
        int parsedFrequency;
        try {
            parsedFrequency = Integer.parseInt(this.frequencyInput.getValue());
        } catch (NumberFormatException exception) {
            parsedFrequency = 0;
        }
        this.frequency = clamp(parsedFrequency, 0, 65535);
        this.frequencyInput.setValue(Integer.toString(this.frequency));
        saveSettings();
    }

    private String selectedPulseId() {
        return this.pulseIds.isEmpty() ? "" : this.pulseIds.get(this.selectedPulseIndex);
    }

    private static String channelLabel(int channel) {
        return switch (channel) {
            case 2 -> "B";
            case 3 -> "A";
            default -> "A+B";
        };
    }

    private static int toUiChannel(int savedChannel) {
        return switch (savedChannel) {
            case 1 -> 3;
            case 3 -> 1;
            default -> 2;
        };
    }

    private static int toSavedChannel(int uiChannel) {
        return toUiChannel(uiChannel);
    }

    private static int sliderXToStrength(int sliderX) {
        return Math.round((sliderX - TRACK_MIN_X) * 100.0F / (TRACK_MAX_X - TRACK_MIN_X));
    }

    private static int strengthToSliderX(int strength) {
        return TRACK_MIN_X + Math.round((TRACK_MAX_X - TRACK_MIN_X) * (clamp(strength, 0, 100) / 100.0F));
    }

    private int textureVForState(int mouseX, int mouseY, int x, int y, int width, int height, int normalV, int hoverV, int pressedV) {
        if (this.leftMouseDown && isInside(mouseX, mouseY, x, y, width, height)) {
            return pressedV;
        }
        return isInside(mouseX, mouseY, x, y, width, height) ? hoverV : normalV;
    }

    private int textureUForState(int mouseX, int mouseY, int x, int y, int width, int height, int normalU, int hoverU, int pressedU) {
        if (this.leftMouseDown && isInside(mouseX, mouseY, x, y, width, height)) {
            return pressedU;
        }
        return isInside(mouseX, mouseY, x, y, width, height) ? hoverU : normalU;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private int leftPos() {
        return (this.width - UI_WIDTH) / 2;
    }

    private int topPos() {
        return (this.height - UI_HEIGHT) / 2;
    }
}