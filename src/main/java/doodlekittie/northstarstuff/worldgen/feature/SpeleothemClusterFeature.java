package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.SpeleothemClusterConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Optional;
import java.util.function.Consumer;

public class SpeleothemClusterFeature extends Feature<SpeleothemClusterConfiguration> {
    public SpeleothemClusterFeature(Codec<SpeleothemClusterConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        LevelAccessor levelaccessor = context.level();
        BlockPos blockpos = context.origin();
        RandomSource randomsource = context.random();
        var config = context.config();
        Optional<Direction> optional = getTipDirection(levelaccessor, context, blockpos, randomsource);
        if (optional.isEmpty()) {
            return false;
        } else {
            BlockPos blockpos1 = blockpos.relative((optional.get()).getOpposite());
            createPatchOfDripstoneBlocks(levelaccessor, randomsource, blockpos1, context);
            int i = randomsource.nextFloat() < config.chanceOfTallerSpeleothem() && DripstoneUtils.isEmptyOrWater(levelaccessor.getBlockState(blockpos.relative(optional.get()))) ? 2 : 1;
            growSpeleothem(levelaccessor, blockpos, optional.get(), context, i);
            return true;
        }
    }

    private static Optional<Direction> getTipDirection(LevelAccessor level, FeaturePlaceContext<SpeleothemClusterConfiguration> context, BlockPos pos, RandomSource random) {
        boolean flag = level.getBlockState(pos.above()).is(context.config().speleothemSupports());
        boolean flag1 = level.getBlockState(pos.below()).is(context.config().speleothemSupports());
        if (flag && flag1) {
            return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
        } else if (flag) {
            return Optional.of(Direction.DOWN);
        } else {
            return flag1 ? Optional.of(Direction.UP) : Optional.empty();
        }
    }

    private static void createPatchOfDripstoneBlocks(LevelAccessor level, RandomSource random, BlockPos pos, FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        placeStoneBlockIfPossible(level, context, pos);

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (!(random.nextFloat() > context.config().chanceOfDirectionalSpread())) {
                BlockPos blockpos = pos.relative(direction);
                placeStoneBlockIfPossible(level, context, blockpos);
                if (!(random.nextFloat() > context.config().chanceOfSpreadRadius2())) {
                    BlockPos blockpos1 = blockpos.relative(Direction.getRandom(random));
                    placeStoneBlockIfPossible(level,context, blockpos1);
                    if (!(random.nextFloat() > context.config().chanceOfSpreadRadius3())) {
                        BlockPos blockpos2 = blockpos1.relative(Direction.getRandom(random));
                        placeStoneBlockIfPossible(level, context, blockpos2);
                    }
                }
            }
        }

    }

    protected static void placeStoneBlockIfPossible(LevelAccessor level, FeaturePlaceContext<SpeleothemClusterConfiguration> context, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(context.config().baseStoneReplaceable())) {
            level.setBlock(pos, context.config().baseStone().getState(context.random(), pos), 2);
        }
    }

    private static void growSpeleothem(LevelAccessor level, BlockPos pos, Direction direction, FeaturePlaceContext<SpeleothemClusterConfiguration> context, int height) {
        if (level.getBlockState(pos).is(context.config().speleothemSupports())) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            buildBaseToTipColumn(context.config().pointedStone().defaultBlockState(), direction, height, (state) -> {
                if (state.is(context.config().pointedStone())) {
                    state = state.setValue(PointedDripstoneBlock.WATERLOGGED, level.isWaterAt(mutablePos));
                }

                level.setBlock(mutablePos, state, 2);
                mutablePos.move(direction);
            });
        }

    }

    protected static void buildBaseToTipColumn(BlockState state, Direction direction, int height, Consumer<BlockState> blockSetter) {
        if (height >= 3) {
            blockSetter.accept(createSpeleothem(state, direction, DripstoneThickness.BASE));

            for(int i = 0; i < height - 3; ++i) {
                blockSetter.accept(createSpeleothem(state, direction, DripstoneThickness.MIDDLE));
            }
        }

        if (height >= 2) {
            blockSetter.accept(createSpeleothem(state, direction, DripstoneThickness.FRUSTUM));
        }

        if (height >= 1) {
            blockSetter.accept(createSpeleothem(state, direction, DripstoneThickness.TIP));
        }

    }

    private static BlockState createSpeleothem(BlockState state, Direction direction, DripstoneThickness dripstoneThickness) {
        return state.setValue(PointedDripstoneBlock.TIP_DIRECTION, direction).setValue(PointedDripstoneBlock.THICKNESS, dripstoneThickness);
    }
}
