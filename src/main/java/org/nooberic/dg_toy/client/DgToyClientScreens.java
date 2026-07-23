package org.nooberic.dg_toy.client;

import net.minecraft.client.Minecraft;
import org.nooberic.dg_toy.block.entity.DgBeaconBlockEntity;
import org.nooberic.dg_toy.block.entity.RedstoneControllerBlockEntity;
import org.nooberic.dg_toy.client.screen.BeaconScreen;
import org.nooberic.dg_toy.client.screen.RedstoneControllerScreen;

public final class DgToyClientScreens {
    private DgToyClientScreens() {
    }

    public static void openBeacon(DgBeaconBlockEntity beacon) {
        Minecraft.getInstance().setScreen(new BeaconScreen(beacon));
    }

    public static void openRedstoneController(RedstoneControllerBlockEntity controller) {
        Minecraft.getInstance().setScreen(new RedstoneControllerScreen(controller));
    }
}