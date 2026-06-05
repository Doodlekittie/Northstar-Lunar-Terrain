package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ReplaceBlockFeature extends Feature<ReplaceBlockConfiguration> {
    public ReplaceBlockFeature(Codec<ReplaceBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceBlockConfiguration> context) {
        var mutablePos = context.origin().mutable();
        var config = context.config();
        var success = false;

        for (var x = 0; x <= config.xRange().sample(context.random()); x++) {
            for (var y = 0; y <= config.yRange().sample(context.random()); y++) {
                for (var z = 0; z <= config.zRange().sample(context.random()); z++) {
                    if (context.level().getBlockState(mutablePos).is(config.targetBlocks())
                            && config.targetBiomes().isEmpty() || context.level().getBiome(mutablePos).is(config.targetBiomes().get())) {
                        context.level().setBlock(mutablePos, config.replaceWith().getState(context.random(), mutablePos), 2);
                        success = true;
                    }
                }
            }
        }

        return success;
    }
}
