package doodlekittie.northstarstuff.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record SpeleothemClusterConfiguration(
        BlockStateProvider baseStone,
        Block pointedStone,
        TagKey<Block> baseStoneReplaceable,
        TagKey<Block> speleothemSupports,
        float chanceOfTallerSpeleothem,
        float chanceOfDirectionalSpread,
        float chanceOfSpreadRadius2,
        float chanceOfSpreadRadius3
) implements FeatureConfiguration {
    public static final Codec<SpeleothemClusterConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockStateProvider.CODEC.fieldOf("base_stone").forGetter(SpeleothemClusterConfiguration::baseStone),
            Block.CODEC.fieldOf("pointed_stone").forGetter(SpeleothemClusterConfiguration::pointedStone),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("base_stone_replaceable").forGetter(SpeleothemClusterConfiguration::baseStoneReplaceable),
            TagKey.hashedCodec(Registries.BLOCK).fieldOf("speleothem_supports").forGetter(SpeleothemClusterConfiguration::speleothemSupports),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_speleothem").orElse(0.2F)
                    .forGetter(SpeleothemClusterConfiguration::chanceOfTallerSpeleothem),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_directional_spread").orElse(0.2F)
                    .forGetter(SpeleothemClusterConfiguration::chanceOfDirectionalSpread),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius2").orElse(0.2F)
                    .forGetter(SpeleothemClusterConfiguration::chanceOfSpreadRadius2),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius3").orElse(0.2F)
                    .forGetter(SpeleothemClusterConfiguration::chanceOfSpreadRadius3)
    ).apply(i, SpeleothemClusterConfiguration::new));
}
