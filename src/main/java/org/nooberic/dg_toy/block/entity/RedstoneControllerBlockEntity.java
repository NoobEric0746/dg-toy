package org.nooberic.dg_toy.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneControllerBlockEntity extends BlockEntity {
    public RedstoneControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDSTONE_CONTROLLER.get(), pos, state);
    }
}