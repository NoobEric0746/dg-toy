package org.nooberic.dg_toy.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.entity.DgBeaconBlockEntity;
import org.nooberic.dg_toy.item.DgBeaconItem;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.network.DgToyC2SBeaconDistanceLimitsSyncPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;

@OnlyIn(Dist.CLIENT)
public class BeaconScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Dg_toy.MODID, "textures/gui/gui_beacon.png");

    private static final int UI_WIDTH = 115;
    private static final int UI_HEIGHT = 133;
    private static final int ITEM_ICON_X = 4;
    private static final int ITEM_ICON_Y = 4;
    private static final int ITEM_NAME_CENTER_X = 71;
    private static final int ITEM_NAME_CENTER_Y = 10;
    private static final int TRACK_X = 20;
    private static final int TRACK_Y = 53;
    private static final int TRACK_WIDTH = 74;
    private static final int TRACK_HEIGHT = 3;
    private static final int TRACK_U = 119;
    private static final int TRACK_LIT_V = 51;
    private static final int TRACK_DARK_V = 55;
    private static final int SLIDER_WIDTH = 7;
    private static final int SLIDER_HEIGHT = 5;
    private static final int SLIDER_NORMAL_U = 119;
    private static final int SLIDER_HOVER_U = 127;
    private static final int SLIDER_PRESSED_U = 135;
    private static final int SLIDER_V = 59;
    private static final int SLIDER_MIN_X = 18;
    private static final int SLIDER_MAX_X = 89;
    private static final int SLIDER_Y = 52;
    private static final int SLIDER_STEPS = 19;

    private int minimumDistanceLimitStep = 6;
    private int maximumDistanceLimitStep = 13;
    private DraggedSlider draggedSlider;
    private double dragGrabOffsetX;
    private final BlockPos blockPos;

    public BeaconScreen() {
        this((BlockPos) null);
    }

    public BeaconScreen(DgBeaconBlockEntity beacon) {
        this(beacon.getBlockPos());
    }

    private BeaconScreen(BlockPos blockPos) {
        super(Component.translatable("item.dg_toy.dg_beacon"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        super.init();
        ItemStack stack = getBeaconStack();
        if (!stack.isEmpty()) {
            this.minimumDistanceLimitStep = distanceLimitToStep(DgBeaconItem.getMinimumDistanceLimit(stack));
            this.maximumDistanceLimitStep = distanceLimitToStep(DgBeaconItem.getMaximumDistanceLimit(stack));
            if (this.minimumDistanceLimitStep >= this.maximumDistanceLimitStep) {
                this.minimumDistanceLimitStep = Math.max(0, this.maximumDistanceLimitStep - 1);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        int left = leftPos();
        int top = topPos();

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, left, top, 0, 0, UI_WIDTH, UI_HEIGHT);
        renderBeaconIcon(guiGraphics, left, top);
        renderTrack(guiGraphics, left, top);
        renderSlider(guiGraphics, left, top, mouseX, mouseY, DraggedSlider.LEFT);
        renderSlider(guiGraphics, left, top, mouseX, mouseY, DraggedSlider.RIGHT);
        RenderSystem.disableBlend();

        renderBeaconName(guiGraphics, left, top);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int left = leftPos();
        int top = topPos();
        if (isInside(mouseX, mouseY, sliderX(minimumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            this.draggedSlider = DraggedSlider.LEFT;
            this.dragGrabOffsetX = mouseX - sliderX(minimumDistanceLimitStep, left);
            updateSlider(mouseX, left);
            return true;
        }
        if (isInside(mouseX, mouseY, sliderX(maximumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            this.draggedSlider = DraggedSlider.RIGHT;
            this.dragGrabOffsetX = mouseX - sliderX(maximumDistanceLimitStep, left);
            updateSlider(mouseX, left);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggedSlider != null) {
            updateSlider(mouseX, leftPos());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggedSlider != null) {
            updateSlider(mouseX, leftPos());
            this.draggedSlider = null;
            this.dragGrabOffsetX = 0;
            return true;
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

    private void renderBeaconIcon(GuiGraphics guiGraphics, int left, int top) {
        ItemStack stack = getBeaconStack();
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

    private void renderBeaconName(GuiGraphics guiGraphics, int left, int top) {
        ItemStack stack = getBeaconStack();
        if (stack.isEmpty()) {
            return;
        }

        Component name = stack.getHoverName();
        int textX = left + ITEM_NAME_CENTER_X - this.font.width(name) / 2;
        int textY = top + ITEM_NAME_CENTER_Y - this.font.lineHeight / 2;
        guiGraphics.drawString(this.font, name, textX, textY, 0xFFFFFF, false);
    }

    private void renderTrack(GuiGraphics guiGraphics, int left, int top) {
        int minimumDistanceSliderX = sliderX(this.minimumDistanceLimitStep, left);
        int maximumDistanceSliderX = sliderX(this.maximumDistanceLimitStep, left);
        int litStart = minimumDistanceSliderX + (SLIDER_WIDTH / 2);
        int litEnd = maximumDistanceSliderX + (SLIDER_WIDTH / 2);
        int trackX = left + TRACK_X;
        int litWidth = Math.max(0, litEnd - litStart + 1);
        int leftDarkWidth = Math.max(0, litStart - trackX);
        int rightDarkWidth = Math.max(0, TRACK_WIDTH - leftDarkWidth - litWidth);

        if (leftDarkWidth > 0) {
            guiGraphics.blit(TEXTURE, trackX, top + TRACK_Y, TRACK_U, TRACK_DARK_V, leftDarkWidth, TRACK_HEIGHT);
        }
        if (litWidth > 0) {
            guiGraphics.blit(TEXTURE, trackX + leftDarkWidth, top + TRACK_Y, TRACK_U + leftDarkWidth, TRACK_LIT_V, litWidth, TRACK_HEIGHT);
        }
        if (rightDarkWidth > 0) {
            guiGraphics.blit(TEXTURE, trackX + leftDarkWidth + litWidth, top + TRACK_Y, TRACK_U + leftDarkWidth + litWidth, TRACK_DARK_V, rightDarkWidth, TRACK_HEIGHT);
        }
    }

    private void renderSlider(GuiGraphics guiGraphics, int left, int top, int mouseX, int mouseY, DraggedSlider slider) {
        int step = slider == DraggedSlider.LEFT ? this.minimumDistanceLimitStep : this.maximumDistanceLimitStep;
        int x = sliderX(step, left);
        boolean hover = isInside(mouseX, mouseY, x, top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT);
        int u = this.draggedSlider == slider ? SLIDER_PRESSED_U : hover ? SLIDER_HOVER_U : SLIDER_NORMAL_U;
        guiGraphics.blit(TEXTURE, x, top + SLIDER_Y, u, SLIDER_V, SLIDER_WIDTH, SLIDER_HEIGHT);
    }

    private void updateSlider(double mouseX, int left) {
        if (this.draggedSlider == null) {
            return;
        }

        int requestedStep = Math.round((float) (mouseX - this.dragGrabOffsetX - left - SLIDER_MIN_X) * SLIDER_STEPS / (SLIDER_MAX_X - SLIDER_MIN_X));
        if (this.draggedSlider == DraggedSlider.LEFT) {
            int nextStep = clamp(requestedStep, 0, this.maximumDistanceLimitStep - 1);
            if (nextStep != this.minimumDistanceLimitStep) {
                this.minimumDistanceLimitStep = nextStep;
                saveDistanceLimits();
            }
        } else {
            int nextStep = clamp(requestedStep, this.minimumDistanceLimitStep + 1, SLIDER_STEPS);
            if (nextStep != this.maximumDistanceLimitStep) {
                this.maximumDistanceLimitStep = nextStep;
                saveDistanceLimits();
            }
        }
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top) {
        if (isInside(mouseX, mouseY, sliderX(this.minimumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, sliderTooltip(this.minimumDistanceLimitStep), mouseX, mouseY);
            return;
        }
        if (isInside(mouseX, mouseY, sliderX(this.maximumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, sliderTooltip(this.maximumDistanceLimitStep), mouseX, mouseY);
            return;
        }
        if (isInside(mouseX, mouseY, left + ITEM_ICON_X, top + ITEM_ICON_Y, 32, 32)) {
            ItemStack stack = getBeaconStack();
            if (!stack.isEmpty()) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }
        }
    }

    private static Component sliderTooltip(int step) {
        return step == SLIDER_STEPS ? Component.literal("无限制") : Component.literal(step + "格");
    }

    private static int stepToDistanceLimit(int step) {
        return step == SLIDER_STEPS ? 100 : step;
    }

    private static int distanceLimitToStep(int distanceLimit) {
        return distanceLimit >= 100 ? SLIDER_STEPS : clamp(distanceLimit, 0, SLIDER_STEPS - 1);
    }

    private void saveDistanceLimits() {
        int minimumDistanceLimit = stepToDistanceLimit(this.minimumDistanceLimitStep);
        int maximumDistanceLimit = stepToDistanceLimit(this.maximumDistanceLimitStep);
        if (this.blockPos == null) {
            ItemStack stack = getHeldBeaconStack();
            if (stack.isEmpty()) {
                return;
            }
            DgBeaconItem.setDistanceLimits(stack, minimumDistanceLimit, maximumDistanceLimit);
            DgToyNetworking.sendToServer(new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit));
        } else {
            DgToyNetworking.sendToServer(new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit, this.blockPos));
        }
    }

    private ItemStack getBeaconStack() {
        if (this.blockPos == null) {
            return getHeldBeaconStack();
        }

        if (!(Minecraft.getInstance().level.getBlockEntity(this.blockPos) instanceof DgBeaconBlockEntity beacon)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.DG_BEACON.get());
        DgBeaconItem.setCollarUuid(stack, beacon.getCollarUuid());
        DgBeaconItem.setDistanceLimits(stack, beacon.getMinimumDistanceLimit(), beacon.getMaximumDistanceLimit());
        return stack;
    }

    private static ItemStack getHeldBeaconStack() {
        Player player = Minecraft.getInstance().player;
        if (player == null || !player.getMainHandItem().is(ModItems.DG_BEACON.get())) {
            return ItemStack.EMPTY;
        }
        return player.getMainHandItem();
    }

    private int sliderX(int step, int left) {
        return left + SLIDER_MIN_X + Math.round(step * (SLIDER_MAX_X - SLIDER_MIN_X) / (float) SLIDER_STEPS);
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

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum DraggedSlider {
        LEFT,
        RIGHT
    }
}