package doodlekittie.northstarstuff.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.Optional;

public record ReplaceBlockConfiguration(
        Optional<TagKey<Biome>> targetBiomes,
        TagKey<Block> targetBlocks,
        BlockStateProvider replaceWith,
        IntProvider xRange,
        IntProvider yRange,
        IntProvider zRange
) implements FeatureConfiguration {
    public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            TagKey.hashedCodec(Registries.BIOME).optionalFieldOf("target_biomes").forGetter(ReplaceBlockConfiguration::targetBiomes),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("target_blocks").forGetter(ReplaceBlockConfiguration::targetBlocks),
            BlockStateProvider.CODEC.fieldOf("replace_with").forGetter(ReplaceBlockConfiguration::replaceWith),
            IntProvider.CODEC.fieldOf("x_range").forGetter(ReplaceBlockConfiguration::xRange),
            IntProvider.CODEC.fieldOf("y_range").forGetter(ReplaceBlockConfiguration::yRange),
            IntProvider.CODEC.fieldOf("z_range").forGetter(ReplaceBlockConfiguration::zRange)
    ).apply(i, ReplaceBlockConfiguration::new));
}
