package org.nooberic.dg_toy.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.nooberic.dg_toy.Dg_toy;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Dg_toy.MODID);

    public static final RegistryObject<Item> PAWPRINT = ITEMS.register("pawprint", PawprintItem::new);
    public static final RegistryObject<Item> POWERBOX = ITEMS.register("powerbox", PowerboxItem::new);
    public static final RegistryObject<Item> DG_COLLAR = ITEMS.register("dg_collar", DgCollarItem::new);
    public static final RegistryObject<Item> REMOTE_CONTROL = ITEMS.register("remote_control", RemoteControlItem::new);

    private ModItems() {
    }
}
