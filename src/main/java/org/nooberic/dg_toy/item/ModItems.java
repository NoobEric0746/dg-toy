package org.nooberic.dg_toy.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.ModBlocks;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Dg_toy.MODID);

    public static final DeferredItem<PawprintItem> PAWPRINT = ITEMS.registerItem("pawprint", PawprintItem::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<PowerboxItem> POWERBOX = ITEMS.registerItem("powerbox", PowerboxItem::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DgCollarItem> DG_COLLAR = ITEMS.registerItem("dg_collar", DgCollarItem::new,
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<DgBeaconItem> DG_BEACON = ITEMS.registerItem("dg_beacon",
            properties -> new DgBeaconItem(ModBlocks.DG_BEACON.get(), properties),
            () -> new Item.Properties().stacksTo(1));
    public static final DeferredItem<BlockItem> REDSTONE_CONTROLLER = ITEMS.registerSimpleBlockItem(ModBlocks.REDSTONE_CONTROLLER);
    public static final DeferredItem<BlockItem> ELECTRIFIED_WIRE_NETTING = ITEMS.registerSimpleBlockItem(ModBlocks.ELECTRIFIED_WIRE_NETTING);
    public static final DeferredItem<Item> DG_CORE = ITEMS.registerSimpleItem("dg_core");
    public static final DeferredItem<RemoteControlItem> REMOTE_CONTROL = ITEMS.registerItem("remote_control", RemoteControlItem::new,
            () -> new Item.Properties().stacksTo(1));

    private ModItems() {
    }
}
