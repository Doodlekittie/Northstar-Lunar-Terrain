package doodlekittie.northstarstuff.worldgen.carver.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class SimpleSphereCarverConfiguration extends CarverConfiguration {
    public static final Codec<SimpleSphereCarverConfiguration> CODEC = RecordCodecBuilder.create((a) -> a.group(
                    CarverConfiguration.CODEC.forGetter((b) -> b),
                    FloatProvider.CODEC.fieldOf("radius").forGetter((c) -> c.radius),
                    IntProvider.CODEC.fieldOf("length").forGetter((c) -> c.length))
            .apply(a, SimpleSphereCarverConfiguration::new));


    public final FloatProvider radius;
    public final IntProvider length;

    public SimpleSphereCarverConfiguration(float probability, HeightProvider y, FloatProvider yScale, VerticalAnchor lavaLevel, CarverDebugSettings debugSettings, HolderSet<Block> replaceable, FloatProvider radius, IntProvider length) {
        super(probability, y, yScale, lavaLevel, debugSettings, replaceable);
        this.radius = radius;
        this.length = length;
    }

    public SimpleSphereCarverConfiguration(CarverConfiguration config, FloatProvider cylinderHeight, IntProvider length) {
        this(config.probability, config.y, config.yScale, config.lavaLevel, config.debugSettings, config.replaceable, cylinderHeight, length);
    }
}
