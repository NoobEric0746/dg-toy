package org.nooberic.dg_toy.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.nooberic.dg_toy.block.ModBlocks;

public final class ModClientEvents {
    private ModClientEvents() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModClientEvents::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELECTRIFIED_WIRE_NETTING.get(), RenderType.cutout()));
    }
}