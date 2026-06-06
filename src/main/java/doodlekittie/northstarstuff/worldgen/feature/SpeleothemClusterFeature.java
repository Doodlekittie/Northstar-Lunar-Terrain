package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.SpeleothemClusterConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.OptionalInt;
import java.util.function.Consumer;

public class SpeleothemClusterFeature extends Feature<SpeleothemClusterConfiguration> {

    public SpeleothemClusterFeature(Codec<SpeleothemClusterConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        var level = context.level();
        var pos = context.origin();
        var config = context.config();
        var random = context.random();
        if (!DripstoneUtils.isEmptyOrWater(level.getBlockState(pos))) {
            return false;
        } else {
            int i = config.height().sample(random);
            float f = config.wetness().sample(random);
            float f1 = config.density().sample(random);
            int j = config.radius().sample(random);
            int k = config.radius().sample(random);

            for(int l = -j; l <= j; ++l) {
                for(int i1 = -k; i1 <= k; ++i1) {
                    double d0 = this.getChanceOfStalagmiteOrStalactite(j, k, l, i1, context);
                    BlockPos blockpos1 = pos.offset(l, 0, i1);
                    this.placeColumn(level, random, blockpos1, l, i1, f, d0, i, f1, context);
                }
            }

            return true;
        }
    }

    private void placeColumn(WorldGenLevel level, RandomSource random, BlockPos pos, int x, int z, float wetness, double chance, int height, float density, FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        var optionalColumn = Column.scan(level, pos, context.config().floorToCeilingSearchRange(), DripstoneUtils::isEmptyOrWater, DripstoneUtils::isNeitherEmptyNorWater);
        if (optionalColumn.isPresent()) {
            var ceilingHeight = OptionalInt.empty();
            var floorHeight = OptionalInt.empty();
            if (context.config().surface().isEmpty() || context.config().surface().get() == CaveSurface.CEILING) {
                ceilingHeight = (optionalColumn.get()).getCeiling();
            }
            if (context.config().surface().isEmpty() || context.config().surface().get() == CaveSurface.FLOOR) {
                floorHeight = (optionalColumn.get()).getFloor();
            }

            if (ceilingHeight.isPresent() || floorHeight.isPresent()) {
                var wet = random.nextFloat() < wetness;
                Column column;
                if (wet && floorHeight.isPresent() && this.canPlacePool(level, pos.atY(floorHeight.getAsInt()), context)) {
                    var i = floorHeight.getAsInt();
                    column = (optionalColumn.get()).withFloor(OptionalInt.of(i - 1));
                    level.setBlock(pos.atY(i), Blocks.WATER.defaultBlockState(), 2);
                } else {
                    column = optionalColumn.get();
                }

                var floorHeight2 = OptionalInt.empty();
                if (context.config().surface().isEmpty() || context.config().surface().get() == CaveSurface.FLOOR) {
                    floorHeight2 = column.getFloor();
                }
                var flag1 = random.nextDouble() < chance;
                int j;
                if (ceilingHeight.isPresent() && flag1 && !this.isLava(level, pos.atY(ceilingHeight.getAsInt()))) {
                    var k = context.config().stoneBlockLayerThickness().sample(random);
                    this.replaceBlocksWithStoneBlocks(level, pos.atY(ceilingHeight.getAsInt()), k, Direction.UP, context);
                    int l;
                    if (floorHeight2.isPresent()) {
                        l = Math.min(height, ceilingHeight.getAsInt() - floorHeight2.getAsInt());
                    } else {
                        l = height;
                    }

                    j = this.getSpeleothemHeight(random, x, z, density, l, context.config());
                } else {
                    j = 0;
                }

                var flag2 = random.nextDouble() < chance;
                int i3;
                if (floorHeight2.isPresent() && flag2 && !this.isLava(level, pos.atY(floorHeight2.getAsInt()))) {
                    var i1 = context.config().stoneBlockLayerThickness().sample(random);
                    this.replaceBlocksWithStoneBlocks(level, pos.atY(floorHeight2.getAsInt()), i1, Direction.DOWN, context);
                    if (ceilingHeight.isPresent()) {
                        i3 = Math.max(0, j + Mth.randomBetweenInclusive(random, -context.config().maxStalagmiteStalactiteHeightDiff(), context.config().maxStalagmiteStalactiteHeightDiff()));
                    } else {
                        i3 = this.getSpeleothemHeight(random, x, z, density, height, context.config());
                    }
                } else {
                    i3 = 0;
                }

                int j3;
                int j1;
                if (ceilingHeight.isPresent() && floorHeight2.isPresent() && ceilingHeight.getAsInt() - j <= floorHeight2.getAsInt() + i3) {
                    var k1 = floorHeight2.getAsInt();
                    var l1 = ceilingHeight.getAsInt();
                    var i2 = Math.max(l1 - j, k1 + 1);
                    var j2 = Math.min(k1 + i3, l1 - 1);
                    var k2 = Mth.randomBetweenInclusive(random, i2, j2 + 1);
                    var l2 = k2 - 1;
                    j3 = l1 - k2;
                    j1 = l2 - k1;
                } else {
                    j3 = j;
                    j1 = i3;
                }

                boolean flag3 = random.nextBoolean() && j3 > 0 && j1 > 0 && column.getHeight().isPresent() && j3 + j1 == column.getHeight().getAsInt();
                if (ceilingHeight.isPresent()) {
                    growSpeleothem(level, pos.atY(ceilingHeight.getAsInt() - 1), Direction.DOWN, context, j3, flag3);
                }

                if (floorHeight2.isPresent()) {
                    growSpeleothem(level, pos.atY(floorHeight2.getAsInt() + 1), Direction.UP, context, j1, flag3);
                }
            }
        }

    }

