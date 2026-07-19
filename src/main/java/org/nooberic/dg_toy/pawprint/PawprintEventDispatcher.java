package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nooberic.dg_toy.Dg_toy;

@Mod.EventBusSubscriber(modid = Dg_toy.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PawprintEventDispatcher {
    private PawprintEventDispatcher() {
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer owner = event.getPlayer();
        for (BasePawprintRuleHandler<ServerChatEvent> handler : PawprintRuleRegistry.getHandlers(ServerChatEvent.class)) {
            handler.handle(owner, event);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer owner)) {
            return;
        }

        for (BasePawprintRuleHandler<TickEvent.PlayerTickEvent> handler : PawprintRuleRegistry.getHandlers(TickEvent.PlayerTickEvent.class)) {
            handler.handle(owner, event);
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) {
            return;
        }

        for (BasePawprintRuleHandler<LivingHurtEvent> handler : PawprintRuleRegistry.getHandlers(LivingHurtEvent.class)) {
            handler.handle(owner, event);
        }
    }
}