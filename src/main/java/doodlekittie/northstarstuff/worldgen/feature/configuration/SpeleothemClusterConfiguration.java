package doodlekittie.northstarstuff.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

import java.util.Optional;

public record SpeleothemClusterConfiguration(
        BlockStateProvider baseStone,
        BlockState pointedStone,
        TagKey<Block> baseStoneReplaceable,
        Optional<CaveSurface> surface,
        int floorToCeilingSearchRange,
        IntProvider height,
        IntProvider radius,
        int maxStalagmiteStalactiteHeightDiff,
        int heightDeviation,
        IntProvider stoneBlockLayerThickness,
        FloatProvider density,
        FloatProvider wetness,
        float chanceOfStoneColumnAtMaxDistanceFromCenter,
        int maxDistanceFromEdgeAffectingChanceOfStoneColumn,
        int maxDistanceFromCenterAffectingHeightBias
) implements FeatureConfiguration {
    public static final Codec<SpeleothemClusterConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockStateProvider.CODEC.fieldOf("base_stone").forGetter(SpeleothemClusterConfiguration::baseStone),
            BlockState.CODEC.fieldOf("pointed_stone").forGetter(SpeleothemClusterConfiguration::pointedStone),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("base_stone_replaceable").forGetter(SpeleothemClusterConfiguration::baseStoneReplaceable),
            CaveSurface.CODEC.optionalFieldOf("surface").forGetter(SpeleothemClusterConfiguration::surface),
            Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").forGetter(SpeleothemClusterConfiguration::floorToCeilingSearchRange),
            IntProvider.codec(1, 128).fieldOf("height").forGetter(SpeleothemClusterConfiguration::height),
            IntProvider.codec(1, 128).fieldOf("radius").forGetter(SpeleothemClusterConfiguration::radius),
            Codec.intRange(0, 64).fieldOf("max_stalagmite_stalactite_height_diff").forGetter(SpeleothemClusterConfiguration::maxStalagmiteStalactiteHeightDiff),
            Codec.intRange(1, 64).fieldOf("height_deviation").forGetter(SpeleothemClusterConfiguration::heightDeviation),
            IntProvider.codec(0, 128).fieldOf("stone_block_layer_thickness").forGetter(SpeleothemClusterConfiguration::stoneBlockLayerThickness),
            FloatProvider.codec(0.0F, 2.0F).fieldOf("density").forGetter(SpeleothemClusterConfiguration::density),
            FloatProvider.codec(0.0F, 2.0F).fieldOf("wetness").forGetter(SpeleothemClusterConfiguration::wetness),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_stone_column_at_max_distance_from_center").forGetter(SpeleothemClusterConfiguration::chanceOfStoneColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_edge_affecting_chance_of_stone_column").forGetter(SpeleothemClusterConfiguration::maxDistanceFromEdgeAffectingChanceOfStoneColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(SpeleothemClusterConfiguration::maxDistanceFromCenterAffectingHeightBias)
    ).apply(i, SpeleothemClusterConfiguration::new));
}
