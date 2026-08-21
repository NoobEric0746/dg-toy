package org.nooberic.dg_toy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.nooberic.dglib.client.particle.DgParticleTypes;
import org.nooberic.dg_toy.config.DgToyConfig;
import org.nooberic.dg_toy.server.RemoteControlStateHelper;
import org.nooberic.dg_toy.server.ShockEffectHelper;

public class ElectrifiedWireNettingBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);
    private static final AABB PRESSURE_PLATE_AABB = new AABB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 0.5D, 15.0D / 16.0D);
    private static final int PARTICLE_INTERVAL_TICKS = 8;
    private static final int PARTICLES_PER_INTERVAL = 2;
    private static final int SHOCK_INTERVAL_TICKS = 25;
    private static final int BOTH_CHANNELS = 3;
    private static final int SHOCK_DURATION_SECONDS = 1;
    private static final String SHOCK_PULSE_ID = "chaos";
    private static final String LAST_CONTACT_TICK_TAG = "dg_toy.electrified_wire_netting.last_contact_tick";
    private static final String LAST_SHOCK_TICK_TAG = "dg_toy.electrified_wire_netting.last_shock_tick";

    /** 0 = 未伪装,1..16 = 对应 DyeColor.getId()+1 的地毯颜色。 */
    public static final IntegerProperty DISGUISE = IntegerProperty.create("disguise", 0, 16);
    private static final int DISGUISED_SHOCK_STRENGTH = 50;
    private static final Block[] CARPETS_BY_COLOR = {
        Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET, Blocks.LIGHT_BLUE_CARPET,
        Blocks.YELLOW_CARPET, Blocks.LIME_CARPET, Blocks.PINK_CARPET, Blocks.GRAY_CARPET,
        Blocks.LIGHT_GRAY_CARPET, Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
        Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET
    };

    public ElectrifiedWireNettingBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(DISGUISE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISGUISE);
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        int disguise = state.getValue(DISGUISE);

        if (player.isShiftKeyDown()) {
            // 空手 shift 右击:取回地毯。
            if (disguise != 0 && stack.isEmpty()) {
                if (!level.isClientSide) {
                    ItemStack carpet = new ItemStack(CARPETS_BY_COLOR[disguise - 1].asItem());
                    if (!player.getInventory().add(carpet)) {
                        player.drop(carpet, false);
                    }
                    level.setBlock(pos, state.setValue(DISGUISE, 0), 3);
                    level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // 非 shift 且未伪装:手持地毯右击进行伪装。
        if (disguise == 0 && stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CarpetBlock carpetBlock) {
            int newDisguise = carpetIndex(carpetBlock);
            if (newDisguise == 0) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(DISGUISE, newDisguise), 3);
                level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (!player.getBoundingBox().intersects(PRESSURE_PLATE_AABB.move(pos))) {
            return;
        }

        CompoundTag persistentData = player.getPersistentData();
        boolean enteredNetting = persistentData.getLong(LAST_CONTACT_TICK_TAG) != gameTime - 1;

        persistentData.putLong(LAST_CONTACT_TICK_TAG, gameTime);
        if (enteredNetting) {
            shockPlayer(player, gameTime, getShockStrength(state));
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
        if (state.getValue(DISGUISE) == 0 && gameTime % PARTICLE_INTERVAL_TICKS == 0) {
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
        int strength = getShockStrength(level.getBlockState(pos));
        for (ServerPlayer player : level.players()) {
            if (!player.onGround() || !pos.equals(player.getOnPos()) || !RemoteControlStateHelper.isShockAvailable(player)) {
                continue;
            }
            shockPlayer(player, gameTime, strength);
        }
    }

    private static void shockPlayer(ServerPlayer player, long gameTime, int strength) {
        CompoundTag persistentData = player.getPersistentData();
        if (!RemoteControlStateHelper.isShockAvailable(player)) {
            return;
        }

        if (persistentData.contains(LAST_SHOCK_TICK_TAG)
                && gameTime - persistentData.getLong(LAST_SHOCK_TICK_TAG) < SHOCK_INTERVAL_TICKS) {
            return;
        }

        ShockEffectHelper.apply(
            player,
            false,
                BOTH_CHANNELS,
                strength,
                SHOCK_PULSE_ID,
                SHOCK_DURATION_SECONDS
        );
        persistentData.putLong(LAST_SHOCK_TICK_TAG, gameTime);
    }

    private static int getShockStrength(BlockState state) {
        return state.getValue(DISGUISE) == 0
                ? DgToyConfig.ELECTRIFIED_WIRE_NETTING_STRENGTH.get()
                : DISGUISED_SHOCK_STRENGTH;
    }

    /** 返回地毯方块对应的 disguise 值(1..16),非地毯方块返回 0。 */
    private static int carpetIndex(CarpetBlock carpetBlock) {
        for (int i = 0; i < CARPETS_BY_COLOR.length; i++) {
            if (CARPETS_BY_COLOR[i] == carpetBlock) {
                return i + 1;
            }
        }
        return 0;
    }
}