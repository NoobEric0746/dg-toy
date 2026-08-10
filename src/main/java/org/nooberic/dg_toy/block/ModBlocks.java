package org.nooberic.dg_toy.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.nooberic.dg_toy.Dg_toy;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Dg_toy.MODID);

    public static final DeferredBlock<DgBeaconBlock> DG_BEACON = BLOCKS.registerBlock("dg_beacon", DgBeaconBlock::new,
            () -> BlockBehaviour.Properties.of().strength(1.5F, 6.0F).pushReaction(PushReaction.BLOCK));
    public static final DeferredBlock<RedstoneControllerBlock> REDSTONE_CONTROLLER = BLOCKS.registerBlock("redstone_controller", RedstoneControllerBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3.5F).pushReaction(PushReaction.BLOCK));
    public static final DeferredBlock<ElectrifiedWireNettingBlock> ELECTRIFIED_WIRE_NETTING = BLOCKS.registerBlock("electrified_wire_netting", ElectrifiedWireNettingBlock::new,
            () -> BlockBehaviour.Properties.of().strength(1.0F).noOcclusion().pushReaction(PushReaction.BLOCK));

    private ModBlocks() {
    }
}
