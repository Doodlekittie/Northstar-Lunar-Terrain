package doodlekittie.northstarstuff.worldgen.feature;

import com.mojang.serialization.Codec;
import doodlekittie.northstarstuff.worldgen.feature.configuration.BoulderConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;

public class BoulderFeature extends Feature<BoulderConfiguration> {
    public BoulderFeature(Codec<BoulderConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NonNull FeaturePlaceContext<BoulderConfiguration> context) {
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

        var xOffset = 0;

        for (var i = 1; i <= 10; i++) {
            if (noise.getValue(i * 100, 0, 0) > 0.2) {
                xOffset = i * 100;
            }
        }

        if (xOffset == 0) {
            return false;
        }

        var filled = new HashSet<BlockPos>();
        var found = new HashSet<BlockPos>();
        var toCheck = new ArrayDeque<BlockPos>();

        found.add(context.origin());
        toCheck.add(context.origin());

        while (!toCheck.isEmpty()) {
            var currentPos = toCheck.pop();

            var distSquared = currentPos.distSqr(context.origin());

            if (distSquared > radiusSquared) {
                continue;
            }

            var distFactor = distSquared / radiusSquared;
            var density = noise.getValue(xOffset + currentPos.getX(), currentPos.getY(), currentPos.getZ()) - distFactor;

            if (density > 0) {
                filled.add(currentPos);

                for (var direction : Direction.values()) {
                    var toAdd = currentPos.relative(direction);
                    if (found.add(toAdd)) {
                        toCheck.add(toAdd);
                    }
                }
            }
        }

        for (var pos : filled) {
            context.level().setBlock(pos, context.config().state().getState(context.random(), pos), 2);
        }
        return true;
    }
}