    private boolean isLava(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.LAVA);
    }

    private int getSpeleothemHeight(RandomSource random, int x, int z, float chance, int height, SpeleothemClusterConfiguration config) {
        if (random.nextFloat() > chance) {
            return 0;
        } else {
            var i = Math.abs(x) + Math.abs(z);
            var  f = (float) Mth.clampedMap(i, 0.0f, config.maxDistanceFromCenterAffectingHeightBias(), (double) height / 2.0d, 0.0d);
            return (int) randomBetweenBiased(random, 0.0f, height, f, config.heightDeviation());
        }
    }

    private boolean canPlacePool(WorldGenLevel level, BlockPos pos, FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        var state = level.getBlockState(pos);
        if (!state.is(Blocks.WATER)
                && !state.is(context.config().pointedStone().getBlock())
                && !state.is(context.config().baseStone().getState(context.random(), pos).getBlock())
        ) {
            if (level.getBlockState(pos.above()).getFluidState().is(FluidTags.WATER)) {
                return false;
            } else {
                for(Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!this.canBeAdjacentToWater(level, pos.relative(direction))) {
                        return false;
                    }
                }

                return this.canBeAdjacentToWater(level, pos.below());
            }
        } else {
            return false;
        }
    }

    private boolean canBeAdjacentToWater(LevelAccessor level, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        return blockstate.is(BlockTags.BASE_STONE_OVERWORLD) || blockstate.getFluidState().is(FluidTags.WATER);
    }

    private void replaceBlocksWithStoneBlocks(WorldGenLevel level, BlockPos pos, int thickness, Direction direction, FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();

        for(int i = 0; i < thickness; ++i) {
            if (placeStoneBlockIfPossible(level, context, blockpos$mutableblockpos)) {
                return;
            }

            blockpos$mutableblockpos.move(direction);
        }

    }

    private double getChanceOfStalagmiteOrStalactite(int xRadius, int zRadius, int x, int z, FeaturePlaceContext<SpeleothemClusterConfiguration> context) {
        var i = xRadius - Math.abs(x);
        var j = zRadius - Math.abs(z);
        var k = Math.min(i, j);
        return Mth.clampedMap(k, 0.0F, context.config().maxDistanceFromEdgeAffectingChanceOfStoneColumn(), context.config().chanceOfStoneColumnAtMaxDistanceFromCenter(), 1.0F);
    }

    private static float randomBetweenBiased(RandomSource random, float min, float max, float mean, float deviation) {
        return ClampedNormalFloat.sample(random, mean, deviation, min, max);
    }

    protected static boolean placeStoneBlockIfPossible(LevelAccessor level, FeaturePlaceContext<SpeleothemClusterConfiguration> context, BlockPos pos) {
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(context.config().baseStoneReplaceable())) {
            level.setBlock(pos, context.config().baseStone().getState(context.random(), pos), 2);
            return true;
        }
        return false;
    }

    private static void growSpeleothem(LevelAccessor level, BlockPos pos, Direction direction, FeaturePlaceContext<SpeleothemClusterConfiguration> context, int height, boolean mergeTip) {
        var supportPos = pos.relative(direction.getOpposite());
        if (level.getBlockState(supportPos).is(context.config().baseStone().getState(context.random(), supportPos).getBlock())) {
            BlockPos.MutableBlockPos mutablePos = pos.mutable();
            buildBaseToTipColumn(context.config().pointedStone(), direction, height, mergeTip, (state) -> {
                if (state.is(context.config().pointedStone().getBlock())) {
                    state = state.setValue(PointedDripstoneBlock.WATERLOGGED, level.isWaterAt(mutablePos));
                }

                level.setBlock(mutablePos, state, 2);
                mutablePos.move(direction);
            });
        }

    }

    protected static void buildBaseToTipColumn(BlockState state, Direction direction, int height, boolean mergeTip, Consumer<BlockState> blockSetter) {
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
            blockSetter.accept(createSpeleothem(state, direction, mergeTip ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }
    }

    private static BlockState createSpeleothem(BlockState state, Direction direction, DripstoneThickness dripstoneThickness) {
        return state.setValue(PointedDripstoneBlock.TIP_DIRECTION, direction).setValue(PointedDripstoneBlock.THICKNESS, dripstoneThickness);
    }
}
