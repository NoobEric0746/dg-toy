package org.nooberic.dg_toy.pawprint;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class PawprintRuleContext<TEvent> {
    private final ServerPlayer owner;
    private final ItemStack pawprintStack;
    private final TEvent event;

    public PawprintRuleContext(ServerPlayer owner, ItemStack pawprintStack, TEvent event) {
        this.owner = owner;
        this.pawprintStack = pawprintStack;
        this.event = event;
    }

    public ServerPlayer owner() {
        return this.owner;
    }

    public ItemStack pawprintStack() {
        return this.pawprintStack;
    }

    public TEvent event() {
        return this.event;
    }
}