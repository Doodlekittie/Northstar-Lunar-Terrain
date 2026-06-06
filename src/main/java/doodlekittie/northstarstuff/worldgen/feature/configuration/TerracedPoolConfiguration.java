package doodlekittie.northstarstuff.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record TerracedPoolConfiguration(
        TagKey<Block> replaceable,
        BlockStateProvider groundState,
        BlockStateProvider poolState,
        IntProvider depth,
        IntProvider xzRadius,
        float extraEdgeColumnChance,
        int verticalRange
) implements FeatureConfiguration {
    public static final Codec<TerracedPoolConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("replaceable").forGetter(TerracedPoolConfiguration::replaceable),
            BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(TerracedPoolConfiguration::groundState),
            BlockStateProvider.CODEC.fieldOf("pool_state").forGetter(TerracedPoolConfiguration::poolState),
            IntProvider.CODEC.fieldOf("depth").forGetter(TerracedPoolConfiguration::depth),
            IntProvider.CODEC.fieldOf("xz_radius").forGetter(TerracedPoolConfiguration::xzRadius),
            Codec.floatRange(0.0F, 1.0F).fieldOf("extra_edge_column_chance").forGetter(TerracedPoolConfiguration::extraEdgeColumnChance),
            Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(TerracedPoolConfiguration::verticalRange)
    ).apply(i, TerracedPoolConfiguration::new));
}