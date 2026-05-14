package doodlekittie.northstarstuff.worldgen.densityfunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import doodlekittie.northstarstuff.worldgen.noise.CircleNoise;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static doodlekittie.northstarstuff.NorthstarStuff.MODID;
import static doodlekittie.northstarstuff.registry.ModRegistries.CIRCLE_NOISE_REGISTRY_KEY;

public class ModDensityFunctions {

    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID);

    public static final DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<CircleNoiseDF>> CIRCLE_NOISE_DENSITY_FUNCTION_TYPE =
            DENSITY_FUNCTION_TYPES.register(
                    "circle_noise",
                    CircleNoiseDF.CODEC::codec
            );

    public static class CircleNoiseDF extends BlendedNoise {

        public static final KeyDispatchDataCodec<CircleNoiseDF> CODEC = KeyDispatchDataCodec.of(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryFileCodec.create(CIRCLE_NOISE_REGISTRY_KEY, CircleNoise.NoiseParameters.DIRECT_CODEC)
                        .xmap(Holder::value, Holder::direct)
                        .fieldOf("noise").forGetter(CircleNoiseDF::noiseParameters)
                ).apply(instance, CircleNoiseDF::new))
        );

        private final CircleNoise.NoiseParameters noiseParameters;
        private final CircleNoise noise;

        public CircleNoiseDF(CircleNoise.NoiseParameters parameters) {
            this(new XoroshiroRandomSource(0), parameters);
        }

        public CircleNoiseDF(RandomSource random, CircleNoise.NoiseParameters parameters) {
            super(random, 0, 0, 0, 0, 0);
            this.noise = new CircleNoise(parameters, random.nextLong());
            this.noiseParameters = parameters;
        }

        public CircleNoise.NoiseParameters noiseParameters() {
            return noiseParameters;
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
