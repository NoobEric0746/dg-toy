package org.nooberic.dg_toy;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotTypeMessage;
import org.nooberic.dg_toy.client.DgCollarCurioRenderer;
import org.nooberic.dg_toy.client.ModClientEvents;
import org.nooberic.dg_toy.client.ModItemTooltips;
import org.nooberic.dg_toy.client.PawprintCurioRenderer;
import org.nooberic.dg_toy.client.PowerboxCurioRenderer;
import org.nooberic.dg_toy.block.ModBlocks;
import org.nooberic.dg_toy.block.entity.ModBlockEntities;
import org.nooberic.dg_toy.config.DgToyConfig;
import org.nooberic.dg_toy.item.ModItems;
import org.nooberic.dg_toy.network.DgToyNetworking;
import org.nooberic.dg_toy.pawprint.PawprintRules;
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
                output.accept(ModItems.PAWPRINT.get());
                output.accept(ModItems.POWERBOX.get());
                output.accept(ModItems.DG_COLLAR.get());
                output.accept(ModItems.DG_BEACON.get());
                output.accept(ModItems.REDSTONE_CONTROLLER.get());
                output.accept(ModItems.ELECTRIFIED_WIRE_NETTING.get());
                output.accept(ModItems.DG_CORE.get());
                output.accept(ModItems.REMOTE_CONTROL.get());
                output.accept(ModItems.DG_PRESSURE_PLATE.get());
                output.accept(ModItems.DG_TOY_GUIDE.get());
            })
            .build());

    public Dg_toy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DgToyConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            PowerboxCurioRenderer.register(modEventBus);
            PawprintCurioRenderer.register(modEventBus);
            DgCollarCurioRenderer.register(modEventBus);
            ModClientEvents.register(modEventBus);
            ModItemTooltips.register();
        });

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DgToyNetworking.register();
            PawprintRules.bootstrap();
            InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                    new SlotTypeMessage.Builder("powerbox")
                        .icon(new ResourceLocation(MODID, "slot/powerbox_slot"))
                        .size(1).priority(110).build());
                InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                    new SlotTypeMessage.Builder("pawprint")
                        .icon(new ResourceLocation(MODID, "slot/pawprint_slot"))
                        .size(2).priority(110).build());
                InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () ->
                    new SlotTypeMessage.Builder("dg_collar")
                        .icon(new ResourceLocation(MODID, "slot/collar_slot"))
                        .size(1).priority(110).build());
        });
        LOGGER.info("DGToy common setup");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("DGToy server starting");
    }
}
