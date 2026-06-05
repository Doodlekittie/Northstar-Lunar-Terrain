package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.TerracedPoolConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.function.Predicate;

public class TerracedPoolFeature extends Feature<TerracedPoolConfiguration> {
    private boolean success = false;

    public TerracedPoolFeature(Codec<TerracedPoolConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<TerracedPoolConfiguration> context) {
        var worldgenlevel = context.level();
        var config = context.config();
        var randomsource = context.random();
        var blockpos = context.origin();
        Predicate<BlockState> predicate = (state) -> state.is(config.replaceable());
        int i = config.xzRadius().sample(randomsource) + 1;
        int j = config.xzRadius().sample(randomsource) + 1;
        this.placeGroundPatch(worldgenlevel, config, randomsource, blockpos, predicate, i, j);
        return success;
    }

    protected void placeGroundPatch(WorldGenLevel level, TerracedPoolConfiguration config, RandomSource random, BlockPos pos, Predicate<BlockState> state, int xRadius, int zRadius) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        BlockPos.MutableBlockPos blockpos$mutableblockpos1 = blockpos$mutableblockpos.mutable();

        for(int i = -xRadius; i <= xRadius; ++i) {
            boolean flag = i == -xRadius || i == xRadius;

            for(int j = -zRadius; j <= zRadius; ++j) {
                boolean flag1 = j == -zRadius || j == zRadius;
                boolean flag2 = flag || flag1;
                boolean flag3 = flag && flag1;
                boolean flag4 = flag2 && !flag3;
                if (!flag3 && (!flag4 || config.extraEdgeColumnChance() != 0.0F && !(random.nextFloat() > config.extraEdgeColumnChance()))) {
                    blockpos$mutableblockpos.setWithOffset(pos, i, 0, j);

                    for(int k = 0; level.isStateAtPosition(blockpos$mutableblockpos, BlockBehaviour.BlockStateBase::isAir) && k < config.verticalRange(); ++k) {
                        blockpos$mutableblockpos.move(Direction.DOWN);
                    }

                    for(int i1 = 0; level.isStateAtPosition(blockpos$mutableblockpos, (stateAtPos) -> !stateAtPos.isAir()) && i1 < config.verticalRange(); ++i1) {
                        blockpos$mutableblockpos.move(Direction.UP);
                    }

                    blockpos$mutableblockpos1.setWithOffset(blockpos$mutableblockpos, Direction.DOWN);
                    BlockState blockstate = level.getBlockState(blockpos$mutableblockpos1);
                    if (level.isEmptyBlock(blockpos$mutableblockpos) && blockstate.isFaceSturdy(level, blockpos$mutableblockpos1, Direction.UP)) {
                        int l = config.depth().sample(random);
                        this.placeGround(level, config, state, random, blockpos$mutableblockpos1, l);
                    }
                }
            }
        }

    }

    protected void placeGround(WorldGenLevel level, TerracedPoolConfiguration config, Predicate<BlockState> replaceableblocks, RandomSource random, BlockPos.MutableBlockPos mutablePos, int maxDistance) {
        for(int i = 0; i < maxDistance; ++i) {
            BlockState groundState = config.groundState().getState(random, mutablePos);
            BlockState poolState = config.poolState().getState(random, mutablePos);
            BlockState blockstate1 = level.getBlockState(mutablePos);
            if(isExposed(level, poolState, mutablePos.immutable(), new BlockPos.MutableBlockPos())) {
                if (!groundState.is(blockstate1.getBlock())) {
                    if (!replaceableblocks.test(blockstate1)) {
                        return;
                    }

                    level.setBlock(mutablePos, groundState, 2);
                    this.success = true;
                    mutablePos.move(Direction.DOWN);
                }
            } else if (!poolState.is(blockstate1.getBlock())) {
                if (!replaceableblocks.test(blockstate1)) {
                    return;
                }

                level.setBlock(mutablePos, poolState, 2);
                this.success = true;
                mutablePos.move(Direction.DOWN);
            }
        }
    }

    private static boolean isExposed(WorldGenLevel level, BlockState poolState, BlockPos pos, BlockPos.MutableBlockPos mutablePos) {
        return isExposedDirection(level, poolState, pos, mutablePos, Direction.NORTH)
                || isExposedDirection(level, poolState, pos, mutablePos, Direction.EAST)
                || isExposedDirection(level, poolState, pos, mutablePos, Direction.SOUTH)
                || isExposedDirection(level, poolState, pos, mutablePos, Direction.WEST)
                || isExposedDirection(level, poolState, pos, mutablePos, Direction.DOWN);
    }

    private static boolean isExposedDirection(WorldGenLevel level, BlockState poolState, BlockPos pos, BlockPos.MutableBlockPos mutablePos, Direction direction) {
        mutablePos.setWithOffset(pos, direction);
        var state = level.getBlockState(mutablePos);
        return !(state.isFaceSturdy(level, mutablePos, direction.getOpposite()) || state.is(poolState.getBlock()));
    }
}