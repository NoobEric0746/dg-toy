package org.nooberic.dg_toy.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.nooberic.dg_toy.Dg_toy;

/**
 * 26.1.2 rework: RenderNameTagEvent 拆分为 CanRender/DoRender，名字标签走新的
 * render-state 管线。这里通过 CanRender#setContent 把连接/配对状态追加到名字标签上
 * （放弃旧的手绘图标绘制）。
 */
@EventBusSubscriber(modid = Dg_toy.MODID, value = Dist.CLIENT)
public final class PlayerStatusNameplateRenderer {
    private PlayerStatusNameplateRenderer() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent.CanRender event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null || mc.level == null) {
            return;
        }

        Component status = buildStatusText(player, localPlayer);
        if (status == null) {
            return;
        }

        Component original = event.getContent();
        event.setContent(original.copy().append(Component.literal("  ")).append(status));
    }

    private static Component buildStatusText(Player player, LocalPlayer localPlayer) {
        PlayerConnectionStatusSnapshot snapshot = PlayerStatusClientCache.get(player.getUUID());
        if (!snapshot.showNameplate()) {
            return null;
        }
        boolean connected = snapshot.connected();
        boolean virtuallyBound = snapshot.virtuallyBound();
        long pairingExpiresAtMs = snapshot.pairingExpiresAtMs();

        MutableComponent text = Component.empty();
        text.append(Component.literal(connected ? "[ON]" : "[OFF]")
                .withStyle(connected ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        if (pairingExpiresAtMs > System.currentTimeMillis()) {
            text.append(Component.literal(" [PAIRING]").withStyle(ChatFormatting.YELLOW));
        } else if (virtuallyBound) {
            text.append(Component.literal(" [BOUND]").withStyle(ChatFormatting.GREEN));
        }

        if (text.getString().isEmpty()) {
            return null;
        }
        return text;
    }
}
