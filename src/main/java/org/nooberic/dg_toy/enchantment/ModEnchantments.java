package org.nooberic.dg_toy.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.nooberic.dg_toy.Dg_toy;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Dg_toy.MODID);

    public static final RegistryObject<Enchantment> ELECTRIC_SHOCK =
            ENCHANTMENTS.register("electric_shock", ElectricShockEnchantment::new);

    private ModEnchantments() {
    }
}
