package doodlekittie.northstarstuff.worldgen.noise;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.apache.commons.codec.digest.MurmurHash3;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class CircleNoise {
    private final double threshold = 0.99;

    private final Holder<NoiseParameters> parameters;
    private final long seed;
    private final Cache<Long, ConcurrentHashMap<Long, Integer>>
            craterCentersCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();
    private int maxRadius;
    private int partitionSize;
    private int nthToCheck;
    private double generalScale;
    private boolean activated = false;
    private PerlinNoise perlinNoise;

    public CircleNoise(Holder<NoiseParameters> parameters, long seed) {
        this.parameters = parameters;
        this.seed = seed;
        this.maxRadius = 0;
    }

    private void activate() {
        var params = this.parameters.value();
        maxRadius = (int) Math.pow(2, params.scale);
        partitionSize = maxRadius * 2 + 2;
        nthToCheck = Math.max(parameters.value().rarity, 1);
        generalScale = (1 / (1 - threshold));
        if (params.shiftFactor > 0) {
            perlinNoise = PerlinNoise.create(new XoroshiroRandomSource(seed), -params.scale, DoubleList.of(1d, 1d, 1d, 1d, 1d));
        }

        activated = true;
    }

    public double getValue(int x, int y) {
        if(!activated) { activate(); }

        var shiftFactor = parameters.value().shiftFactor;
        if(shiftFactor > 0) {
            var factor = (double) parameters.value().scale * shiftFactor;
            var sX = perlinNoise.getValue(x, 0d, y) * factor;
            var sY = perlinNoise.getValue(x + 747268473, 0d, y) * factor;
            x = (int) (x + sX);
            y = (int) (y + sY);
        }

        var pX = (int) Math.floor((double) x / partitionSize);
        var pY = (int) Math.floor((double) y / partitionSize);
        var lX = x - pX * partitionSize;
        var lY = y - pY * partitionSize;

        var halfPartitionSize = partitionSize / 2;

        var oX = lX > halfPartitionSize ? 1 : -1;
        var oY = lY > halfPartitionSize ? 1 : -1;

        var val = 0d;

        val = Math.max(val, checkPartition(pX, pY, x, y));
        val = Math.max(val, checkPartition(pX + oX, pY, x, y));
        val = Math.max(val, checkPartition(pX + oX, pY + oY, x, y));
        val = Math.max(val, checkPartition(pX, pY + oY, x, y));

        return val;
    }

    private double checkPartition(int pX, int pY, int x, int y) {
        final double[] maxVal = {0d};

        getPartitionCenters(pX, pY).forEach((key, radius) -> {
            int cX = (int) (key >> 32);
            int cY = key.intValue();
            var dist = Math.sqrt(Math.pow(cX - x, 2) + Math.pow(cY - y, 2));
            var val = Math.max(1 - (dist * 1 / radius), 0);
            maxVal[0] = Math.max(maxVal[0], val);
        });

        return maxVal[0];
    }

    private ConcurrentHashMap<Long, Integer> getPartitionCenters(int x, int y) {
        return craterCentersCache.get(pack(x, y),
                l -> generatePartitionCenters(x, y));
    }

    private ConcurrentHashMap<Long, Integer> generatePartitionCenters(int pX, int pY) {
        var centers = new ConcurrentHashMap<Long, Integer>();

        for (var lC = 0; lC <  Math.pow(partitionSize, 2); lC = lC + nthToCheck) {
            var lX = (int) Math.floor(((double) lC / partitionSize));

            var x = lX + pX * partitionSize;
            var y = lC % partitionSize + pY * partitionSize;

            var val = getCircleValue(x, y);
                if (val >= threshold) {
                    var thisRadius = (int) ((((val - threshold) * generalScale) / 2 + 0.5) * maxRadius);
                    centers.put(pack(x, y), thisRadius);
                }
            }
        return centers;
    }

    private double getCircleValue(int x, int y) {
        var hash = MurmurHash3.hash32(pack(x, y), seed);
        return Math.max(Math.abs((float) hash / Integer.MAX_VALUE), 0);
    }

    private static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public record NoiseParameters(int scale, int rarity, double shiftFactor) {
        public static final Codec<CircleNoise.NoiseParameters> DIRECT_CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                                Codec.INT.fieldOf("scale").forGetter(CircleNoise.NoiseParameters::scale),
                                Codec.INT.fieldOf("rarity").forGetter(CircleNoise.NoiseParameters::rarity),
                                Codec.DOUBLE.fieldOf("shift_factor").forGetter(CircleNoise.NoiseParameters::shiftFactor)
                ).apply(instance, CircleNoise.NoiseParameters::new));

        public static final Codec<Holder<CircleNoise.NoiseParameters>> CODEC;

        static {
            CODEC = RegistryFileCodec.create(CIRCLE_NOISE_REGISTRY_KEY, DIRECT_CODEC);
        }
    }

    private record PartitionData(long[] centers, double[] radii) {}
}
