package org.nooberic.dg_toy.pawprint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.server.PlayerMessageCompat;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public abstract class BasePawprintRuleHandler<TEvent> {
    public abstract String getDisplayName();

    public String getDescription() {
        return "暂无规则描述。";
    }

    public final void handle(ServerPlayer owner, TEvent event) {
        for (SlotResult pawprintSlot : PawprintRuleNbtHelper.getEquippedPawprints(owner)) {
            handleEquippedPawprint(owner, event, pawprintSlot);
        }
    }

    private void handleEquippedPawprint(ServerPlayer owner, TEvent event, SlotResult pawprintSlot) {
        ItemStack pawprintStack = pawprintSlot.stack();
        if (pawprintStack.isEmpty() || !PawprintRuleNbtHelper.isRuleEnabled(pawprintStack, getRuleId())) {
            return;
        }
        if (PawprintRuleRegistry.getMode(this) != PawprintRuleNbtHelper.getMode(pawprintStack)) {
            return;
        }

        try {
            initializeDefaults(pawprintStack);

            PawprintRuleContext<TEvent> context = new PawprintRuleContext<>(owner, pawprintStack, event);
            if (!matches(context)) {
                return;
            }

            long currentTick = owner.level().getGameTime();
            String cooldownId = getRuleId() + "@" + pawprintSlot.slotContext().identifier() + ":" + pawprintSlot.slotContext().index();
            if (PawprintRuntimeState.isOnCooldown(owner.getUUID(), cooldownId, currentTick, getCooldownTicks(context))) {
                return;
            }

            PawprintEffect effect = resolveEffect(context);
            if (effect == null) {
                return;
            }

            if (PawprintEffectExecutor.apply(owner, effect.channel(), effect)) {
                PawprintRuntimeState.markTriggered(owner.getUUID(), cooldownId, currentTick);
                PlayerMessageCompat.displayClientMessage(owner, Component.literal("爪印规则触发: " + getDisplayName()), true);
                afterTriggered(context);
            }
        } catch (Exception ex) {
            PlayerMessageCompat.displayClientMessage(owner, Component.literal("爪印规则执行失败: " + getRuleId()), true);
        }
    }

    public abstract String getRuleId();

    public abstract Class<TEvent> getEventType();

    public List<PawprintRuleVariable<?>> getVariables() {
        return List.of();
    }

    protected int getCooldownTicks(PawprintRuleContext<TEvent> context) {
        return 0;
    }

    protected void afterTriggered(PawprintRuleContext<TEvent> context) {
    }

    protected abstract boolean matches(PawprintRuleContext<TEvent> context);

    protected abstract PawprintEffect resolveEffect(PawprintRuleContext<TEvent> context);

    private void initializeDefaults(ItemStack pawprintStack) {
        for (PawprintRuleVariable<?> variable : getVariables()) {
            if (variable.type() == String.class) {
                @SuppressWarnings("unchecked")
                PawprintRuleVariable<String> stringVariable = (PawprintRuleVariable<String>) variable;
                PawprintRuleNbtHelper.getStringVariable(pawprintStack, getRuleId(), stringVariable);
            }
        }
    }

    protected CompoundTag getRuleVariables(ItemStack pawprintStack) {
        return PawprintRuleNbtHelper.getOrCreateVariablesTag(pawprintStack, getRuleId());
    }
}
