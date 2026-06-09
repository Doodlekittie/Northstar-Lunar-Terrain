package doodlekittie.northstarstuff.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BoulderConfiguration(
        BlockStateProvider state,
        IntProvider scale,
        IntProvider detail
) implements FeatureConfiguration {
    public static final Codec<BoulderConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(BoulderConfiguration::state),
            IntProvider.CODEC.fieldOf("scale").forGetter(BoulderConfiguration::scale),
            IntProvider.CODEC.fieldOf("detail").forGetter(BoulderConfiguration::detail)
    ).apply(i, BoulderConfiguration::new));
}
