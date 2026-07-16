package org.nooberic.dg_toy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;

@OnlyIn(Dist.CLIENT)
public class PawprintScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/gui/gui_pawprint.png");

    private static final int UI_WIDTH = 114;
    private static final int UI_HEIGHT = 132;

    private static final int MODE_X = 41;
    private static final int MODE_Y = 25;
    private static final int MODE_WIDTH = 67;
    private static final int MODE_HEIGHT = 13;
    private static final int MODE_U = 118;
    private static final int MODE_NORMAL_V = 3;
    private static final int MODE_HOVER_V = 18;
    private static final int MODE_PRESSED_V = 33;

    private static final int BAR_X = 21;
    private static final int BAR_Y = 53;
    private static final int BAR_WIDTH = 55;
    private static final int BAR_HEIGHT = 3;
    private static final int BAR_U = 119;
    private static final int BAR_LIT_V = 51;
    private static final int BAR_DARK_V = 55;

    private static final int SLIDER_WIDTH = 7;
    private static final int SLIDER_HEIGHT = 5;
    private static final int SLIDER_NORMAL_U = 119;
    private static final int SLIDER_HOVER_U = 127;
    private static final int SLIDER_PRESSED_U = 135;
    private static final int SLIDER_V = 59;
    private static final int TRACK_MIN_CENTER_X = 19;
    private static final int TRACK_MAX_CENTER_X = 72;
    private static final int TRACK_CENTER_Y = 54;
    private static final int SLIDER_CENTER_OFFSET_X = 2;
    private static final int MODE_BUTTON_X = 92;
    private static final int MODE_BUTTON_Y = 49;
    private static final int MODE_BUTTON_SIZE = 12;

    private static final int SOFT_NORMAL_U = 187;
    private static final int HARD_NORMAL_U = 200;
    private static final int MODE_BUTTON_NORMAL_V = 28;
    private static final int MODE_BUTTON_HOVER_V = 15;
    private static final int MODE_BUTTON_PRESSED_V = 2;

    private static final String[] PAWPRINT_MODES = {"默认", "模式1", "模式2"};

    private int pawprintModeIndex;
    private boolean hardMode;
    private int sliderCenterX = TRACK_MIN_CENTER_X;
    private boolean draggingSlider;
    private boolean leftMouseDown;

    public PawprintScreen() {
        super(Component.translatable("screen.dg_toy.pawprint"));
    }

    @Override
    protected void init() {
        super.init();
        this.draggingSlider = false;
        this.leftMouseDown = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int left = leftPos();
        int top = topPos();

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, UI_WIDTH, UI_HEIGHT);
        renderModeButton(guiGraphics, left, top, mouseX, mouseY);
        renderStrengthBar(guiGraphics, left, top);
        renderStrengthModeButtons(guiGraphics, left, top, mouseX, mouseY);
        renderSlider(guiGraphics, left, top, mouseX, mouseY);
        RenderSystem.disableBlend();

        renderModeText(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        this.leftMouseDown = true;
        int left = leftPos();
        int top = topPos();
        int sliderX = left + this.sliderCenterX + SLIDER_CENTER_OFFSET_X - (SLIDER_WIDTH / 2);
        int sliderY = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2);

        if (isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            this.draggingSlider = true;
            updateSlider(mouseX, left);
            return true;
        }

        if (isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, MODE_WIDTH, MODE_HEIGHT)) {
            this.pawprintModeIndex = (this.pawprintModeIndex + 1) % PAWPRINT_MODES.length;
            return true;
        }

        if (isInside(mouseX, mouseY, left + MODE_BUTTON_X, top + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            this.hardMode = !this.hardMode;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
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
                updateSlider(mouseX, leftPos());
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 || this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderModeButton(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + MODE_X;
        int y = top + MODE_Y;
        int v = textureVForState(mouseX, mouseY, x, y, MODE_WIDTH, MODE_HEIGHT, MODE_NORMAL_V, MODE_HOVER_V, MODE_PRESSED_V);
        guiGraphics.blit(TEXTURE, x, y, MODE_U, v, MODE_WIDTH, MODE_HEIGHT);
    }

    private void renderModeText(GuiGraphics guiGraphics, int left, int top) {
        Component text = Component.literal(PAWPRINT_MODES[this.pawprintModeIndex]);
        int textWidth = this.font.width(text);
        boolean pressed = this.leftMouseDown && isInside(this.minecraft != null ? this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getScreenWidth() : -1,
                this.minecraft != null ? this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getScreenHeight() : -1,
                left + MODE_X, top + MODE_Y, MODE_WIDTH, MODE_HEIGHT);
        int pressedOffset = pressed ? 1 : 0;
        int textX = left + MODE_X + (MODE_WIDTH - textWidth) / 2 + pressedOffset;
        int textY = top + MODE_Y + 2 + pressedOffset;
        guiGraphics.drawString(this.font, text, textX, textY, 0xFFFFFF, false);
    }

    private void renderStrengthBar(GuiGraphics guiGraphics, int left, int top) {
        int litWidth = Math.max(0, Math.min(BAR_WIDTH, this.sliderCenterX - TRACK_MIN_CENTER_X + 1));
        int darkWidth = Math.max(0, BAR_WIDTH - litWidth);

        if (litWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X, top + BAR_Y, BAR_U, BAR_LIT_V, litWidth, BAR_HEIGHT);
        }
        if (darkWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X + litWidth, top + BAR_Y, BAR_U + litWidth, BAR_DARK_V, darkWidth, BAR_HEIGHT);
        }
    }

    private void renderSlider(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + this.sliderCenterX + SLIDER_CENTER_OFFSET_X - (SLIDER_WIDTH / 2);
        int y = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2);
        boolean hover = isInside(mouseX, mouseY, x, y, SLIDER_WIDTH, SLIDER_HEIGHT);
        int u = this.draggingSlider ? SLIDER_PRESSED_U : hover ? SLIDER_HOVER_U : SLIDER_NORMAL_U;
        guiGraphics.blit(TEXTURE, x, y, u, SLIDER_V, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    private void renderStrengthModeButtons(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        renderStrengthModeButton(guiGraphics, left + MODE_BUTTON_X, top + MODE_BUTTON_Y, mouseX, mouseY);
    }

    private void renderStrengthModeButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        boolean hover = isInside(mouseX, mouseY, x, y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE);
        int u = this.hardMode ? HARD_NORMAL_U : SOFT_NORMAL_U;
        int v = textureVForState(mouseX, mouseY, x, y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE,
                MODE_BUTTON_NORMAL_V, MODE_BUTTON_HOVER_V, MODE_BUTTON_PRESSED_V);
        guiGraphics.blit(TEXTURE, x, y, u, v, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE);
    }

    private void updateSlider(double mouseX, int left) {
        int localX = (int) Math.round(mouseX - left - SLIDER_CENTER_OFFSET_X);
        this.sliderCenterX = clamp(localX, TRACK_MIN_CENTER_X, TRACK_MAX_CENTER_X);
    }

    private int leftPos() {
        return (this.width - UI_WIDTH) / 2;
    }

    private int topPos() {
        return (this.height - UI_HEIGHT) / 2;
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int textureVForState(int mouseX, int mouseY, int x, int y, int width, int height, int normalV, int hoverV, int pressedV) {
        if (isInside(mouseX, mouseY, x, y, width, height)) {
            return this.leftMouseDown ? pressedV : hoverV;
        }
        return normalV;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}