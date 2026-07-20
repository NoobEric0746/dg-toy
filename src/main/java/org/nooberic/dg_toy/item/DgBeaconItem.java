package org.nooberic.dg_toy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.UUID;

public class DgBeaconItem extends Item {
    private static final String COLLAR_UUID_TAG = "CollarUuid";
    private static final String COLLAR_SLOT_ID = "collar";

    public DgBeaconItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !(entity instanceof ServerPlayer targetPlayer) || !(player instanceof ServerPlayer serverPlayer) || targetPlayer == serverPlayer) {
            return InteractionResult.PASS;
        }

        UUID collarUuid = getEquippedCollarUuid(targetPlayer);
        if (collarUuid == null) {
            serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.dg_beacon.target_not_activated"), true);
            return InteractionResult.CONSUME;
        }

        stack.getOrCreateTag().putUUID(COLLAR_UUID_TAG, collarUuid);
        serverPlayer.getInventory().setChanged();
        serverPlayer.setItemInHand(hand, stack);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, serverPlayer.getInventory().selected, stack.copy()));
        serverPlayer.displayClientMessage(Component.translatable("item.dg_toy.dg_beacon.bound", collarUuid), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        UUID collarUuid = getCollarUuid(stack);
        if (collarUuid == null) {
            tooltip.add(Component.translatable("item.dg_toy.dg_beacon.unbound"));
        } else {
            tooltip.add(Component.translatable("item.dg_toy.dg_beacon.bound_uuid", collarUuid));
        }
    }

    @Nullable
    private static UUID getEquippedCollarUuid(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve()
                .flatMap(handler -> handler.findCurios(stack -> stack.is(ModItems.DG_COLLAR.get())).stream()
                        .map(result -> DgCollarItem.getCollarUuid(result.stack()))
                        .filter(uuid -> uuid != null)
                        .findFirst())
                .orElse(null);
    }

    @Nullable
    private static UUID getCollarUuid(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(COLLAR_UUID_TAG) ? tag.getUUID(COLLAR_UUID_TAG) : null;
    }
}