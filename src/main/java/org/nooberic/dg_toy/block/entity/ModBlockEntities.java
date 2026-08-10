package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nooberic.dg_toy.Dg_toy;
import org.nooberic.dg_toy.block.ModBlocks;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Dg_toy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DgBeaconBlockEntity>> DG_BEACON = BLOCK_ENTITIES.register("dg_beacon", () ->
            new BlockEntityType<>(DgBeaconBlockEntity::new, ModBlocks.DG_BEACON.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneControllerBlockEntity>> REDSTONE_CONTROLLER = BLOCK_ENTITIES.register("redstone_controller", () ->
            new BlockEntityType<>(RedstoneControllerBlockEntity::new, ModBlocks.REDSTONE_CONTROLLER.get()));

    private ModBlockEntities() {
    }
}
