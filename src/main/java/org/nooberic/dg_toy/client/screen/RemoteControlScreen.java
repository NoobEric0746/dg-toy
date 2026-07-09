package org.nooberic.dg_toy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.client.PlayerConnectionStatusSnapshot;
import org.nooberic.dg_toy.client.PlayerStatusClientCache;
import org.nooberic.dg_toy.item.RemoteControlItem;
import org.nooberic.dg_toy.network.DgToyC2SRemoteSettingsSyncPacket;
import org.nooberic.dg_toy.network.DgToyC2SRemoteShockPacket;
import org.nooberic.dg_toy.network.DgToyC2SRemoteUnbindPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.YELLOW;

@OnlyIn(Dist.CLIENT)
public class RemoteControlScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/gui/remote_control_ui.png");

    private static final int UI_WIDTH = 115;
    private static final int UI_HEIGHT = 133;
    private static final int PANEL_U = 0;
    private static final int PANEL_V = 0;
    private static final int BUTTON_SIZE = 12;
    private static final int SAVE_WIDTH = 86;
    private static final int SAVE_HEIGHT = 13;
    private static final int SHOCK_WIDTH = 25;
    private static final int SHOCK_HEIGHT = 25;
    private static final int TIME_WIDTH = 8;
    private static final int TIME_HEIGHT = 4;
    private static final int SLIDER_WIDTH = 7;
    private static final int SLIDER_HEIGHT = 5;
    private static final int BAR_WIDTH = 55;
    private static final int BAR_HEIGHT = 3;
    private static final int SOFT_U = 116;
    private static final int HARD_U = 129;
    private static final int UNBIND_U = 142;
    private static final int SHOCK_NORMAL_U = 144;
    private static final int SHOCK_HOVER_U = 170;
    private static final int SHOCK_PRESSED_U = 196;
    private static final int SAVE_U = 116;
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
    private static final int BUTTON_PRESSED_V = 5;
    private static final int BUTTON_HOVER_V = 18;
    private static final int BUTTON_NORMAL_V = 31;
    private static final int SHOCK_V = 59;
    private static final int SAVE_NORMAL_V = 88;
    private static final int SAVE_HOVER_V = 102;
    private static final int SAVE_PRESSED_V = 116;
    private static final int UNBIND_X = 91;
    private static final int UNBIND_Y = 6;
    private static final int MODE_X = 91;
    private static final int MODE_Y = 42;
    private static final int BAR_X = 20;
    private static final int BAR_Y = 46;
    private static final int SHOCK_X = 19;
    private static final int SHOCK_Y = 64;
    private static final int TIME_PLUS_X = 85;
    private static final int TIME_PLUS_Y = 68;
    private static final int TIME_MINUS_X = 85;
    private static final int TIME_MINUS_Y = 82;
    private static final int SAVE_X = 13;
    private static final int SAVE_Y = 106;
    private static final int STATUS_TEXT_X = 12;
    private static final int STATUS_TEXT_Y = 7;
    private static final int PULSE_TEXT_X = 13;
    private static final int PULSE_TEXT_Y = 24;
    private static final int PULSE_TEXT_WIDTH = 88;
    private static final int PULSE_TEXT_HEIGHT = 8;
    private static final int DURATION_TEXT_X = 64;
    private static final int DURATION_TEXT_Y = 73;
    private static final int TRACK_MIN_CENTER_X = 18;
    private static final int TRACK_MAX_CENTER_X = 70;
    private static final int TRACK_CENTER_Y = 45;
    private static final int SLIDER_RENDER_OFFSET_X = 3;
    private static final int SLIDER_RENDER_OFFSET_Y = 2;

    private boolean hardMode = true;
    private int sliderCenterX = TRACK_MIN_CENTER_X;
    private int duration = 1;
    private List<String> pulseIds = List.of();
    private int selectedPulseIndex;
    private boolean draggingSlider;
    private boolean leftMouseDown;

    public RemoteControlScreen() {
        super(Component.translatable("screen.dg_toy.remote_control"));
    }

    @Override
    protected void init() {
        super.init();
        loadStateFromHeldRemote();
        this.draggingSlider = false;
        this.leftMouseDown = false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int left = leftPos();
        int top = topPos();

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, PANEL_U, PANEL_V, UI_WIDTH, UI_HEIGHT);
        renderStrengthBar(guiGraphics, left, top);
        renderModeButton(guiGraphics, left, top, mouseX, mouseY);
        renderSimpleButton(guiGraphics, left + UNBIND_X, top + UNBIND_Y, mouseX, mouseY, BUTTON_SIZE, BUTTON_SIZE, UNBIND_U, BUTTON_NORMAL_V);
        renderShockButton(guiGraphics, left + SHOCK_X, top + SHOCK_Y, mouseX, mouseY);
        renderTimeButton(guiGraphics, left + TIME_PLUS_X, top + TIME_PLUS_Y, mouseX, mouseY, TIME_PLUS_V);
        renderTimeButton(guiGraphics, left + TIME_MINUS_X, top + TIME_MINUS_Y, mouseX, mouseY, TIME_MINUS_V);
        renderSaveButton(guiGraphics, left + SAVE_X, top + SAVE_Y, mouseX, mouseY);
        renderSlider(guiGraphics, left, top, mouseX, mouseY);
        RenderSystem.disableBlend();
        renderConnectionText(guiGraphics, left, top);
        renderPulseText(guiGraphics, left, top);
        renderDurationText(guiGraphics, left, top);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    private void renderConnectionText(GuiGraphics guiGraphics, int left, int top) {
        Component statusText = buildConnectionText();
        guiGraphics.drawString(this.font, statusText, left + STATUS_TEXT_X, top + STATUS_TEXT_Y, 0xFFFFFF, false);
    }

    private void renderPulseText(GuiGraphics guiGraphics, int left, int top) {
        String pulseId = getSelectedPulseId();
        if (pulseId.isEmpty()) {
            return;
        }
        guiGraphics.drawString(this.font, pulseId, left + PULSE_TEXT_X, top + PULSE_TEXT_Y, 0xFFFFFF, false);
    }

    private void renderDurationText(GuiGraphics guiGraphics, int left, int top) {
        guiGraphics.drawString(this.font, formatDuration(this.duration), left + DURATION_TEXT_X, top + DURATION_TEXT_Y, 0xFFFFFF, false);
    }

    private void renderStrengthBar(GuiGraphics guiGraphics, int left, int top) {
        int litWidth = Math.max(0, Math.min(BAR_WIDTH, sliderCenterX - TRACK_MIN_CENTER_X + 1));
        int darkWidth = Math.max(0, BAR_WIDTH - litWidth);

        if (litWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X, top + BAR_Y, BAR_U, BAR_LIT_V, litWidth, BAR_HEIGHT);
        }
        if (darkWidth > 0) {
            guiGraphics.blit(TEXTURE, left + BAR_X + litWidth, top + BAR_Y, BAR_U + litWidth, BAR_DARK_V, darkWidth, BAR_HEIGHT);
        }
    }

    private void renderModeButton(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + MODE_X;
        int y = top + MODE_Y;
        int u = this.hardMode ? HARD_U : SOFT_U;
        int v = textureVForState(mouseX, mouseY, x, y, BUTTON_SIZE, BUTTON_SIZE, BUTTON_NORMAL_V, BUTTON_HOVER_V, BUTTON_PRESSED_V);
        guiGraphics.blit(TEXTURE, x, y, u, v, BUTTON_SIZE, BUTTON_SIZE);
    }

    private void renderSimpleButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, int width, int height, int u, int normalV) {
        int v = textureVForState(mouseX, mouseY, x, y, width, height, normalV, BUTTON_HOVER_V, BUTTON_PRESSED_V);
        guiGraphics.blit(TEXTURE, x, y, u, v, width, height);
    }

    private void renderShockButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int u = textureUForState(mouseX, mouseY, x, y, SHOCK_WIDTH, SHOCK_HEIGHT, SHOCK_NORMAL_U, SHOCK_HOVER_U, SHOCK_PRESSED_U);
        guiGraphics.blit(TEXTURE, x, y, u, SHOCK_V, SHOCK_WIDTH, SHOCK_HEIGHT);
    }

    private void renderTimeButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, int v) {
        int u = textureUForState(mouseX, mouseY, x, y, TIME_WIDTH, TIME_HEIGHT, TIME_NORMAL_U, TIME_HOVER_U, TIME_PRESSED_U);
        guiGraphics.blit(TEXTURE, x, y, u, v, TIME_WIDTH, TIME_HEIGHT);
    }

    private void renderSaveButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int v = textureVForState(mouseX, mouseY, x, y, SAVE_WIDTH, SAVE_HEIGHT, SAVE_NORMAL_V, SAVE_HOVER_V, SAVE_PRESSED_V);
        guiGraphics.blit(TEXTURE, x, y, SAVE_U, v, SAVE_WIDTH, SAVE_HEIGHT);
    }

    private void renderSlider(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY) {
        int x = left + this.sliderCenterX - (SLIDER_WIDTH / 2) + SLIDER_RENDER_OFFSET_X;
        int y = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2) + SLIDER_RENDER_OFFSET_Y;
        boolean hover = isInside(mouseX, mouseY, x, y, SLIDER_WIDTH, SLIDER_HEIGHT);
        int u = this.draggingSlider ? SLIDER_PRESSED_U : hover ? SLIDER_HOVER_U : SLIDER_NORMAL_U;
        guiGraphics.blit(TEXTURE, x, y, u, SLIDER_V, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        this.leftMouseDown = true;
        int left = leftPos();
        int top = topPos();
        int sliderX = left + this.sliderCenterX - (SLIDER_WIDTH / 2) + SLIDER_RENDER_OFFSET_X;
        int sliderY = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2) + SLIDER_RENDER_OFFSET_Y;

        if (isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            this.draggingSlider = true;
            updateSlider(mouseX, left);
            return true;
        }

        if (isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            this.hardMode = !this.hardMode;
            saveModeToHeldRemote();
            return true;
        }

        if (isInside(mouseX, mouseY, left + UNBIND_X, top + UNBIND_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            ItemStack stack = getHeldRemoteStack();
            if (!stack.isEmpty() && RemoteControlItem.isBound(stack)) {
                DgToyNetworking.sendToServer(new DgToyC2SRemoteUnbindPacket());
                if (this.minecraft != null) {
                    this.minecraft.setScreen(null);
                }
            }
            return true;
        }

        if (isInside(mouseX, mouseY, left + TIME_PLUS_X, top + TIME_PLUS_Y, TIME_WIDTH, TIME_HEIGHT)) {
            changeDuration(true);
            return true;
        }

        if (isInside(mouseX, mouseY, left + TIME_MINUS_X, top + TIME_MINUS_Y, TIME_WIDTH, TIME_HEIGHT)) {
            changeDuration(false);
            return true;
        }

        if (isInside(mouseX, mouseY, left + SHOCK_X, top + SHOCK_Y, SHOCK_WIDTH, SHOCK_HEIGHT)) {
            ItemStack stack = getHeldRemoteStack();
            if (!stack.isEmpty() && RemoteControlItem.isBound(stack)) {
                DgToyNetworking.sendToServer(new DgToyC2SRemoteShockPacket());
            }
            return true;
        }

        if (isRegularButton(mouseX, mouseY, left, top)) {
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
                saveStrengthToHeldRemote();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        int size = this.pulseIds.size();
        this.selectedPulseIndex = (this.selectedPulseIndex + direction + size) % size;
        savePulseIdToHeldRemote();
        return true;
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

    private boolean isRegularButton(double mouseX, double mouseY, int left, int top) {
        return isInside(mouseX, mouseY, left + UNBIND_X, top + UNBIND_Y, BUTTON_SIZE, BUTTON_SIZE)
                || isInside(mouseX, mouseY, left + SHOCK_X, top + SHOCK_Y, SHOCK_WIDTH, SHOCK_HEIGHT)
                || isInside(mouseX, mouseY, left + TIME_PLUS_X, top + TIME_PLUS_Y, TIME_WIDTH, TIME_HEIGHT)
                || isInside(mouseX, mouseY, left + TIME_MINUS_X, top + TIME_MINUS_Y, TIME_WIDTH, TIME_HEIGHT)
                || isInside(mouseX, mouseY, left + SAVE_X, top + SAVE_Y, SAVE_WIDTH, SAVE_HEIGHT);
    }

    private void updateSlider(double mouseX, int left) {
        int localX = (int) Math.round(mouseX - left - SLIDER_RENDER_OFFSET_X);
        this.sliderCenterX = clamp(localX, TRACK_MIN_CENTER_X, TRACK_MAX_CENTER_X);
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top) {
        int sliderX = left + this.sliderCenterX - (SLIDER_WIDTH / 2) + SLIDER_RENDER_OFFSET_X;
        int sliderY = top + TRACK_CENTER_Y - (SLIDER_HEIGHT / 2) + SLIDER_RENDER_OFFSET_Y;
        if (isInside(mouseX, mouseY, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.literal(String.valueOf(sliderXToStrength(this.sliderCenterX))), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + MODE_X, top + MODE_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            guiGraphics.renderTooltip(this.font, Component.translatable(this.hardMode ? "item.dg_toy.remote_control.mode.hard" : "item.dg_toy.remote_control.mode.soft"), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + UNBIND_X, top + UNBIND_Y, BUTTON_SIZE, BUTTON_SIZE)) {
            guiGraphics.renderTooltip(this.font, Component.literal("断开连接"), mouseX, mouseY);
            return;
        }

        if (isInside(mouseX, mouseY, left + PULSE_TEXT_X, top + PULSE_TEXT_Y, PULSE_TEXT_WIDTH, PULSE_TEXT_HEIGHT)) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("当前波形:"));
            tooltip.add(Component.literal(getSelectedPulseId()));
            tooltip.add(Component.literal("使用鼠标滚轮切换"));
            guiGraphics.renderTooltip(this.font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private Component buildConnectionText() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty() || !RemoteControlItem.isBound(stack)) {
            return Component.literal("遥控器未连接玩家");
        }

        String playerName = RemoteControlItem.getBoundPlayerName(stack);
        UUID playerId = RemoteControlItem.getBoundPlayerUuid(stack);
        if (playerId == null) {
            return Component.literal("遥控器未连接玩家");
        }

        boolean online = isPlayerOnline(playerId);
        String safeName = playerName == null || playerName.isBlank() ? playerId.toString() : playerName;
        return Component.literal("连接到")
                .append(coloredPlayerName(safeName))
                .append(Component.literal(online ? "在线" : "离线").withStyle(online ? GREEN : GRAY));
    }

    private boolean isPlayerOnline(UUID playerId) {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null) {
            return false;
        }

        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer != null && localPlayer.getUUID().equals(playerId)) {
            return true;
        }

        PlayerConnectionStatusSnapshot snapshot = PlayerStatusClientCache.get(playerId);
        return snapshot.connected() || minecraft.level != null && minecraft.level.getPlayerByUUID(playerId) != null;
    }

    private static MutableComponent coloredPlayerName(String playerName) {
        return Component.literal(" " + playerName + " ").withStyle(YELLOW);
    }

    private void loadStateFromHeldRemote() {
        ItemStack stack = getHeldRemoteStack();
        this.hardMode = RemoteControlItem.isHardMode(stack);
        this.sliderCenterX = strengthToSliderX(RemoteControlItem.getStrength(stack));
        this.duration = RemoteControlItem.getDuration(stack);
        this.pulseIds = RemoteControlItem.getAvailablePulseIds();
        String pulseId = RemoteControlItem.getPulseId(stack);
        this.selectedPulseIndex = this.pulseIds.indexOf(pulseId);
        if (this.selectedPulseIndex < 0) {
            this.selectedPulseIndex = 0;
            savePulseIdToHeldRemote();
        }
    }

    private void saveStrengthToHeldRemote() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty()) {
            return;
        }
        RemoteControlItem.setStrength(stack, sliderXToStrength(this.sliderCenterX));
        syncHeldRemoteSettings();
    }

    private void saveModeToHeldRemote() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty()) {
            return;
        }
        RemoteControlItem.setHardMode(stack, this.hardMode);
        syncHeldRemoteSettings();
    }

    private void savePulseIdToHeldRemote() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty() || this.pulseIds.isEmpty()) {
            return;
        }
        RemoteControlItem.setPulseId(stack, getSelectedPulseId());
        syncHeldRemoteSettings();
    }

    private void saveDurationToHeldRemote() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty()) {
            return;
        }
        RemoteControlItem.setDuration(stack, this.duration);
        syncHeldRemoteSettings();
    }

    private void syncHeldRemoteSettings() {
        ItemStack stack = getHeldRemoteStack();
        if (stack.isEmpty()) {
            return;
        }
        DgToyNetworking.sendToServer(new DgToyC2SRemoteSettingsSyncPacket(
                RemoteControlItem.getStrength(stack),
                RemoteControlItem.isHardMode(stack),
                RemoteControlItem.getPulseId(stack),
                RemoteControlItem.getDuration(stack)
        ));
    }

    private void changeDuration(boolean increase) {
        if (increase) {
            this.duration = Math.min(10, Math.max(1, this.duration) + 1);
        } else {
            this.duration = Math.max(1, this.duration - 1);
        }
        saveDurationToHeldRemote();
    }

    private String getSelectedPulseId() {
        if (this.pulseIds.isEmpty()) {
            return "";
        }
        int index = clamp(this.selectedPulseIndex, 0, this.pulseIds.size() - 1);
        return this.pulseIds.get(index);
    }

    private static String formatDuration(int duration) {
        return Integer.toString(duration);
    }

    private ItemStack getHeldRemoteStack() {
        Minecraft minecraft = this.minecraft;
        if (minecraft == null) {
            return ItemStack.EMPTY;
        }

        Player player = minecraft.player;
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RemoteControlItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof RemoteControlItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
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

    private int textureVForState(int mouseX, int mouseY, int x, int y, int width, int height, int normalV, int hoverV, int pressedV) {
        if (this.leftMouseDown && isInside(mouseX, mouseY, x, y, width, height)) {
            return pressedV;
        }
        if (isInside(mouseX, mouseY, x, y, width, height)) {
            return hoverV;
        }
        return normalV;
    }

    private int textureUForState(int mouseX, int mouseY, int x, int y, int width, int height, int normalU, int hoverU, int pressedU) {
        if (this.leftMouseDown && isInside(mouseX, mouseY, x, y, width, height)) {
            return pressedU;
        }
        if (isInside(mouseX, mouseY, x, y, width, height)) {
            return hoverU;
        }
        return normalU;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
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