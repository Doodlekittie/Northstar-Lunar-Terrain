package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.BoulderConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public class BoulderFeature extends Feature<BoulderConfiguration> {
    public BoulderFeature(Codec<BoulderConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NonNull FeaturePlaceContext<BoulderConfiguration> context) {
        var success = false;

        var random = context.random();
        var scale = context.config().scale().sample(random);
        var octaves = new double[context.config().detail().sample(random)];
        Arrays.fill(octaves, 1f);
        var noise = NormalNoise.create(
                random,
                -scale,
                octaves
        );

        var radius = (int) Math.pow(2, scale);
        var radiusSquared = Math.pow(radius, 2);
        var mutablePos = new BlockPos.MutableBlockPos(0, 0, 0);

        var oX = context.origin().getX();
        var oY = context.origin().getY();
        var oZ = context.origin().getZ();

        for (var x = -radius; x <= radius; x++) {
            for (var y = -radius; y <= radius; y++) {
                for (var z = -radius; z <= radius; z++) {

                    var distSquared = Math.pow(x, 2)
                            + Math.pow(y, 2)
                            + Math.pow(z, 2);

                    if (distSquared > radiusSquared) {
                        continue;
                    }
                    var distFactor = distSquared / radiusSquared;
                    var density = noise.getValue(x, y, z) - distFactor;

                    if (density > 0) {
                        mutablePos.set(oX + x, oY + y, oZ + z);
                        context.level().setBlock(mutablePos, context.config().state().getState(context.random(),
                                mutablePos), 2);
                        success = true;
                    }
                }
            }
        }
        return success;
    }
}
