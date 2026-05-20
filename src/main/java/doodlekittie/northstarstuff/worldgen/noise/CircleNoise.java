package doodlekittie.northstarstuff.worldgen.noise;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import doodlekittie.northstarstuff.util.NorthstarStuffUtil;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import org.apache.commons.codec.digest.MurmurHash3;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class CircleNoise {
    private final Holder<NoiseParameters> parameters;
    private int maxRadius;
    private int partitionSize;
    private double threshold;
    private int nthToCheck;
    private double generalScale;
    private final long seed;
    private boolean activated = false;
    private final Cache<NorthstarStuffUtil.Coordinate, ConcurrentHashMap<NorthstarStuffUtil.Coordinate, Integer>>
            craterCentersCache = Caffeine.newBuilder()
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
        nthToCheck = Math.max(parameters.value().rarity / 10, 1);
        threshold = 1 - (1d / (10 * params.rarity)) * nthToCheck;
        generalScale = (1 / (1 - threshold));

        activated = true;
    }

    public double getValue(int x, int y) {
        if(!activated) { activate(); }
        var toCheck = getCoordinates(x, y);

        var val = 0d;

        var match = false;

        for (var partition : toCheck) {
            var partitionCenters = getPartitionCenters(partition);
            for (var center : partitionCenters.keySet()) {
                var dist = Math.sqrt(Math.pow(center.x() - x, 2) + Math.pow(center.y() - y, 2));
                var newVal = Math.max(1 - (dist * 1 / partitionCenters.get(center)), 0);

                if (!match || newVal > val) {
                    val = newVal;
                    match = true;
                }
            }
        }

        return val;
    }

    private @NonNull HashSet<NorthstarStuffUtil.Coordinate> getCoordinates(int x, int y) {
        var pX = (int) Math.floor((double) x / partitionSize);
        var pY = (int) Math.floor((double) y / partitionSize);
        var lX = x - pX * partitionSize;
        var lY = y - pY * partitionSize;

        var halfPartitionSize = partitionSize / 2;

        var oX = lX > halfPartitionSize ? 1 : -1;
        var oY = lY > halfPartitionSize ? 1 : -1;

        var toCheck = new HashSet<NorthstarStuffUtil.Coordinate>();
        toCheck.add(new NorthstarStuffUtil.Coordinate(pX, pY));
        toCheck.add(new NorthstarStuffUtil.Coordinate(pX + oX, pY));
        toCheck.add(new NorthstarStuffUtil.Coordinate(pX + oX, pY + oY));
        toCheck.add(new NorthstarStuffUtil.Coordinate(pX, pY + oY));
        return toCheck;
    }

    private ConcurrentHashMap<NorthstarStuffUtil.Coordinate, Integer> getPartitionCenters(NorthstarStuffUtil.Coordinate coordinate) {
        return craterCentersCache.get(new NorthstarStuffUtil.Coordinate(coordinate.x(), coordinate.y()),
                c -> generatePartitionCenters(c.x(), c.y()));
    }

    private ConcurrentHashMap<NorthstarStuffUtil.Coordinate, Integer> generatePartitionCenters(int pX, int pY) {
        var centers = new ConcurrentHashMap<NorthstarStuffUtil.Coordinate, Integer>();

        for (var lX = 0; lX <  partitionSize; lX++) {
            var x = pX * partitionSize + lX;
            for (var lY = 0; lY < partitionSize; lY++) {
                if (lY % nthToCheck == 0) {
                    var y = pY * partitionSize + lY;
                    var val = getCircleValue(x, y);
                    if (val >= threshold) {
                        var thisRadius = (int) (((val - threshold) * generalScale) / 2 + 0.5) * maxRadius;
                        centers.put(new NorthstarStuffUtil.Coordinate(x, y), thisRadius);
                    }
                }
            }
        }
        return centers;
    }

 
    private double getCircleValue(int x, int y) {
        var hash = MurmurHash3.hash32(((long) x << 32) + (long) y, seed);
        return Math.max(Math.abs((float) hash / Integer.MAX_VALUE), 0);
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
