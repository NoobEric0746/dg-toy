package org.nooberic.dg_toy.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.nooberic.dg_toy.Dg_toy;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PlayerStatusNameplateRenderer {
    private static final float TEXT_SCALE = 0.025F;
    private static final float Y_OFFSET = 100.0F;
    private static final ResourceLocation POWERBOX_ICON = new ResourceLocation(Dg_toy.MODID, "textures/gui/status/icon_powerbox.png");
    private static final ResourceLocation CONTROLLER_ICON = new ResourceLocation(Dg_toy.MODID, "textures/gui/status/icon_controller.png");
    private static final float ICON_TEXT_GAP = 2.0F;
    private static final float GROUP_GAP = 8.0F;

    private PlayerStatusNameplateRenderer() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null || mc.level == null) {
            return;
        }

        NameplateStatusDisplay display = buildStatusDisplay(player, localPlayer);
        if (display.isEmpty()) {
            return;
        }

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(0.0F, Y_OFFSET * TEXT_SCALE, 0.0F);

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        event.getPoseStack().mulPose(dispatcher.cameraOrientation());
        event.getPoseStack().scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        Matrix4f matrix = event.getPoseStack().last().pose();
        Font font = mc.font;
        float totalWidth = display.width(font);
        float x = -totalWidth / 2.0F;
        int backgroundColor = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        display.draw(event.getPoseStack(), font, matrix, bufferSource, x, backgroundColor);
        bufferSource.endBatch();

        event.getPoseStack().popPose();
    }

    private static NameplateStatusDisplay buildStatusDisplay(Player player, LocalPlayer localPlayer) {
        PlayerConnectionStatusSnapshot snapshot = PlayerStatusClientCache.get(player.getUUID());
        boolean connected = snapshot.connected();
        boolean virtuallyBound = snapshot.virtuallyBound();
        long pairingExpiresAtMs = snapshot.pairingExpiresAtMs();

        return new NameplateStatusDisplay(
                POWERBOX_ICON,
                getPowerboxStatusText(connected),
                CONTROLLER_ICON,
                buildVirtualPairingText(virtuallyBound, pairingExpiresAtMs)
        );
    }

    private static Component getPowerboxStatusText(boolean connected) {
        return Component.literal(connected ? "✔" : "✘")
                .withStyle(connected ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    private static Component buildVirtualPairingText(boolean virtuallyBound, long pairingExpiresAtMs) {
        if (pairingExpiresAtMs > System.currentTimeMillis()) {
            return Component.literal("配对中").withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal(virtuallyBound ? "✔" : "✘")
                .withStyle(virtuallyBound ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    private record NameplateStatusDisplay(ResourceLocation leftIcon, Component leftText, ResourceLocation rightIcon, Component rightText) {
        boolean isEmpty() {
            return leftText.getString().isEmpty() && rightText.getString().isEmpty();
        }

        float width(Font font) {
            float iconSize = font.lineHeight;
            return iconSize + ICON_TEXT_GAP + font.width(leftText)
                    + GROUP_GAP
                    + iconSize + ICON_TEXT_GAP + font.width(rightText);
        }

        void draw(PoseStack poseStack, Font font, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource, float startX, int backgroundColor) {
            float iconSize = font.lineHeight;
            float y = -(font.lineHeight - iconSize) / 2.0F;
            float x = startX;

            drawIcon(poseStack, leftIcon, x, y, iconSize);
            x += iconSize + ICON_TEXT_GAP;
            drawText(font, matrix, bufferSource, leftText, x, 0.0F, backgroundColor);
            x += font.width(leftText) + GROUP_GAP;

            drawIcon(poseStack, rightIcon, x, y, iconSize);
            x += iconSize + ICON_TEXT_GAP;
            drawText(font, matrix, bufferSource, rightText, x, 0.0F, backgroundColor);
        }

        private static void drawText(Font font, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource, Component text, float x, float y, int backgroundColor) {
            font.drawInBatch(text, x, y, 0x20FFFFFF, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
            font.drawInBatch(text, x, y, 0xFFFFFFFF, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }

        private static void drawIcon(PoseStack poseStack, ResourceLocation fallbackTexture, float x, float y, float size) {
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, fallbackTexture);
            var matrix4f = poseStack.last().pose();
            var buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(matrix4f, x, y + size, 0.0F).uv(0.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x + size, y + size, 0.0F).uv(1.0F, 1.0F).endVertex();
            buffer.vertex(matrix4f, x + size, y, 0.0F).uv(1.0F, 0.0F).endVertex();
            buffer.vertex(matrix4f, x, y, 0.0F).uv(0.0F, 0.0F).endVertex();
            BufferUploader.drawWithShader(buffer.end());
            RenderSystem.disableBlend();
        }
    }
}
