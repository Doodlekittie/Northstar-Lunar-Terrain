package doodlekittie.northstarstuff.worldgen.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import org.apache.commons.codec.digest.MurmurHash3;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class CircleNoise {
    private final Holder<NoiseParameters> parameters;
    private int max_radius;
    private final long seed;

    public CircleNoise(Holder<NoiseParameters> parameters, long seed) {
        this.parameters = parameters;
        this.seed = seed;
        this.max_radius = 0;
    }

    public double getValue(int x, int y) {
        if (max_radius == 0) {
            this.max_radius = (int) Math.pow(2, this.parameters.value().scale);
        }

        var currentValue = 0.0;
        var currentValueX = 0;
        var currentValueY = 0;
        var threshold = 1 - (1f / (100 * parameters.value().rarity));
        var match = false;

        for (var pX = x - max_radius; pX <=  x + max_radius; pX++) {
            for (var pY = y - max_radius; pY <= y + max_radius; pY++) {
                var value = getCircleValue(pX, pY);
                if (value > threshold) {
                    if(!match || Math.pow(pX - x, 2) + Math.pow(pY - y, 2) < Math.pow(currentValueX - x, 2) + Math.pow(currentValueY - y, 2)) {
                        currentValue = value;
                        currentValueX = pX;
                        currentValueY = pY;
                        match = true;
                    }
                }
            }
        }

        if (match) {
            var scale = 1 / (1 - threshold);
            var thisRadius = max_radius * ((currentValue - threshold) * scale / 2 + 0.5);

            var dist = Math.sqrt(Math.pow(currentValueX - x, 2) + Math.pow(currentValueY - y, 2));

            if (dist <= thisRadius) {
                return 1f / thisRadius * dist;
            }
        }

        return 0;
    }

    public double getCircleValue(int x, int y) {
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
