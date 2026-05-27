package org.nooberic.dg_toy.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.nooberic.dg_toy.Dg_toy;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PlayerStatusNameplateRenderer {
    private static final float TEXT_SCALE = 0.025F;
    private static final float Y_OFFSET = 100.0F;

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

        Component statusLine = buildStatusLine(player, localPlayer);
        if (statusLine.getString().isEmpty()) {
            return;
        }

        event.getPoseStack().pushPose();
        event.getPoseStack().translate(0.0F, Y_OFFSET * TEXT_SCALE, 0.0F);

        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        event.getPoseStack().mulPose(dispatcher.cameraOrientation());
        event.getPoseStack().scale(-TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        Matrix4f matrix = event.getPoseStack().last().pose();
        Font font = mc.font;
        float x = -font.width(statusLine) / 2.0F;
        int backgroundColor = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        font.drawInBatch(statusLine, x, 0.0F, 0x20FFFFFF, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
        font.drawInBatch(statusLine, x, 0.0F, 0xFFFFFFFF, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        bufferSource.endBatch();

        event.getPoseStack().popPose();
    }

    private static Component buildStatusLine(Player player, LocalPlayer localPlayer) {
        PlayerConnectionStatusSnapshot snapshot = PlayerStatusClientCache.get(player.getUUID());
        boolean connected = snapshot.connected();
        boolean virtuallyBound = snapshot.virtuallyBound();
        long pairingExpiresAtMs = snapshot.pairingExpiresAtMs();

        MutableComponent root = Component.empty();
        root.append(Component.literal(connected ? "已连接" : "未连接")
                .withStyle(connected ? ChatFormatting.GREEN : ChatFormatting.RED));
        root.append(Component.literal("  ").withStyle(ChatFormatting.WHITE));
        root.append(buildVirtualPairingText(virtuallyBound, pairingExpiresAtMs));
        return root;
    }

    private static Component buildVirtualPairingText(boolean virtuallyBound, long pairingExpiresAtMs) {
        if (pairingExpiresAtMs > System.currentTimeMillis()) {
            return Component.literal("配对中").withStyle(ChatFormatting.YELLOW);
        }
        return Component.literal(virtuallyBound ? "已配对" : "未配对")
                .withStyle(virtuallyBound ? ChatFormatting.AQUA : ChatFormatting.GRAY);
    }
}
