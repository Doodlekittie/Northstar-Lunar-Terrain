package doodlekittie.northstarstuff.noise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class CircleNoise {
    private final NoiseParameters parameters;
    private final RandomSource random;
    private final int max_radius;

    //  TODO: Unchecked random okay?
    public CircleNoise(RandomSource random, NoiseParameters parameters) {
        this.parameters = parameters;
        this.random = random;
        this.max_radius = (int) Math.pow(2, this.parameters.scale);
    }

    public double getValue(int x, int y){
        /// Pick random value

        /// for pixel in radius scale
            /// if pixel value > thresh and > current
                /// assign pixel

        /// return dist to pixel


        var currentValue = 0.0;
        int currentValueX; //TODO: How check??
        int currentValueY;

        for (var pX = -max_radius; pX <= max_radius; pX++) {
            for (var pY = -max_radius; pY <= max_radius; pY++) {
                var value = getCraterValue(pX, pY);
                if (value > 1 - (1f / (10 * parameters.rarity)) && value > currentValue) {
                    currentValue = value;
                    currentValueX = pX;
                    currentValueY = pY;
                }
            }
        }

        //TODO: return dist to pixel
    }

    private double getCraterValue(int x, int y) {
        random.setSeed(((long) x >>> 32) + (long) y);
        var value = random.nextFloat();
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
