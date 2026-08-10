package org.nooberic.dg_toy;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nooberic.dg_toy.block.ModBlocks;
import org.nooberic.dg_toy.block.entity.ModBlockEntities;
import org.nooberic.dg_toy.config.DgToyConfig;
import org.nooberic.dg_toy.item.DgCollarItem;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.item.PawprintItem;
import org.nooberic.dg_toy.item.PowerboxItem;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dg_toy.pawprint.PawprintRules;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Dg_toy.MODID)
public class Dg_toy {

    public static final String MODID = "dg_toy";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DG_TOY_TAB = CREATIVE_MODE_TABS.register("dg_toy_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> ModItems.POWERBOX.get().getDefaultInstance())
            .title(Component.translatable("itemGroup." + MODID + ".main"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.PAWPRINT.get());
                output.accept(ModItems.POWERBOX.get());
                output.accept(ModItems.DG_COLLAR.get());
                output.accept(ModItems.DG_BEACON.get());
                output.accept(ModItems.REDSTONE_CONTROLLER.get());
                output.accept(ModItems.ELECTRIFIED_WIRE_NETTING.get());
                output.accept(ModItems.DG_CORE.get());
                output.accept(ModItems.REMOTE_CONTROL.get());
            })
            .build());

    public Dg_toy(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, DgToyConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DgToyNetworking::register);
        modEventBus.addListener(Dg_toy::registerCapabilities);

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        PowerboxItem.registerCurioCapability(event);
        PawprintItem.registerCurioCapability(event);
        DgCollarItem.registerCurioCapability(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PawprintRules.bootstrap();
            LOGGER.info("DGToy common setup complete");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("DGToy server starting");
    }
}
