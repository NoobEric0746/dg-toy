package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.config.DgToyConfig;
import org.nooberic.dg_toy.event.DgBeaconSignalEvent;
import org.nooberic.dg_toy.event.RedstoneControllerActivatedEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

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
    public static void onDgBeaconSignal(DgBeaconSignalEvent event) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        double signalRange = DgToyConfig.BEACON_SIGNAL_RANGE.get();
        double signalRangeSqr = signalRange * signalRange;
        for (ServerPlayer owner : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (owner.position().distanceToSqr(event.getSourcePosition()) > signalRangeSqr) {
                continue;
            }
            for (BasePawprintRuleHandler<DgBeaconSignalEvent> handler : PawprintRuleRegistry.getHandlers(DgBeaconSignalEvent.class)) {
                handler.handle(owner, event);
            }
        }
    }

    @SubscribeEvent
    public static void onRedstoneControllerActivated(RedstoneControllerActivatedEvent event) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }

        double controlRange = DgToyConfig.REDSTONE_CONTROLLER_RANGE.get();
        double controlRangeSqr = controlRange * controlRange;
        Vec3 eventPosition = Vec3.atCenterOf(event.getBlockPos());
        for (ServerPlayer owner : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (owner.position().distanceToSqr(eventPosition) > controlRangeSqr) {
                continue;
            }
            for (BasePawprintRuleHandler<RedstoneControllerActivatedEvent> handler : PawprintRuleRegistry.getHandlers(RedstoneControllerActivatedEvent.class)) {
                handler.handle(owner, event);
            }
        }
    }

    public static void onPlayerMovement(ServerPlayer owner, ServerboundMovePlayerPacket packet) {
        for (BasePawprintRuleHandler<ServerboundMovePlayerPacket> handler : PawprintRuleRegistry.getHandlers(ServerboundMovePlayerPacket.class)) {
            handler.handle(owner, packet);
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

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) {
            return;
        }

        for (BasePawprintRuleHandler<AttackEntityEvent> handler : PawprintRuleRegistry.getHandlers(AttackEntityEvent.class)) {
            handler.handle(owner, event);
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer owner)) {
            return;
        }

        for (BasePawprintRuleHandler<PlayerInteractEvent> handler : PawprintRuleRegistry.getHandlers(PlayerInteractEvent.class)) {
            handler.handle(owner, event);
        }
    }
}