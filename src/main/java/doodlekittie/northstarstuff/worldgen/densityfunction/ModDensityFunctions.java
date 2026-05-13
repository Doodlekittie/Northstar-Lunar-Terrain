package doodlekittie.northstarstuff.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import doodlekittie.northstarstuff.worldgen.noise.CircleNoise;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;

import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class ModDensityFunctions {

    protected record CircleNoiseDF(CircleNoise.NoiseParameters parameters, CircleNoise noise) implements DensityFunction {
        public static final MapCodec<ModDensityFunctions.CircleNoiseDF> DATA_CODEC =
                RecordCodecBuilder.mapCodec((instance) -> instance.group(
                                RegistryFileCodec.create(CIRCLE_NOISE_REGISTRY_KEY, CircleNoise.NoiseParameters.DIRECT_CODEC)
                                        .xmap(Holder::value, Holder::direct)
                                        .fieldOf("noise").forGetter(CircleNoiseDF::parameters))
                        .apply(instance, ModDensityFunctions.CircleNoiseDF::new));
        public static final KeyDispatchDataCodec<ModDensityFunctions.CircleNoiseDF> CODEC;

        static {
            CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
        }

        public CircleNoiseDF(CircleNoise.NoiseParameters parameters) {
            this(parameters, new CircleNoise(parameters));
        }

        @Override
        public double compute(@NotNull FunctionContext functionContext) {
            return noise.getValue(functionContext.blockX(), functionContext.blockZ());
        }

        @Override
        public void fillArray(double @NotNull [] doubles, @NotNull ContextProvider contextProvider) {
            contextProvider.fillAllDirectly(doubles, this);
        }

        @Override
        @NotNull
        public DensityFunction mapAll(@NotNull Visitor visitor) {
            return visitor.apply(this);
        }

        @Override
        public double minValue() {
            return 0;
        }

        @Override
        public double maxValue() {
            return 1;
        }

        @Override
        @NotNull
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }
}
