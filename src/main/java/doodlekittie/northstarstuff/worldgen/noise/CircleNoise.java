package doodlekittie.northstarstuff.worldgen.noise;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Interner;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import doodlekittie.northstarstuff.util.NorthstarStuffUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import org.apache.commons.codec.digest.MurmurHash3;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class CircleNoise {
    private final Holder<NoiseParameters> parameters;
    private int maxRadius;
    private int partitionSize;
    private double threshold;
    private final long seed;
    private boolean activated = false;
    private final Cache<NorthstarStuffUtil.Coordinate, Cache<NorthstarStuffUtil.Coordinate, Boolean>> craterCentersCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public CircleNoise(Holder<NoiseParameters> parameters, long seed) {
        this.parameters = parameters;
        this.seed = seed;
        this.maxRadius = 0;
    }

    private void activate() {
        var params = this.parameters.value();
        maxRadius = (int) Math.pow(2, params.scale);
        partitionSize = maxRadius * 2 + 2;
        threshold = 1 - (1d / (100 * parameters.value().rarity));
        activated = true;
    }

    public double getValue(int x, int y) {
        if(!activated) { activate(); }
        var pX = (int) Math.floor((double) x / partitionSize);
        var pY = (int) Math.floor((double) y / partitionSize);
        var lX = x - pX;
        var lY = y - pY;

        var halfPartitionSize = partitionSize / 2;

        var oX = lX > halfPartitionSize ? 1 : -1;
        var oY = lY > halfPartitionSize ? 1 : -1;

        var craterCenters = getPartitionCenters(pX, pY);
        craterCenters.putAll(getPartitionCenters(pX + oX, pY).asMap());
        craterCenters.putAll(getPartitionCenters(pX + oX, pY + oY).asMap());
        craterCenters.putAll(getPartitionCenters(pX, pY + oY).asMap());

        var distSquared = Integer.MAX_VALUE;
        var closestCenterX = 0;
        var closestCenterY = 0;
        var match = false;

        for (var center : craterCenters.asMap().keySet()) {
            var newDistSquared = (int) (Math.pow(center.x(), 2) + Math.pow(center.y(), 2));
            if (newDistSquared < distSquared) {
                closestCenterX = center.x();
                closestCenterY = center.y();
                distSquared = newDistSquared;
                match = true;
            }
        }


//        var currentValue = 0.0;
//        var currentValueX = 0;
//        var currentValueY = 0;
//        var match = false;
//
//        for (var pX = x - maxRadius; pX <=  x + maxRadius; pX++) {
//            for (var pY = y - maxRadius; pY <= y + maxRadius; pY++) {
//                var value = getCircleValue(pX, pY);
//                if (value > threshold) {
//                    if(!match || Math.pow(pX - x, 2) + Math.pow(pY - y, 2) < Math.pow(currentValueX - x, 2) + Math.pow(currentValueY - y, 2)) {
//                        currentValue = value;
//                        currentValueX = pX;
//                        currentValueY = pY;
//                        match = true;
//                    }
//                }
//            }
//        }
//
        if (match) {
            var scale = 1 / (1 - threshold);
            var thisRadius = maxRadius * ((getCircleValue(closestCenterX, closestCenterY) - threshold) * scale / 2 + 0.5);

            var dist = Math.sqrt(distSquared);

            if (dist <= thisRadius) {
                return 1f / thisRadius * dist;
            }
        }
        return 0;
    }

    private Cache<NorthstarStuffUtil.Coordinate, Boolean> getPartitionCenters(int pX, int pY) {
        return craterCentersCache.get(new NorthstarStuffUtil.Coordinate(pX, pY),
                c -> generatePartitionCenters(pX, pY));
    }

    private Cache<NorthstarStuffUtil.Coordinate, Boolean> generatePartitionCenters(int pX, int pY) {
        Cache<NorthstarStuffUtil.Coordinate, Boolean> centers = Caffeine.newBuilder().build();

        for (var lX = 0; lX <=  partitionSize; lX++) {
            var x = pX * partitionSize + lX;
            for (var lY = 0; lY <= partitionSize; lY++) {
                var y = pY * partitionSize + lY;
                if(getCircleValue(lX, lY) >= threshold) {
                    centers.put(new NorthstarStuffUtil.Coordinate(x, y), true);
                }
            }
        }
        return centers;
    }


    private double getCircleValue(int x, int y) {
        var hash = MurmurHash3.hash32(((long) x << 32) + (long) y, seed);
        return Math.abs((float) hash / Integer.MAX_VALUE);
    }

    public record NoiseParameters(int scale, int rarity) {
        public static final Codec<CircleNoise.NoiseParameters> DIRECT_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                                Codec.INT.fieldOf("scale").forGetter(CircleNoise.NoiseParameters::scale),
                                Codec.INT.fieldOf("rarity").forGetter(CircleNoise.NoiseParameters::rarity)
                ).apply(instance, CircleNoise.NoiseParameters::new));

        public static final Codec<Holder<CircleNoise.NoiseParameters>> CODEC;

        static {
            CODEC = RegistryFileCodec.create(CIRCLE_NOISE_REGISTRY_KEY, DIRECT_CODEC);
        }
    }
}
