package org.nooberic.dg_toy.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.entity.DgBeaconBlockEntity;
import org.nooberic.dg_toy.item.DgBeaconItem;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.network.DgToyC2SBeaconDistanceLimitsSyncPacket;
import org.nooberic.dg_toy.network.DgToyNetworking;

@OnlyIn(Dist.CLIENT)
public class BeaconScreen extends Screen {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Dg_toy.MODID, "textures/gui/gui_beacon.png");
    private static final int TEXTURE_SIZE = 256;

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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
                int left = leftPos();
        int top = topPos();

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left, top, (float) 0, (float) 0, UI_WIDTH, UI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        renderBeaconIcon(guiGraphics, left, top);
        renderTrack(guiGraphics, left, top);
        renderSlider(guiGraphics, left, top, mouseX, mouseY, DraggedSlider.LEFT);
        renderSlider(guiGraphics, left, top, mouseX, mouseY, DraggedSlider.RIGHT);
        
        renderBeaconName(guiGraphics, left, top);
    renderDistanceRangeLabel(guiGraphics, left, top);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(guiGraphics, mouseX, mouseY, left, top);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int left = leftPos();
        int top = topPos();
        if (isInside(mouseX, mouseY, sliderX(minimumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.draggedSlider = DraggedSlider.LEFT;
            this.dragGrabOffsetX = mouseX - sliderX(minimumDistanceLimitStep, left);
            updateSlider(mouseX, left);
            return true;
        }
        if (isInside(mouseX, mouseY, sliderX(maximumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            DgToyScreenSounds.playButtonClick();
            this.draggedSlider = DraggedSlider.RIGHT;
            this.dragGrabOffsetX = mouseX - sliderX(maximumDistanceLimitStep, left);
            updateSlider(mouseX, left);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && this.draggedSlider != null) {
            updateSlider(mouseX, leftPos());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (button == 0 && this.draggedSlider != null) {
            updateSlider(mouseX, leftPos());
            this.draggedSlider = null;
            this.dragGrabOffsetX = 0;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        if (keyCode == 256 || this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderBeaconIcon(GuiGraphicsExtractor guiGraphics, int left, int top) {
        ItemStack stack = getBeaconStack();
        if (stack.isEmpty()) {
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(left + ITEM_ICON_X, top + ITEM_ICON_Y);
        guiGraphics.pose().scale(2.0F, 2.0F);
        guiGraphics.item(stack, 0, 0);
        guiGraphics.itemDecorations(this.font, stack, 0, 0);
        guiGraphics.pose().popMatrix();
    }

    private void renderBeaconName(GuiGraphicsExtractor guiGraphics, int left, int top) {
        ItemStack stack = getBeaconStack();
        if (stack.isEmpty()) {
            return;
        }

        Component name = stack.getHoverName();
        int textX = left + ITEM_NAME_CENTER_X - this.font.width(name) / 2;
        int textY = top + ITEM_NAME_CENTER_Y - this.font.lineHeight / 2;
        guiGraphics.text(this.font, name, textX, textY, 0xFFFFFFFF);
    }

    private void renderDistanceRangeLabel(GuiGraphicsExtractor guiGraphics, int left, int top) {
        guiGraphics.text(this.font, "允许的距离范围", left + 11, top + 39, 0xFFFFFFFF);
    }

    private void renderTrack(GuiGraphicsExtractor guiGraphics, int left, int top) {
        int minimumDistanceSliderX = sliderX(this.minimumDistanceLimitStep, left);
        int maximumDistanceSliderX = sliderX(this.maximumDistanceLimitStep, left);
        int litStart = minimumDistanceSliderX + (SLIDER_WIDTH / 2);
        int litEnd = maximumDistanceSliderX + (SLIDER_WIDTH / 2);
        int trackX = left + TRACK_X;
        int litWidth = Math.max(0, litEnd - litStart + 1);
        int leftDarkWidth = Math.max(0, litStart - trackX);
        int rightDarkWidth = Math.max(0, TRACK_WIDTH - leftDarkWidth - litWidth);

        if (leftDarkWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, trackX, top + TRACK_Y, (float) TRACK_U, (float) TRACK_DARK_V, leftDarkWidth, TRACK_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        if (litWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, trackX + leftDarkWidth, top + TRACK_Y, (float) TRACK_U + leftDarkWidth, (float) TRACK_LIT_V, litWidth, TRACK_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
        if (rightDarkWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, trackX + leftDarkWidth + litWidth, top + TRACK_Y, (float) TRACK_U + leftDarkWidth + litWidth, (float) TRACK_DARK_V, rightDarkWidth, TRACK_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    }

    private void renderSlider(GuiGraphicsExtractor guiGraphics, int left, int top, int mouseX, int mouseY, DraggedSlider slider) {
        int step = slider == DraggedSlider.LEFT ? this.minimumDistanceLimitStep : this.maximumDistanceLimitStep;
        int x = sliderX(step, left);
        boolean hover = isInside(mouseX, mouseY, x, top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT);
        int u = this.draggedSlider == slider ? SLIDER_PRESSED_U : hover ? SLIDER_HOVER_U : SLIDER_NORMAL_U;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, top + SLIDER_Y, (float) u, (float) SLIDER_V, SLIDER_WIDTH, SLIDER_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
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

    private void renderHoverTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int left, int top) {
        if (isInside(mouseX, mouseY, sliderX(this.minimumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.setTooltipForNextFrame(this.font, sliderTooltip(this.minimumDistanceLimitStep), mouseX, mouseY);
            return;
        }
        if (isInside(mouseX, mouseY, sliderX(this.maximumDistanceLimitStep, left), top + SLIDER_Y, SLIDER_WIDTH, SLIDER_HEIGHT)) {
            guiGraphics.setTooltipForNextFrame(this.font, sliderTooltip(this.maximumDistanceLimitStep), mouseX, mouseY);
            return;
        }
        if (isInside(mouseX, mouseY, left + ITEM_ICON_X, top + ITEM_ICON_Y, 32, 32)) {
            ItemStack stack = getBeaconStack();
            if (!stack.isEmpty()) {
                guiGraphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
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
            DgToyNetworking.sendToServer(new DgToyC2SBeaconDistanceLimitsSyncPacket(minimumDistanceLimit, maximumDistanceLimit, null));
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
