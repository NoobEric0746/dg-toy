package org.nooberic.dg_toy.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.nooberic.dg_toy.Dg_toy;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Dg_toy.MODID);

    public static final RegistryObject<Block> DG_BEACON = BLOCKS.register("dg_beacon", () ->
            new DgBeaconBlock(BlockBehaviour.Properties.of()
                .strength(1.5F, 6.0F)
                .pushReaction(PushReaction.BLOCK)));

    private ModBlocks() {
    }
}