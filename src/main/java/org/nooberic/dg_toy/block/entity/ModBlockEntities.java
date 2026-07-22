package org.nooberic.dg_toy.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.ModBlocks;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Dg_toy.MODID);

    public static final RegistryObject<BlockEntityType<DgBeaconBlockEntity>> DG_BEACON = BLOCK_ENTITIES.register("dg_beacon", () ->
            BlockEntityType.Builder.of(DgBeaconBlockEntity::new, ModBlocks.DG_BEACON.get()).build(null));
        public static final RegistryObject<BlockEntityType<RedstoneControllerBlockEntity>> REDSTONE_CONTROLLER = BLOCK_ENTITIES.register("redstone_controller", () ->
            BlockEntityType.Builder.of(RedstoneControllerBlockEntity::new, ModBlocks.REDSTONE_CONTROLLER.get()).build(null));

    private ModBlockEntities() {
    }
}