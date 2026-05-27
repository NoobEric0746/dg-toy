package org.nooberic.dg_toy;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Dg_toy.MODID)
public class Dg_toy {

    public static final String MODID = "dg_toy";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> DG_TOY_TAB = CREATIVE_MODE_TABS.register("dg_toy_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .icon(() -> ModItems.POWERBOX.get().getDefaultInstance())
            .title(net.minecraft.network.chat.Component.translatable("itemGroup." + MODID + ".main"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.POWERBOX.get());
                output.accept(ModItems.REMOTE_CONTROL.get());
            })
            .build());

    public Dg_toy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(DgToyNetworking::register);
        LOGGER.info("DGToy common setup");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("DGToy server starting");
    }
}
