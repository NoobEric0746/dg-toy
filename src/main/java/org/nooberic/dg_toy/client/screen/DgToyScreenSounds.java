package org.nooberic.dg_toy.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

final class DgToyScreenSounds {
    private DgToyScreenSounds() {
    }

    static void playButtonClick() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}