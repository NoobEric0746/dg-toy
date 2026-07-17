package org.nooberic.dg_toy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.network.DgToyC2SPawprintSettingsSyncPacket;
import org.nooberic.dg_toy.network.DgToyC2SPawprintRuleTogglePacket;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dg_toy.pawprint.BasePawprintRuleHandler;
import org.nooberic.dg_toy.pawprint.PawprintRuleNbtHelper;
import org.nooberic.dg_toy.pawprint.PawprintRuleRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PawprintScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/gui/gui_pawprint.png");

    private static final int UI_WIDTH = 114;
    private static final int UI_HEIGHT = 132;
    private static final int ITEM_ICON_X = 4;
    private static final int ITEM_ICON_Y = 4;
    private static final int ITEM_ICON_SIZE = 32;
    private static final int ITEM_NAME_CENTER_X = 71;
    private static final int ITEM_NAME_CENTER_Y = 10;

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
    private static final int RULE_TOGGLE_OFF_U = 119;
    private static final int RULE_TOGGLE_OFF_HOVER_U = 130;
    private static final int RULE_TOGGLE_OFF_PRESSED_U = 141;
    private static final int RULE_TOGGLE_ON_U = 119;
    private static final int RULE_TOGGLE_ON_HOVER_U = 130;
    private static final int RULE_TOGGLE_ON_PRESSED_U = 141;
    private static final int RULE_TOGGLE_OFF_V = 89;
    private static final int RULE_TOGGLE_ON_V = 100;
    private static final int RULE_SCROLL_STEP = 4;

    private static final int RULE_X = 8;
    private static final int RULE_Y = 73;
    private static final int RULE_SPACING_Y = 20;
    private static final int RULE_WIDTH = 90;
    private static final int RULE_HEIGHT = 17;
    private static final int RULE_U = 119;
    private static final int RULE_V = 71;
    private static final int RULE_TOGGLE_OFFSET_X = 78;
    private static final int RULE_TOGGLE_OFFSET_Y = 3;
    private static final int RULE_TOGGLE_SIZE = 9;
    private static final int RULES_VIEWPORT_TOP = RULE_Y;
    private static final int RULES_VIEWPORT_BOTTOM = 127;

    private PawprintItem.PawprintMode pawprintMode = PawprintItem.PawprintMode.MOTION;
    private boolean hardMode;
    private int sliderCenterX = TRACK_MIN_CENTER_X;
    private boolean draggingSlider;
    private boolean leftMouseDown;
    private final List<RuleEntry> rules = new ArrayList<>();
    private int ruleScrollOffset;

    public PawprintScreen() {
        super(Component.translatable("screen.dg_toy.pawprint"));
    }

    @Override
    protected void init() {
        super.init();
        loadStateFromHeldPawprint();
        this.draggingSlider = false;
        this.leftMouseDown = false;
        refreshRules();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int left = leftPos();
        int top = topPos();

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, UI_WIDTH, UI_HEIGHT);
        renderPawprintIcon(guiGraphics, left, top);
        renderModeButton(guiGraphics, left, top, mouseX, mouseY);
        renderStrengthBar(guiGraphics, left, top);
        renderStrengthModeButtons(guiGraphics, left, top, mouseX, mouseY);
        renderSlider(guiGraphics, left, top, mouseX, mouseY);
        renderRuleBlocks(guiGraphics, left, top, mouseX, mouseY);
        RenderSystem.disableBlend();

        renderModeText(guiGraphics, left, top);
        renderPawprintName(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            this.leftMouseDown = true;
        }
        int left = leftPos();
        int top = topPos();
        int sliderX = left + this.sliderCenterX + SLIDER_CENTER_OFFSET_X - (SLIDER_WIDTH / 2);
        int sliderY = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2);

        if (button == 0 && isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            this.draggingSlider = true;
            updateSlider(mouseX, left);
            return true;
        }

        if (button == 0 && isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, MODE_WIDTH, MODE_HEIGHT)) {
            this.pawprintMode = this.pawprintMode.next();
            saveSettingsToHeldPawprint();
            refreshRules();
            return true;
        }

        if (button == 0 && isInside(mouseX, mouseY, left + MODE_BUTTON_X, top + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
            this.hardMode = !this.hardMode;
            saveSettingsToHeldPawprint();
            return true;
        }

        for (int index = 0; index < this.rules.size(); index++) {
            RuleEntry rule = this.rules.get(index);
            int ruleY = top + RULE_Y + index * RULE_SPACING_Y - this.ruleScrollOffset;
            if (!isRuleVisible(top, ruleY)) {
                continue;
            }

            int toggleX = left + RULE_X + RULE_TOGGLE_OFFSET_X;
            int toggleY = ruleY + RULE_TOGGLE_OFFSET_Y;
            if (isInside(mouseX, mouseY, toggleX, toggleY, RULE_TOGGLE_SIZE, RULE_TOGGLE_SIZE)) {
                if (button == 0) {
                    rule.enabled = !rule.enabled;
                    DgToyNetworking.sendToServer(new DgToyC2SPawprintRuleTogglePacket(rule.ruleId, rule.enabled));
                } else if (button == 1) {
                    openRuleSettings(rule);
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int left = leftPos();
        int top = topPos();
        if (!isInside(mouseX, mouseY, left + RULE_X, top + RULES_VIEWPORT_TOP, RULE_WIDTH, RULES_VIEWPORT_BOTTOM - RULES_VIEWPORT_TOP)) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int maxScroll = maxRuleScroll();
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int nextOffset = this.ruleScrollOffset - (int) Math.signum(delta) * RULE_SCROLL_STEP;
        this.ruleScrollOffset = clamp(nextOffset, 0, maxScroll);
        return true;
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
                saveSettingsToHeldPawprint();
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
        Component text = this.pawprintMode.displayName();
        int textWidth = this.font.width(text);
        boolean pressed = this.leftMouseDown && isInside(this.minecraft != null ? this.minecraft.mouseHandler.xpos() * this.minecraft.getWindow().getGuiScaledWidth() / this.minecraft.getWindow().getScreenWidth() : -1,
                this.minecraft != null ? this.minecraft.mouseHandler.ypos() * this.minecraft.getWindow().getGuiScaledHeight() / this.minecraft.getWindow().getScreenHeight() : -1,
                left + MODE_X, top + MODE_Y, MODE_WIDTH, MODE_HEIGHT);
        int pressedOffset = pressed ? 1 : 0;
        int textX = left + MODE_X + (MODE_WIDTH - textWidth) / 2 + pressedOffset;
        int textY = top + MODE_Y + 2 + pressedOffset;
        guiGraphics.drawString(this.font, text, textX, textY, 0xFFFFFF, false);
    }

    private void renderPawprintName(GuiGraphics guiGraphics, int left, int top) {
        ItemStack stack = getHeldPawprintStack();
        if (stack.isEmpty()) {
            return;
        }
        Component name = stack.getHoverName();
        int textX = left + ITEM_NAME_CENTER_X - this.font.width(name) / 2;
        int textY = top + ITEM_NAME_CENTER_Y - this.font.lineHeight / 2;
        guiGraphics.drawString(this.font, name, textX, textY, 0xFFFFFF, false);
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

    private void renderPawprintIcon(GuiGraphics guiGraphics, int left, int top) {
        ItemStack stack = getHeldPawprintStack();
        if (stack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(left + ITEM_ICON_X, top + ITEM_ICON_Y, 0);
        guiGraphics.pose().scale(2.0F, 2.0F, 1.0F);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.renderItemDecorations(this.font, stack, 0, 0);
        guiGraphics.pose().popPose();
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

    private void renderRuleBlocks(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        guiGraphics.enableScissor(left + RULE_X, top + RULES_VIEWPORT_TOP, left + RULE_X + RULE_WIDTH, top + RULES_VIEWPORT_BOTTOM);
        for (int index = 0; index < this.rules.size(); index++) {
            RuleEntry rule = this.rules.get(index);
            int ruleX = left + RULE_X;
            int ruleY = top + RULE_Y + index * RULE_SPACING_Y - this.ruleScrollOffset;
            if (!isRuleVisible(top, ruleY)) {
                continue;
            }

            guiGraphics.blit(TEXTURE, ruleX, ruleY, RULE_U, RULE_V, RULE_WIDTH, RULE_HEIGHT);

            int toggleX = ruleX + RULE_TOGGLE_OFFSET_X;
            int toggleY = ruleY + RULE_TOGGLE_OFFSET_Y;
            renderRuleToggle(guiGraphics, toggleX, toggleY, mouseX, mouseY, rule.enabled);
            guiGraphics.drawString(this.font, rule.name, ruleX + 5, ruleY + 4, 0xFFFFFF, false);
        }
        guiGraphics.disableScissor();
    }

    private void renderRuleToggle(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, boolean enabled) {
        boolean hover = isInside(mouseX, mouseY, x, y, RULE_TOGGLE_SIZE, RULE_TOGGLE_SIZE);
        boolean pressed = hover && this.leftMouseDown;
        int u;
        int v;
        if (enabled) {
            u = pressed ? RULE_TOGGLE_ON_PRESSED_U : hover ? RULE_TOGGLE_ON_HOVER_U : RULE_TOGGLE_ON_U;
            v = RULE_TOGGLE_ON_V;
        } else {
            u = pressed ? RULE_TOGGLE_OFF_PRESSED_U : hover ? RULE_TOGGLE_OFF_HOVER_U : RULE_TOGGLE_OFF_U;
            v = RULE_TOGGLE_OFF_V;
        }
        guiGraphics.blit(TEXTURE, x, y, u, v, RULE_TOGGLE_SIZE, RULE_TOGGLE_SIZE);
    }

    private boolean isRuleVisible(int top, int ruleY) {
        int viewportTop = top + RULES_VIEWPORT_TOP;
        int viewportBottom = top + RULES_VIEWPORT_BOTTOM;
        return ruleY + RULE_HEIGHT > viewportTop && ruleY < viewportBottom;
    }

    private int maxRuleScroll() {
        int contentHeight = this.rules.size() * RULE_SPACING_Y;
        int viewportHeight = RULES_VIEWPORT_BOTTOM - RULES_VIEWPORT_TOP;
        return Math.max(0, contentHeight - viewportHeight);
    }

    private void updateSlider(double mouseX, int left) {
        int localX = (int) Math.round(mouseX - left - SLIDER_CENTER_OFFSET_X);
        this.sliderCenterX = clamp(localX, TRACK_MIN_CENTER_X, TRACK_MAX_CENTER_X);
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top) {
        if (isInside(mouseX, mouseY, left + ITEM_ICON_X, top + ITEM_ICON_Y, ITEM_ICON_SIZE, ITEM_ICON_SIZE)) {
            ItemStack stack = getHeldPawprintStack();
            if (!stack.isEmpty()) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
            return;
        }

        int sliderX = left + this.sliderCenterX + SLIDER_CENTER_OFFSET_X - (SLIDER_WIDTH / 2);
        int sliderY = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2);
        if (isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(sliderXToStrength(this.sliderCenterX))), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + MODE_BUTTON_X, top + MODE_BUTTON_Y, MODE_BUTTON_SIZE, MODE_BUTTON_SIZE)) {
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
        }
    }

    private void loadStateFromHeldPawprint() {
        ItemStack stack = getHeldPawprintStack();
        if (stack.isEmpty()) {
            return;
        }
        this.hardMode = PawprintRuleNbtHelper.isHardMode(stack);
        this.sliderCenterX = strengthToSliderX(PawprintRuleNbtHelper.getStrength(stack));
        this.pawprintMode = PawprintItem.getMode(stack);
    }

    private void saveSettingsToHeldPawprint() {
        ItemStack stack = getHeldPawprintStack();
        if (stack.isEmpty()) {
            return;
        }
        int strength = sliderXToStrength(this.sliderCenterX);
        PawprintRuleNbtHelper.setStrength(stack, strength);
        PawprintRuleNbtHelper.setHardMode(stack, this.hardMode);
        PawprintItem.setMode(stack, this.pawprintMode);
        DgToyNetworking.sendToServer(new DgToyC2SPawprintSettingsSyncPacket(strength, this.hardMode, this.pawprintMode.id()));
    }

    private ItemStack getHeldPawprintStack() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof PawprintItem ? mainHand : ItemStack.EMPTY;
    }

    private static int sliderXToStrength(int sliderCenterX) {
        int range = TRACK_MAX_CENTER_X - TRACK_MIN_CENTER_X;
        if (range <= 0) {
            return 0;
        }
        return Math.round((sliderCenterX - TRACK_MIN_CENTER_X) * 100.0F / range);
    }

    private static int strengthToSliderX(int strength) {
        int clampedStrength = clamp(strength, 0, 100);
        int range = TRACK_MAX_CENTER_X - TRACK_MIN_CENTER_X;
        return TRACK_MIN_CENTER_X + Math.round(range * (clampedStrength / 100.0F));
    }

    private void refreshRules() {
        this.rules.clear();
        Player player = Minecraft.getInstance().player;
        ItemStack pawprintStack = player == null ? ItemStack.EMPTY : player.getMainHandItem();

        List<BasePawprintRuleHandler<?>> handlers = new ArrayList<>(PawprintRuleRegistry.getHandlersForMode(this.pawprintMode));
        handlers.sort(Comparator.comparing(BasePawprintRuleHandler::getDisplayName));
        for (BasePawprintRuleHandler<?> handler : handlers) {
            boolean enabled = !pawprintStack.isEmpty() && PawprintRuleNbtHelper.isRuleEnabled(pawprintStack, handler.getRuleId());
            this.rules.add(new RuleEntry(handler.getRuleId(), handler.getDisplayName(), enabled, handler));
        }
    }

    private void openRuleSettings(RuleEntry rule) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new PawprintRuleSettingsScreen(this, rule.handler));
        }
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

    private static class RuleEntry {
        private final String ruleId;
        private final String name;
        private final BasePawprintRuleHandler<?> handler;
        private boolean enabled;

        private RuleEntry(String ruleId, String name, boolean enabled, BasePawprintRuleHandler<?> handler) {
            this.ruleId = ruleId;
            this.name = name;
            this.enabled = enabled;
            this.handler = handler;
        }
    }
}