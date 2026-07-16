package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
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
}