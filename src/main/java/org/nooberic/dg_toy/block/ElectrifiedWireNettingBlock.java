package org.nooberic.dg_toy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.nooberic.dglib.client.particle.DgParticleTypes;
import org.nooberic.dglib.multiplayer.DgServerCoyoteApi;
import org.nooberic.dg_toy.config.DgToyConfig;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;

public class ElectrifiedWireNettingBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    private static final int PARTICLE_INTERVAL_TICKS = 8;
    private static final int PARTICLES_PER_INTERVAL = 2;
    private static final int SHOCK_INTERVAL_TICKS = 25;
    private static final int BOTH_CHANNELS = 3;
    private static final int SHOCK_DURATION_SECONDS = 1;
    private static final String SHOCK_PULSE_ID = "chaos";
    private static final String LAST_CONTACT_POS_TAG = "dg_toy.electrified_wire_netting.last_contact_pos";
    private static final String LAST_CONTACT_TICK_TAG = "dg_toy.electrified_wire_netting.last_contact_tick";
    private static final String LAST_SHOCK_TICK_TAG = "dg_toy.electrified_wire_netting.last_shock_tick";

    public ElectrifiedWireNettingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }

        long gameTime = level.getGameTime();
        CompoundTag persistentData = player.getPersistentData();
        long packedPos = pos.asLong();
        boolean enteredNetting = persistentData.getLong(LAST_CONTACT_POS_TAG) != packedPos
                || persistentData.getLong(LAST_CONTACT_TICK_TAG) != gameTime - 1;

        persistentData.putLong(LAST_CONTACT_POS_TAG, packedPos);
        persistentData.putLong(LAST_CONTACT_TICK_TAG, gameTime);
        if (enteredNetting) {
            shockPlayer(player, gameTime);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        long gameTime = level.getGameTime();
        if (gameTime % PARTICLE_INTERVAL_TICKS == 0) {
            spawnParticles(level, pos, random);
        }
        if (gameTime % SHOCK_INTERVAL_TICKS == 0) {
            shockStandingPlayers(level, pos);
        }
        level.scheduleTick(pos, this, 1);
    }

    private static void spawnParticles(ServerLevel level, BlockPos pos, RandomSource random) {
        for (int index = 0; index < PARTICLES_PER_INTERVAL; index++) {
            level.sendParticles(
                    DgParticleTypes.ELECTRICITY.get(),
                    pos.getX() + random.nextDouble(),
                    pos.getY() + random.nextDouble() / 16.0D,
                    pos.getZ() + random.nextDouble(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private static void shockStandingPlayers(ServerLevel level, BlockPos pos) {
        long gameTime = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (!player.onGround() || !pos.equals(player.getOnPos()) || !RemoteControlStateHelper.isShockAvailable(player)) {
                continue;
            }
            shockPlayer(player, gameTime);
        }
    }

    private static void shockPlayer(ServerPlayer player, long gameTime) {
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.getLong(LAST_SHOCK_TICK_TAG) == gameTime
                || !RemoteControlStateHelper.isShockAvailable(player)) {
            return;
        }
        DgServerCoyoteApi.get().getCoyote(player).controlSoft(
                BOTH_CHANNELS,
                DgToyConfig.ELECTRIFIED_WIRE_NETTING_STRENGTH.get(),
                SHOCK_PULSE_ID,
                SHOCK_DURATION_SECONDS
        );
        persistentData.putLong(LAST_SHOCK_TICK_TAG, gameTime);
    }
}